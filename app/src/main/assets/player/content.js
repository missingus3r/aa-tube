// Controla el <video> de YouTube desde la app. Busca el elemento de forma genérica para no
// depender de los nombres de botones de YouTube, que cambian seguido.
"use strict";

const port = browser.runtime.connectNative("aatube");
const FULL_CLASS = "aatube-full";

const style = document.createElement("style");
style.textContent = `
  html.${FULL_CLASS} video {
    position: fixed !important; inset: 0 !important;
    width: 100vw !important; height: 100vh !important;
    z-index: 2147483647 !important; background: #000 !important;
    object-fit: contain !important;
  }
  html.${FULL_CLASS}, html.${FULL_CLASS} body { overflow: hidden !important; }
  html.${FULL_CLASS} header, html.${FULL_CLASS} ytm-mobile-topbar-renderer,
  html.${FULL_CLASS} #header-bar, html.${FULL_CLASS} ytm-pivot-bar-renderer { display: none !important; }
`;
document.documentElement.appendChild(style);

const video = () => document.querySelector("video");

function report() {
  const v = video();
  port.postMessage({
    ready: !!v,
    playing: !!v && !v.paused && !v.ended,
    title: document.title.replace(/ - YouTube$/, ""),
    positionMs: v ? Math.round(v.currentTime * 1000) : 0,
    // Las transmisiones en vivo no tienen duración: -1 y Android Auto no dibuja la barra.
    durationMs: v && isFinite(v.duration) ? Math.round(v.duration * 1000) : -1,
  });
}

function next() {
  const btn = document.querySelector(
    '.ytp-next-button, button[aria-label*="Siguiente"], button[aria-label*="Next"]'
  );
  if (btn) return btn.click();
  const v = video();
  if (v && isFinite(v.duration)) v.currentTime = v.duration; // dispara la reproducción automática
}

const commands = {
  TOGGLE: (v) => (v.paused ? v.play() : v.pause()),
  BACK10: (v) => (v.currentTime = Math.max(0, v.currentTime - 10)),
  FWD10: (v) => (v.currentTime = Math.min(v.duration || Infinity, v.currentTime + 10)),
  NEXT: () => next(),
  // Como cualquier reproductor: si ya avanzó, vuelve al principio; si recién empieza, al anterior.
  PREV: (v) => (v && v.currentTime > 3 ? (v.currentTime = 0) : history.back()),
  FULLSCREEN: () => document.documentElement.classList.toggle(FULL_CLASS),
  SEEK: (v, msg) => (v.currentTime = msg.ms / 1000),
};

port.onMessage.addListener((msg) => {
  const v = video();
  const run = commands[msg.cmd];
  if (!run || (!v && msg.cmd !== "PREV")) return;
  run(v, msg);
  setTimeout(report, 200);
});

// Al salir del video, se sale también de la pantalla completa propia.
let lastUrl = location.href;
setInterval(() => {
  if (location.href !== lastUrl) {
    lastUrl = location.href;
    if (!location.pathname.startsWith("/watch")) document.documentElement.classList.remove(FULL_CLASS);
    report();
  }
}, 1000);

// Android Auto avanza la barra solo a partir de la última posición y la velocidad, así que alcanza
// con avisar cuando algo cambia (no en cada cuadro).
for (const ev of ["play", "pause", "ended", "loadedmetadata", "durationchange", "seeked", "ratechange"]) {
  document.addEventListener(ev, report, true);
}
new MutationObserver(report).observe(document.querySelector("title") || document.head, {
  childList: true, subtree: true, characterData: true,
});
report();

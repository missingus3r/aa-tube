# AA Tube

YouTube en la pantalla de Android Auto, con tu cuenta de Google, para ver videos con el auto estacionado.
*YouTube on the Android Auto screen, signed in with your Google account, for use while parked.*

> Proyecto personal, no oficial. No está afiliado a Google, YouTube ni Android Auto.

## Qué hace

- Muestra `m.youtube.com` en la pantalla del auto: tocás, deslizás, buscás y abrís videos como en el
  teléfono, con tu cuenta (suscripciones, historial, Premium).
- Barra propia con ⏪ 10 s, ⏯, ⏩ 10 s, pantalla completa y recargar.
- Botones del volante (play/pausa, siguiente, anterior) y la tarjeta de medios de Android Auto, con
  título y barra de avance.
- **Al moverse el auto, Android Auto cierra la pantalla** ("No disponible mientras conduces") y el
  audio sigue sonando, controlable desde el volante y la tarjeta de medios. Si queda en pausa sin
  pantalla, se apaga sola a los 10 minutos.

## Cómo funciona

| Pieza | Qué hace |
|---|---|
| `car/CarActivity` | La pantalla del auto. Es una app "para usar estacionado" (categoría juego, `CAR_LAUNCHER`): Android Auto la dibuja como cualquier pantalla de Android y la cierra al manejar. |
| `GeckoHolder` | Un único motor [GeckoView](https://mozilla.github.io/geckoview/) (el de Firefox) y una sola pestaña compartida entre el teléfono y el auto. Se usa GeckoView y no WebView porque Google no permite iniciar sesión dentro de un WebView. |
| `MainActivity` | En el teléfono: iniciar sesión en Google. La sesión queda guardada y vale en el auto. |
| `player/` + `assets/player/` | Una extensión web integrada que controla el `<video>` de la página e informa título, estado y posición. |
| `media/` | Sesión de medios (volante, tarjeta de medios) y servicio en primer plano que mantiene el audio cuando la pantalla se cierra. |

## Requisitos

- Teléfono con Android 15 o superior y Android Auto.
- Para compilar: JDK 17 y Android SDK con la plataforma **37.2** (la pide GeckoView 158).
- Solo se compila para `arm64-v8a`.

## Instalar

1. Compilar: `./gradlew assembleDebug` (queda en `app/build/outputs/apk/debug/`).
2. Instalar el APK en el teléfono (`adb install app-debug.apk`, o copiándolo).
3. En el teléfono, en **Android Auto**: tocar varias veces "Versión" para activar el modo
   desarrollador, y en ⋮ → Configuración para desarrolladores activar **Fuentes desconocidas**.
4. Abrir **AA Tube en el teléfono** e iniciar sesión en Google.
5. En el auto, AA Tube aparece en el lanzador de Android Auto (si no, agregarla desde
   Android Auto → Personalizar selector).

Para probar sin auto se puede usar el
[Desktop Head Unit](https://developer.android.com/training/cars/testing/dhu) del SDK.

## Avisos

- Usala solo con el auto detenido. El bloqueo al manejar es de Android Auto y está ahí por seguridad.
- Es una página web dentro de un navegador: si YouTube cambia su sitio, algunos controles pueden dejar
  de andar. El uso de YouTube queda sujeto a sus términos.

## Tests

`./gradlew testDebugUnitTest`

## Licencia

[MIT](LICENSE)

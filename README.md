# AA Tube

YouTube en la pantalla de Android Auto, con tu cuenta de Google, para ver videos con el auto estacionado.
*YouTube on the Android Auto screen, signed in with your Google account, for use while parked.*

> Proyecto personal, no oficial. No está afiliado a Google, YouTube ni Android Auto.
> Hermano: **[AA Disney](https://github.com/missingus3r/aa-disney)** (Disney+ en Android Auto), hecho a
> partir de este mismo código.

> [!IMPORTANT]
> **Para que funcione hay que activar "Fuentes desconocidas" en el modo desarrollador de Android Auto.**
> Android Auto no muestra apps que no se instalaron desde Google Play si esa opción está apagada.
> Ver [Activar fuentes desconocidas](#activar-fuentes-desconocidas) (son 2 minutos).

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
3. Activar **Fuentes desconocidas** en Android Auto (ver [abajo](#activar-fuentes-desconocidas)).
4. Abrir **AA Tube en el teléfono** e iniciar sesión en Google.
5. En el auto, AA Tube aparece en el lanzador de Android Auto (si no, agregarla desde
   Android Auto → Personalizar selector).

## Activar fuentes desconocidas

Se hace una sola vez, en el teléfono, con el teléfono desconectado del auto:

1. Abrir la configuración de **Android Auto**: Ajustes del teléfono → buscar "Android Auto" (suele estar
   en Dispositivos conectados → Preferencias de conexión → Android Auto).
2. Bajar hasta el final y tocar **Versión** unas 10 veces seguidas, hasta que aparezca
   "¿Permitir la configuración de desarrollo?". Tocar **Aceptar**.
3. Tocar el menú **⋮** (arriba a la derecha) → **Configuración para desarrolladores**.
4. Bajar hasta **Fuentes desconocidas** y marcarla.
5. Cerrar Android Auto y volver a conectar el teléfono al auto (o desconectar y reconectar el cable).

Si después de eso la app no aparece en el auto, agregarla desde Android Auto → **Personalizar selector**.

## Autos confirmados

| Auto | Año |
|---|---|
| Peugeot 208 | 2025 |
| Chevrolet Prisma | 2019 |

**¿Te sirvió el repo?** Hacé un PR y agregá tu modelo de auto (y el año) a esta tabla, así otros
saben en qué autos funciona.

## Probar sin auto

Para probar sin auto se puede usar el
[Desktop Head Unit](https://developer.android.com/training/cars/testing/dhu) del SDK.

## Avisos

- Usala solo con el auto detenido. El bloqueo al manejar es de Android Auto y está ahí por seguridad.
- Es una página web dentro de un navegador: si YouTube cambia su sitio, algunos controles pueden dejar
  de andar. El uso de YouTube queda sujeto a sus términos.

## Ver también

- [AA Disney](https://github.com/missingus3r/aa-disney): Disney+ en Android Auto.

## Tests

`./gradlew testDebugUnitTest`

## Licencia

[MIT](LICENSE)

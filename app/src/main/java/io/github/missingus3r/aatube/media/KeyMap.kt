package io.github.missingus3r.aatube.media

import io.github.missingus3r.aatube.player.PlayerCmd
import io.github.missingus3r.aatube.player.PlayerControls

/** Nombre del callback de MediaSession → comando del reproductor. */
object KeyMap {
    fun toCmd(callback: String): PlayerCmd? = when (callback) {
        "play", "pause" -> PlayerCmd.TOGGLE
        "skipToNext" -> PlayerCmd.NEXT
        "skipToPrevious" -> PlayerCmd.PREV
        "fastForward" -> PlayerCmd.FWD10
        "rewind" -> PlayerCmd.BACK10
        else -> null
    }
}

/** Sin página de video lista, las teclas se descartan en vez de quedar encoladas. */
class MediaDispatcher(private val controls: PlayerControls) {
    fun dispatch(callback: String) {
        val cmd = KeyMap.toCmd(callback) ?: return
        if (controls.state.value.ready) controls.send(cmd)
    }

    /** Desde la barra de avance de la tarjeta de medios de Android Auto. */
    fun seekTo(ms: Long) {
        if (controls.state.value.ready) controls.seekTo(ms)
    }
}

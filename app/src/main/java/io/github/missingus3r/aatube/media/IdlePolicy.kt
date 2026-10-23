package io.github.missingus3r.aatube.media

/**
 * Cuándo apagar el servicio de audio. Mientras la pantalla del auto está abierta o suena algo, sigue.
 * Si se pausó con la pantalla cerrada (manejando), espera [graceMs] antes de apagarse, para que se
 * pueda seguir desde el volante o la tarjeta de medios.
 */
class IdlePolicy(private val graceMs: Long) {
    private var idleSince: Long? = null

    fun shouldStop(carVisible: Boolean, playing: Boolean, nowMs: Long): Boolean {
        if (carVisible || playing) {
            idleSince = null
            return false
        }
        val since = idleSince ?: nowMs.also { idleSince = it }
        return nowMs - since >= graceMs
    }
}

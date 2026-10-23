package io.github.missingus3r.aatube.player

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

enum class PlayerCmd { TOGGLE, BACK10, FWD10, NEXT, PREV, FULLSCREEN }

/** [durationMs] es -1 cuando no hay duración (transmisión en vivo o nada cargado). */
data class PlayerState(
    val ready: Boolean = false,
    val playing: Boolean = false,
    val title: String = "",
    val positionMs: Long = 0,
    val durationMs: Long = -1,
)

interface PlayerControls {
    val state: StateFlow<PlayerState>
    fun send(cmd: PlayerCmd)
    fun seekTo(ms: Long)
}

/** Habla con `assets/player/content.js` por mensajería nativa de GeckoView. */
class PlayerBridge(runtime: GeckoRuntime, private val session: GeckoSession) : PlayerControls {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state
    private var port: WebExtension.Port? = null

    private val portDelegate = object : WebExtension.PortDelegate {
        override fun onPortMessage(message: Any, port: WebExtension.Port) {
            val m = message as? JSONObject ?: return
            _state.value = PlayerState(
                ready = m.optBoolean("ready"),
                playing = m.optBoolean("playing"),
                title = m.optString("title"),
                positionMs = m.optLong("positionMs"),
                durationMs = m.optLong("durationMs", -1),
            )
        }

        override fun onDisconnect(port: WebExtension.Port) {
            if (this@PlayerBridge.port === port) {
                this@PlayerBridge.port = null
                _state.value = PlayerState()
            }
        }
    }

    init {
        runtime.webExtensionController
            .ensureBuiltIn("resource://android/assets/player/", "player@aatube")
            .accept({ ext ->
                ext ?: return@accept
                session.webExtensionController.setMessageDelegate(ext, object : WebExtension.MessageDelegate {
                    override fun onConnect(port: WebExtension.Port) {
                        this@PlayerBridge.port = port
                        port.setDelegate(portDelegate)
                    }
                }, NATIVE_APP)
            }, { e -> Log.e(TAG, "No se pudo cargar la extensión del reproductor", e) })
    }

    override fun send(cmd: PlayerCmd) {
        port?.postMessage(JSONObject().put("cmd", cmd.name))
    }

    override fun seekTo(ms: Long) {
        port?.postMessage(JSONObject().put("cmd", "SEEK").put("ms", ms))
    }

    private companion object {
        const val NATIVE_APP = "aatube"
        const val TAG = "PlayerBridge"
    }
}

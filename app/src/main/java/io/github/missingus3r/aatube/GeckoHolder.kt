package io.github.missingus3r.aatube

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import io.github.missingus3r.aatube.player.PlayerBridge

const val START_URL = "https://m.youtube.com/"

/**
 * Un único motor y una única pestaña por proceso: la pantalla del teléfono y la del auto
 * muestran la misma sesión, así que la cuenta de Google iniciada en el teléfono vale en el auto.
 * Una sesión solo puede dibujarse en un lugar a la vez; el auto tiene prioridad.
 */
object GeckoHolder {
    private var runtime: GeckoRuntime? = null
    private var session: GeckoSession? = null

    @Volatile var canGoBack = false
        private set

    /** true mientras la pantalla del auto tiene la sesión. */
    var carActive = false
        private set

    /** La vista del teléfono se registra acá para soltar o retomar la sesión. */
    var phoneListener: ((carActive: Boolean) -> Unit)? = null

    /**
     * Gecko calcula la densidad de pantalla una sola vez, con la primera pantalla que la pida (a veces
     * la del teléfono, a veces la del auto), así que se fija acá. Está pensada para el auto; en el
     * teléfono, que solo se usa para iniciar sesión, la página se ve más chica.
     */
    const val PAGE_DENSITY = 1.4f

    fun runtime(ctx: Context): GeckoRuntime =
        runtime ?: GeckoRuntime.create(
            ctx.applicationContext,
            GeckoRuntimeSettings.Builder()
                .displayDensityOverride(PAGE_DENSITY)
                .displayDpiOverride((PAGE_DENSITY * 160).toInt())
                .build(),
        ).also { runtime = it }

    fun session(ctx: Context): GeckoSession =
        session ?: GeckoSession(
            GeckoSessionSettings.Builder()
                .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
                .usePrivateMode(false)
                .build()
        ).also {
            it.navigationDelegate = object : GeckoSession.NavigationDelegate {
                override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                    this@GeckoHolder.canGoBack = canGoBack
                }
            }
            it.open(runtime(ctx))
            session = it
            // La extensión del reproductor se registra antes de cargar la página para que su
            // script entre en la primera carga.
            player = PlayerBridge(runtime(ctx), it)
            it.loadUri(START_URL)
        }

    private var player: PlayerBridge? = null

    fun player(ctx: Context): PlayerBridge {
        session(ctx)
        return player!!
    }

    fun setCarActive(active: Boolean) {
        carActive = active
        phoneListener?.invoke(active)
    }
}

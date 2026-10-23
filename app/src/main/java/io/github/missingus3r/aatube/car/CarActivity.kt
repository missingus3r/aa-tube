package io.github.missingus3r.aatube.car

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.mozilla.geckoview.GeckoView
import io.github.missingus3r.aatube.GeckoHolder
import io.github.missingus3r.aatube.R
import io.github.missingus3r.aatube.media.PlaybackService
import io.github.missingus3r.aatube.player.PlayerCmd

/**
 * La pantalla del auto. Es una app "para usar estacionado": Android Auto la dibuja en el auto como
 * cualquier pantalla de Android (los toques llegan solos) y la cierra apenas el auto se mueve, así que
 * el bloqueo del video al manejar lo hace Android Auto. El audio sigue gracias a [PlaybackService].
 */
class CarActivity : AppCompatActivity() {
    private lateinit var geckoView: GeckoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)
        geckoView = findViewById(R.id.gecko)

        // La barra de Android Auto (atrás / salir) va encima de la app: se deja su lugar libre.
        val root = findViewById<View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val player = GeckoHolder.player(this)
        val session = GeckoHolder.session(this)
        bind(R.id.rewind) { player.send(PlayerCmd.BACK10) }
        bind(R.id.toggle) { player.send(PlayerCmd.TOGGLE) }
        bind(R.id.forward) { player.send(PlayerCmd.FWD10) }
        bind(R.id.fullscreen) { player.send(PlayerCmd.FULLSCREEN) }
        bind(R.id.reload) { session.reload() }

        // La flecha de Android Auto vuelve en la página; en la primera, sale.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (GeckoHolder.canGoBack) session.goBack() else finish()
            }
        })
    }

    private fun bind(id: Int, onClick: () -> Unit) = findViewById<View>(id).setOnClickListener { onClick() }

    override fun onStart() {
        super.onStart()
        // El teléfono suelta la sesión antes de que el auto la tome.
        GeckoHolder.setCarActive(true)
        val session = GeckoHolder.session(this)
        geckoView.setSession(session)
        session.setActive(true)
        session.setFocused(true)
        PlaybackService.start(this)
    }

    override fun onStop() {
        val session = GeckoHolder.session(this)
        geckoView.releaseSession()
        // Sin vista la página sigue "visible" para que YouTube no pause el audio al manejar.
        session.setActive(true)
        GeckoHolder.setCarActive(false)
        super.onStop()
    }
}

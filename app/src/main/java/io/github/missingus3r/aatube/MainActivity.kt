package io.github.missingus3r.aatube

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.geckoview.GeckoView

private const val SIGN_IN_URL =
    "https://accounts.google.com/ServiceLogin?service=youtube&continue=https%3A%2F%2Fm.youtube.com%2F"

class MainActivity : AppCompatActivity() {
    private lateinit var geckoView: GeckoView
    private lateinit var carNotice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val session = GeckoHolder.session(this)
        geckoView = findViewById(R.id.gecko)
        carNotice = findViewById(R.id.carNotice)

        findViewById<Button>(R.id.signIn).setOnClickListener { session.loadUri(SIGN_IN_URL) }
        findViewById<Button>(R.id.youtube).setOnClickListener { session.loadUri(START_URL) }

        // Opcional: sin él el audio igual sigue al manejar, pero no se ve la notificación con controles.
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (GeckoHolder.canGoBack) session.goBack() else finish()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        GeckoHolder.phoneListener = { carActive -> runOnUiThread { showOnPhone(!carActive) } }
        showOnPhone(!GeckoHolder.carActive)
    }

    override fun onStop() {
        GeckoHolder.phoneListener = null
        showOnPhone(false)
        super.onStop()
    }

    private fun showOnPhone(show: Boolean) {
        if (show && geckoView.session == null) geckoView.setSession(GeckoHolder.session(this))
        if (!show && geckoView.session != null) geckoView.releaseSession()
        geckoView.visibility = if (show) View.VISIBLE else View.GONE
        carNotice.visibility = if (!show && GeckoHolder.carActive) View.VISIBLE else View.GONE
    }
}

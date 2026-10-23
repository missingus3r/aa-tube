package io.github.missingus3r.aatube.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.missingus3r.aatube.GeckoHolder
import io.github.missingus3r.aatube.MainActivity
import io.github.missingus3r.aatube.R

/**
 * Mantiene vivo el audio y la tarjeta de medios cuando Android Auto cierra la pantalla del auto
 * (pasa apenas el auto se mueve). Sin un servicio en primer plano, Android congela el proceso y el
 * video se corta. Lo arranca [io.github.missingus3r.aatube.car.CarActivity]; se apaga solo según [IdlePolicy].
 */
class PlaybackService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val idle = IdlePolicy(IDLE_GRACE_MS)
    private lateinit var media: MediaSessionHub

    override fun onCreate() {
        super.onCreate()
        val player = GeckoHolder.player(this)
        media = MediaSessionHub(this, player)
        media.start(scope)
        startForeground(NOTIFICATION_ID, notification(""), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        scope.launch {
            player.state.map { it.title }.distinctUntilChanged().collect { title ->
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(title))
            }
        }
        scope.launch {
            while (true) {
                val playing = player.state.value.playing
                if (idle.shouldStop(GeckoHolder.carActive, playing, SystemClock.elapsedRealtime())) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }
                delay(CHECK_EVERY_MS)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        media.stop()
        scope.cancel()
        super.onDestroy()
    }

    private fun notification(title: String): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Reproducción", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle(title.ifBlank { "AA Tube" })
            .setContentText("YouTube")
            .setContentIntent(open)
            .setOngoing(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(media.token))
            .build()
    }

    companion object {
        private const val CHANNEL = "playback"
        private const val NOTIFICATION_ID = 1
        private const val CHECK_EVERY_MS = 15_000L
        // Una pausa manejando (semáforo, llamada) no apaga el audio: hay 10 min para seguir.
        private const val IDLE_GRACE_MS = 10 * 60_000L

        fun start(ctx: Context) = ctx.startForegroundService(Intent(ctx, PlaybackService::class.java))
    }
}

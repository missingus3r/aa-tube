package io.github.missingus3r.aatube.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import io.github.missingus3r.aatube.player.PlayerControls

/** Recibe los botones del volante y publica el video actual en la tarjeta de medios. */
class MediaSessionHub(ctx: Context, private val controls: PlayerControls) {
    private val dispatcher = MediaDispatcher(controls)
    private val session = MediaSessionCompat(ctx, "AATube").apply {
        setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() = dispatcher.dispatch("play")
            override fun onPause() = dispatcher.dispatch("pause")
            override fun onSkipToNext() = dispatcher.dispatch("skipToNext")
            override fun onSkipToPrevious() = dispatcher.dispatch("skipToPrevious")
            override fun onFastForward() = dispatcher.dispatch("fastForward")
            override fun onRewind() = dispatcher.dispatch("rewind")
            override fun onSeekTo(pos: Long) = dispatcher.seekTo(pos)
        })
    }
    private var job: Job? = null

    /** Para que la notificación del teléfono muestre los mismos controles. */
    val token: MediaSessionCompat.Token get() = session.sessionToken

    fun start(scope: CoroutineScope) {
        session.isActive = true
        job = scope.launch {
            controls.state.collect { s ->
                // Con duración y posición, Android Auto dibuja la barra de avance en su tarjeta de
                // medios, que se sigue viendo con el auto en movimiento.
                session.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, s.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "YouTube")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, s.durationMs)
                        .build()
                )
                val hasTimeline = s.durationMs > 0
                session.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setActions(if (hasTimeline) ACTIONS or PlaybackStateCompat.ACTION_SEEK_TO else ACTIONS)
                        .setState(
                            if (s.playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                            if (hasTimeline) s.positionMs else PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            if (s.playing) 1f else 0f,
                        )
                        .build()
                )
            }
        }
    }

    fun stop() {
        job?.cancel()
        session.isActive = false
        session.release()
    }

    private companion object {
        const val ACTIONS = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_FAST_FORWARD or
            PlaybackStateCompat.ACTION_REWIND
    }
}

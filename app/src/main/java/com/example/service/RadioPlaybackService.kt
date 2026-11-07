package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.MainActivity
import com.example.R
import com.example.RadioApp
import com.example.model.PlaybackStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioPlaybackService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var stateJob: Job? = null
    private var trackJob: Job? = null
    private var artworkJob: Job? = null

    private var mediaSession: MediaSessionCompat? = null
    private var currentArtworkBitmap: Bitmap? = null
    private var lastLoadedArtworkUrl: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "RenuevoRadioSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    (application as? RadioApp)?.repository?.play()
                }

                override fun onPause() {
                    (application as? RadioApp)?.repository?.pause()
                }

                override fun onStop() {
                    (application as? RadioApp)?.repository?.stop()
                    stopForegroundService()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? RadioApp
        val repo = app?.repository

        when (intent?.action) {
            ACTION_PLAY -> repo?.play()
            ACTION_PAUSE -> repo?.pause()
            ACTION_TOGGLE -> repo?.togglePlayPause()
            ACTION_STOP -> {
                repo?.stop()
                stopForegroundService()
                return START_NOT_STICKY
            }
        }

        startForegroundWithNotification()
        observeRadioState()

        return START_STICKY
    }

    private fun observeRadioState() {
        val app = application as? RadioApp ?: return
        val repo = app.repository

        stateJob?.cancel()
        stateJob = scope.launch {
            repo.playbackStatus.collectLatest { status ->
                updateMediaSessionState(status)
                if (status == PlaybackStatus.IDLE) {
                    stopForegroundService()
                } else {
                    updateNotification()
                }
            }
        }

        trackJob?.cancel()
        trackJob = scope.launch {
            repo.currentTrack.collectLatest { track ->
                loadArtworkIfAvailable(track?.artworkUrl)
                updateNotification()
            }
        }
    }

    private fun updateMediaSessionState(status: PlaybackStatus) {
        val session = mediaSession ?: return
        val state = when (status) {
            PlaybackStatus.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            PlaybackStatus.BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
            PlaybackStatus.PAUSED -> PlaybackStateCompat.STATE_PAUSED
            PlaybackStatus.IDLE -> PlaybackStateCompat.STATE_STOPPED
            PlaybackStatus.ERROR -> PlaybackStateCompat.STATE_ERROR
        }

        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_STOP
                )
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        )
    }

    private fun loadArtworkIfAvailable(artworkUrl: String?) {
        if (artworkUrl.isNullOrBlank()) {
            currentArtworkBitmap = null
            lastLoadedArtworkUrl = null
            return
        }

        if (artworkUrl == lastLoadedArtworkUrl && currentArtworkBitmap != null) {
            return
        }

        artworkJob?.cancel()
        artworkJob = scope.launch(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(this@RadioPlaybackService)
                val request = ImageRequest.Builder(this@RadioPlaybackService)
                    .data(artworkUrl)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        currentArtworkBitmap = bitmap
                        lastLoadedArtworkUrl = artworkUrl
                        updateNotification()
                    }
                }
            } catch (ignored: Exception) {}
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val app = application as? RadioApp
        val repo = app?.repository
        val currentTrack = repo?.currentTrack?.value
        val isPlaying = repo?.playbackStatus?.value == PlaybackStatus.PLAYING

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = currentTrack?.displayTitle ?: "Renuevo Radio"
        val artist = currentTrack?.displayArtist ?: "Life Turned Up! • En Vivo"

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseLabel = if (isPlaying) "Pausar" else "Reproducir"

        // Default to station logo if track artwork not downloaded yet
        val largeIconBitmap = currentArtworkBitmap ?: try {
            BitmapFactory.decodeResource(resources, R.drawable.ic_renuevo_artwork)
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("Renuevo Radio • En Vivo")
            .setSmallIcon(R.drawable.ic_stat_radio)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(playPauseIcon, playPauseLabel, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
        }

        return builder.build()
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transmisión de Radio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción para la radio en vivo"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stateJob?.cancel()
        trackJob?.cancel()
        artworkJob?.cancel()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "radio_live_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.radio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.radio.ACTION_PAUSE"
        const val ACTION_TOGGLE = "com.example.radio.ACTION_TOGGLE"
        const val ACTION_STOP = "com.example.radio.ACTION_STOP"

        fun startService(context: Context, action: String = ACTION_PLAY) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                this.action = action
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

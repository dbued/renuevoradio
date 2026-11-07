package com.example.player

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.metadata.icy.IcyInfo
import com.example.model.PlaybackStatus
import com.example.model.SongTrack
import com.example.model.StreamInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

class RadioPlayerManager(
    private val context: Context,
    private val onNewTrackDetected: (SongTrack) -> Unit
) {
    private val tag = "RadioPlayerManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val streamInfo = StreamInfo(stationName = "Renuevo Radio")

    private var exoPlayer: ExoPlayer? = null

    private val _status = MutableStateFlow(PlaybackStatus.IDLE)
    val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

    private val _currentTrack = MutableStateFlow<SongTrack?>(null)
    val currentTrack: StateFlow<SongTrack?> = _currentTrack.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0L)
    val sessionDurationSeconds: StateFlow<Long> = _sessionDurationSeconds.asStateFlow()

    private val _sleepTimerRemainingMinutes = MutableStateFlow(0)
    val sleepTimerRemainingMinutes: StateFlow<Int> = _sleepTimerRemainingMinutes.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow(0L)
    val sleepTimerRemainingSeconds: StateFlow<Long> = _sleepTimerRemainingSeconds.asStateFlow()

    private val _sleepTimerInitialSeconds = MutableStateFlow(0L)
    val sleepTimerInitialSeconds: StateFlow<Long> = _sleepTimerInitialSeconds.asStateFlow()

    private val _isFadeOutEnabled = MutableStateFlow(true)
    val isFadeOutEnabled: StateFlow<Boolean> = _isFadeOutEnabled.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var durationJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var metadataJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    init {
        _currentTrack.value = SongTrack(
            title = "Transmisión en Vivo",
            artist = streamInfo.stationName,
            rawTitle = "${streamInfo.stationName} - En Vivo"
        )
        initExoPlayer()
    }

    private fun initExoPlayer() {
        if (exoPlayer != null) return

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setUserAgent("RenuevoRadio/2.0 (Linux; Android)")

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        player.setAudioAttributes(audioAttributes, true)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _status.value = PlaybackStatus.BUFFERING
                    }
                    Player.STATE_READY -> {
                        _status.value = if (player.isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                        _errorMessage.value = null
                    }
                    Player.STATE_ENDED -> {
                        _status.value = PlaybackStatus.IDLE
                    }
                    Player.STATE_IDLE -> {
                        if (_status.value != PlaybackStatus.ERROR) {
                            _status.value = PlaybackStatus.IDLE
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    _status.value = PlaybackStatus.PLAYING
                    startDurationTicker()
                } else if (_status.value == PlaybackStatus.PLAYING) {
                    _status.value = PlaybackStatus.PAUSED
                    stopDurationTicker()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(tag, "ExoPlayer error: ${error.message}", error)
                _status.value = PlaybackStatus.ERROR
                _errorMessage.value = "Error de conexión con la radio: ${error.localizedMessage ?: "Reintentando..."}"
                stopDurationTicker()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val title = mediaMetadata.title?.toString()
                val artist = mediaMetadata.artist?.toString()
                if (!title.isNullOrBlank()) {
                    val fullRaw = if (!artist.isNullOrBlank()) "$artist - $title" else title
                    parseAndEmitTrack(fullRaw, streamInfo.stationName)
                }
            }

            override fun onMetadata(metadata: Metadata) {
                for (i in 0 until metadata.length()) {
                    val entry = metadata.get(i)
                    if (entry is IcyInfo) {
                        val streamTitle = entry.title
                        if (!streamTitle.isNullOrBlank()) {
                            parseAndEmitTrack(streamTitle, streamInfo.stationName)
                        }
                    }
                }
            }
        })

        exoPlayer = player
        applyVolume()
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying || _status.value == PlaybackStatus.PLAYING) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _errorMessage.value = null
        _status.value = PlaybackStatus.BUFFERING

        scope.launch {
            try {
                val player = exoPlayer ?: run {
                    initExoPlayer()
                    exoPlayer!!
                }

                // Resolve URL if needed or use fallback stream
                val targetUrl = resolveLiveUrl()
                Log.d(tag, "Playing stream target URL: $targetUrl")

                val mediaItem = MediaItem.Builder()
                    .setUri(targetUrl)
                    .setLiveConfiguration(
                        MediaItem.LiveConfiguration.Builder()
                            .setMaxPlaybackSpeed(1.0f)
                            .build()
                    )
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                applyVolume()
                startMetadataListener()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(tag, "Error initiating ExoPlayer: ${e.message}", e)
                _status.value = PlaybackStatus.ERROR
                _errorMessage.value = "No se pudo conectar a Renuevo Radio. Verifica tu internet."
            }
        }
    }

    fun pause() {
        try {
            exoPlayer?.pause()
        } catch (e: Exception) {
            Log.w(tag, "Error pausing ExoPlayer: ${e.message}")
        }
        _status.value = PlaybackStatus.PAUSED
        stopDurationTicker()
    }

    fun stop() {
        try {
            exoPlayer?.stop()
        } catch (e: Exception) {
            Log.w(tag, "Error stopping ExoPlayer: ${e.message}")
        }
        _status.value = PlaybackStatus.IDLE
        _sessionDurationSeconds.value = 0L
        stopDurationTicker()
        cancelSleepTimer()
    }

    private suspend fun resolveLiveUrl(): String = withContext(Dispatchers.IO) {
        // Try getting fresh 302 target from stream.surfernetwork.com
        try {
            val probeClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .followRedirects(false)
                .build()

            val request = Request.Builder()
                .url(streamInfo.fallbackUrl)
                .header("User-Agent", "RenuevoRadio/2.0 (Android)")
                .build()

            val response = probeClient.newCall(request).execute()
            val redirectLoc = response.header("Location")
            response.close()

            if (!redirectLoc.isNullOrBlank()) {
                Log.d(tag, "Resolved fresh stream redirect location: $redirectLoc")
                return@withContext redirectLoc
            }
        } catch (e: Exception) {
            Log.w(tag, "Fallback probe redirect failed, using primary stream URL: ${e.message}")
        }
        return@withContext streamInfo.streamUrl
    }

    fun setVolume(newVolume: Float) {
        val clamped = newVolume.coerceIn(0f, 1f)
        _volume.value = clamped
        if (_isMuted.value && clamped > 0f) {
            _isMuted.value = false
        }
        applyVolume()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        applyVolume()
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        applyVolume()
    }

    private fun applyVolume() {
        val effective = if (_isMuted.value) 0f else _volume.value
        setPlayerVolume(effective)
    }

    private fun setPlayerVolume(vol: Float) {
        try {
            exoPlayer?.volume = vol
        } catch (e: Exception) {
            Log.w(tag, "Failed to set player volume: ${e.message}")
        }
    }

    private fun startDurationTicker() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (_status.value == PlaybackStatus.PLAYING) {
                    _sessionDurationSeconds.value += 1
                }
            }
        }
    }

    private fun stopDurationTicker() {
        durationJob?.cancel()
        durationJob = null
    }

    // Sleep timer: set minutes (5, 10, 15, 30, etc.)
    fun setSleepTimer(minutes: Int, fadeOut: Boolean = _isFadeOutEnabled.value) {
        sleepTimerJob?.cancel()
        _isFadeOutEnabled.value = fadeOut
        if (minutes <= 0) {
            _sleepTimerRemainingMinutes.value = 0
            _sleepTimerRemainingSeconds.value = 0L
            _sleepTimerInitialSeconds.value = 0L
            return
        }

        val totalSeconds = minutes * 60L
        _sleepTimerInitialSeconds.value = totalSeconds
        _sleepTimerRemainingSeconds.value = totalSeconds
        _sleepTimerRemainingMinutes.value = minutes

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            val originalVolume = _volume.value
            while (isActive && remaining > 0L) {
                delay(1000)
                remaining -= 1
                _sleepTimerRemainingSeconds.value = remaining
                _sleepTimerRemainingMinutes.value = ((remaining + 59) / 60).toInt()

                // Optional gentle fade out in last 20 seconds
                if (_isFadeOutEnabled.value && remaining <= 20L && remaining > 0L) {
                    val fadeFactor = (remaining.toFloat() / 20f).coerceIn(0f, 1f)
                    val fadeVol = if (_isMuted.value) 0f else (originalVolume * fadeFactor)
                    setPlayerVolume(fadeVol)
                }
            }

            if (isActive && remaining <= 0L) {
                Log.d(tag, "Sleep timer expired, stopping playback")
                _sleepTimerRemainingSeconds.value = 0L
                _sleepTimerRemainingMinutes.value = 0
                _sleepTimerInitialSeconds.value = 0L
                stop()
                applyVolume()
            }
        }
    }

    fun addSleepTimerMinutes(extraMinutes: Int) {
        val currentRemaining = _sleepTimerRemainingSeconds.value
        val newTotal = (currentRemaining + extraMinutes * 60L).coerceAtLeast(60L)
        val minutes = ((newTotal + 59) / 60).toInt()
        setSleepTimer(minutes, _isFadeOutEnabled.value)
    }

    fun setFadeOutEnabled(enabled: Boolean) {
        _isFadeOutEnabled.value = enabled
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMinutes.value = 0
        _sleepTimerRemainingSeconds.value = 0L
        _sleepTimerInitialSeconds.value = 0L
        applyVolume()
    }

    // Secondary real-time ICY Metadata scraper via OkHttp socket
    private fun startMetadataListener() {
        metadataJob?.cancel()
        metadataJob = scope.launch(Dispatchers.IO) {
            var stream: InputStream? = null
            try {
                val req = Request.Builder()
                    .url(streamInfo.fallbackUrl)
                    .header("Icy-MetaData", "1")
                    .header("User-Agent", "RenuevoRadio/2.0 (Android ICY-Client)")
                    .build()

                val response = httpClient.newCall(req).execute()
                val metaIntHeader = response.header("icy-metaint")?.toIntOrNull() ?: 0
                val stationNameHeader = response.header("icy-name") ?: streamInfo.stationName

                if (metaIntHeader <= 0) {
                    response.close()
                    return@launch
                }

                stream = response.body?.byteStream() ?: return@launch
                val audioBuffer = ByteArray(metaIntHeader)

                while (isActive && stream != null) {
                    var bytesRead = 0
                    while (bytesRead < metaIntHeader && isActive) {
                        val read = stream.read(audioBuffer, bytesRead, metaIntHeader - bytesRead)
                        if (read == -1) break
                        bytesRead += read
                    }
                    if (bytesRead < metaIntHeader) break

                    val metaLenByte = stream.read()
                    if (metaLenByte == -1) break
                    val metaLen = metaLenByte * 16

                    if (metaLen > 0) {
                        val metaBytes = ByteArray(metaLen)
                        var metaBytesRead = 0
                        while (metaBytesRead < metaLen && isActive) {
                            val r = stream.read(metaBytes, metaBytesRead, metaLen - metaBytesRead)
                            if (r == -1) break
                            metaBytesRead += r
                        }

                        val metaString = String(metaBytes, Charsets.UTF_8).trim()
                        val streamTitle = extractStreamTitle(metaString)

                        if (!streamTitle.isNullOrBlank()) {
                            parseAndEmitTrack(streamTitle, stationNameHeader)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.w(tag, "ICY listener notice: ${e.message}")
                }
            } finally {
                try {
                    stream?.close()
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun extractStreamTitle(metaString: String): String? {
        val titlePrefix = "StreamTitle='"
        val startIndex = metaString.indexOf(titlePrefix)
        if (startIndex != -1) {
            val contentStart = startIndex + titlePrefix.length
            val endIndex = metaString.indexOf("';", contentStart)
            if (endIndex != -1) {
                return metaString.substring(contentStart, endIndex).trim()
            }
            val singleQuoteEnd = metaString.indexOf("'", contentStart)
            if (singleQuoteEnd != -1) {
                return metaString.substring(contentStart, singleQuoteEnd).trim()
            }
        }
        return null
    }

    private fun parseAndEmitTrack(rawTitle: String, fallbackStation: String) {
        if (rawTitle.isBlank()) return

        var artist = ""
        var title = ""

        if (rawTitle.contains(" - ")) {
            val parts = rawTitle.split(" - ", limit = 2)
            val first = parts[0].trim()
            val second = parts[1].trim()

            if (first.contains("Radio", ignoreCase = true)) {
                artist = first
                title = second
            } else {
                title = first
                artist = second
            }
        } else {
            title = rawTitle
            artist = fallbackStation
        }

        val track = SongTrack(
            title = title,
            artist = artist,
            rawTitle = rawTitle,
            timestamp = System.currentTimeMillis()
        )

        val current = _currentTrack.value
        if (current == null || current.rawTitle != rawTitle) {
            _currentTrack.value = track
            scope.launch(Dispatchers.Main) {
                onNewTrackDetected(track)
            }
        }
    }

    fun updateCurrentTrackArtwork(artworkUrl: String) {
        val current = _currentTrack.value ?: return
        _currentTrack.value = current.copy(artworkUrl = artworkUrl)
    }

    fun release() {
        stop()
        metadataJob?.cancel()
        sleepTimerJob?.cancel()
        durationJob?.cancel()
        try {
            exoPlayer?.release()
        } catch (ignored: Exception) {}
        exoPlayer = null
    }
}

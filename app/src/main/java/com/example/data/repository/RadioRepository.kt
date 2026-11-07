package com.example.data.repository

import com.example.data.local.SongDao
import com.example.data.preferences.UserPreferences
import com.example.data.remote.ArtworkService
import com.example.data.remote.BibleVerseService
import com.example.model.AppThemeMode
import com.example.model.BibleVerse
import com.example.model.PlaybackStatus
import com.example.model.SongTrack
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioRepository(
    private val songDao: SongDao,
    val userPreferences: UserPreferences,
    val playerManager: RadioPlayerManager,
    val artworkService: ArtworkService = ArtworkService(),
    val bibleVerseService: BibleVerseService = BibleVerseService()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val songHistory: Flow<List<SongTrack>> = songDao.getAllSongs()
    val playbackStatus: StateFlow<PlaybackStatus> = playerManager.status
    val currentTrack: StateFlow<SongTrack?> = playerManager.currentTrack
    val volume: StateFlow<Float> = playerManager.volume
    val isMuted: StateFlow<Boolean> = playerManager.isMuted
    val sessionDurationSeconds: StateFlow<Long> = playerManager.sessionDurationSeconds
    val sleepTimerRemainingMinutes: StateFlow<Int> = playerManager.sleepTimerRemainingMinutes
    val sleepTimerRemainingSeconds: StateFlow<Long> = playerManager.sleepTimerRemainingSeconds
    val sleepTimerInitialSeconds: StateFlow<Long> = playerManager.sleepTimerInitialSeconds
    val isFadeOutEnabled: StateFlow<Boolean> = playerManager.isFadeOutEnabled
    val errorMessage: StateFlow<String?> = playerManager.errorMessage
    val themeMode: StateFlow<AppThemeMode> = userPreferences.themeMode

    private val _verseOfTheDay = MutableStateFlow<BibleVerse?>(null)
    val verseOfTheDay: StateFlow<BibleVerse?> = _verseOfTheDay.asStateFlow()

    private val _isVerseLoading = MutableStateFlow(false)
    val isVerseLoading: StateFlow<Boolean> = _isVerseLoading.asStateFlow()

    init {
        // Sync preferences with player manager
        playerManager.setVolume(userPreferences.volume.value)
        playerManager.setMuted(userPreferences.isMuted.value)

        scope.launch {
            seedInitialTracksIfEmpty()
        }

        scope.launch {
            loadVerseOfTheDay()
        }
    }

    suspend fun loadVerseOfTheDay() {
        _isVerseLoading.value = true
        try {
            val verse = bibleVerseService.getVerseOfTheDay()
            _verseOfTheDay.value = verse
        } finally {
            _isVerseLoading.value = false
        }
    }

    fun fetchRandomVerse() {
        scope.launch {
            _isVerseLoading.value = true
            try {
                val verse = bibleVerseService.getRandomVerse()
                _verseOfTheDay.value = verse
            } finally {
                _isVerseLoading.value = false
            }
        }
    }

    private suspend fun seedInitialTracksIfEmpty() {
        val count = songDao.getCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val sampleTracks = listOf(
                SongTrack(
                    title = "Alabaré (That's Who I Praise)",
                    artist = "Kirios",
                    rawTitle = "Alavare Thats Who I Praise Brandon lake cover - Kirios",
                    timestamp = now - 180_000,
                    isFavorite = true,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/90/13/2e/90132ef2-5b9c-ca68-3e9a-7640b8a4f895/artwork.jpg/600x600bb.jpg"
                ),
                SongTrack(
                    title = "Danzando",
                    artist = "Gateway Worship Español",
                    rawTitle = "Gateway Worship Español - Danzando",
                    timestamp = now - 420_000,
                    isFavorite = false,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/bf/1d/18/bf1d1891-b66d-1bf8-aa7f-6a9ad626cfb7/196626577881.jpg/600x600bb.jpg"
                ),
                SongTrack(
                    title = "Hermoso Momento",
                    artist = "Kike Pavón",
                    rawTitle = "Kike Pavon - Hermoso Momento",
                    timestamp = now - 680_000,
                    isFavorite = true,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/1f/e7/8a/1fe78a2e-4bca-ea85-d6ca-5b5cb3cbbba6/artwork.jpg/600x600bb.jpg"
                ),
                SongTrack(
                    title = "Fiesta En El Desierto",
                    artist = "Montesanto",
                    rawTitle = "Montesanto - Fiesta En El Desierto",
                    timestamp = now - 940_000,
                    isFavorite = false,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/e5/22/19/e522197f-94ad-ba90-50b3-1cb8f9d0c242/artwork.jpg/600x600bb.jpg"
                ),
                SongTrack(
                    title = "A Danzar",
                    artist = "Barak ft. Redimi2",
                    rawTitle = "Barak - A Danzar",
                    timestamp = now - 1_200_000,
                    isFavorite = false,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/33/27/ef/3327ef86-f1c2-6da5-b541-e1fbb4eb25b6/194491979927.jpg/600x600bb.jpg"
                ),
                SongTrack(
                    title = "Tumbas a Jardines",
                    artist = "Elevation Worship",
                    rawTitle = "Elevation Worship - Tumbas a Jardines",
                    timestamp = now - 1_460_000,
                    isFavorite = false,
                    artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/bf/82/8a/bf828a15-0819-bf9f-e685-61884aa3a1f8/195497223072.jpg/600x600bb.jpg"
                )
            )

            for (track in sampleTracks) {
                songDao.insertSong(track)
            }
        }
    }

    fun onTrackDetected(track: SongTrack) {
        scope.launch {
            var trackWithArt = track
            val artUrl = artworkService.getArtworkUrl(track.artist, track.title)
            if (artUrl != null) {
                trackWithArt = track.copy(artworkUrl = artUrl)
                playerManager.updateCurrentTrackArtwork(artUrl)
            }

            // Check if same track recorded recently within 3 minutes
            val latest = songDao.getLatestByRawTitle(track.rawTitle)
            val now = System.currentTimeMillis()
            if (latest == null || (now - latest.timestamp) > 180_000) {
                songDao.insertSong(trackWithArt)
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun play() {
        playerManager.play()
    }

    fun pause() {
        playerManager.pause()
    }

    fun stop() {
        playerManager.stop()
    }

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
        userPreferences.setVolume(volume)
    }

    fun toggleMute() {
        playerManager.toggleMute()
        userPreferences.setMuted(playerManager.isMuted.value)
    }

    fun setThemeMode(mode: AppThemeMode) {
        userPreferences.setThemeMode(mode)
    }

    fun setSleepTimer(minutes: Int, fadeOut: Boolean = true) {
        playerManager.setSleepTimer(minutes, fadeOut)
    }

    fun addSleepTimerMinutes(extraMinutes: Int) {
        playerManager.addSleepTimerMinutes(extraMinutes)
    }

    fun setFadeOutEnabled(enabled: Boolean) {
        playerManager.setFadeOutEnabled(enabled)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun toggleFavorite(track: SongTrack) {
        scope.launch {
            songDao.updateFavorite(track.id, !track.isFavorite)
        }
    }

    fun deleteSong(id: Long) {
        scope.launch {
            songDao.deleteSong(id)
        }
    }

    fun clearHistory() {
        scope.launch {
            songDao.clearHistory()
        }
    }
}

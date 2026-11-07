package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RadioApp
import com.example.model.AppThemeMode
import com.example.model.BibleVerse
import com.example.model.PlaybackStatus
import com.example.model.SongTrack
import com.example.model.StreamInfo
import com.example.service.RadioPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    PLAYER,
    VERSE,
    HISTORY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RadioApp
    private val repository = app.repository

    val streamInfo: StreamInfo = repository.playerManager.streamInfo

    val playbackStatus: StateFlow<PlaybackStatus> = repository.playbackStatus
    val currentTrack: StateFlow<SongTrack?> = repository.currentTrack
    val volume: StateFlow<Float> = repository.volume
    val isMuted: StateFlow<Boolean> = repository.isMuted
    val sessionDurationSeconds: StateFlow<Long> = repository.sessionDurationSeconds
    val sleepTimerRemainingMinutes: StateFlow<Int> = repository.sleepTimerRemainingMinutes
    val sleepTimerRemainingSeconds: StateFlow<Long> = repository.sleepTimerRemainingSeconds
    val sleepTimerInitialSeconds: StateFlow<Long> = repository.sleepTimerInitialSeconds
    val isFadeOutEnabled: StateFlow<Boolean> = repository.isFadeOutEnabled
    val errorMessage: StateFlow<String?> = repository.errorMessage
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode
    val verseOfTheDay: StateFlow<BibleVerse?> = repository.verseOfTheDay
    val isVerseLoading: StateFlow<Boolean> = repository.isVerseLoading

    val songHistory: StateFlow<List<SongTrack>> = repository.songHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTab = MutableStateFlow(MainTab.PLAYER)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _isSleepTimerDialogOpen = MutableStateFlow(false)
    val isSleepTimerDialogOpen: StateFlow<Boolean> = _isSleepTimerDialogOpen.asStateFlow()

    private val _isStreamInfoDialogOpen = MutableStateFlow(false)
    val isStreamInfoDialogOpen: StateFlow<Boolean> = _isStreamInfoDialogOpen.asStateFlow()

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun togglePlayPause() {
        val currentStatus = playbackStatus.value
        if (currentStatus == PlaybackStatus.PLAYING || currentStatus == PlaybackStatus.BUFFERING) {
            RadioPlaybackService.startService(app, RadioPlaybackService.ACTION_PAUSE)
        } else {
            RadioPlaybackService.startService(app, RadioPlaybackService.ACTION_PLAY)
        }
    }

    fun play() {
        RadioPlaybackService.startService(app, RadioPlaybackService.ACTION_PLAY)
    }

    fun pause() {
        RadioPlaybackService.startService(app, RadioPlaybackService.ACTION_PAUSE)
    }

    fun stop() {
        RadioPlaybackService.stopService(app)
    }

    fun setVolume(newVolume: Float) {
        repository.setVolume(newVolume)
    }

    fun toggleMute() {
        repository.toggleMute()
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setSleepTimer(minutes: Int, fadeOut: Boolean = true) {
        repository.setSleepTimer(minutes, fadeOut)
    }

    fun addSleepTimerMinutes(extraMinutes: Int) {
        repository.addSleepTimerMinutes(extraMinutes)
    }

    fun setFadeOutEnabled(enabled: Boolean) {
        repository.setFadeOutEnabled(enabled)
    }

    fun cancelSleepTimer() {
        repository.cancelSleepTimer()
    }

    fun openSleepTimerDialog() {
        _isSleepTimerDialogOpen.value = true
    }

    fun closeSleepTimerDialog() {
        _isSleepTimerDialogOpen.value = false
    }

    fun openStreamInfoDialog() {
        _isStreamInfoDialogOpen.value = true
    }

    fun closeStreamInfoDialog() {
        _isStreamInfoDialogOpen.value = false
    }

    fun toggleFavorite(track: SongTrack) {
        repository.toggleFavorite(track)
    }

    fun deleteSong(id: Long) {
        repository.deleteSong(id)
    }

    fun clearHistory() {
        repository.clearHistory()
    }

    fun refreshVerse() {
        viewModelScope.launch {
            repository.loadVerseOfTheDay()
        }
    }

    fun fetchRandomVerse() {
        repository.fetchRandomVerse()
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatSleepTimer(remainingSeconds: Long): String {
        if (remainingSeconds <= 0L) return "00:00"
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

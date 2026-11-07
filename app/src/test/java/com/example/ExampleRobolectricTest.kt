package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppThemeMode
import com.example.model.PlaybackStatus
import com.example.model.SongTrack
import com.example.player.RadioPlayerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Renuevo Radio", appName)
    }

    @Test
    fun songTrackDisplayProperties() {
        val track = SongTrack(
            title = "Alabaré",
            artist = "Kirios",
            rawTitle = "Alavare - Kirios",
            isFavorite = true
        )
        assertEquals("Alabaré", track.displayTitle)
        assertEquals("Kirios", track.displayArtist)
        assertTrue(track.isFavorite)

        val fallbackTrack = SongTrack(
            title = "",
            artist = "",
            rawTitle = ""
        )
        assertEquals("Transmisión en Vivo", fallbackTrack.displayTitle)
        assertEquals("Renuevo Radio", fallbackTrack.displayArtist)
        assertFalse(fallbackTrack.isFavorite)
    }

    @Test
    fun themeModesAvailable() {
        assertEquals(3, AppThemeMode.values().size)
        assertTrue(AppThemeMode.values().contains(AppThemeMode.DARK))
        assertTrue(AppThemeMode.values().contains(AppThemeMode.LIGHT))
        assertTrue(AppThemeMode.values().contains(AppThemeMode.SYSTEM))
    }

    @Test
    fun playbackStatusesAvailable() {
        assertTrue(PlaybackStatus.values().contains(PlaybackStatus.PLAYING))
        assertTrue(PlaybackStatus.values().contains(PlaybackStatus.BUFFERING))
        assertTrue(PlaybackStatus.values().contains(PlaybackStatus.PAUSED))
    }

    @Test
    fun sleepTimerManagement() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = RadioPlayerManager(context) {}

        assertEquals(0L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(0, manager.sleepTimerRemainingMinutes.value)

        // Set 15 minutes preset
        manager.setSleepTimer(15, fadeOut = true)
        assertEquals(15 * 60L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(15, manager.sleepTimerRemainingMinutes.value)
        assertTrue(manager.isFadeOutEnabled.value)

        // Set 30 minutes preset
        manager.setSleepTimer(30, fadeOut = true)
        assertEquals(30 * 60L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(30, manager.sleepTimerRemainingMinutes.value)

        // Set 60 minutes preset
        manager.setSleepTimer(60, fadeOut = true)
        assertEquals(60 * 60L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(60, manager.sleepTimerRemainingMinutes.value)

        // Extend by +15 minutes
        manager.addSleepTimerMinutes(15)
        assertEquals(75 * 60L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(75, manager.sleepTimerRemainingMinutes.value)

        // Cancel
        manager.cancelSleepTimer()
        assertEquals(0L, manager.sleepTimerRemainingSeconds.value)
        assertEquals(0, manager.sleepTimerRemainingMinutes.value)
    }

    @Test
    fun bibleVerseModelAndShareFormat() {
        val verse = com.example.model.BibleVerse(
            reference = "Filipenses 4:13",
            text = "Todo lo puedo en Cristo que me fortalece.",
            version = "RVR1960",
            theme = "Fortaleza y Victoria"
        )
        assertEquals("Filipenses 4:13", verse.reference)
        assertEquals("RVR1960", verse.version)
        assertTrue(verse.formattedShareText.contains("Todo lo puedo en Cristo"))
        assertTrue(verse.formattedShareText.contains("Filipenses 4:13 (RVR1960)"))
        assertTrue(verse.formattedShareText.contains("Renuevo Radio"))
    }

    @Test
    fun songTrackArtworkProperty() {
        val track = SongTrack(
            title = "Danzando",
            artist = "Gateway Worship",
            rawTitle = "Gateway Worship - Danzando",
            artworkUrl = "https://example.com/artwork.jpg"
        )
        assertEquals("https://example.com/artwork.jpg", track.artworkUrl)
    }

    @Test
    fun mainTabsIncludeVerse() {
        val tabs = com.example.ui.MainTab.values()
        assertTrue(tabs.contains(com.example.ui.MainTab.PLAYER))
        assertTrue(tabs.contains(com.example.ui.MainTab.VERSE))
        assertTrue(tabs.contains(com.example.ui.MainTab.HISTORY))
        assertTrue(tabs.contains(com.example.ui.MainTab.SETTINGS))
    }
}

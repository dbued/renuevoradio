package com.example

import android.app.Application
import com.example.data.local.RadioDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.RadioRepository
import com.example.player.RadioPlayerManager

class RadioApp : Application() {
    lateinit var database: RadioDatabase
        private set

    lateinit var preferences: UserPreferences
        private set

    lateinit var playerManager: RadioPlayerManager
        private set

    lateinit var repository: RadioRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = RadioDatabase.getInstance(this)
        preferences = UserPreferences(this)
        playerManager = RadioPlayerManager(
            context = this,
            onNewTrackDetected = { track ->
                if (::repository.isInitialized) {
                    repository.onTrackDetected(track)
                }
            }
        )
        repository = RadioRepository(
            songDao = database.songDao(),
            userPreferences = preferences,
            playerManager = playerManager
        )
    }

    override fun onTerminate() {
        if (::playerManager.isInitialized) {
            playerManager.release()
        }
        super.onTerminate()
    }

    companion object {
        lateinit var instance: RadioApp
            private set
    }
}

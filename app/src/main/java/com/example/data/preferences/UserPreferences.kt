package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("radio_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _volume = MutableStateFlow(prefs.getFloat(KEY_VOLUME, 0.85f))
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(prefs.getBoolean(KEY_IS_MUTED, false))
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat(KEY_VOLUME, clamped).apply()
        _volume.value = clamped
    }

    fun setMuted(muted: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MUTED, muted).apply()
        _isMuted.value = muted
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_VOLUME = "pref_volume"
        private const val KEY_IS_MUTED = "pref_is_muted"
    }
}

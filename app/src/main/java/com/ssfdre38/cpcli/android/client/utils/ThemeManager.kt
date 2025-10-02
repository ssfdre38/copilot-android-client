package com.ssfdre38.cpcli.android.client.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

object ThemeManager {
    
    private const val PREF_THEME_MODE = "theme_mode"
    
    enum class ThemeMode(val value: Int) {
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    fun applyTheme(context: Context) {
        val themeMode = getThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(themeMode.value)
    }
    
    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(PREF_THEME_MODE, themeMode.name).apply()
        AppCompatDelegate.setDefaultNightMode(themeMode.value)
    }
    
    fun getThemeMode(context: Context): ThemeMode {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val themeName = prefs.getString(PREF_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    fun isDarkModeActive(context: Context): Boolean {
        return when (getThemeMode(context)) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    fun getThemeModeDisplayName(context: Context, themeMode: ThemeMode): String {
        return when (themeMode) {
            ThemeMode.SYSTEM -> "System Default"
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
        }
    }
}
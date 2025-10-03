package com.ssfdre38.cpcli.android.client.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private var isThemeApplied = false
    
    fun isDarkModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        val currentTheme = isDarkModeEnabled(context)
        
        // Only change theme if it's actually different
        if (currentTheme != enabled) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
            
            // Apply theme immediately with smooth transition
            applyThemeImmediate(enabled)
        }
    }
    
    fun applyTheme(context: Context) {
        // Only apply theme once per activity lifecycle to prevent flicker
        if (!isThemeApplied) {
            val isDarkMode = isDarkModeEnabled(context)
            applyThemeImmediate(isDarkMode)
            isThemeApplied = true
        }
    }
    
    private fun applyThemeImmediate(isDarkMode: Boolean) {
        // Use immediate mode change to prevent flicker
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        // Only set if different from current
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
    
    fun resetThemeState() {
        isThemeApplied = false
    }
}
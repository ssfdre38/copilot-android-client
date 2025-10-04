package com.ssfdre38.cpcli.android.client

import android.app.Application
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

/**
 * Application class for GitHub Copilot CLI
 * Handles global app initialization and theme management
 */
class CopilotApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize modern theme system on app startup
        initializeTheme()
    }
    
    private fun initializeTheme() {
        // Apply the saved theme mode on app startup
        ThemeManager.applyTheme(this, ThemeManager.getThemeMode(this))
    }
}
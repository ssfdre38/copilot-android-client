package com.github.copilot.client

import android.app.Application
import com.github.copilot.client.utils.ThemeManager

class CopilotApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply the saved theme when the app starts
        ThemeManager.applyTheme(this)
    }
}
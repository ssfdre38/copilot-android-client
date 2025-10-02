package com.ssfdre38.cpcli.android.client

import android.app.Application
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class CopilotApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply the saved theme when the app starts
        ThemeManager.applyTheme(this)
    }
}
package com.ssfdre38.cpcli.android.client.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ssfdre38.cpcli.android.client.ui.ModernUIManager

/**
 * Modern Theme Manager that provides universal theming without relying on XML themes
 * This manager automatically detects system theme preferences and provides consistent styling
 */
object ThemeManager {
    private const val PREFS_NAME = "modern_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_USE_SYSTEM_THEME = "use_system_theme"
    
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    /**
     * Gets the current theme mode preference
     */
    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useSystemTheme = prefs.getBoolean(KEY_USE_SYSTEM_THEME, true)
        
        return if (useSystemTheme) {
            ThemeMode.SYSTEM
        } else {
            val themeOrdinal = prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal)
            ThemeMode.values().getOrElse(themeOrdinal) { ThemeMode.SYSTEM }
        }
    }
    
    /**
     * Sets the theme mode preference
     */
    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_THEME_MODE, themeMode.ordinal)
            putBoolean(KEY_USE_SYSTEM_THEME, themeMode == ThemeMode.SYSTEM)
            apply()
        }
        
        applyTheme(context, themeMode)
    }
    
    /**
     * Checks if dark mode is currently active
     */
    fun isDarkModeActive(context: Context): Boolean {
        return when (getThemeMode(context)) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkMode(context)
        }
    }
    
    /**
     * Checks if the system is in dark mode
     */
    private fun isSystemInDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * Applies the theme to the entire app
     */
    fun applyTheme(context: Context, themeMode: ThemeMode = getThemeMode(context)) {
        val nightMode = when (themeMode) {
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        // Only apply if different to prevent unnecessary recreations
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }
    
    /**
     * Applies modern theme to an activity (should be called before setContentView)
     */
    fun applyActivityTheme(activity: Activity) {
        // Apply the global theme mode with explicit theme mode parameter
        applyTheme(activity, getThemeMode(activity))
        
        // Configure window for modern look
        configureWindow(activity)
    }
    
    /**
     * Configures the window for a modern, immersive experience
     */
    private fun configureWindow(activity: Activity) {
        val window = activity.window
        
        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Configure status bar and navigation bar
        val isDark = isDarkModeActive(activity)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        
        // Set status bar appearance
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
        
        // Set window colors based on theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = if (isDark) {
                ModernUIManager.Colors.SURFACE_DARK
            } else {
                ModernUIManager.Colors.SURFACE_LIGHT
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.navigationBarColor = if (isDark) {
                    ModernUIManager.Colors.SURFACE_DARK
                } else {
                    ModernUIManager.Colors.SURFACE_LIGHT
                }
            }
        }
        
        // Ensure hardware acceleration is enabled for smooth animations
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
    }
    
    /**
     * Applies theme to a view hierarchy programmatically
     */
    fun applyThemeToView(context: Context, view: View) {
        // Set the background to match the current theme
        ModernUIManager.setWindowBackground(context, view)
        
        // Apply theme recursively to child views if needed
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                applyThemeToChildView(context, child)
            }
        }
    }
    
    /**
     * Applies appropriate theming to specific child view types
     */
    private fun applyThemeToChildView(context: Context, view: View) {
        when (view) {
            is android.widget.Button -> {
                ModernUIManager.styleButton(context, view)
            }
            is android.widget.EditText -> {
                ModernUIManager.styleEditText(context, view)
            }
            is android.widget.TextView -> {
                ModernUIManager.styleTextView(context, view)
            }
            is android.view.ViewGroup -> {
                // Recursively apply to nested views
                for (i in 0 until view.childCount) {
                    applyThemeToChildView(context, view.getChildAt(i))
                }
            }
        }
    }
    
    /**
     * Legacy compatibility methods for existing code
     */
    @Deprecated("Use getThemeMode() and isDarkModeActive() instead")
    fun isDarkModeEnabled(context: Context): Boolean {
        return isDarkModeActive(context)
    }
    
    @Deprecated("Use setThemeMode() instead")
    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        setThemeMode(context, if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }
    
    @Deprecated("Use applyTheme(context, themeMode) instead")
    fun initializeTheme(context: Context) {
        applyTheme(context, getThemeMode(context))
    }
}
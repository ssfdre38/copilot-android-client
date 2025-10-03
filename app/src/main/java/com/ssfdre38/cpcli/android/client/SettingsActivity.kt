package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.setPadding
import com.ssfdre38.cpcli.android.client.data.StorageManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var storageManager: StorageManager
    private var isDarkMode = false
    private var autoUpdates = true
    private var notificationsEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            storageManager = StorageManager(this)
            
            // Load current settings
            loadSettings()
            
            // Create dynamic responsive layout
            createDynamicLayout()
            
            title = "Settings"
            
            Toast.makeText(this, "✅ Settings loaded with dynamic layout!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading settings: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun loadSettings() {
        try {
            val settings = storageManager.getAppSettings()
            isDarkMode = settings.isDarkMode
            autoUpdates = settings.autoCheckUpdates
            
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        } catch (e: Exception) {
            // Use defaults if loading fails
            isDarkMode = false
            autoUpdates = true
            notificationsEnabled = true
        }
    }
    
    private fun createDynamicLayout() {
        // Main container - automatically adapts to screen size
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16))
        }
        
        // Title header that scales with screen
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            textSize = calculateTextSize(24f)
            gravity = Gravity.CENTER
            setPadding(dpToPx(16))
        }
        
        // Scrollable content area - responsive to screen height
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // Take remaining space
            )
        }
        
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(8))
        }
        
        // Create responsive sections
        contentLayout.addView(createAppearanceSection())
        contentLayout.addView(createSpacing())
        contentLayout.addView(createSystemSection())
        contentLayout.addView(createSpacing())
        contentLayout.addView(createActionButtons())
        
        scrollView.addView(contentLayout)
        
        mainLayout.addView(titleText)
        mainLayout.addView(scrollView)
        
        setContentView(mainLayout)
    }
    
    private fun createAppearanceSection(): View {
        val section = createSection("🎨 Appearance")
        
        // Dark mode toggle
        val darkModeRow = createToggleRow(
            "🌙 Dark Mode",
            "Switch between light and dark themes",
            isDarkMode
        ) { enabled ->
            isDarkMode = enabled
            saveSettings()
            Toast.makeText(this, if (enabled) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
        }
        
        // Auto updates toggle  
        val updatesRow = createToggleRow(
            "🔄 Auto Updates",
            "Automatically check for app updates",
            autoUpdates
        ) { enabled ->
            autoUpdates = enabled
            saveSettings()
            Toast.makeText(this, if (enabled) "Auto updates enabled" else "Auto updates disabled", Toast.LENGTH_SHORT).show()
        }
        
        section.addView(darkModeRow)
        section.addView(createDivider())
        section.addView(updatesRow)
        
        return section
    }
    
    private fun createSystemSection(): View {
        val section = createSection("🔧 System")
        
        val notificationRow = createToggleRow(
            "🔔 Notifications",
            "Enable app notifications",
            notificationsEnabled
        ) { enabled ->
            notificationsEnabled = enabled
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", enabled).apply()
            Toast.makeText(this, if (enabled) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
        
        val helpButton = createButtonRow("❓ Help & Support", "Get help using the app") {
            Toast.makeText(this, "Help coming soon - check GitHub for documentation", Toast.LENGTH_SHORT).show()
        }
        
        val aboutButton = createButtonRow("ℹ️ About", "About this app") {
            Toast.makeText(this, "Copilot Android Client v2.1.0 - Dynamic Layout Edition", Toast.LENGTH_LONG).show()
        }
        
        section.addView(notificationRow)
        section.addView(createDivider())
        section.addView(helpButton)
        section.addView(createDivider())
        section.addView(aboutButton)
        
        return section
    }
    
    private fun createActionButtons(): View {
        val section = createSection("⚡ Actions")
        
        val clearDataButton = createButtonRow("🗑️ Clear All Data", "Reset app to defaults", true) {
            clearData()
        }
        
        val backButton = createButtonRow("← Back to Main", "Return to main screen") {
            finish()
        }
        
        section.addView(clearDataButton)
        section.addView(createDivider())
        section.addView(backButton)
        
        return section
    }
    
    private fun createSection(title: String): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createCardBackground()
            setPadding(dpToPx(16))
        }
        
        val titleView = TextView(this).apply {
            text = title
            textSize = calculateTextSize(18f)
            setPadding(0, 0, 0, dpToPx(8))
        }
        
        section.addView(titleView)
        return section
    }
    
    private fun createToggleRow(title: String, description: String, checked: Boolean, onToggle: (Boolean) -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
        
        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val titleView = TextView(this).apply {
            text = title
            textSize = calculateTextSize(16f)
        }
        
        val descView = TextView(this).apply {
            text = description
            textSize = calculateTextSize(12f)
            alpha = 0.7f
        }
        
        val switch = Switch(this).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
        }
        
        textContainer.addView(titleView)
        textContainer.addView(descView)
        
        row.addView(textContainer)
        row.addView(switch)
        
        return row
    }
    
    private fun createButtonRow(title: String, description: String, isDangerous: Boolean = false, onClick: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(12), 0, dpToPx(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            background = createButtonBackground()
        }
        
        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val titleView = TextView(this).apply {
            text = title
            textSize = calculateTextSize(16f)
            if (isDangerous) setTextColor(0xFFE53E3E.toInt())
        }
        
        val descView = TextView(this).apply {
            text = description
            textSize = calculateTextSize(12f)
            alpha = 0.7f
        }
        
        val arrow = TextView(this).apply {
            text = "→"
            textSize = calculateTextSize(18f)
        }
        
        textContainer.addView(titleView)
        textContainer.addView(descView)
        
        row.addView(textContainer)
        row.addView(arrow)
        
        return row
    }
    
    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
            ).apply {
                setMargins(0, dpToPx(4), 0, dpToPx(4))
            }
            setBackgroundColor(0x1F000000)
        }
    }
    
    private fun createSpacing(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(16)
            )
        }
    }
    
    private fun createCardBackground(): android.graphics.drawable.Drawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(8).toFloat()
        drawable.setColor(0x08000000)
        drawable.setStroke(dpToPx(1), 0x1F000000)
        return drawable
    }
    
    private fun createButtonBackground(): android.graphics.drawable.Drawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(4).toFloat()
        drawable.setColor(0x05000000)
        return drawable
    }
    
    // Responsive text sizing based on screen density and size
    private fun calculateTextSize(baseSizeSp: Float): Float {
        val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        val scaleFactor = when {
            screenWidthDp < 360 -> 0.85f // Small phones
            screenWidthDp > 600 -> 1.15f // Tablets
            else -> 1.0f // Normal phones
        }
        return baseSizeSp * scaleFactor
    }
    
    // Convert dp to px for current screen density
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    private fun saveSettings() {
        try {
            val settings = storageManager.getAppSettings().copy(
                isDarkMode = isDarkMode,
                autoCheckUpdates = autoUpdates
            )
            storageManager.saveAppSettings(settings)
            
            // Apply theme change
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving settings", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearData() {
        try {
            // Reset preferences
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().clear().apply()
            
            Toast.makeText(this, "All data cleared successfully", Toast.LENGTH_LONG).show()
            
            // Return to main activity
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
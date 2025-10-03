package com.ssfdre38.cpcli.android.client.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var darkModeSwitch: Switch
    private lateinit var autoConnectSwitch: Switch
    private lateinit var notificationsSwitch: Switch
    private lateinit var chatHistoryLimitSpinner: Spinner
    private lateinit var serverTimeoutSeekBar: SeekBar
    private lateinit var serverTimeoutLabel: TextView
    private lateinit var clearDataButton: Button
    private lateinit var exportConfigButton: Button
    private lateinit var aboutButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            setupUI()
            loadSettings()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupUI() {
        val scrollView = ScrollView(this)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        
        // Appearance Section
        val appearanceCard = createSectionCard("🎨 Appearance")
        val appearanceLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Dark Mode
        val darkModeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val darkModeLabel = TextView(this).apply {
            text = "Dark Mode"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        darkModeSwitch = Switch(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                // Show immediate feedback
                Toast.makeText(this@SettingsActivity, "Applying theme...", Toast.LENGTH_SHORT).show()
                
                // Set theme with smooth transition
                ThemeManager.setDarkModeEnabled(this@SettingsActivity, isChecked)
                
                // Show confirmation
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this@SettingsActivity, "✅ Theme applied successfully!", Toast.LENGTH_SHORT).show()
                }, 300)
            }
        }
        
        darkModeLayout.addView(darkModeLabel)
        darkModeLayout.addView(darkModeSwitch)
        
        // Connection Section
        val connectionCard = createSectionCard("🌐 Connection")
        val connectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Auto Connect
        val autoConnectLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val autoConnectLabel = TextView(this).apply {
            text = "Auto-connect to last server"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        autoConnectSwitch = Switch(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                saveAutoConnectSetting(isChecked)
            }
        }
        
        autoConnectLayout.addView(autoConnectLabel)
        autoConnectLayout.addView(autoConnectSwitch)
        
        // Server Timeout
        val timeoutLabel = TextView(this).apply {
            text = "Connection Timeout"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }
        
        serverTimeoutLabel = TextView(this).apply {
            text = "30 seconds"
            textSize = 14f
            gravity = Gravity.CENTER
        }
        
        serverTimeoutSeekBar = SeekBar(this).apply {
            max = 120 // 120 seconds max
            progress = 30 // default 30 seconds
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val timeout = if (progress < 5) 5 else progress
                    serverTimeoutLabel.text = "$timeout seconds"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val timeout = if (seekBar!!.progress < 5) 5 else seekBar.progress
                    saveTimeoutSetting(timeout)
                }
            })
        }
        
        // Notifications Section
        val notificationsCard = createSectionCard("🔔 Notifications")
        val notificationsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        val notificationsToggleLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val notificationsLabel = TextView(this).apply {
            text = "Show connection notifications"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        notificationsSwitch = Switch(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                saveNotificationsSetting(isChecked)
            }
        }
        
        notificationsToggleLayout.addView(notificationsLabel)
        notificationsToggleLayout.addView(notificationsSwitch)
        
        // Chat History Section
        val historyCard = createSectionCard("💬 Chat History")
        val historyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        val historyLimitLabel = TextView(this).apply {
            text = "Maximum messages to keep"
            textSize = 16f
            setPadding(0, 0, 0, 10)
        }
        
        val historyOptions = arrayOf("100", "500", "1000", "2000", "Unlimited")
        chatHistoryLimitSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_item, historyOptions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(2) // Default to 1000
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    val limit = when (position) {
                        0 -> 100
                        1 -> 500
                        2 -> 1000
                        3 -> 2000
                        4 -> -1 // Unlimited
                        else -> 1000
                    }
                    saveChatHistoryLimit(limit)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        
        // Data Management Section
        val dataCard = createSectionCard("🗂️ Data Management")
        val dataLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        clearDataButton = Button(this).apply {
            text = "🗑️ Clear All Chat History"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
            setOnClickListener { clearAllData() }
        }
        
        exportConfigButton = Button(this).apply {
            text = "📤 Export Server Configuration"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
            setOnClickListener { exportConfiguration() }
        }
        
        // About Section
        val aboutCard = createSectionCard("ℹ️ About")
        val aboutLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        aboutButton = Button(this).apply {
            text = "📖 About GitHub Copilot CLI"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { 
                // Open dedicated About activity instead of Help
                val intent = android.content.Intent(this@SettingsActivity, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Back to Main"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 30, 0, 0)
            }
            setOnClickListener { finish() }
        }
        
        // Assemble sections
        appearanceLayout.addView(darkModeLayout)
        appearanceCard.addView(appearanceLayout)
        
        connectionLayout.addView(autoConnectLayout)
        connectionLayout.addView(timeoutLabel)
        connectionLayout.addView(serverTimeoutLabel)
        connectionLayout.addView(serverTimeoutSeekBar)
        connectionCard.addView(connectionLayout)
        
        notificationsLayout.addView(notificationsToggleLayout)
        notificationsCard.addView(notificationsLayout)
        
        historyLayout.addView(historyLimitLabel)
        historyLayout.addView(chatHistoryLimitSpinner)
        historyCard.addView(historyLayout)
        
        dataLayout.addView(clearDataButton)
        dataLayout.addView(exportConfigButton)
        dataCard.addView(dataLayout)
        
        aboutLayout.addView(aboutButton)
        aboutCard.addView(aboutLayout)
        
        // Add all to main layout
        mainLayout.addView(titleText)
        mainLayout.addView(appearanceCard)
        mainLayout.addView(connectionCard)
        mainLayout.addView(notificationsCard)
        mainLayout.addView(historyCard)
        mainLayout.addView(dataCard)
        mainLayout.addView(aboutCard)
        mainLayout.addView(backButton)
        
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun createSectionCard(title: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
        }
        
        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setPadding(16, 16, 16, 8)
        }
        
        card.addView(titleView)
        return card
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        
        // Load dark mode from ThemeManager instead of app_settings
        darkModeSwitch.isChecked = ThemeManager.isDarkModeEnabled(this)
        autoConnectSwitch.isChecked = prefs.getBoolean("auto_connect", false)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications", true)
        
        val timeout = prefs.getInt("connection_timeout", 30)
        serverTimeoutSeekBar.progress = timeout
        serverTimeoutLabel.text = "$timeout seconds"
        
        val historyLimit = prefs.getInt("chat_history_limit", 1000)
        val spinnerPosition = when (historyLimit) {
            100 -> 0
            500 -> 1
            1000 -> 2
            2000 -> 3
            -1 -> 4
            else -> 2
        }
        chatHistoryLimitSpinner.setSelection(spinnerPosition)
    }
    
    // Remove old dark mode methods - now handled by ThemeManager
    
    private fun saveAutoConnectSetting(enabled: Boolean) {
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putBoolean("auto_connect", enabled)
            .apply()
    }
    
    private fun saveTimeoutSetting(timeout: Int) {
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putInt("connection_timeout", timeout)
            .apply()
    }
    
    private fun saveNotificationsSetting(enabled: Boolean) {
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putBoolean("notifications", enabled)
            .apply()
    }
    
    private fun saveChatHistoryLimit(limit: Int) {
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putInt("chat_history_limit", limit)
            .apply()
    }
    
    private fun clearAllData() {
        // Enhanced confirmation dialog with more details
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Clear All Data")
            .setMessage("This will permanently delete:\n\n" +
                       "• All chat history from all servers\n" +
                       "• All app settings and preferences\n" +
                       "• Theme preferences\n" +
                       "• Connection settings\n\n" +
                       "⚠️ This action cannot be undone!\n\n" +
                       "Server configurations will be preserved.")
            .setPositiveButton("Clear All Data") { _, _ ->
                // Show loading dialog
                val loadingDialog = showLoadingDialog("Clearing all data...")
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    
                    try {
                        // Clear chat history - use a simple approach
                        val chatPrefs = getSharedPreferences("chat_history", MODE_PRIVATE)
                        chatPrefs.edit().clear().apply()
                        
                        // Clear settings
                        getSharedPreferences("app_settings", MODE_PRIVATE).edit().clear().apply()
                        
                        // Clear theme preferences
                        getSharedPreferences("theme_prefs", MODE_PRIVATE).edit().clear().apply()
                        
                        Toast.makeText(this, "✅ All data cleared successfully", Toast.LENGTH_SHORT).show()
                        
                        // Show restart recommendation
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Data Cleared")
                            .setMessage("All data has been cleared. It's recommended to restart the app for best results.")
                            .setPositiveButton("Restart App") { _, _ ->
                                // Restart the app
                                val intent = packageManager.getLaunchIntentForPackage(packageName)
                                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            .setNegativeButton("Continue", null)
                            .show()
                            
                    } catch (e: Exception) {
                        showErrorDialog("Clear Failed", "Failed to clear some data: ${e.message}")
                    }
                }, 1500) // 1.5 second processing simulation
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear Chat Only") { _, _ ->
                // Option to clear only chat history
                clearChatHistoryOnly()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun clearChatHistoryOnly() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Clear Chat History")
            .setMessage("This will delete all chat conversations but preserve your settings and server configurations.")
            .setPositiveButton("Clear Chat History") { _, _ ->
                val loadingDialog = showLoadingDialog("Clearing chat history...")
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    
                    val chatPrefs = getSharedPreferences("chat_history", MODE_PRIVATE)
                    chatPrefs.edit().clear().apply()
                    
                    Toast.makeText(this, "✅ Chat history cleared", Toast.LENGTH_SHORT).show()
                }, 800)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLoadingDialog(message: String): android.app.AlertDialog {
        val loadingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 40, 40, 40)
            gravity = android.view.Gravity.CENTER
        }
        
        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 20, 0)
            }
        }
        
        val messageText = TextView(this).apply {
            text = message
            textSize = 16f
        }
        
        loadingLayout.addView(progressBar)
        loadingLayout.addView(messageText)
        
        return android.app.AlertDialog.Builder(this)
            .setView(loadingLayout)
            .setCancelable(false)
            .create().apply { show() }
    }
    
    private fun showErrorDialog(title: String, message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("❌ $title")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun exportConfiguration() {
        try {
            val serverManager = ServerConfigManager(this)
            val servers = serverManager.getAllServers()
            val config = StringBuilder()
            
            config.append("# GitHub Copilot CLI - Server Configuration Export\n")
            config.append("# Generated: ${java.util.Date()}\n\n")
            
            servers.forEach { server ->
                config.append("Server: ${server.name}\n")
                config.append("URL: ${server.getFullUrl()}\n")
                config.append("Description: ${server.description}\n")
                config.append("---\n")
            }
            
            // For now, just show in a dialog (in a real app, you'd save to file)
            android.app.AlertDialog.Builder(this)
                .setTitle("Server Configuration")
                .setMessage(config.toString())
                .setPositiveButton("OK", null)
                .show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.resetThemeState()
    }
}
package com.ssfdre38.cpcli.android.client.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.ui.ModernUIManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var themeModeSpinner: Spinner
    private lateinit var autoConnectSwitch: Switch
    private lateinit var notificationsSwitch: Switch
    private lateinit var chatHistoryLimitSpinner: Spinner
    private lateinit var serverTimeoutSeekBar: SeekBar
    private lateinit var serverTimeoutLabel: TextView
    private lateinit var clearDataButton: Button
    private lateinit var exportConfigButton: Button
    private lateinit var aboutButton: Button
    private lateinit var mainLayout: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply modern theme
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            createModernSettingsLayout()
            loadSettings()
            setupWindowInsets()
            
            // Apply modern theme to all UI elements
            ThemeManager.applyThemeToView(this, mainLayout)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun createModernSettingsLayout() {
        val scrollView = ScrollView(this)
        
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM)
            )
        }
        ModernUIManager.setWindowBackground(this, mainLayout)
        
        // Title
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.LARGE))
            }
        }
        ModernUIManager.styleTextView(this, titleText, ModernUIManager.TextType.HEADLINE_MEDIUM)
        
        // Appearance Section
        val appearanceCard = createModernSectionCard("🎨 Appearance")
        
        // Theme mode selection
        val themeModeLayout = createSettingRow("Theme Mode")
        val themeModeOptions = arrayOf("System Default", "Light", "Dark")
        themeModeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_item, themeModeOptions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    val themeMode = when (position) {
                        0 -> ThemeManager.ThemeMode.SYSTEM
                        1 -> ThemeManager.ThemeMode.LIGHT
                        2 -> ThemeManager.ThemeMode.DARK
                        else -> ThemeManager.ThemeMode.SYSTEM
                    }
                    
                    // Apply theme with smooth transition
                    Toast.makeText(this@SettingsActivity, "Applying theme...", Toast.LENGTH_SHORT).show()
                    ThemeManager.setThemeMode(this@SettingsActivity, themeMode)
                    
                    // Show confirmation
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        Toast.makeText(this@SettingsActivity, "✅ Theme applied successfully!", Toast.LENGTH_SHORT).show()
                    }, 300)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        themeModeLayout.addView(themeModeSpinner)
        appearanceCard.addView(themeModeLayout)
        
        // Connection Section
        val connectionCard = createModernSectionCard("🌐 Connection")
        
        // Auto Connect
        val autoConnectLayout = createSettingRow("Auto-connect to last server")
        autoConnectSwitch = Switch(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                saveAutoConnectSetting(isChecked)
            }
        }
        autoConnectLayout.addView(autoConnectSwitch)
        connectionCard.addView(autoConnectLayout)
        
        // Server Timeout
        val timeoutContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
            )
        }
        
        val timeoutLabel = TextView(this).apply {
            text = "Connection Timeout"
        }
        ModernUIManager.styleTextView(this, timeoutLabel, ModernUIManager.TextType.BODY_LARGE)
        
        serverTimeoutLabel = TextView(this).apply {
            text = "30 seconds"
            gravity = Gravity.CENTER
            setPadding(0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL), 0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL))
        }
        ModernUIManager.styleTextView(this, serverTimeoutLabel, ModernUIManager.TextType.BODY_MEDIUM)
        
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
        
        timeoutContainer.addView(timeoutLabel)
        timeoutContainer.addView(serverTimeoutLabel)
        timeoutContainer.addView(serverTimeoutSeekBar)
        connectionCard.addView(timeoutContainer)
        
        // Notifications Section
        val notificationsCard = createModernSectionCard("🔔 Notifications")
        val notificationsLayout = createSettingRow("Show connection notifications")
        notificationsSwitch = Switch(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                saveNotificationsSetting(isChecked)
            }
        }
        notificationsLayout.addView(notificationsSwitch)
        notificationsCard.addView(notificationsLayout)
        
        // Chat History Section
        val historyCard = createModernSectionCard("💬 Chat History")
        val historyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
            )
        }
        
        val historyLimitLabel = TextView(this).apply {
            text = "Maximum messages to keep"
            setPadding(0, 0, 0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL))
        }
        ModernUIManager.styleTextView(this, historyLimitLabel, ModernUIManager.TextType.BODY_LARGE)
        
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
        
        historyContainer.addView(historyLimitLabel)
        historyContainer.addView(chatHistoryLimitSpinner)
        historyCard.addView(historyContainer)
        
        // Data Management Section
        val dataCard = createModernSectionCard("🗂️ Data Management")
        
        clearDataButton = Button(this).apply {
            text = "🗑️ Clear All Chat History"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
                )
            }
            setOnClickListener { clearAllData() }
        }
        ModernUIManager.styleButton(this, clearDataButton, ModernUIManager.ButtonType.SECONDARY)
        
        exportConfigButton = Button(this).apply {
            text = "📤 Export Server Configuration"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    0,
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
                )
            }
            setOnClickListener { exportConfiguration() }
        }
        ModernUIManager.styleButton(this, exportConfigButton, ModernUIManager.ButtonType.SECONDARY)
        
        dataCard.addView(clearDataButton)
        dataCard.addView(exportConfigButton)
        
        // About Section
        val aboutCard = createModernSectionCard("ℹ️ About")
        
        aboutButton = Button(this).apply {
            text = "📖 About GitHub Copilot CLI"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                    ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
                )
            }
            setOnClickListener { 
                val intent = Intent(this@SettingsActivity, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        ModernUIManager.styleButton(this, aboutButton, ModernUIManager.ButtonType.TEXT)
        
        aboutCard.addView(aboutButton)
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Back to Main"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.LARGE), 0, 0)
            }
            setOnClickListener { finish() }
        }
        ModernUIManager.styleButton(this, backButton, ModernUIManager.ButtonType.PRIMARY)
        
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
    
    private fun createModernSectionCard(title: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL), 0, ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL))
            }
        }
        ModernUIManager.styleContainer(this, card)
        
        val titleView = TextView(this).apply {
            text = title
            setPadding(
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
            )
        }
        ModernUIManager.styleTextView(this, titleView, ModernUIManager.TextType.TITLE_MEDIUM)
        
        card.addView(titleView)
        return card
    }
    
    private fun createSettingRow(labelText: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@SettingsActivity, ModernUIManager.Spacing.SMALL)
            )
        }
        
        val label = TextView(this).apply {
            text = labelText
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        ModernUIManager.styleTextView(this, label, ModernUIManager.TextType.BODY_LARGE)
        
        row.addView(label)
        return row
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM),
                top = systemBars.top + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.SMALL),
                right = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM),
                bottom = systemBars.bottom + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.SMALL)
            )
            insets
        }
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        
        // Load theme mode from modern ThemeManager
        val currentThemeMode = ThemeManager.getThemeMode(this)
        val spinnerPosition = when (currentThemeMode) {
            ThemeManager.ThemeMode.SYSTEM -> 0
            ThemeManager.ThemeMode.LIGHT -> 1
            ThemeManager.ThemeMode.DARK -> 2
        }
        themeModeSpinner.setSelection(spinnerPosition)
        
        autoConnectSwitch.isChecked = prefs.getBoolean("auto_connect", false)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications", true)
        
        val timeout = prefs.getInt("connection_timeout", 30)
        serverTimeoutSeekBar.progress = timeout
        serverTimeoutLabel.text = "$timeout seconds"
        
        val historyLimit = prefs.getInt("chat_history_limit", 1000)
        val historySpinnerPosition = when (historyLimit) {
            100 -> 0
            500 -> 1
            1000 -> 2
            2000 -> 3
            -1 -> 4
            else -> 2
        }
        chatHistoryLimitSpinner.setSelection(historySpinnerPosition)
    }
    
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
                        // Clear chat history
                        val chatPrefs = getSharedPreferences("chat_history", MODE_PRIVATE)
                        chatPrefs.edit().clear().apply()
                        
                        // Clear settings
                        getSharedPreferences("app_settings", MODE_PRIVATE).edit().clear().apply()
                        
                        // Clear theme preferences
                        getSharedPreferences("modern_theme_prefs", MODE_PRIVATE).edit().clear().apply()
                        
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
        }
        ModernUIManager.styleTextView(this@SettingsActivity, messageText, ModernUIManager.TextType.BODY_LARGE)
        
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
}
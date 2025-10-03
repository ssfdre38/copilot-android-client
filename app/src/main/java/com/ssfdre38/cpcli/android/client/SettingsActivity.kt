package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.ssfdre38.cpcli.android.client.data.StorageManager
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: MaterialSwitch
    private lateinit var switchAutoUpdates: MaterialSwitch
    private lateinit var buttonServerManagement: MaterialButton
    private lateinit var buttonChatHistory: MaterialButton
    private lateinit var buttonHelp: MaterialButton
    private lateinit var buttonAbout: MaterialButton
    private lateinit var buttonCheckUpdates: MaterialButton
    private lateinit var buttonClearAllData: MaterialButton
    
    private lateinit var storageManager: StorageManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storageManager = StorageManager(this)
        
        initViews()
        setupListeners()
        loadSettings()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Settings"
    }

    private fun initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchAutoUpdates = findViewById(R.id.switchAutoUpdates)
        buttonServerManagement = findViewById(R.id.buttonServerManagement)
        buttonChatHistory = findViewById(R.id.buttonChatHistory)
        buttonHelp = findViewById(R.id.buttonHelp)
        buttonAbout = findViewById(R.id.buttonAbout)
        buttonCheckUpdates = findViewById(R.id.buttonCheckUpdates)
        buttonClearAllData = findViewById(R.id.buttonClearAllData)
    }
    
    private fun loadSettings() {
        val settings = storageManager.getAppSettings()
        switchDarkMode.isChecked = settings.isDarkMode
        switchAutoUpdates.isChecked = settings.autoCheckUpdates
    }

    private fun setupListeners() {
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            storageManager.setDarkMode(isChecked)
            applyTheme(isChecked)
            Toast.makeText(this, if (isChecked) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
        }
        
        switchAutoUpdates.setOnCheckedChangeListener { _, isChecked ->
            storageManager.setAutoUpdates(isChecked)
            Toast.makeText(this, if (isChecked) "Auto-updates enabled" else "Auto-updates disabled", Toast.LENGTH_SHORT).show()
        }

        buttonServerManagement.setOnClickListener {
            val intent = Intent(this, ServerManagementActivity::class.java)
            startActivity(intent)
        }
        
        buttonChatHistory.setOnClickListener {
            val intent = Intent(this, ChatHistoryActivity::class.java)
            startActivity(intent)
        }

        buttonHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        buttonAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        
        buttonCheckUpdates.setOnClickListener {
            checkForUpdates()
        }
        
        buttonClearAllData.setOnClickListener {
            showClearDataConfirmation()
        }
    }
    
    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    private fun checkForUpdates() {
        scope.launch {
            try {
                buttonCheckUpdates.text = "Checking..."
                buttonCheckUpdates.isEnabled = false
                
                // Simulate update check
                delay(2000)
                
                // Check GitHub releases API
                checkGitHubReleases()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Failed to check for updates: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                buttonCheckUpdates.text = "Check for Updates"
                buttonCheckUpdates.isEnabled = true
            }
        }
    }
    
    private fun checkGitHubReleases() {
        // For now, just show a message. In a real implementation, this would 
        // call the GitHub API to check for new releases
        val currentVersion = BuildConfig.VERSION_NAME
        Toast.makeText(this, "Current version: $currentVersion\nLatest version check coming soon!", Toast.LENGTH_LONG).show()
        
        // Update the last check timestamp
        storageManager.setLastUpdateCheck(System.currentTimeMillis())
    }
    
    private fun showClearDataConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will delete all chat history, server configurations, and settings. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearAllData() {
        try {
            // Clear all stored data
            storageManager.clearAllChatHistory()
            
            // Reset to default settings
            storageManager.saveAppSettings(com.ssfdre38.cpcli.android.client.data.AppSettings())
            
            // Clear servers except default
            val servers = storageManager.getAllServers()
            servers.forEach { server ->
                if (server.id != "default") {
                    storageManager.deleteServer(server.id)
                }
            }
            
            Toast.makeText(this, "All data cleared successfully", Toast.LENGTH_SHORT).show()
            
            // Restart activity to reflect changes
            recreate()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to clear data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
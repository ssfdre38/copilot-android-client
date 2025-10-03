package com.ssfdre38.cpcli.android.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.data.ImportExportManager
import com.ssfdre38.cpcli.android.client.data.ImportResult
import com.ssfdre38.cpcli.android.client.data.ValidationResult
import com.ssfdre38.cpcli.android.client.service.UpdateManager
import com.ssfdre38.cpcli.android.client.service.NotificationService
import com.ssfdre38.cpcli.android.client.ui.SearchActivity
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: MaterialSwitch
    private lateinit var switchAutoUpdates: MaterialSwitch
    private lateinit var switchNotifications: MaterialSwitch
    private lateinit var buttonServerManagement: MaterialButton
    private lateinit var buttonChatHistory: MaterialButton
    private lateinit var buttonSearchHistory: MaterialButton
    private lateinit var buttonImportData: MaterialButton
    private lateinit var buttonExportData: MaterialButton
    private lateinit var buttonHelp: MaterialButton
    private lateinit var buttonAbout: MaterialButton
    private lateinit var buttonCheckUpdates: MaterialButton
    private lateinit var buttonClearAllData: MaterialButton
    
    private lateinit var storageManager: StorageManager
    private lateinit var importExportManager: ImportExportManager
    private lateinit var updateManager: UpdateManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Activity result launchers
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportData(it) }
    }
    
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importData(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storageManager = StorageManager(this)
        importExportManager = ImportExportManager(this)
        updateManager = UpdateManager(this)
        
        initViews()
        setupListeners()
        loadSettings()
        setupUpdateManager()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Settings"
    }

    private fun initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchAutoUpdates = findViewById(R.id.switchAutoUpdates)
        switchNotifications = findViewById(R.id.switchNotifications)
        buttonServerManagement = findViewById(R.id.buttonServerManagement)
        buttonChatHistory = findViewById(R.id.buttonChatHistory)
        buttonSearchHistory = findViewById(R.id.buttonSearchHistory)
        buttonImportData = findViewById(R.id.buttonImportData)
        buttonExportData = findViewById(R.id.buttonExportData)
        buttonHelp = findViewById(R.id.buttonHelp)
        buttonAbout = findViewById(R.id.buttonAbout)
        buttonCheckUpdates = findViewById(R.id.buttonCheckUpdates)
        buttonClearAllData = findViewById(R.id.buttonClearAllData)
    }
    
    private fun loadSettings() {
        val settings = storageManager.getAppSettings()
        switchDarkMode.isChecked = settings.isDarkMode
        switchAutoUpdates.isChecked = settings.autoCheckUpdates
        switchNotifications.isChecked = getNotificationPreference()
    }
    
    private fun getNotificationPreference(): Boolean {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
    }
    
    private fun setNotificationPreference(enabled: Boolean) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    private fun setupListeners() {
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            storageManager.setDarkMode(isChecked)
            applyTheme(isChecked)
            Toast.makeText(this, if (isChecked) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
        }
        
        switchAutoUpdates.setOnCheckedChangeListener { _, isChecked ->
            storageManager.setAutoUpdates(isChecked)
            updateManager.setAutoUpdateEnabled(isChecked)
            Toast.makeText(this, if (isChecked) "Auto-updates enabled" else "Auto-updates disabled", Toast.LENGTH_SHORT).show()
        }
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            setNotificationPreference(isChecked)
            if (isChecked) {
                NotificationService.startService(this)
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                NotificationService.stopService(this)
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }

        buttonServerManagement.setOnClickListener {
            val intent = Intent(this, ServerManagementActivity::class.java)
            startActivity(intent)
        }
        
        buttonChatHistory.setOnClickListener {
            val intent = Intent(this, ChatHistoryActivity::class.java)
            startActivity(intent)
        }
        
        buttonSearchHistory.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        
        buttonImportData.setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "*/*"))
        }
        
        buttonExportData.setOnClickListener {
            val filename = importExportManager.generateExportFilename()
            exportLauncher.launch(filename)
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
                
                val updateAvailable = updateManager.checkForUpdates()
                updateManager.saveLastUpdateCheck()
                
                if (!updateAvailable) {
                    Toast.makeText(this@SettingsActivity, "You're running the latest version!", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Failed to check for updates: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                buttonCheckUpdates.text = "Check for Updates"
                buttonCheckUpdates.isEnabled = true
            }
        }
    }
    
    private fun setupUpdateManager() {
        updateManager.setUpdateListener(object : UpdateManager.UpdateListener {
            override fun onUpdateAvailable(releaseInfo: com.ssfdre38.cpcli.android.client.service.ReleaseInfo) {
                showUpdateDialog(releaseInfo)
            }
            
            override fun onUpdateNotAvailable() {
                // Already handled in checkForUpdates()
            }
            
            override fun onUpdateError(error: String) {
                Toast.makeText(this@SettingsActivity, "Update check failed: $error", Toast.LENGTH_LONG).show()
            }
            
            override fun onDownloadProgress(progress: Int) {
                runOnUiThread {
                    buttonCheckUpdates.text = "Downloading... $progress%"
                }
            }
            
            override fun onDownloadComplete(file: java.io.File) {
                runOnUiThread {
                    buttonCheckUpdates.text = "Check for Updates"
                    buttonCheckUpdates.isEnabled = true
                    
                    androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                        .setTitle("Update Downloaded")
                        .setMessage("The update has been downloaded. Do you want to install it now?")
                        .setPositiveButton("Install") { _, _ ->
                            updateManager.installUpdate(file)
                        }
                        .setNegativeButton("Later", null)
                        .show()
                }
            }
            
            override fun onDownloadError(error: String) {
                runOnUiThread {
                    buttonCheckUpdates.text = "Check for Updates"
                    buttonCheckUpdates.isEnabled = true
                    Toast.makeText(this@SettingsActivity, "Download failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    
    private fun showUpdateDialog(releaseInfo: com.ssfdre38.cpcli.android.client.service.ReleaseInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Available")
            .setMessage("Version ${releaseInfo.tag_name} is available!\n\n${releaseInfo.body}")
            .setPositiveButton("Download") { _, _ ->
                scope.launch {
                    buttonCheckUpdates.text = "Downloading..."
                    buttonCheckUpdates.isEnabled = false
                    updateManager.downloadUpdate(releaseInfo)
                }
            }
            .setNegativeButton("Later", null)
            .show()
    }
    
    private fun exportData(uri: Uri) {
        scope.launch {
            try {
                val success = importExportManager.exportToFile(uri, includeHistory = true)
                if (success) {
                    Toast.makeText(this@SettingsActivity, "Data exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "Export failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun importData(uri: Uri) {
        scope.launch {
            try {
                // First validate the file
                when (val validation = importExportManager.validateImportFile(uri)) {
                    is ValidationResult.Valid -> {
                        showImportConfirmationDialog(uri, validation)
                    }
                    is ValidationResult.Invalid -> {
                        Toast.makeText(this@SettingsActivity, "Invalid file: ${validation.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Import error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showImportConfirmationDialog(uri: Uri, validation: ValidationResult.Valid) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Data")
            .setMessage("Import ${validation.serverCount} servers and ${validation.historyCount} chat messages?\n\nExport date: ${validation.exportDate}\nApp version: ${validation.appVersion}")
            .setPositiveButton("Import") { _, _ ->
                performImport(uri)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performImport(uri: Uri) {
        scope.launch {
            try {
                when (val result = importExportManager.importFromFile(uri)) {
                    is ImportResult.Success -> {
                        val message = "Imported: ${result.serversImported} servers, ${result.historyImported} messages"
                        if (result.serversSkipped > 0) {
                            Toast.makeText(this@SettingsActivity, "$message\n${result.serversSkipped} servers skipped (already exist)", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ImportResult.Error -> {
                        Toast.makeText(this@SettingsActivity, "Import failed: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Import error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
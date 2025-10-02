package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.ssfdre38.cpcli.android.client.utils.StorageManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager
import com.ssfdre38.cpcli.android.client.utils.UpdateManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.settings)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        
        private lateinit var storageManager: StorageManager
        private lateinit var updateManager: UpdateManager
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            storageManager = StorageManager(requireContext())
            updateManager = UpdateManager(requireContext())
            
            setupPreferences()
        }
        
        private fun setupPreferences() {
            // Dark Mode
            findPreference<SwitchPreferenceCompat>("dark_mode")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val isDarkMode = newValue as Boolean
                    val themeMode = if (isDarkMode) ThemeManager.ThemeMode.DARK else ThemeManager.ThemeMode.LIGHT
                    ThemeManager.setThemeMode(requireContext(), themeMode)
                    true
                }
            }
            
            // Server Management
            findPreference<Preference>("manage_servers")?.apply {
                setOnPreferenceClickListener {
                    val intent = Intent(requireContext(), ServerManagementActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            
            // Help & Documentation
            findPreference<Preference>("help_docs")?.apply {
                setOnPreferenceClickListener {
                    val intent = Intent(requireContext(), HelpActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            
            // Check for Updates
            findPreference<Preference>("check_updates")?.apply {
                setOnPreferenceClickListener {
                    checkForUpdates()
                    true
                }
            }
            
            // Clear Chat History
            findPreference<Preference>("clear_chat_history")?.apply {
                setOnPreferenceClickListener {
                    clearChatHistory()
                    true
                }
            }
            
            // Auto Updates
            findPreference<SwitchPreferenceCompat>("auto_updates")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val autoUpdatesEnabled = newValue as Boolean
                    val currentSettings = storageManager.loadAppSettings()
                    storageManager.saveAppSettings(currentSettings.copy(autoUpdatesEnabled = autoUpdatesEnabled))
                    true
                }
            }
            
            // Chat History Enabled
            findPreference<SwitchPreferenceCompat>("chat_history_enabled")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val chatHistoryEnabled = newValue as Boolean
                    val currentSettings = storageManager.loadAppSettings()
                    storageManager.saveAppSettings(currentSettings.copy(chatHistoryEnabled = chatHistoryEnabled))
                    true
                }
            }
            
            // About
            findPreference<Preference>("about")?.apply {
                summary = "Version ${updateManager.getAppVersion()}"
                setOnPreferenceClickListener {
                    val intent = Intent(requireContext(), AboutActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
        }
        
        private fun checkForUpdates() {
            lifecycleScope.launch {
                val updateInfo = updateManager.checkForUpdates()
                if (updateInfo != null) {
                    showUpdateDialog(updateInfo)
                } else {
                    showNoUpdatesDialog()
                }
            }
        }
        
        private fun clearChatHistory() {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear Chat History")
                .setMessage("This will delete all chat history for all servers. This action cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    val historyKeys = storageManager.getAllChatHistoryKeys()
                    historyKeys.forEach { key ->
                        val serverId = key.removePrefix("chat_history_")
                        storageManager.clearChatHistory(serverId)
                    }
                    android.widget.Toast.makeText(requireContext(), "Chat history cleared", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        private fun showUpdateDialog(updateInfo: com.ssfdre38.cpcli.android.client.model.UpdateInfo) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Update Available")
                .setMessage("Version ${updateInfo.version} is available.\n\n${updateInfo.releaseNotes}")
                .setPositiveButton("Download") { _, _ ->
                    val intent = updateManager.getDownloadIntent(updateInfo.downloadUrl)
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .setNeutralButton("Skip Version") { _, _ ->
                    UpdateManager.setSkipVersion(requireContext(), updateInfo.version)
                }
                .show()
        }
        
        private fun showNoUpdatesDialog() {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("No Updates")
                .setMessage("You are running the latest version.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
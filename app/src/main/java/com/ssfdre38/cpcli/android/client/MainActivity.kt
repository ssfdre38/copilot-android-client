package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.ui.ServerManagementActivity
import com.ssfdre38.cpcli.android.client.ui.SettingsActivity
import com.ssfdre38.cpcli.android.client.ui.HelpActivity
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var startChatButton: Button
    private lateinit var manageServersButton: Button
    private lateinit var serverStatusText: TextView
    private lateinit var serverManager: ServerConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize server manager
            serverManager = ServerConfigManager(this)
            
            // Create layout programmatically to avoid XML layout issues
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 40, 40, 40)
                gravity = Gravity.CENTER
            }
            
            // App title
            val titleText = TextView(this).apply {
                text = "🤖 GitHub Copilot CLI"
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 30)
            }
            
            // Server status
            serverStatusText = TextView(this).apply {
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 30)
            }
            
            // Start Chat button
            startChatButton = Button(this).apply {
                text = "💬 Start Chat"
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 20)
                }
                setOnClickListener { startChat() }
            }
            
            // Now update server status after buttons are initialized
            updateServerStatus()
            
            // Manage Servers button
            manageServersButton = Button(this).apply {
                text = "🖥️ Manage Servers"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 20)
                }
                setOnClickListener { openServerManagement() }
            }
            
            // Settings button
            val settingsButton = Button(this).apply {
                text = "⚙️ Settings"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 20)
                }
                setOnClickListener { openSettings() }
            }
            
            // Help & Info button
            val helpButton = Button(this).apply {
                text = "📖 Help & Info"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { openHelp() }
            }

            mainLayout.addView(titleText)
            mainLayout.addView(serverStatusText)
            mainLayout.addView(startChatButton)
            mainLayout.addView(manageServersButton)
            mainLayout.addView(settingsButton)
            mainLayout.addView(helpButton)

            setContentView(mainLayout)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateServerStatus()
    }
    
    private fun updateServerStatus() {
        // Check if UI elements are initialized before accessing them
        if (!::startChatButton.isInitialized || !::serverStatusText.isInitialized) {
            return
        }
        
        val activeServer = serverManager.getActiveServer()
        if (activeServer != null) {
            serverStatusText.text = "🟢 Active Server: ${activeServer.name}\n${activeServer.getFullUrl()}"
            startChatButton.isEnabled = true
        } else {
            serverStatusText.text = "⚠️ No server selected\nPlease configure a server first"
            startChatButton.isEnabled = false
        }
    }
    
    private fun startChat() {
        val activeServer = serverManager.getActiveServer()
        if (activeServer != null) {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please select a server first", Toast.LENGTH_SHORT).show()
            openServerManagement()
        }
    }
    
    private fun openServerManagement() {
        val intent = Intent(this, ServerManagementActivity::class.java)
        startActivity(intent)
    }
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun openHelp() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }
}
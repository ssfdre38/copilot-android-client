package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.ui.ServerManagementActivity
import com.ssfdre38.cpcli.android.client.ui.SettingsActivity
import com.ssfdre38.cpcli.android.client.ui.HelpActivity
import com.ssfdre38.cpcli.android.client.ui.ModernUIManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var startChatButton: Button
    private lateinit var manageServersButton: Button
    private lateinit var serverStatusText: TextView
    private lateinit var serverManager: ServerConfigManager
    private lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply modern theme before calling super.onCreate
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize server manager
            serverManager = ServerConfigManager(this)
            
            // Create modern main layout
            createModernLayout()
            
            // Apply modern theme to all UI elements
            ThemeManager.applyThemeToView(this, mainLayout)
            
            // Set up window insets for edge-to-edge display
            setupWindowInsets()
            
            // Update server status after UI is created
            updateServerStatus()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createModernLayout() {
        // Create main container with modern styling
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.LARGE),
                ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.EXTRA_LARGE),
                ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.LARGE),
                ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.LARGE)
            )
        }
        
        // Apply window background
        ModernUIManager.setWindowBackground(this, mainLayout)
        
        // App title with modern typography
        val titleText = TextView(this).apply {
            text = "🤖 GitHub Copilot CLI"
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.LARGE))
            }
        }
        ModernUIManager.styleTextView(this, titleText, ModernUIManager.TextType.HEADLINE_MEDIUM)
        
        // Server status card
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.LARGE))
            }
        }
        ModernUIManager.styleContainer(this, statusCard)
        
        // Server status text
        serverStatusText = TextView(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        ModernUIManager.styleTextView(this, serverStatusText, ModernUIManager.TextType.BODY_MEDIUM)
        
        statusCard.addView(serverStatusText)
        
        // Button container
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Start Chat button (primary)
        startChatButton = Button(this).apply {
            text = "💬 Start Chat"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.MEDIUM))
            }
            setOnClickListener { startChat() }
        }
        ModernUIManager.styleButton(this, startChatButton, ModernUIManager.ButtonType.PRIMARY)
        
        // Manage Servers button (secondary)
        manageServersButton = Button(this).apply {
            text = "🖥️ Manage Servers"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.MEDIUM))
            }
            setOnClickListener { openServerManagement() }
        }
        ModernUIManager.styleButton(this, manageServersButton, ModernUIManager.ButtonType.SECONDARY)
        
        // Settings button (secondary)
        val settingsButton = Button(this).apply {
            text = "⚙️ Settings"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@MainActivity, ModernUIManager.Spacing.MEDIUM))
            }
            setOnClickListener { openSettings() }
        }
        ModernUIManager.styleButton(this, settingsButton, ModernUIManager.ButtonType.SECONDARY)
        
        // Help & Info button (text button)
        val helpButton = Button(this).apply {
            text = "📖 Help & Info"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { openHelp() }
        }
        ModernUIManager.styleButton(this, helpButton, ModernUIManager.ButtonType.TEXT)
        
        // Add all views to containers
        buttonContainer.addView(startChatButton)
        buttonContainer.addView(manageServersButton)
        buttonContainer.addView(settingsButton)
        buttonContainer.addView(helpButton)
        
        mainLayout.addView(titleText)
        mainLayout.addView(statusCard)
        mainLayout.addView(buttonContainer)
        
        setContentView(mainLayout)
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.LARGE),
                top = systemBars.top + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM),
                right = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.LARGE),
                bottom = systemBars.bottom + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM)
            )
            insets
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateServerStatus()
    }
    
    private fun updateServerStatus() {
        // Check if UI elements are initialized before accessing them
        if (!::startChatButton.isInitialized || !::serverStatusText.isInitialized || !::serverManager.isInitialized) {
            return
        }
        
        val activeServer = serverManager.getActiveServer()
        if (activeServer != null) {
            serverStatusText.text = "🟢 Active Server: ${activeServer.name}\n${activeServer.getFullUrl()}"
            startChatButton.isEnabled = true
            startChatButton.alpha = 1.0f
        } else {
            serverStatusText.text = "⚠️ No server selected\nPlease configure a server first"
            startChatButton.isEnabled = false
            startChatButton.alpha = 0.6f
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
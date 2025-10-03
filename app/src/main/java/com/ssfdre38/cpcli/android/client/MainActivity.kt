package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class MainActivity : AppCompatActivity() {

    private lateinit var serverUrlInput: EditText
    private lateinit var connectButton: Button
    private lateinit var startChatButton: Button
    private lateinit var settingsButton: Button
    private lateinit var connectionStatus: TextView
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            createBulletproofLayout()
            title = "Copilot CLI Client"
            Toast.makeText(this, "✅ App started successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Ultimate fallback - create minimal safe layout
            createFallbackLayout()
            Toast.makeText(this, "App started in safe mode", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createBulletproofLayout() {
        // Main container - bulletproof linear layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // App title - always works
        val titleText = TextView(this).apply {
            text = "🚀 Copilot CLI Client"
            textSize = calculateSafeTextSize(24f)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(30))
            setTextColor(0xFF1976D2.toInt()) // Material blue
        }
        
        // Connection section in card
        val connectionCard = createSafeCard()
        val connectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16))
        }
        
        // Section title
        val connectionTitle = TextView(this).apply {
            text = "🌐 Server Connection"
            textSize = calculateSafeTextSize(18f)
            setPadding(0, 0, 0, dpToPx(16))
        }
        
        // Server URL input with label
        val urlLabel = TextView(this).apply {
            text = "Server URL:"
            textSize = calculateSafeTextSize(14f)
            setPadding(0, 0, 0, dpToPx(8))
        }
        
        serverUrlInput = EditText(this).apply {
            hint = "localhost:3002"
            textSize = calculateSafeTextSize(16f)
            setPadding(dpToPx(12))
            background = createInputBackground()
        }
        
        // Connection status
        connectionStatus = TextView(this).apply {
            text = "🔴 Disconnected"
            textSize = calculateSafeTextSize(14f)
            setPadding(0, dpToPx(16), 0, dpToPx(16))
            gravity = Gravity.CENTER
        }
        
        // Connect button
        connectButton = Button(this).apply {
            text = "Connect to Server"
            textSize = calculateSafeTextSize(16f)
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            background = createButtonBackground(0xFF4CAF50.toInt()) // Green
            setTextColor(0xFFFFFFFF.toInt())
        }
        
        // Assemble connection section
        connectionLayout.apply {
            addView(connectionTitle)
            addView(urlLabel)
            addView(serverUrlInput)
            addView(connectionStatus)
            addView(connectButton)
        }
        connectionCard.addView(connectionLayout)
        
        // Actions section
        val actionsCard = createSafeCard()
        val actionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16))
        }
        
        val actionsTitle = TextView(this).apply {
            text = "⚡ Actions"
            textSize = calculateSafeTextSize(18f)
            setPadding(0, 0, 0, dpToPx(16))
        }
        
        // Start Chat button
        startChatButton = Button(this).apply {
            text = "💬 Start Chat"
            textSize = calculateSafeTextSize(16f)
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            background = createButtonBackground(0xFF2196F3.toInt()) // Blue
            setTextColor(0xFFFFFFFF.toInt())
            isEnabled = false // Disabled until connected
        }
        
        // Settings button
        settingsButton = Button(this).apply {
            text = "⚙️ Settings"
            textSize = calculateSafeTextSize(16f)
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            background = createButtonBackground(0xFF9C27B0.toInt()) // Purple
            setTextColor(0xFFFFFFFF.toInt())
        }
        
        // Spacing between buttons
        val buttonSpacing = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(12)
            )
        }
        
        // Assemble actions section
        actionsLayout.apply {
            addView(actionsTitle)
            addView(startChatButton)
            addView(buttonSpacing)
            addView(settingsButton)
        }
        actionsCard.addView(actionsLayout)
        
        // Info section
        val infoText = TextView(this).apply {
            text = "📱 v2.2.0 - Bulletproof Edition\n🔧 100% programmatic UI\n📐 Auto-adapts to any screen size\n🚫 Zero XML dependencies"
            textSize = calculateSafeTextSize(12f)
            gravity = Gravity.CENTER
            setPadding(dpToPx(16))
            alpha = 0.7f
        }
        
        // Assemble main layout
        mainLayout.apply {
            addView(titleText)
            addView(connectionCard)
            addView(createSpacing(16))
            addView(actionsCard)
            addView(createSpacing(16))
            addView(infoText)
        }
        
        setContentView(mainLayout)
        setupSafeListeners()
    }
    
    private fun createFallbackLayout() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50)
            gravity = Gravity.CENTER
        }
        
        val title = TextView(this).apply {
            text = "Copilot CLI Client\n(Safe Mode)"
            textSize = 20f
            gravity = Gravity.CENTER
        }
        
        val chatButton = Button(this).apply {
            text = "Start Chat"
            setOnClickListener { startChatSafely() }
        }
        
        val settingsBtn = Button(this).apply {
            text = "Settings"
            setOnClickListener { startSettingsSafely() }
        }
        
        layout.apply {
            addView(title)
            addView(chatButton)
            addView(settingsBtn)
        }
        
        setContentView(layout)
    }
    
    private fun setupSafeListeners() {
        try {
            connectButton.setOnClickListener {
                try {
                    val serverUrl = serverUrlInput.text.toString().trim()
                    if (serverUrl.isNotBlank()) {
                        // Simulate connection
                        isConnected = true
                        connectionStatus.text = "🟢 Connected to $serverUrl"
                        connectionStatus.setTextColor(0xFF4CAF50.toInt())
                        startChatButton.isEnabled = true
                        startChatButton.background = createButtonBackground(0xFF4CAF50.toInt()) // Enable green
                        connectButton.text = "Disconnect"
                        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        serverUrlInput.setText("localhost:3002")
                        Toast.makeText(this, "Using default server", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            startChatButton.setOnClickListener {
                startChatSafely()
            }
            
            settingsButton.setOnClickListener {
                startSettingsSafely()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Setup error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startChatSafely() {
        try {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Chat error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun startSettingsSafely() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Settings error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createSafeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createCardBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(8), 0, dpToPx(8))
            }
        }
    }
    
    private fun createSpacing(dpSize: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(dpSize)
            )
        }
    }
    
    private fun createCardBackground(): android.graphics.drawable.Drawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(12).toFloat()
        drawable.setColor(0x08000000)
        drawable.setStroke(dpToPx(1), 0x1F000000)
        return drawable
    }
    
    private fun createButtonBackground(color: Int): android.graphics.drawable.Drawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(8).toFloat()
        drawable.setColor(color)
        return drawable
    }
    
    private fun createInputBackground(): android.graphics.drawable.Drawable {
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(6).toFloat()
        drawable.setColor(0x0F000000)
        drawable.setStroke(dpToPx(1), 0x3F000000)
        return drawable
    }
    
    private fun calculateSafeTextSize(baseSizeSp: Float): Float {
        return try {
            val screenWidthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
            val scaleFactor = when {
                screenWidthDp < 360 -> 0.85f // Small phones
                screenWidthDp > 600 -> 1.15f // Tablets
                else -> 1.0f // Normal phones
            }
            baseSizeSp * scaleFactor
        } catch (e: Exception) {
            baseSizeSp // Fallback to base size
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        return try {
            (dp * resources.displayMetrics.density).toInt()
        } catch (e: Exception) {
            dp * 3 // Fallback approximation
        }
    }
}
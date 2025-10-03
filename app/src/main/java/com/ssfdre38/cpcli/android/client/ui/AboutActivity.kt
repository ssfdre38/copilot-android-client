package com.ssfdre38.cpcli.android.client.ui

import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class AboutActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            setupUI()
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
            text = "ℹ️ About GitHub Copilot CLI"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        
        // App Icon/Logo Section
        val logoCard = createSectionCard("🤖 Application")
        val logoContent = """
        **GitHub Copilot CLI for Android**
        Version: 3.2.2 (Build 34)
        
        A mobile client for connecting to GitHub Copilot CLI servers, enabling AI-powered coding assistance on Android devices.
        
        **Features:**
        • Multi-server support
        • Secure WebSocket connections  
        • Dark/Light theme
        • Offline help documentation
        • Persistent chat history
        """.trimIndent()
        
        logoCard.addView(createContentText(logoContent))
        
        // Developer Section
        val developerCard = createSectionCard("👨‍💻 Developer")
        val developerContent = """
        **Developed by:** SSFDRE38
        **Contact:** GitHub @ssfdre38
        
        This application was created to provide mobile access to GitHub Copilot CLI functionality.
        """.trimIndent()
        
        developerCard.addView(createContentText(developerContent))
        
        // Legal Section
        val legalCard = createSectionCard("⚖️ Legal & Disclaimers")
        val legalContent = """
        **Disclaimer:**
        This application is not affiliated with, endorsed by, or sponsored by GitHub, Inc. or Microsoft Corporation.
        
        **Trademarks:**
        • GitHub® and Copilot® are registered trademarks of GitHub, Inc. (a subsidiary of Microsoft Corporation)
        • Microsoft® is a registered trademark of Microsoft Corporation
        • Android™ is a trademark of Google LLC
        
        **License:**
        This software is provided as-is without warranty. Users are responsible for compliance with GitHub's terms of service when using Copilot services.
        """.trimIndent()
        
        legalCard.addView(createContentText(legalContent))
        
        // Privacy Section
        val privacyCard = createSectionCard("🔒 Privacy & Security")
        val privacyContent = """
        **Data Handling:**
        • Chat messages are sent to your configured Copilot server
        • No data is collected or transmitted by this app itself
        • Chat history is stored locally on your device
        • Server configurations are stored locally
        • No analytics or tracking implemented
        
        **Security:**
        • Supports secure WebSocket connections (WSS)
        • Local data storage only
        • No cloud synchronization
        • User controls all server connections
        """.trimIndent()
        
        privacyCard.addView(createContentText(privacyContent))
        
        // Technical Section
        val techCard = createSectionCard("🔧 Technical Information")
        val techContent = """
        **Built With:**
        • Kotlin for Android
        • WebSocket client for real-time communication
        • Material Design components
        • SharedPreferences for local storage
        
        **Requirements:**
        • Android 7.0 (API 24) or higher
        • Internet connection for server communication
        • ~6.5MB storage space
        
        **Compatibility:**
        • Supports Android 7.0 to Android 15
        • Works on phones and tablets
        • Portrait and landscape orientations
        """.trimIndent()
        
        techCard.addView(createContentText(techContent))
        
        // Open Source Section
        val openSourceCard = createSectionCard("📂 Open Source")
        val openSourceContent = """
        **Third-Party Libraries:**
        • Kotlin Coroutines
        • AndroidX libraries
        • Gson for JSON parsing
        • OkHttp WebSocket client
        
        **Contributing:**
        This project welcomes contributions and feedback. Please visit the project repository for more information.
        """.trimIndent()
        
        openSourceCard.addView(createContentText(openSourceContent))
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Back to Settings"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 30, 0, 0)
            }
            setOnClickListener { finish() }
        }
        
        // Add all sections to main layout
        mainLayout.addView(titleText)
        mainLayout.addView(logoCard)
        mainLayout.addView(developerCard)
        mainLayout.addView(legalCard)
        mainLayout.addView(privacyCard)
        mainLayout.addView(techCard)
        mainLayout.addView(openSourceCard)
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
    
    private fun createContentText(content: String): TextView {
        return TextView(this).apply {
            text = content
            textSize = 14f
            setPadding(16, 8, 16, 16)
            setLineSpacing(4f, 1f)
        }
    }
}
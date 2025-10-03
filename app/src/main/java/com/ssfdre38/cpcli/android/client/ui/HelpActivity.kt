package com.ssfdre38.cpcli.android.client.ui

import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
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
            text = "📖 Help & Information"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        // Quick Start Section
        val quickStartCard = createSectionCard("🚀 Quick Start Guide")
        val quickStartContent = """
        1. **Add a Server**
           • Tap "Manage Servers" on the main screen
           • Tap "Add New Server"
           • Enter your Copilot CLI server details
           • Choose HTTP (ws://) or HTTPS (wss://)
           
        2. **Select Active Server**
           • In the server list, tap "Select" on your preferred server
           • The main screen will show your active server
           
        3. **Start Chatting**
           • Tap "Start Chat" to connect to your server
           • Type questions and get AI assistance
           • Use natural language to describe what you need
        """.trimIndent()
        
        quickStartCard.addView(createContentText(quickStartContent))
        
        // Server Configuration Section
        val serverConfigCard = createSectionCard("🖥️ Server Configuration")
        val serverConfigContent = """
        **Server Types:**
        • **HTTP (ws://)**: Standard WebSocket connection
        • **HTTPS (wss://)**: Secure WebSocket with SSL/TLS
        
        **Common Ports:**
        • 3002: Default HTTP port for Copilot CLI
        • 8443: Common HTTPS port for secure connections
        • 80/443: Standard web ports
        
        **Examples:**
        • ws://localhost:3002 (Local development)
        • wss://myserver.com:8443 (Secure remote)
        • ws://192.168.1.100:3002 (Local network)
        """.trimIndent()
        
        serverConfigCard.addView(createContentText(serverConfigContent))
        
        // Troubleshooting Section
        val troubleshootingCard = createSectionCard("🔧 Troubleshooting")
        val troubleshootingContent = """
        **Connection Issues:**
        • Check server URL and port are correct
        • Verify the server is running and accessible
        • Try HTTP (ws://) if HTTPS (wss://) fails
        • Check firewall settings on your network
        
        **Common Errors:**
        • "Connection refused": Server not running
        • "Network unreachable": Check network/WiFi
        • "SSL error": Try HTTP or check certificates
        • "Timeout": Increase timeout in Settings
        
        **Performance Tips:**
        • Use local servers when possible for speed
        • Clear chat history if app becomes slow
        • Enable notifications for connection status
        """.trimIndent()
        
        troubleshootingCard.addView(createContentText(troubleshootingContent))
        
        // Features Section
        val featuresCard = createSectionCard("✨ Features")
        val featuresContent = """
        **Multi-Server Support:**
        • Add unlimited Copilot CLI servers
        • Switch between development/production
        • Each server maintains separate chat history
        
        **Chat Features:**
        • Natural language questions
        • Code explanation and generation
        • Debugging assistance
        • Best practices suggestions
        
        **Settings:**
        • Dark/Light mode toggle
        • Connection timeout adjustment
        • Chat history limits
        • Auto-connect to last server
        """.trimIndent()
        
        featuresCard.addView(createContentText(featuresContent))
        
        // About Section
        val aboutCard = createSectionCard("ℹ️ About")
        val aboutContent = """
        **GitHub Copilot CLI Android Client**
        Version: 3.0.0
        
        This application provides mobile access to GitHub Copilot CLI servers, enabling AI-powered coding assistance on Android devices.
        
        **Disclaimer:**
        This application is not affiliated with, endorsed by, or sponsored by GitHub, Inc. or Microsoft Corporation.
        
        **Trademarks:**
        GitHub® and Copilot® are registered trademarks of GitHub, Inc. (a subsidiary of Microsoft Corporation).
        Microsoft® is a registered trademark of Microsoft Corporation.
        
        **Privacy:**
        • Chat data is sent to your configured Copilot server
        • No data is collected by this app
        • Chat history stored locally on your device
        • Server configurations stored locally
        """.trimIndent()
        
        aboutCard.addView(createContentText(aboutContent))
        
        // Commands Reference Section
        val commandsCard = createSectionCard("💻 Common Commands")
        val commandsContent = """
        **Code Generation:**
        • "Create a function that..."
        • "Write a class for..."
        • "Generate code to..."
        
        **Code Explanation:**
        • "Explain this code: [paste code]"
        • "What does this function do?"
        • "How does this algorithm work?"
        
        **Debugging:**
        • "Help me debug this error: [error message]"
        • "Why is this code not working?"
        • "Find the bug in this code: [code]"
        
        **Best Practices:**
        • "How can I improve this code?"
        • "What are best practices for..."
        • "Is there a better way to..."
        """.trimIndent()
        
        commandsCard.addView(createContentText(commandsContent))
        
        // FAQ Section
        val faqCard = createSectionCard("❓ Frequently Asked Questions")
        val faqContent = """
        **Q: Do I need a GitHub Copilot subscription?**
        A: This app connects to Copilot CLI servers. You need access to a configured Copilot CLI server.
        
        **Q: Can I use this offline?**
        A: No, this app requires a connection to a Copilot CLI server for AI responses.
        
        **Q: Is my code data secure?**
        A: Your code is sent to the Copilot server you configure. Use secure (wss://) connections when possible.
        
        **Q: Can I connect to multiple servers?**
        A: Yes! Add multiple servers and switch between them as needed.
        
        **Q: Why can't I connect to my server?**
        A: Check the server URL, ensure the server is running, and verify network connectivity.
        """.trimIndent()
        
        faqCard.addView(createContentText(faqContent))
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Back"
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
        mainLayout.addView(quickStartCard)
        mainLayout.addView(serverConfigCard)
        mainLayout.addView(troubleshootingCard)
        mainLayout.addView(featuresCard)
        mainLayout.addView(aboutCard)
        mainLayout.addView(commandsCard)
        mainLayout.addView(faqCard)
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
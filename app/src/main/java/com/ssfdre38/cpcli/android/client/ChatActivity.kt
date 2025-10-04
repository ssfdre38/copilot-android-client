package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.network.WebSocketListener
import com.ssfdre38.cpcli.android.client.network.WebSocketMessage
import com.ssfdre38.cpcli.android.client.ui.ChatAdapter
import com.ssfdre38.cpcli.android.client.ui.ModernUIManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager
import java.util.*

class ChatActivity : AppCompatActivity(), WebSocketListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var storageManager: StorageManager
    private lateinit var connectionStatus: TextView
    private lateinit var mainLayout: LinearLayout
    private var webSocketClient: CopilotWebSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply modern theme
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            createModernChatLayout()
            setupChat()
            setupWindowInsets()
            
            // Apply modern theme to all UI elements
            ThemeManager.applyThemeToView(this, mainLayout)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createModernChatLayout() {
        // Main container
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM),
                ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM)
            )
        }
        ModernUIManager.setWindowBackground(this, mainLayout)
        
        // Header container
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM))
            }
        }
        ModernUIManager.styleContainer(this, headerContainer)
        
        // Title
        val titleText = TextView(this).apply {
            text = "🤖 GitHub Copilot CLI"
            gravity = Gravity.CENTER
        }
        ModernUIManager.styleTextView(this, titleText, ModernUIManager.TextType.TITLE_LARGE)
        
        // Connection status
        connectionStatus = TextView(this).apply {
            text = "🔴 Connecting to server..."
            gravity = Gravity.CENTER
            setPadding(0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.SMALL), 0, 0)
        }
        ModernUIManager.styleTextView(this, connectionStatus, ModernUIManager.TextType.BODY_MEDIUM)
        
        headerContainer.addView(titleText)
        headerContainer.addView(connectionStatus)
        
        // Chat messages container (RecyclerView)
        val chatContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f // Take remaining space
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.MEDIUM))
            }
        }
        ModernUIManager.styleContainer(this, chatContainer)
        
        recyclerView = RecyclerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            clipToPadding = false
        }
        
        chatContainer.addView(recyclerView)
        
        // Input container
        val inputContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        ModernUIManager.styleContainer(this, inputContainer)
        
        // Message input area
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.SMALL))
            }
        }
        
        messageInput = EditText(this).apply {
            hint = "Type your message..."
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                setMargins(0, 0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.SMALL), 0)
            }
            maxLines = 4
        }
        ModernUIManager.styleEditText(this, messageInput)
        
        sendButton = Button(this).apply {
            text = "Send"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { sendMessage() }
        }
        ModernUIManager.styleButton(this, sendButton, ModernUIManager.ButtonType.PRIMARY)
        
        inputLayout.addView(messageInput)
        inputLayout.addView(sendButton)
        
        // Action buttons
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val ctrlCButton = Button(this).apply {
            text = "Ctrl+C"
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, ModernUIManager.dpToPx(this@ChatActivity, ModernUIManager.Spacing.SMALL), 0)
            }
            setOnClickListener { sendCtrlC() }
        }
        ModernUIManager.styleButton(this, ctrlCButton, ModernUIManager.ButtonType.SECONDARY)
        
        val backButton = Button(this).apply {
            text = "← Back"
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setOnClickListener { finish() }
        }
        ModernUIManager.styleButton(this, backButton, ModernUIManager.ButtonType.TEXT)
        
        buttonLayout.addView(ctrlCButton)
        buttonLayout.addView(backButton)
        
        // Add input components to container
        inputContainer.addView(inputLayout)
        inputContainer.addView(buttonLayout)
        
        // Assemble main layout
        mainLayout.addView(headerContainer)
        mainLayout.addView(chatContainer)
        mainLayout.addView(inputContainer)
        
        setContentView(mainLayout)
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            view.updatePadding(
                left = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM),
                top = systemBars.top + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.SMALL),
                right = ModernUIManager.dpToPx(this, ModernUIManager.Spacing.MEDIUM),
                bottom = maxOf(systemBars.bottom, ime.bottom) + ModernUIManager.dpToPx(this, ModernUIManager.Spacing.SMALL)
            )
            insets
        }
    }
    
    private fun setupChat() {
        try {
            setupRecyclerView()
            setupListeners()
            connectToServer()
            
            Toast.makeText(this, "🎉 ChatActivity with real Copilot integration!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        try {
            storageManager = StorageManager(this)
            chatAdapter = ChatAdapter()
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = chatAdapter
            
            // Add a welcome message
            val welcomeMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "🤖 Welcome to Real GitHub Copilot CLI!\n\nConnecting to server...",
                isFromUser = false,
                timestamp = System.currentTimeMillis(),
                serverId = "system"
            )
            chatAdapter.addMessage(welcomeMessage)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Setup error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupListeners() {
        sendButton.setOnClickListener { sendMessage() }
    }
    
    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isNotBlank()) {
            // Add user message to chat
            val chatMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                serverId = "user"
            )
            
            chatAdapter.addMessage(chatMessage)
            messageInput.text.clear()
            
            // Scroll to bottom
            recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            
            // Send to server via WebSocket
            if (webSocketClient?.isConnected() == true) {
                webSocketClient?.sendMessage(message)
                Toast.makeText(this, "Message sent to Copilot!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendCtrlC() {
        if (webSocketClient?.isConnected() == true) {
            webSocketClient?.sendMessage("\u0003") // Ctrl+C character
            Toast.makeText(this, "Ctrl+C sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun connectToServer() {
        try {
            // Show loading state
            connectionStatus?.text = "🔄 Connecting..."
            
            // Get the active server from user configuration
            val serverManager = ServerConfigManager(this)
            val activeServer = serverManager.getActiveServer()
            
            if (activeServer != null) {
                val serverUrl = activeServer.getFullUrl()
                
                // Validate server URL format
                if (!isValidWebSocketUrl(serverUrl)) {
                    connectionStatus?.text = "❌ Invalid server URL format"
                    showErrorDialog("Connection Error", "Invalid server URL: $serverUrl")
                    return
                }
                
                webSocketClient = CopilotWebSocketClient(serverUrl, this)
                
                // Show connecting message with timeout
                Toast.makeText(this, "🔄 Connecting to ${activeServer.name}...", Toast.LENGTH_SHORT).show()
                
                // Set connection timeout
                val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
                val timeoutRunnable = Runnable {
                    if (connectionStatus?.text?.contains("Connecting") == true) {
                        connectionStatus?.text = "⏰ Connection timeout"
                        showErrorDialog("Connection Timeout", 
                            "Failed to connect to ${activeServer.name} within 30 seconds.\n\n" +
                            "Please check:\n" +
                            "• Server is running and accessible\n" +
                            "• URL and port are correct\n" +
                            "• Network connection is stable")
                    }
                }
                timeoutHandler.postDelayed(timeoutRunnable, 30000) // 30 second timeout
                
                webSocketClient?.connect()
                
                // Update last connected time
                serverManager.updateLastConnected(activeServer.id)
                
            } else {
                connectionStatus?.text = "⚠️ No server configured"
                showErrorDialog("No Server", "No server configured. Please go to Settings → Manage Servers to add a server.")
                finish()
            }
        } catch (e: Exception) {
            connectionStatus?.text = "❌ Connection failed"
            showErrorDialog("Connection Error", "Failed to connect: ${e.message}")
        }
    }
    
    private fun isValidWebSocketUrl(url: String): Boolean {
        return url.startsWith("ws://") || url.startsWith("wss://")
    }
    
    private fun showErrorDialog(title: String, message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("❌ $title")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNegativeButton("Retry") { _, _ ->
                connectToServer()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    // WebSocketListener implementation
    override fun onConnected() {
        connectionStatus?.text = "🟢 Connected to Copilot server"
        val connectMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = "✅ Connected to GitHub Copilot CLI server!\n\nYou can now chat with real AI assistance.",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            serverId = "system"
        )
        chatAdapter.addMessage(connectMessage)
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }
    
    override fun onDisconnected() {
        connectionStatus?.text = "🔴 Disconnected from server"
        Toast.makeText(this, "Disconnected from server", Toast.LENGTH_SHORT).show()
    }
    
    override fun onMessageReceived(message: WebSocketMessage) {
        val chatMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message.message ?: "Empty response",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            serverId = message.sessionId ?: "copilot"
        )
        chatAdapter.addMessage(chatMessage)
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }
    
    override fun onError(error: String) {
        connectionStatus?.text = "🔴 Connection error"
        val errorMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = "❌ Error: $error",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            serverId = "error"
        )
        chatAdapter.addMessage(errorMessage)
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.cleanup()
    }
}
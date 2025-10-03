package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.ui.ChatAdapter
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.network.WebSocketListener
import com.ssfdre38.cpcli.android.client.network.WebSocketMessage
import java.util.*

class ChatActivity : AppCompatActivity(), WebSocketListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var storageManager: StorageManager
    private var webSocketClient: CopilotWebSocketClient? = null
    private var connectionStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Create layout programmatically to avoid complex XML layout issues
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
            }
            
            // Title and connection status
            val titleText = TextView(this).apply {
                text = "🤖 Real GitHub Copilot CLI"
                textSize = 20f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 10)
            }
            
            connectionStatus = TextView(this).apply {
                text = "🔴 Connecting to server..."
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 20)
            }
            
            // RecyclerView for messages
            recyclerView = RecyclerView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f // weight to take remaining space
                )
            }
            
            // Input area
            val inputLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 20, 0, 0)
            }
            
            messageInput = EditText(this).apply {
                hint = "Type your message..."
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            
            sendButton = Button(this).apply {
                text = "Send"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Action buttons
            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 0)
            }
            
            val ctrlCButton = Button(this).apply {
                text = "Ctrl+C"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            
            val backButton = Button(this).apply {
                text = "← Back"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { finish() }
            }
            
            // Assembly layout
            inputLayout.addView(messageInput)
            inputLayout.addView(sendButton)
            
            buttonLayout.addView(ctrlCButton)
            buttonLayout.addView(backButton)
            
            mainLayout.addView(titleText)
            mainLayout.addView(connectionStatus)
            mainLayout.addView(recyclerView)
            mainLayout.addView(inputLayout)
            mainLayout.addView(buttonLayout)
            
            setContentView(mainLayout)
            
            // Initialize functionality
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
        sendButton.setOnClickListener {
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
    }
    
    private fun connectToServer() {
        try {
            // Default to local server, but in production you'd get this from settings
            val serverUrl = "ws://54.37.254.74:3002"
            webSocketClient = CopilotWebSocketClient(serverUrl, this)
            webSocketClient?.connect()
        } catch (e: Exception) {
            Toast.makeText(this, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
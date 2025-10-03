package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.network.WebSocketListener
import com.ssfdre38.cpcli.android.client.network.WebSocketMessage
import com.ssfdre38.cpcli.android.client.ui.ChatAdapter
import java.util.*

class ChatActivity : AppCompatActivity(), WebSocketListener {

    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: TextInputEditText
    private lateinit var buttonSendMessage: MaterialButton
    
    // Keyboard buttons
    private lateinit var buttonCtrlC: Button
    private lateinit var buttonCtrlV: Button
    private lateinit var buttonTab: Button
    private lateinit var buttonEnter: Button
    private lateinit var buttonEsc: Button
    private lateinit var buttonArrowUp: Button
    private lateinit var buttonArrowDown: Button
    private lateinit var buttonBackspace: Button
    
    // Optional tablet buttons
    private var buttonClear: Button? = null
    private var buttonHistory: Button? = null

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var storageManager: StorageManager
    private var webSocketClient: CopilotWebSocketClient? = null
    private var currentSessionId: String = UUID.randomUUID().toString()
    private var currentServerId: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("ChatActivity", "Starting ChatActivity onCreate")
            setContentView(R.layout.activity_chat)

            // Initialize storage manager first
            storageManager = StorageManager(this)
            android.util.Log.d("ChatActivity", "Storage manager initialized")
            
            // Initialize UI components
            initViews()
            android.util.Log.d("ChatActivity", "Views initialized")
            
            // Setup RecyclerView 
            setupRecyclerView()
            android.util.Log.d("ChatActivity", "RecyclerView setup complete")
            
            // Setup button listeners
            setupListeners()
            android.util.Log.d("ChatActivity", "Listeners setup complete")
            
            // Load chat history safely (non-critical)
            try {
                loadChatHistory()
                android.util.Log.d("ChatActivity", "Chat history loaded successfully")
            } catch (e: Exception) {
                android.util.Log.w("ChatActivity", "Failed to load chat history - continuing without it", e)
                // Show empty state instead of crashing
                val emptyList = emptyList<ChatMessage>()
                chatAdapter.setMessages(emptyList)
            }
            
            // Connect to server if available (non-critical)
            try {
                connectToServer()
                android.util.Log.d("ChatActivity", "Server connection attempted")
            } catch (e: Exception) {
                android.util.Log.w("ChatActivity", "Failed to connect to server - user can try again later", e)
                runOnUiThread {
                    Toast.makeText(this, "Could not connect to server. Check settings.", Toast.LENGTH_SHORT).show()
                }
            }
            
            android.util.Log.d("ChatActivity", "ChatActivity onCreate completed successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Critical error during onCreate", e)
            // Show user-friendly error message
            try {
                runOnUiThread {
                    Toast.makeText(this, "Error starting chat. Please restart the app.", Toast.LENGTH_LONG).show()
                }
            } catch (toastError: Exception) {
                android.util.Log.e("ChatActivity", "Could not even show error toast", toastError)
            }
            
            // Don't crash the app, just close this activity gracefully
            finish()
        }
    }

    private fun initViews() {
        try {
            // Core UI elements - these must exist
            recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
            editTextMessage = findViewById(R.id.editTextMessage)
            buttonSendMessage = findViewById(R.id.buttonSendMessage)
            
            // Keyboard buttons - check each one safely
            buttonCtrlC = findViewById(R.id.buttonCtrlC)
            buttonCtrlV = findViewById(R.id.buttonCtrlV)
            buttonTab = findViewById(R.id.buttonTab)
            buttonEnter = findViewById(R.id.buttonEnter)
            buttonEsc = findViewById(R.id.buttonEsc)
            buttonArrowUp = findViewById(R.id.buttonArrowUp)
            buttonArrowDown = findViewById(R.id.buttonArrowDown)
            buttonBackspace = findViewById(R.id.buttonBackspace)
            
            // Optional tablet buttons - safely handle missing elements
            buttonClear = try {
                findViewById<Button>(R.id.buttonClear)
            } catch (e: Exception) {
                android.util.Log.d("ChatActivity", "buttonClear not found in layout - tablet feature not available")
                null
            }
            
            buttonHistory = try {
                findViewById<Button>(R.id.buttonHistory)
            } catch (e: Exception) {
                android.util.Log.d("ChatActivity", "buttonHistory not found in layout - tablet feature not available")
                null
            }
            
            android.util.Log.d("ChatActivity", "Views initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Critical error initializing views", e)
            throw RuntimeException("Failed to initialize core views: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            chatAdapter = ChatAdapter()
            recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
                stackFromEnd = true
            }
            recyclerViewMessages.adapter = chatAdapter
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error setting up RecyclerView", e)
            throw RuntimeException("Failed to setup RecyclerView: ${e.message}", e)
        }
    }
    
    private fun loadChatHistory() {
        try {
            val history = storageManager.getChatHistory(currentServerId)
            chatAdapter.setMessages(history)
            if (history.isNotEmpty()) {
                recyclerViewMessages.scrollToPosition(history.size - 1)
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error loading chat history", e)
            // Don't show toast for this as it's not critical
        }
    }
    
    private fun connectToServer() {
        try {
            val defaultServer = storageManager.getDefaultServer()
            if (defaultServer != null) {
                currentServerId = defaultServer.id
                val serverUrl = "ws://${defaultServer.url}:${defaultServer.port}"
                webSocketClient = CopilotWebSocketClient(serverUrl, this)
                webSocketClient?.connect()
            } else {
                Toast.makeText(this, "No server configured. Please set up a server in settings.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error connecting to server", e)
            Toast.makeText(this, "Failed to connect to server: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        buttonSendMessage.setOnClickListener {
            sendMessage()
        }

        // Keyboard button listeners
        buttonCtrlC.setOnClickListener {
            sendCommand("^C")
            Toast.makeText(this, "Sent Ctrl+C", Toast.LENGTH_SHORT).show()
        }

        buttonCtrlV.setOnClickListener {
            sendCommand("^V")
            Toast.makeText(this, "Sent Ctrl+V", Toast.LENGTH_SHORT).show()
        }

        buttonTab.setOnClickListener {
            sendCommand("\\t")
            Toast.makeText(this, "Sent Tab", Toast.LENGTH_SHORT).show()
        }

        buttonEnter.setOnClickListener {
            sendCommand("\\n")
            Toast.makeText(this, "Sent Enter", Toast.LENGTH_SHORT).show()
        }

        buttonEsc.setOnClickListener {
            sendCommand("\\e")
            Toast.makeText(this, "Sent Escape", Toast.LENGTH_SHORT).show()
        }

        buttonArrowUp.setOnClickListener {
            sendCommand("\\u001b[A")
            Toast.makeText(this, "Sent Arrow Up", Toast.LENGTH_SHORT).show()
        }

        buttonArrowDown.setOnClickListener {
            sendCommand("\\u001b[B")
            Toast.makeText(this, "Sent Arrow Down", Toast.LENGTH_SHORT).show()
        }

        buttonBackspace.setOnClickListener {
            sendCommand("\\b")
            Toast.makeText(this, "Sent Backspace", Toast.LENGTH_SHORT).show()
        }
        
        // Optional tablet button listeners
        buttonClear?.setOnClickListener {
            editTextMessage.text?.clear()
            Toast.makeText(this, "Input cleared", Toast.LENGTH_SHORT).show()
        }
        
        buttonHistory?.setOnClickListener {
            // Open chat history activity
            val intent = android.content.Intent(this, ChatHistoryActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun sendMessage() {
        try {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotBlank()) {
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = message,
                    isFromUser = true,
                    timestamp = System.currentTimeMillis(),
                    serverId = currentServerId
                )
                
                // Add to UI safely
                if (::chatAdapter.isInitialized) {
                    chatAdapter.addMessage(chatMessage)
                    scrollToBottom()
                }
                
                // Save to storage safely
                try {
                    storageManager.saveChatMessage(chatMessage)
                } catch (e: Exception) {
                    android.util.Log.e("ChatActivity", "Error saving user message", e)
                }
                
                // Send to server safely
                try {
                    webSocketClient?.sendMessage(message, currentSessionId)
                } catch (e: Exception) {
                    android.util.Log.e("ChatActivity", "Error sending message to server", e)
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
                
                // Clear input safely
                try {
                    editTextMessage.text?.clear()
                } catch (e: Exception) {
                    android.util.Log.e("ChatActivity", "Error clearing input text", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in sendMessage", e)
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendCommand(command: String) {
        try {
            webSocketClient?.sendCommand(command, currentSessionId)
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error sending command: $command", e)
        }
    }
    
    private fun scrollToBottom() {
        try {
            if (::chatAdapter.isInitialized && ::recyclerViewMessages.isInitialized) {
                val itemCount = chatAdapter.itemCount
                if (itemCount > 0) {
                    recyclerViewMessages.scrollToPosition(itemCount - 1)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error scrolling to bottom", e)
        }
    }
    
    // WebSocket listener methods
    override fun onConnected() {
        try {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    try {
                        Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("ChatActivity", "Error showing connected toast", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onConnected", e)
        }
    }
    
    override fun onDisconnected() {
        try {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    try {
                        Toast.makeText(this, "Disconnected from server", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("ChatActivity", "Error showing disconnected toast", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onDisconnected", e)
        }
    }
    
    override fun onMessageReceived(message: WebSocketMessage) {
        try {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    try {
                        when (message.type) {
                            "response", "welcome" -> {
                                val chatMessage = ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    content = message.message ?: "Empty response",
                                    isFromUser = false,
                                    timestamp = System.currentTimeMillis(),
                                    serverId = currentServerId
                                )
                                
                                // Add to UI safely
                                if (::chatAdapter.isInitialized) {
                                    chatAdapter.addMessage(chatMessage)
                                    scrollToBottom()
                                }
                                
                                // Save to storage safely
                                try {
                                    storageManager.saveChatMessage(chatMessage)
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatActivity", "Error saving message", e)
                                }
                            }
                            "error" -> {
                                Toast.makeText(this, "Server error: ${message.error}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatActivity", "Error processing message in UI thread", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onMessageReceived", e)
        }
    }
    
    override fun onError(error: String) {
        try {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    try {
                        Toast.makeText(this, "Connection error: $error", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        android.util.Log.e("ChatActivity", "Error showing error toast", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onError", e)
        }
    }
    
    override fun onDestroy() {
        try {
            android.util.Log.d("ChatActivity", "onDestroy called")
            webSocketClient?.cleanup()
            webSocketClient = null
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }
}
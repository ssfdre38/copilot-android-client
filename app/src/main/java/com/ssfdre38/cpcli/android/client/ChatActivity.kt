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
            setContentView(R.layout.activity_chat)

            storageManager = StorageManager(this)
            
            initViews()
            setupRecyclerView()
            setupListeners()
            
            // Load chat history
            loadChatHistory()
            
            // Connect to server if available
            connectToServer()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Error initializing chat: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSendMessage = findViewById(R.id.buttonSendMessage)
        
        // Keyboard buttons
        buttonCtrlC = findViewById(R.id.buttonCtrlC)
        buttonCtrlV = findViewById(R.id.buttonCtrlV)
        buttonTab = findViewById(R.id.buttonTab)
        buttonEnter = findViewById(R.id.buttonEnter)
        buttonEsc = findViewById(R.id.buttonEsc)
        buttonArrowUp = findViewById(R.id.buttonArrowUp)
        buttonArrowDown = findViewById(R.id.buttonArrowDown)
        buttonBackspace = findViewById(R.id.buttonBackspace)
        
        // Optional tablet buttons - safely handle missing elements
        try {
            buttonClear = findViewById(R.id.buttonClear)
        } catch (e: Exception) {
            buttonClear = null
        }
        
        try {
            buttonHistory = findViewById(R.id.buttonHistory)
        } catch (e: Exception) {
            buttonHistory = null
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.adapter = chatAdapter
    }
    
    private fun loadChatHistory() {
        val history = storageManager.getChatHistory(currentServerId)
        chatAdapter.setMessages(history)
        if (history.isNotEmpty()) {
            recyclerViewMessages.scrollToPosition(history.size - 1)
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
        val message = editTextMessage.text.toString().trim()
        if (message.isNotBlank()) {
            val chatMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                serverId = currentServerId
            )
            
            // Add to UI
            chatAdapter.addMessage(chatMessage)
            scrollToBottom()
            
            // Save to storage
            storageManager.saveChatMessage(chatMessage)
            
            // Send to server
            webSocketClient?.sendMessage(message, currentSessionId)
            
            // Clear input
            editTextMessage.text?.clear()
        }
    }
    
    private fun sendCommand(command: String) {
        webSocketClient?.sendCommand(command, currentSessionId)
    }
    
    private fun scrollToBottom() {
        recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }
    
    // WebSocket listener methods
    override fun onConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            Toast.makeText(this, "Disconnected from server", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onMessageReceived(message: WebSocketMessage) {
        runOnUiThread {
            when (message.type) {
                "response", "welcome" -> {
                    val chatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = message.message ?: "Empty response",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis(),
                        serverId = currentServerId
                    )
                    
                    // Add to UI
                    chatAdapter.addMessage(chatMessage)
                    scrollToBottom()
                    
                    // Save to storage
                    storageManager.saveChatMessage(chatMessage)
                }
                "error" -> {
                    Toast.makeText(this, "Server error: ${message.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection error: $error", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.cleanup()
    }
}
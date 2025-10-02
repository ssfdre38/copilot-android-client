package com.ssfdre38.cpcli.android.client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssfdre38.cpcli.android.client.databinding.ActivityChatBinding
import com.ssfdre38.cpcli.android.client.model.ChatMessage
import com.ssfdre38.cpcli.android.client.model.MessageSender
import com.ssfdre38.cpcli.android.client.model.QuickAction
import com.ssfdre38.cpcli.android.client.model.QuickActionTemplates
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.ui.ChatAdapter
import com.ssfdre38.cpcli.android.client.ui.QuickActionAdapter
import com.ssfdre38.cpcli.android.client.utils.StorageManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager
import kotlinx.coroutines.launch
import java.util.*

class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var quickActionAdapter: QuickActionAdapter
    private lateinit var storageManager: StorageManager
    private val chatMessages = mutableListOf<ChatMessage>()
    private var sessionId: String = UUID.randomUUID().toString()
    private var isQuickActionsVisible = false
    private var serverId: String? = null
    private var serverName: String? = null
    
    companion object {
        var webSocketClient: CopilotWebSocketClient? = null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = StorageManager(this)
        
        // Get server info from intent
        serverId = intent.getStringExtra("server_id")
        serverName = intent.getStringExtra("server_name")
        
        setupToolbar()
        setupRecyclerView()
        setupQuickActions()
        setupMessageInput()
        setupKeystrokeButtons()
        loadChatHistory()
        observeMessages()
        
        if (webSocketClient?.isConnected() != true) {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Send welcome message
        addSystemMessage("Connected to Copilot CLI. How can I help you today?")
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chat -> {
                clearChat()
                true
            }
            R.id.action_copy_all -> {
                copyAllMessages()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }
    
    private fun setupQuickActions() {
        quickActionAdapter = QuickActionAdapter { action ->
            useQuickAction(action)
        }
        
        binding.recyclerViewQuickActions.apply {
            adapter = quickActionAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        
        quickActionAdapter.submitList(QuickActionTemplates.COMMON_ACTIONS)
    }
    
    private fun setupMessageInput() {
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }
        
        binding.editTextMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
        
        binding.buttonQuickActions.setOnClickListener {
            toggleQuickActions()
        }
    }
    
    private fun setupKeystrokeButtons() {
        binding.apply {
            buttonCtrlC.setOnClickListener {
                copySelectedText()
            }
            
            buttonCtrlV.setOnClickListener {
                pasteFromClipboard()
            }
            
            buttonTab.setOnClickListener {
                insertText("    ") // 4 spaces for tab
            }
            
            buttonEnter.setOnClickListener {
                insertText("\n")
            }
            
            buttonEsc.setOnClickListener {
                editTextMessage.text?.clear()
                hideQuickActions()
            }
            
            buttonArrowUp.setOnClickListener {
                // Get previous message from history (implement if needed)
                Toast.makeText(this@ChatActivity, "Previous command", Toast.LENGTH_SHORT).show()
            }
            
            buttonArrowDown.setOnClickListener {
                // Get next message from history (implement if needed)  
                Toast.makeText(this@ChatActivity, "Next command", Toast.LENGTH_SHORT).show()
            }
            
            buttonClear.setOnClickListener {
                editTextMessage.text?.clear()
            }
        }
    }
    
    private fun toggleQuickActions() {
        isQuickActionsVisible = !isQuickActionsVisible
        binding.quickActionsContainer.visibility = if (isQuickActionsVisible) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
        
        binding.buttonQuickActions.text = if (isQuickActionsVisible) "⚡" else "⚡"
    }
    
    private fun hideQuickActions() {
        isQuickActionsVisible = false
        binding.quickActionsContainer.visibility = android.view.View.GONE
        binding.buttonQuickActions.text = "⚡"
    }
    
    private fun useQuickAction(action: QuickAction) {
        val currentText = binding.editTextMessage.text.toString()
        val newText = if (currentText.isEmpty()) {
            action.template + " "
        } else {
            "$currentText\n${action.template} "
        }
        binding.editTextMessage.setText(newText)
        binding.editTextMessage.setSelection(newText.length)
        hideQuickActions()
    }
    
    private fun copySelectedText() {
        val text = binding.editTextMessage.text.toString()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copilot Message", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text.toString()
            insertText(text)
        }
    }
    
    private fun insertText(text: String) {
        val currentText = binding.editTextMessage.text.toString()
        val cursorPosition = binding.editTextMessage.selectionStart
        val newText = currentText.substring(0, cursorPosition) + text + currentText.substring(cursorPosition)
        binding.editTextMessage.setText(newText)
        binding.editTextMessage.setSelection(cursorPosition + text.length)
    }
    
    private fun clearChat() {
        AlertDialog.Builder(this)
            .setTitle("Clear Chat")
            .setMessage("Are you sure you want to clear all messages?")
            .setPositiveButton("Clear") { _, _ ->
                chatMessages.clear()
                chatAdapter.submitList(emptyList())
                addSystemMessage("Chat cleared. How can I help you?")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun copyAllMessages() {
        val allText = chatMessages.joinToString("\n\n") { message ->
            "${message.sender}: ${message.content}"
        }
        
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Chat History", allText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "All messages copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendMessage() {
        val messageText = binding.editTextMessage.text.toString().trim()
        if (messageText.isEmpty()) return
        
        // Add user message to chat
        val userMessage = ChatMessage(
            content = messageText,
            sender = MessageSender.USER
        )
        addMessageToChat(userMessage)
        
        // Clear input
        binding.editTextMessage.text?.clear()
        hideQuickActions()
        
        // Show typing indicator
        val typingMessage = ChatMessage(
            content = "",
            sender = MessageSender.COPILOT,
            isTyping = true
        )
        addMessageToChat(typingMessage)
        
        // Send to server
        webSocketClient?.sendMessage(messageText, sessionId)
    }
    
    private fun observeMessages() {
        lifecycleScope.launch {
            webSocketClient?.messages?.collect { response ->
                response?.let {
                    runOnUiThread {
                        handleCopilotResponse(it)
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            webSocketClient?.connectionState?.collect { state ->
                runOnUiThread {
                    when (state) {
                        CopilotWebSocketClient.ConnectionState.DISCONNECTED,
                        CopilotWebSocketClient.ConnectionState.ERROR -> {
                            Toast.makeText(this@ChatActivity, 
                                "Connection lost", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
            }
        }
    }
    
    private fun handleCopilotResponse(response: com.ssfdre38.cpcli.android.client.model.CopilotResponse) {
        // Remove typing indicator
        removeTypingIndicator()
        
        if (response.error != null) {
            addSystemMessage("Error: ${response.error}")
            return
        }
        
        // Add Copilot's response
        val copilotMessage = ChatMessage(
            content = response.message,
            sender = MessageSender.COPILOT
        )
        addMessageToChat(copilotMessage)
        
        // Update session ID if provided
        response.sessionId?.let {
            sessionId = it
        }
    }
    
    private fun addSystemMessage(message: String) {
        val systemMessage = ChatMessage(
            content = message,
            sender = MessageSender.COPILOT
        )
        addMessageToChat(systemMessage)
    }
    
    private fun removeTypingIndicator() {
        val lastMessage = chatMessages.lastOrNull()
        if (lastMessage?.isTyping == true) {
            chatMessages.removeAt(chatMessages.size - 1)
            chatAdapter.submitList(chatMessages.toList())
        }
    }
    
    private fun loadChatHistory() {
        serverId?.let { id ->
            val settings = storageManager.loadAppSettings()
            if (settings.chatHistoryEnabled) {
                lifecycleScope.launch {
                    val history = storageManager.loadChatHistory(id)
                    chatMessages.addAll(history)
                    chatAdapter.submitList(chatMessages.toList())
                    scrollToBottom()
                }
            }
        }
    }
    
    private fun saveChatHistory() {
        serverId?.let { id ->
            val settings = storageManager.loadAppSettings()
            if (settings.chatHistoryEnabled) {
                lifecycleScope.launch {
                    // Only save non-typing messages
                    val messagesToSave = chatMessages.filter { !it.isTyping }
                    storageManager.saveChatHistory(id, messagesToSave)
                    
                    // Cleanup old messages if needed
                    storageManager.cleanupOldHistory(id, settings.maxChatHistory)
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        saveChatHistory()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        saveChatHistory()
    }
    
    private fun addMessageToChat(message: ChatMessage) {
        val messageWithServerId = message.copy(serverId = serverId)
        chatMessages.add(messageWithServerId)
        chatAdapter.submitList(chatMessages.toList())
        scrollToBottom()
        
        // Auto-save after adding new messages (but don't block UI)
        lifecycleScope.launch {
            val settings = storageManager.loadAppSettings()
            if (settings.chatHistoryEnabled) {
                saveChatHistory()
            }
        }
    }
    
    private fun scrollToBottom() {
        binding.recyclerViewChat.post {
            if (chatMessages.isNotEmpty()) {
                binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
            }
        }
    }
}
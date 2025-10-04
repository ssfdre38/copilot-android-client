package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.ui.ChatAdapter
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var buttonClearHistory: MaterialButton
    
    private lateinit var storageManager: StorageManager
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        storageManager = StorageManager(this)
        
        initViews()
        setupRecyclerView()
        setupListeners()
        loadChatHistory()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Chat History"
    }

    private fun initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        buttonClearHistory = findViewById(R.id.buttonClearHistory)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        recyclerViewHistory.adapter = chatAdapter
    }

    private fun setupListeners() {
        buttonClearHistory.setOnClickListener {
            showClearHistoryConfirmation()
        }
    }
    
    private fun loadChatHistory() {
        // Load history from all servers
        val allServers = storageManager.getAllServers()
        val allMessages = mutableListOf<com.ssfdre38.cpcli.android.client.data.ChatMessage>()
        
        allServers.forEach { server ->
            val serverMessages = storageManager.getChatHistory(server.id)
            allMessages.addAll(serverMessages)
        }
        
        // Sort by timestamp
        allMessages.sortBy { it.timestamp }
        
        chatAdapter.setMessages(allMessages)
        
        if (allMessages.isEmpty()) {
            Toast.makeText(this, "No chat history found", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showClearHistoryConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Chat History")
            .setMessage("This will delete all chat history from all servers. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                storageManager.clearAllChatHistory()
                chatAdapter.clearMessages()
                Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
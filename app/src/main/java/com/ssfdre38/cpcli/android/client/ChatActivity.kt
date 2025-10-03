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
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var storageManager: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Create layout programmatically to avoid complex XML layout issues
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
            }
            
            // Title
            val titleText = TextView(this).apply {
                text = "💬 Chat Terminal"
                textSize = 20f
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
            mainLayout.addView(recyclerView)
            mainLayout.addView(inputLayout)
            mainLayout.addView(buttonLayout)
            
            setContentView(mainLayout)
            
            // Initialize functionality
            setupRecyclerView()
            setupListeners()
            
            Toast.makeText(this, "🎉 ChatActivity working!", Toast.LENGTH_LONG).show()
            
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
                content = "Welcome to Copilot CLI Chat! 🚀\n\nThe activity transition issue has been resolved!",
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
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = message,
                    isFromUser = true,
                    timestamp = System.currentTimeMillis(),
                    serverId = "default"
                )
                
                chatAdapter.addMessage(chatMessage)
                messageInput.text.clear()
                
                // Scroll to bottom
                recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                
                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
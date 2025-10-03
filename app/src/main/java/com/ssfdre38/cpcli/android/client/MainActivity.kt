package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import com.ssfdre38.cpcli.android.client.data.StorageManager
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var editTextServerUrl: TextInputEditText
    private lateinit var buttonConnect: MaterialButton
    private lateinit var buttonStartChat: MaterialButton
    private lateinit var buttonSettings: MaterialButton
    private lateinit var textConnectionStatus: TextView
    
    private lateinit var storageManager: StorageManager
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageManager = StorageManager(this)
        
        initViews()
        setupListeners()
        loadDefaultServer()
    }

    private fun initViews() {
        editTextServerUrl = findViewById(R.id.editTextServerUrl)
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonStartChat = findViewById(R.id.buttonStartChat)
        buttonSettings = findViewById(R.id.buttonSettings)
        textConnectionStatus = findViewById(R.id.textConnectionStatus)
    }
    
    private fun loadDefaultServer() {
        val defaultServer = storageManager.getDefaultServer()
        if (defaultServer != null) {
            editTextServerUrl.setText("${defaultServer.url}:${defaultServer.port}")
            textConnectionStatus.text = "Server configured: ${defaultServer.name}"
            buttonStartChat.isEnabled = true
        } else {
            // Create a default server for first time users
            val defaultServerConfig = ServerConfig(
                id = "default",
                name = "Local Server",
                url = "localhost",
                port = 3002,
                isDefault = true
            )
            storageManager.saveServerConfig(defaultServerConfig)
            storageManager.setDefaultServer("default")
            editTextServerUrl.setText("localhost:3002")
            textConnectionStatus.text = "Default server ready"
            buttonStartChat.isEnabled = true
        }
    }

    private fun setupListeners() {
        buttonConnect.setOnClickListener {
            val serverUrl = editTextServerUrl.text.toString()
            if (serverUrl.isNotBlank()) {
                // Parse server URL
                val parts = serverUrl.split(":")
                if (parts.size >= 2) {
                    val url = parts[0]
                    val port = parts[1].toIntOrNull() ?: 3002
                    
                    val serverConfig = ServerConfig(
                        id = "temp_${System.currentTimeMillis()}",
                        name = "Temporary Server",
                        url = url,
                        port = port,
                        isDefault = false
                    )
                    
                    // Save as temporary server
                    storageManager.saveServerConfig(serverConfig)
                    
                    isConnected = true
                    textConnectionStatus.text = getString(R.string.connected)
                    buttonConnect.text = getString(R.string.disconnect)
                    buttonStartChat.isEnabled = true
                    Toast.makeText(this, "Server configured: $serverUrl", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter URL in format: host:port", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStartChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh server configuration in case it was changed in settings
        loadDefaultServer()
    }
}
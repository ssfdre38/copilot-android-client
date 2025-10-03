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
import com.ssfdre38.cpcli.android.client.network.ServerDiscovery
import com.ssfdre38.cpcli.android.client.network.DiscoveredServer
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.data.ServerConfig

class MainActivity : AppCompatActivity() {

    private lateinit var editTextServerUrl: TextInputEditText
    private lateinit var buttonConnect: MaterialButton
    private lateinit var buttonStartChat: MaterialButton
    private lateinit var buttonSettings: MaterialButton
    private lateinit var buttonDiscoverServers: MaterialButton
    private lateinit var textConnectionStatus: TextView
    
    private lateinit var serverDiscovery: ServerDiscovery
    private lateinit var storageManager: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverDiscovery = ServerDiscovery(this)
        storageManager = StorageManager(this)
        
        initViews()
        setupListeners()
        setupServerDiscovery()
    }

    private fun initViews() {
        editTextServerUrl = findViewById(R.id.editTextServerUrl)
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonStartChat = findViewById(R.id.buttonStartChat)
        buttonSettings = findViewById(R.id.buttonSettings)
        buttonDiscoverServers = findViewById(R.id.buttonDiscoverServers)
        textConnectionStatus = findViewById(R.id.textConnectionStatus)
    }

    private fun setupListeners() {
        buttonConnect.setOnClickListener {
            val serverUrl = editTextServerUrl.text.toString()
            if (serverUrl.isNotBlank()) {
                // Simulate connection
                textConnectionStatus.text = getString(R.string.connected)
                buttonStartChat.isEnabled = true
                Toast.makeText(this, "Connected to $serverUrl", Toast.LENGTH_SHORT).show()
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
        
        buttonDiscoverServers.setOnClickListener {
            startServerDiscovery()
        }
    }
    
    private fun setupServerDiscovery() {
        serverDiscovery.setDiscoveryListener(object : ServerDiscovery.DiscoveryListener {
            override fun onServerFound(server: DiscoveredServer) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Found: ${server.name} at ${server.host}:${server.port}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onDiscoveryComplete(servers: List<DiscoveredServer>) {
                runOnUiThread {
                    buttonDiscoverServers.text = "Discover Servers"
                    buttonDiscoverServers.isEnabled = true
                    
                    if (servers.isNotEmpty()) {
                        showDiscoveredServersDialog(servers)
                    } else {
                        Toast.makeText(this@MainActivity, "No servers found on the network", Toast.LENGTH_LONG).show()
                    }
                }
            }
            
            override fun onDiscoveryError(error: String) {
                runOnUiThread {
                    buttonDiscoverServers.text = "Discover Servers"
                    buttonDiscoverServers.isEnabled = true
                    Toast.makeText(this@MainActivity, "Discovery failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    
    private fun startServerDiscovery() {
        buttonDiscoverServers.text = "Scanning..."
        buttonDiscoverServers.isEnabled = false
        serverDiscovery.startDiscovery()
        Toast.makeText(this, "Scanning local network for servers...", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDiscoveredServersDialog(servers: List<DiscoveredServer>) {
        val serverNames = servers.map { "${it.name} (${it.host}:${it.port})" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Discovered Servers")
            .setItems(serverNames) { _, which ->
                val selectedServer = servers[which]
                useDiscoveredServer(selectedServer)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun useDiscoveredServer(server: DiscoveredServer) {
        editTextServerUrl.setText("ws://${server.host}:${server.port}")
        
        // Ask if user wants to save this server
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Save Server")
            .setMessage("Do you want to save this server for future use?")
            .setPositiveButton("Save") { _, _ ->
                saveDiscoveredServer(server)
            }
            .setNegativeButton("Use Once", null)
            .show()
    }
    
    private fun saveDiscoveredServer(server: DiscoveredServer) {
        val serverConfig = ServerConfig(
            id = "discovered_${System.currentTimeMillis()}",
            name = server.name,
            url = "ws://${server.host}:${server.port}",
            port = server.port,
            isDefault = false
        )
        
        storageManager.saveServerConfig(serverConfig)
        Toast.makeText(this, "Server saved: ${server.name}", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        serverDiscovery.stopDiscovery()
        super.onDestroy()
    }
}
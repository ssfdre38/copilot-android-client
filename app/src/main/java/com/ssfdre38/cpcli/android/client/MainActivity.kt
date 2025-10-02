package com.ssfdre38.cpcli.android.client

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.ssfdre38.cpcli.android.client.databinding.ActivityMainBinding
import com.ssfdre38.cpcli.android.client.model.ServerConfig
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.utils.StorageManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager
import com.ssfdre38.cpcli.android.client.utils.UpdateManager
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var storageManager: StorageManager
    private lateinit var updateManager: UpdateManager
    private var servers = mutableListOf<ServerConfig>()
    private var currentServer: ServerConfig? = null
    private var isConnected = false
    private var webSocketClient: CopilotWebSocketClient? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        storageManager = StorageManager(this)
        updateManager = UpdateManager(this)
        
        setupUI()
        loadServers()
        checkForUpdatesIfNeeded()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            R.id.action_servers -> {
                startActivity(Intent(this, ServerManagementActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            buttonConnect.setOnClickListener {
                if (isConnected) {
                    disconnectFromServer()
                } else {
                    connectToServer()
                }
            }
            
            buttonChat.setOnClickListener {
                startChatActivity()
            }
            
            buttonSettings.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            
            buttonAddServer.setOnClickListener {
                startActivity(Intent(this@MainActivity, ServerManagementActivity::class.java))
            }
            
            buttonAddFirstServer.setOnClickListener {
                startActivity(Intent(this@MainActivity, ServerManagementActivity::class.java))
            }
            
            buttonTestLocalhost.setOnClickListener {
                testLocalhost()
            }
            
            buttonScanNetwork.setOnClickListener {
                scanNetwork()
            }
            
            spinnerServers.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (servers.isNotEmpty() && position < servers.size) {
                        currentServer = servers[position]
                        storageManager.saveCurrentServerId(currentServer?.id)
                        updateConnectionUI()
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
            
            updateConnectButtonState()
        }
    }
    
    private fun loadServers() {
        lifecycleScope.launch {
            servers.clear()
            servers.addAll(storageManager.loadServers())
            
            val currentServerId = storageManager.getCurrentServerId()
            currentServer = servers.find { it.id == currentServerId } ?: servers.firstOrNull { it.isDefault }
            
            updateServerSpinner()
            updateConnectionUI()
            
            // Auto-connect if enabled
            if (preferences.getBoolean("auto_connect", false) && currentServer != null) {
                connectToServer()
            }
        }
    }
    
    private fun updateServerSpinner() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        servers.forEach { server ->
            val displayName = if (server.isDefault) "${server.name} (Default)" else server.name
            adapter.add(displayName)
        }
        
        binding.spinnerServers.adapter = adapter
        
        currentServer?.let { current ->
            val position = servers.indexOfFirst { it.id == current.id }
            if (position >= 0) {
                binding.spinnerServers.setSelection(position)
            }
        }
        
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        binding.apply {
            if (servers.isEmpty()) {
                layoutServerSelection.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
                buttonConnect.isEnabled = false
            } else {
                layoutServerSelection.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
                updateConnectButtonState()
            }
        }
    }
    
    private fun updateConnectionUI() {
        currentServer?.let { server ->
            binding.textServerInfo.text = "Server: ${server.name}\nURL: ${server.url}"
        } ?: run {
            binding.textServerInfo.text = "No server selected"
        }
    }
    
    private fun updateConnectButtonState() {
        binding.buttonConnect.isEnabled = currentServer != null && !binding.textViewStatus.text.toString().contains("onnecting")
    }
    
    private fun testLocalhost() {
        val testServer = ServerConfig(
            name = "Localhost Test",
            url = "ws://localhost:3002",
            isDefault = false
        )
        
        servers.add(0, testServer)
        currentServer = testServer
        updateServerSpinner()
        updateConnectionUI()
        
        Toast.makeText(this, "Added localhost test server", Toast.LENGTH_SHORT).show()
    }
    
    private fun scanNetwork() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.textViewStatus.text = "Scanning network..."
        
        // Simple network scan simulation
        binding.root.postDelayed({
            binding.textViewStatus.text = "No servers found on network"
            Toast.makeText(this, "No Copilot servers found", Toast.LENGTH_LONG).show()
        }, 3000)
    }
    
    private fun connectToServer() {
        val server = currentServer
        if (server == null) {
            Toast.makeText(this, "No server selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.textViewStatus.text = "Connecting to ${server.name}..."
        binding.buttonConnect.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Update last used time
                val updatedServer = server.copy(lastUsed = Date())
                val serverIndex = servers.indexOfFirst { it.id == server.id }
                if (serverIndex >= 0) {
                    servers[serverIndex] = updatedServer
                    currentServer = updatedServer
                    storageManager.saveServers(servers)
                }
                
                // Create and connect WebSocket client
                webSocketClient = CopilotWebSocketClient(server.url, server.apiKey)
                
                // Observe connection state in a separate coroutine
                launch {
                    webSocketClient?.connectionState?.collect { state ->
                        runOnUiThread {
                            when (state) {
                                CopilotWebSocketClient.ConnectionState.CONNECTING -> {
                                    binding.textViewStatus.text = "Connecting to ${server.name}..."
                                }
                                CopilotWebSocketClient.ConnectionState.CONNECTED -> {
                                    isConnected = true
                                    binding.textViewStatus.text = "Connected to ${server.name} ✓"
                                    binding.buttonConnect.text = "Disconnect"
                                    binding.buttonChat.isEnabled = true
                                    
                                    // Set the global WebSocket client for ChatActivity
                                    ChatActivity.webSocketClient = webSocketClient
                                    
                                    Toast.makeText(this@MainActivity, "Connected successfully!", Toast.LENGTH_SHORT).show()
                                    binding.buttonConnect.isEnabled = true
                                }
                                CopilotWebSocketClient.ConnectionState.ERROR -> {
                                    binding.textViewStatus.text = "Connection failed"
                                    isConnected = false
                                    binding.buttonConnect.text = "Connect"
                                    binding.buttonChat.isEnabled = false
                                    binding.buttonConnect.isEnabled = true
                                    
                                    webSocketClient?.errors?.value?.let { error ->
                                        Toast.makeText(this@MainActivity, "Connection failed: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                                CopilotWebSocketClient.ConnectionState.DISCONNECTED -> {
                                    isConnected = false
                                    binding.textViewStatus.text = "Disconnected"
                                    binding.buttonConnect.text = "Connect"
                                    binding.buttonChat.isEnabled = false
                                    binding.buttonConnect.isEnabled = true
                                }
                            }
                        }
                    }
                }
                
                // Start the connection
                webSocketClient?.connect()
                
            } catch (e: Exception) {
                binding.textViewStatus.text = "Connection failed: ${e.message}"
                Toast.makeText(this@MainActivity, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                binding.buttonConnect.isEnabled = true
            }
        }
    }
    
    private fun disconnectFromServer() {
        webSocketClient?.disconnect()
        webSocketClient = null
        ChatActivity.webSocketClient = null
        isConnected = false
        
        binding.textViewStatus.text = "Disconnected"
        binding.buttonConnect.text = "Connect"
        binding.buttonChat.isEnabled = false
        updateConnectButtonState()
    }
    
    private fun startChatActivity() {
        if (isConnected && currentServer != null) {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("server_id", currentServer!!.id)
                putExtra("server_name", currentServer!!.name)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please connect to a server first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkForUpdatesIfNeeded() {
        if (UpdateManager.shouldCheckForUpdates(this)) {
            lifecycleScope.launch {
                val updateInfo = updateManager.checkForUpdates()
                if (updateInfo != null) {
                    val skipVersion = UpdateManager.getSkipVersion(this@MainActivity)
                    if (skipVersion != updateInfo.version) {
                        showUpdateNotification(updateInfo)
                    }
                }
                UpdateManager.markUpdateChecked(this@MainActivity)
            }
        }
    }
    
    private fun showUpdateNotification(updateInfo: com.ssfdre38.cpcli.android.client.model.UpdateInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Available")
            .setMessage("Version ${updateInfo.version} is available. Would you like to download it?")
            .setPositiveButton("Download") { _, _ ->
                val intent = updateManager.getDownloadIntent(updateInfo.downloadUrl)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .setNeutralButton("Skip This Version") { _, _ ->
                UpdateManager.setSkipVersion(this, updateInfo.version)
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadServers() // Reload servers in case they were modified
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
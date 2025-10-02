package com.github.copilot.client

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.copilot.client.databinding.ActivityMainBinding
import com.github.copilot.client.network.CopilotWebSocketClient
import com.github.copilot.client.network.NetworkScanner
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private var webSocketClient: CopilotWebSocketClient? = null
    private var isConnected = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        setupUI()
        loadSavedSettings()
        observeConnectionState()
        
        // Auto-connect if enabled and settings are available
        if (preferences.getBoolean(getString(R.string.pref_auto_connect), false)) {
            val savedUrl = preferences.getString(getString(R.string.pref_server_url), "")
            if (!savedUrl.isNullOrEmpty() && isValidWebSocketUrl(savedUrl)) {
                connectToServer()
            }
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
            
            buttonTestLocalhost.setOnClickListener {
                testLocalhost()
            }
            
            buttonScanNetwork.setOnClickListener {
                scanNetwork()
            }
            
            // Enable/disable connect button based on URL input
            editTextServerUrl.addTextChangedListener { text ->
                updateConnectButtonState()
                // Save URL as user types
                saveServerSettings()
            }
            
            editTextApiKey.addTextChangedListener {
                saveServerSettings()
            }
        }
    }
    
    private fun loadSavedSettings() {
        val savedUrl = preferences.getString(getString(R.string.pref_server_url), "")
        val savedApiKey = preferences.getString(getString(R.string.pref_api_key), "")
        
        binding.editTextServerUrl.setText(savedUrl)
        binding.editTextApiKey.setText(savedApiKey)
        
        updateConnectButtonState()
    }
    
    private fun saveServerSettings() {
        val url = binding.editTextServerUrl.text.toString().trim()
        val apiKey = binding.editTextApiKey.text.toString().trim()
        
        preferences.edit()
            .putString(getString(R.string.pref_server_url), url)
            .putString(getString(R.string.pref_api_key), apiKey)
            .apply()
    }
    
    private fun updateConnectButtonState() {
        val url = binding.editTextServerUrl.text.toString().trim()
        binding.buttonConnect.isEnabled = url.isNotEmpty() && !binding.textViewStatus.text.toString().contains("onnecting")
    }
    
    private fun testLocalhost() {
        binding.editTextServerUrl.setText("ws://localhost:3001")
        Toast.makeText(this, "Set to localhost - make sure server is running locally", Toast.LENGTH_SHORT).show()
    }
    
    private fun scanNetwork() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.error_network_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.textViewStatus.text = getString(R.string.scanning_network)
        binding.textViewStatus.setTextColor(getColor(R.color.status_connecting))
        
        lifecycleScope.launch {
            try {
                val networkScanner = NetworkScanner()
                val servers = networkScanner.scanForServers()
                
                runOnUiThread {
                    if (servers.isNotEmpty()) {
                        val serverUrl = servers.first()
                        binding.editTextServerUrl.setText(serverUrl)
                        Toast.makeText(this@MainActivity, 
                            getString(R.string.servers_found, servers.size), 
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, 
                            getString(R.string.no_servers_found), 
                            Toast.LENGTH_SHORT).show()
                    }
                    updateConnectionStatusUI(CopilotWebSocketClient.ConnectionState.DISCONNECTED)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Network scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    updateConnectionStatusUI(CopilotWebSocketClient.ConnectionState.DISCONNECTED)
                }
            }
        }
    }
    
    private fun connectToServer() {
        val url = binding.editTextServerUrl.text.toString().trim()
        val apiKey = binding.editTextApiKey.text.toString().trim().takeIf { it.isNotEmpty() }
        
        if (!isValidWebSocketUrl(url)) {
            Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.error_network_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save settings before connecting
        saveServerSettings()
        
        // Update UI to show connecting state
        updateConnectionStatusUI(CopilotWebSocketClient.ConnectionState.CONNECTING)
        
        // Create and connect WebSocket client
        webSocketClient = CopilotWebSocketClient(url, apiKey)
        
        // Connect in background
        lifecycleScope.launch {
            try {
                webSocketClient?.connect()
            } catch (e: Exception) {
                runOnUiThread {
                    showConnectionError(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    private fun disconnectFromServer() {
        updateConnectionStatusUI(CopilotWebSocketClient.ConnectionState.DISCONNECTED)
        webSocketClient?.disconnect()
        webSocketClient = null
        isConnected = false
    }
    
    private fun observeConnectionState() {
        lifecycleScope.launch {
            webSocketClient?.connectionState?.collect { state ->
                runOnUiThread {
                    updateConnectionStatusUI(state)
                }
            }
        }
        
        lifecycleScope.launch {
            webSocketClient?.errors?.collect { error ->
                error?.let {
                    runOnUiThread {
                        showConnectionError(it)
                    }
                }
            }
        }
    }
    
    private fun updateConnectionStatusUI(state: CopilotWebSocketClient.ConnectionState) {
        binding.apply {
            when (state) {
                CopilotWebSocketClient.ConnectionState.CONNECTING -> {
                    textViewStatus.text = getString(R.string.connecting)
                    textViewStatus.setTextColor(getColor(R.color.status_connecting))
                    buttonConnect.isEnabled = false
                    buttonChat.isEnabled = false
                    isConnected = false
                }
                CopilotWebSocketClient.ConnectionState.CONNECTED -> {
                    textViewStatus.text = getString(R.string.connected)
                    textViewStatus.setTextColor(getColor(R.color.status_connected))
                    buttonConnect.isEnabled = true
                    buttonConnect.text = getString(R.string.disconnect)
                    buttonChat.isEnabled = true
                    isConnected = true
                    Toast.makeText(this@MainActivity, "Successfully connected to server!", Toast.LENGTH_SHORT).show()
                }
                CopilotWebSocketClient.ConnectionState.DISCONNECTED -> {
                    textViewStatus.text = getString(R.string.not_connected)
                    textViewStatus.setTextColor(getColor(R.color.status_disconnected))
                    buttonConnect.isEnabled = true
                    buttonConnect.text = getString(R.string.connect_to_server)
                    buttonChat.isEnabled = false
                    isConnected = false
                }
                CopilotWebSocketClient.ConnectionState.ERROR -> {
                    textViewStatus.text = getString(R.string.connection_failed)
                    textViewStatus.setTextColor(getColor(R.color.status_disconnected))
                    buttonConnect.isEnabled = true
                    buttonConnect.text = getString(R.string.connect_to_server)
                    buttonChat.isEnabled = false
                    isConnected = false
                }
            }
        }
    }
    
    private fun showConnectionError(error: String) {
        Toast.makeText(this, "Connection error: $error", Toast.LENGTH_LONG).show()
        updateConnectionStatusUI(CopilotWebSocketClient.ConnectionState.ERROR)
    }
    
    private fun startChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        ChatActivity.webSocketClient = webSocketClient
        startActivity(intent)
    }
    
    private fun isValidWebSocketUrl(url: String): Boolean {
        return (url.startsWith("ws://") || url.startsWith("wss://")) && 
               Patterns.WEB_URL.matcher(url.replace("ws://", "http://").replace("wss://", "https://")).matches()
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't disconnect here to allow background connection
    }
    
    override fun onResume() {
        super.onResume()
        // Reload settings in case they were changed in SettingsActivity
        loadSavedSettings()
        
        // Re-observe connection state if client exists
        webSocketClient?.let {
            observeConnectionState()
        }
    }
}
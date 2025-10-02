package com.ssfdre38.cpcli.android.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ssfdre38.cpcli.android.client.databinding.ActivityServerEditBinding
import com.ssfdre38.cpcli.android.client.model.ServerConfig
import com.ssfdre38.cpcli.android.client.utils.StorageManager
import kotlinx.coroutines.launch
import java.util.Date

class ServerEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityServerEditBinding
    private lateinit var storageManager: StorageManager
    private var serverToEdit: ServerConfig? = null
    private var isEditMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = StorageManager(this)
        
        setupFromIntent()
        setupActionBar()
        setupUI()
        
        if (isEditMode) {
            loadServerData()
        }
    }
    
    private fun setupFromIntent() {
        serverToEdit = intent.getSerializableExtra(EXTRA_SERVER) as? ServerConfig
        isEditMode = serverToEdit != null
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (isEditMode) "Edit Server" else "Add Server"
        }
    }
    
    private fun setupUI() {
        binding.apply {
            buttonSave.setOnClickListener {
                saveServer()
            }
            
            buttonTestConnection.setOnClickListener {
                testConnection()
            }
            
            switchDefault.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isEditMode) {
                    // Show warning about making this the default
                    Toast.makeText(this@ServerEditActivity, 
                        "This will be set as your default server", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadServerData() {
        serverToEdit?.let { server ->
            binding.apply {
                editTextName.setText(server.name)
                editTextUrl.setText(server.url)
                editTextApiKey.setText(server.apiKey ?: "")
                switchDefault.isChecked = server.isDefault
            }
        }
    }
    
    private fun saveServer() {
        val name = binding.editTextName.text.toString().trim()
        val url = binding.editTextUrl.text.toString().trim()
        val apiKey = binding.editTextApiKey.text.toString().trim()
        val isDefault = binding.switchDefault.isChecked
        
        // Validation
        if (name.isEmpty()) {
            binding.editTextName.error = "Server name is required"
            return
        }
        
        if (url.isEmpty()) {
            binding.editTextUrl.error = "Server URL is required"
            return
        }
        
        if (!isValidUrl(url)) {
            binding.editTextUrl.error = "Invalid URL format"
            return
        }
        
        lifecycleScope.launch {
            val servers = storageManager.loadServers().toMutableList()
            
            val serverConfig = if (isEditMode) {
                serverToEdit!!.copy(
                    name = name,
                    url = url,
                    apiKey = apiKey.ifEmpty { null },
                    isDefault = isDefault,
                    lastUsed = Date()
                )
            } else {
                ServerConfig(
                    name = name,
                    url = url,
                    apiKey = apiKey.ifEmpty { null },
                    isDefault = isDefault
                )
            }
            
            // Handle default server logic
            if (isDefault) {
                for (i in servers.indices) {
                    servers[i] = servers[i].copy(isDefault = false)
                }
            }
            
            if (isEditMode) {
                val index = servers.indexOfFirst { it.id == serverToEdit!!.id }
                if (index >= 0) {
                    servers[index] = serverConfig
                }
            } else {
                servers.add(serverConfig)
            }
            
            storageManager.saveServers(servers)
            
            if (isDefault) {
                storageManager.saveCurrentServerId(serverConfig.id)
            }
            
            setResult(RESULT_OK)
            finish()
        }
    }
    
    private fun testConnection() {
        val url = binding.editTextUrl.text.toString().trim()
        val apiKey = binding.editTextApiKey.text.toString().trim()
        
        if (url.isEmpty()) {
            binding.editTextUrl.error = "Enter URL to test"
            return
        }
        
        if (!isValidUrl(url)) {
            binding.editTextUrl.error = "Invalid URL format"
            return
        }
        
        binding.buttonTestConnection.isEnabled = false
        binding.buttonTestConnection.text = "Testing..."
        
        // TODO: Implement actual connection test
        // For now, just simulate a test
        binding.root.postDelayed({
            binding.buttonTestConnection.isEnabled = true
            binding.buttonTestConnection.text = "Test Connection"
            Toast.makeText(this, "Connection test completed", Toast.LENGTH_SHORT).show()
        }, 2000)
    }
    
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("ws://") || url.startsWith("wss://") || 
               url.startsWith("http://") || url.startsWith("https://")
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        private const val EXTRA_SERVER = "server"
        
        fun createIntent(context: Context, server: ServerConfig?): Intent {
            return Intent(context, ServerEditActivity::class.java).apply {
                if (server != null) {
                    putExtra(EXTRA_SERVER, server)
                }
            }
        }
    }
}
package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssfdre38.cpcli.android.client.databinding.ActivityServerManagementBinding
import com.ssfdre38.cpcli.android.client.model.ServerConfig
import com.ssfdre38.cpcli.android.client.ui.ServerAdapter
import com.ssfdre38.cpcli.android.client.utils.StorageManager
import kotlinx.coroutines.launch
import java.util.Date

class ServerManagementActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityServerManagementBinding
    private lateinit var serverAdapter: ServerAdapter
    private lateinit var storageManager: StorageManager
    private var servers = mutableListOf<ServerConfig>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = StorageManager(this)
        
        setupActionBar()
        setupRecyclerView()
        setupFab()
        loadServers()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Server Management"
        }
    }
    
    private fun setupRecyclerView() {
        serverAdapter = ServerAdapter(
            onServerClick = { server -> editServer(server) },
            onServerDelete = { server -> deleteServer(server) },
            onSetDefault = { server -> setDefaultServer(server) }
        )
        
        binding.recyclerViewServers.apply {
            layoutManager = LinearLayoutManager(this@ServerManagementActivity)
            adapter = serverAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddServer.setOnClickListener {
            addNewServer()
        }
    }
    
    private fun loadServers() {
        lifecycleScope.launch {
            servers.clear()
            servers.addAll(storageManager.loadServers())
            serverAdapter.submitList(servers.toList())
            
            binding.textEmptyState.visibility = if (servers.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }
    
    private fun addNewServer() {
        val intent = ServerEditActivity.createIntent(this, null)
        startActivityForResult(intent, REQUEST_EDIT_SERVER)
    }
    
    private fun editServer(server: ServerConfig) {
        val intent = ServerEditActivity.createIntent(this, server)
        startActivityForResult(intent, REQUEST_EDIT_SERVER)
    }
    
    private fun deleteServer(server: ServerConfig) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Server")
            .setMessage("Are you sure you want to delete \"${server.name}\"?\n\nThis will also delete all chat history for this server.")
            .setPositiveButton("Delete") { _, _ ->
                servers.removeAll { it.id == server.id }
                storageManager.saveServers(servers)
                storageManager.clearChatHistory(server.id)
                serverAdapter.submitList(servers.toList())
                
                // If this was the current server, clear the selection
                if (storageManager.getCurrentServerId() == server.id) {
                    storageManager.saveCurrentServerId(null)
                }
                
                updateEmptyState()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setDefaultServer(server: ServerConfig) {
        // Update default status
        for (i in servers.indices) {
            servers[i] = servers[i].copy(isDefault = servers[i].id == server.id)
        }
        storageManager.saveServers(servers)
        storageManager.saveCurrentServerId(server.id)
        serverAdapter.submitList(servers.toList())
    }
    
    private fun updateEmptyState() {
        binding.textEmptyState.visibility = if (servers.isEmpty()) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_SERVER && resultCode == RESULT_OK) {
            loadServers() // Reload servers after edit
        }
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
        private const val REQUEST_EDIT_SERVER = 1001
    }
}
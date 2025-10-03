package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import com.ssfdre38.cpcli.android.client.data.StorageManager
import java.util.*

class ServerManagementActivity : AppCompatActivity() {

    private lateinit var recyclerViewServers: RecyclerView
    private lateinit var buttonAddServer: MaterialButton
    
    private lateinit var storageManager: StorageManager
    private lateinit var serverAdapter: ServerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_management)

        storageManager = StorageManager(this)
        
        initViews()
        setupRecyclerView()
        setupListeners()
        loadServers()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Server Management"
    }

    private fun initViews() {
        recyclerViewServers = findViewById(R.id.recyclerViewServers)
        buttonAddServer = findViewById(R.id.buttonAddServer)
    }

    private fun setupRecyclerView() {
        serverAdapter = ServerAdapter { server, action ->
            when (action) {
                ServerAdapter.Action.EDIT -> editServer(server)
                ServerAdapter.Action.DELETE -> deleteServer(server)
                ServerAdapter.Action.SET_DEFAULT -> setDefaultServer(server)
            }
        }
        recyclerViewServers.layoutManager = LinearLayoutManager(this)
        recyclerViewServers.adapter = serverAdapter
    }

    private fun setupListeners() {
        buttonAddServer.setOnClickListener {
            showAddServerDialog()
        }
    }
    
    private fun loadServers() {
        val servers = storageManager.getAllServers()
        serverAdapter.updateServers(servers)
    }

    private fun showAddServerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_server, null)
        val editName = dialogView.findViewById<TextInputEditText>(R.id.editServerName)
        val editUrl = dialogView.findViewById<TextInputEditText>(R.id.editServerUrl)
        val editPort = dialogView.findViewById<TextInputEditText>(R.id.editServerPort)
        
        // Set default values
        editPort.setText("3002")

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Server")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editName.text.toString().trim()
                val url = editUrl.text.toString().trim()
                val portStr = editPort.text.toString().trim()
                
                if (name.isNotBlank() && url.isNotBlank() && portStr.isNotBlank()) {
                    val port = portStr.toIntOrNull()
                    if (port != null && port in 1..65535) {
                        val server = ServerConfig(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            url = url,
                            port = port
                        )
                        storageManager.saveServerConfig(server)
                        loadServers()
                        Toast.makeText(this, "Server added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid port number (1-65535)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun editServer(server: ServerConfig) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_server, null)
        val editName = dialogView.findViewById<TextInputEditText>(R.id.editServerName)
        val editUrl = dialogView.findViewById<TextInputEditText>(R.id.editServerUrl)
        val editPort = dialogView.findViewById<TextInputEditText>(R.id.editServerPort)
        
        // Pre-fill with existing values
        editName.setText(server.name)
        editUrl.setText(server.url)
        editPort.setText(server.port.toString())

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Server")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editName.text.toString().trim()
                val url = editUrl.text.toString().trim()
                val portStr = editPort.text.toString().trim()
                
                if (name.isNotBlank() && url.isNotBlank() && portStr.isNotBlank()) {
                    val port = portStr.toIntOrNull()
                    if (port != null && port in 1..65535) {
                        val updatedServer = server.copy(
                            name = name,
                            url = url,
                            port = port
                        )
                        storageManager.saveServerConfig(updatedServer)
                        loadServers()
                        Toast.makeText(this, "Server updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid port number (1-65535)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteServer(server: ServerConfig) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Server")
            .setMessage("Are you sure you want to delete '${server.name}'? This will also delete all chat history for this server.")
            .setPositiveButton("Delete") { _, _ ->
                storageManager.deleteServer(server.id)
                loadServers()
                Toast.makeText(this, "Server deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setDefaultServer(server: ServerConfig) {
        storageManager.setDefaultServer(server.id)
        loadServers()
        Toast.makeText(this, "'${server.name}' set as default server", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
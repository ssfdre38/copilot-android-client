package com.ssfdre38.cpcli.android.client.ui

import android.os.Bundle
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import com.ssfdre38.cpcli.android.client.data.ServerConfigManager
import com.ssfdre38.cpcli.android.client.utils.ThemeManager
import java.util.*

class ServerManagementActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var addServerButton: Button
    private lateinit var serverAdapter: ServerAdapter
    private lateinit var serverManager: ServerConfigManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        
        try {
            setupUI()
            setupData()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupUI() {
        // Create layout programmatically
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "🖥️ Server Management"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        // Description
        val descText = TextView(this).apply {
            text = "Add and manage your Copilot CLI servers"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        // Add server button
        addServerButton = Button(this).apply {
            text = "➕ Add New Server"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
            setOnClickListener { showAddServerDialog() }
        }
        
        // RecyclerView for servers
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Back to Main"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
            setOnClickListener { finish() }
        }
        
        mainLayout.addView(titleText)
        mainLayout.addView(descText)
        mainLayout.addView(addServerButton)
        mainLayout.addView(recyclerView)
        mainLayout.addView(backButton)
        
        setContentView(mainLayout)
    }
    
    private fun setupData() {
        serverManager = ServerConfigManager(this)
        serverAdapter = ServerAdapter(
            servers = serverManager.getAllServers().toMutableList(),
            onServerClick = { server -> selectServer(server) },
            onServerEdit = { server -> showEditServerDialog(server) },
            onServerDelete = { server -> deleteServer(server) }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = serverAdapter
        
        // Mark active server
        val activeServer = serverManager.getActiveServer()
        serverAdapter.setActiveServer(activeServer?.id)
    }
    
    private fun showAddServerDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        
        val nameInput = EditText(this).apply {
            hint = "Server Name (e.g., My Copilot Server)"
        }
        
        val urlInput = EditText(this).apply {
            hint = "Server URL (e.g., my-server.com)"
        }
        
        val portInput = EditText(this).apply {
            hint = "Port (e.g., 3002 or 8443)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        val sslCheckbox = CheckBox(this).apply {
            text = "Use SSL/TLS (wss://)"
            isChecked = true
        }
        
        val descInput = EditText(this).apply {
            hint = "Description (optional)"
        }
        
        dialogLayout.addView(TextView(this).apply { 
            text = "Add New Server"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        })
        dialogLayout.addView(nameInput)
        dialogLayout.addView(urlInput)
        dialogLayout.addView(portInput)
        dialogLayout.addView(sslCheckbox)
        dialogLayout.addView(descInput)
        
        android.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Add") { _, _ ->
                addNewServer(
                    name = nameInput.text.toString(),
                    url = urlInput.text.toString(),
                    port = portInput.text.toString(),
                    isSecure = sslCheckbox.isChecked,
                    description = descInput.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditServerDialog(server: ServerConfig) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        
        val nameInput = EditText(this).apply {
            setText(server.name)
            hint = "Server Name"
        }
        
        val urlInput = EditText(this).apply {
            setText(server.url)
            hint = "Server URL"
        }
        
        val portInput = EditText(this).apply {
            setText(server.port.toString())
            hint = "Port"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        val sslCheckbox = CheckBox(this).apply {
            text = "Use SSL/TLS (wss://)"
            isChecked = server.isSecure
        }
        
        val descInput = EditText(this).apply {
            setText(server.description)
            hint = "Description (optional)"
        }
        
        dialogLayout.addView(TextView(this).apply { 
            text = "Edit Server"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        })
        dialogLayout.addView(nameInput)
        dialogLayout.addView(urlInput)
        dialogLayout.addView(portInput)
        dialogLayout.addView(sslCheckbox)
        dialogLayout.addView(descInput)
        
        android.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                updateServer(
                    server = server,
                    name = nameInput.text.toString(),
                    url = urlInput.text.toString(),
                    port = portInput.text.toString(),
                    isSecure = sslCheckbox.isChecked,
                    description = descInput.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addNewServer(name: String, url: String, port: String, isSecure: Boolean, description: String) {
        // Enhanced input validation
        if (name.isBlank()) {
            showValidationError("Server name is required")
            return
        }
        
        if (name.length < 2) {
            showValidationError("Server name must be at least 2 characters")
            return
        }
        
        if (url.isBlank()) {
            showValidationError("Server URL is required")
            return
        }
        
        // Validate URL format
        if (!isValidUrl(url)) {
            showValidationError("Please enter a valid URL (e.g., example.com or 192.168.1.100)")
            return
        }
        
        if (port.isBlank()) {
            showValidationError("Port number is required")
            return
        }
        
        val portNum = port.toIntOrNull()
        if (portNum == null || portNum !in 1..65535) {
            showValidationError("Port must be a number between 1 and 65535")
            return
        }
        
        // Show loading dialog
        val loadingDialog = showLoadingDialog("Adding server...")
        
        try {
            val server = ServerConfig(
                id = UUID.randomUUID().toString(),
                name = name,
                url = cleanUrl(url),
                isSecure = isSecure,
                port = portNum,
                description = description
            )
            
            if (serverManager.addServer(server)) {
                loadingDialog.dismiss()
                serverAdapter.updateServers(serverManager.getAllServers())
                showSuccessMessage("✅ Server '${server.name}' added successfully")
            } else {
                loadingDialog.dismiss()
                showErrorMessage("❌ Server Already Exists", 
                    "A server with this URL and port already exists.\n\n" +
                    "URL: ${server.getFullUrl()}")
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
            showErrorMessage("❌ Failed to Add Server", 
                "An error occurred while adding the server: ${e.message}")
        }
    }
    
    private fun updateServer(server: ServerConfig, name: String, url: String, port: String, isSecure: Boolean, description: String) {
        // Enhanced input validation
        if (name.isBlank()) {
            showValidationError("Server name is required")
            return
        }
        
        if (name.length < 2) {
            showValidationError("Server name must be at least 2 characters")
            return
        }
        
        if (url.isBlank()) {
            showValidationError("Server URL is required")
            return
        }
        
        // Validate URL format
        if (!isValidUrl(url)) {
            showValidationError("Please enter a valid URL (e.g., example.com or 192.168.1.100)")
            return
        }
        
        if (port.isBlank()) {
            showValidationError("Port number is required")
            return
        }
        
        val portNum = port.toIntOrNull()
        if (portNum == null || portNum !in 1..65535) {
            showValidationError("Port must be a number between 1 and 65535")
            return
        }
        
        // Show loading dialog
        val loadingDialog = showLoadingDialog("Updating server...")
        
        try {
            val updatedServer = server.copy(
                name = name,
                url = cleanUrl(url),
                isSecure = isSecure,
                port = portNum,
                description = description
            )
            
            if (serverManager.updateServer(server.id, updatedServer)) {
                loadingDialog.dismiss()
                serverAdapter.updateServers(serverManager.getAllServers())
                showSuccessMessage("✅ Server '${updatedServer.name}' updated successfully")
            } else {
                loadingDialog.dismiss()
                showErrorMessage("❌ Update Failed", "Failed to update server configuration")
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
            showErrorMessage("❌ Update Error", 
                "An error occurred while updating the server: ${e.message}")
        }
    }
    
    private fun selectServer(server: ServerConfig) {
        // Show loading dialog
        val loadingDialog = showLoadingDialog("Selecting server...")
        
        // Simulate server validation/ping
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            loadingDialog.dismiss()
            
            serverManager.setActiveServer(server.id)
            serverAdapter.setActiveServer(server.id)
            serverAdapter.notifyDataSetChanged() // Force UI refresh
            
            Toast.makeText(this, "✅ Selected server: ${server.name}", Toast.LENGTH_SHORT).show()
        }, 1000) // 1 second delay
    }
    
    // Helper methods for validation and UI
    private data class ValidationResult(val isValid: Boolean, val errorMessage: String)
    
    private fun validateServerInput(name: String, url: String, portStr: String): ValidationResult {
        // Validate name
        if (name.isEmpty()) {
            return ValidationResult(false, "Server name cannot be empty")
        }
        if (name.length < 2) {
            return ValidationResult(false, "Server name must be at least 2 characters")
        }
        if (name.length > 50) {
            return ValidationResult(false, "Server name must be less than 50 characters")
        }
        
        // Validate URL
        if (url.isEmpty()) {
            return ValidationResult(false, "Server URL cannot be empty")
        }
        
        // Basic URL format validation
        val urlPattern = Regex("^[a-zA-Z0-9.-]+\$")
        if (!url.matches(urlPattern)) {
            return ValidationResult(false, "Invalid URL format. Use domain name or IP address only (no protocol)")
        }
        
        // Validate common invalid URLs
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ws://") || url.startsWith("wss://")) {
            return ValidationResult(false, "URL should not include protocol (http/https/ws/wss). Protocol is selected separately.")
        }
        
        // Validate port
        if (portStr.isNotEmpty()) {
            val port = portStr.toIntOrNull()
            if (port == null) {
                return ValidationResult(false, "Port must be a valid number")
            }
            if (port < 1 || port > 65535) {
                return ValidationResult(false, "Port must be between 1 and 65535")
            }
            if (port < 1024 && port != 80 && port != 443) {
                return ValidationResult(false, "Port $port is reserved. Use ports 1024+ or standard ports 80/443")
            }
        }
        
        return ValidationResult(true, "")
    }
    
    private fun isDuplicateServer(url: String, port: Int): Boolean {
        return serverManager.getAllServers().any { 
            it.url.equals(url, ignoreCase = true) && it.port == port 
        }
    }
    
    private fun showErrorDialog(title: String, message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("❌ $title")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun deleteServer(server: ServerConfig) {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Server")
            .setMessage("Are you sure you want to delete '${server.name}'?\n\n" +
                       "URL: ${server.getFullUrl()}\n" +
                       "This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                try {
                    // Show loading dialog
                    val loadingDialog = showLoadingDialog("Deleting server...")
                    
                    // Perform deletion on a background thread to avoid UI blocking
                    Thread {
                        try {
                            val deleteResult = serverManager.deleteServer(server.id)
                            
                            // Update UI on main thread
                            runOnUiThread {
                                try {
                                    loadingDialog.dismiss()
                                    
                                    if (deleteResult) {
                                        // Get completely fresh server list
                                        val updatedServers = serverManager.getAllServers()
                                        
                                        // Update adapter with new data
                                        serverAdapter.updateServers(updatedServers)
                                        
                                        // Update active server display
                                        val activeServer = serverManager.getActiveServer()
                                        serverAdapter.setActiveServer(activeServer?.id)
                                        
                                        // Force adapter to notify changes
                                        serverAdapter.notifyDataSetChanged()
                                        
                                        Toast.makeText(this@ServerManagementActivity, 
                                            "✅ Server '${server.name}' deleted successfully", 
                                            Toast.LENGTH_SHORT).show()
                                        
                                        // If no servers left, show helpful message
                                        if (updatedServers.isEmpty()) {
                                            Toast.makeText(this@ServerManagementActivity, 
                                                "💡 Add a new server to get started", 
                                                Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(this@ServerManagementActivity, 
                                            "❌ Failed to delete server - please try again", 
                                            Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@ServerManagementActivity, 
                                        "❌ Error updating UI: ${e.message}", 
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            // Handle background thread errors
                            runOnUiThread {
                                loadingDialog.dismiss()
                                Toast.makeText(this@ServerManagementActivity, 
                                    "❌ Error deleting server: ${e.message}", 
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }.start()
                } catch (e: Exception) {
                    Toast.makeText(this, "❌ Error starting delete operation: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            val cleanedUrl = cleanUrl(url)
            
            // Check for valid patterns
            val ipPattern = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
            val domainPattern = Regex("""^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$""")
            
            // Check if it's a valid IP address
            if (ipPattern.matches(cleanedUrl)) {
                val parts = cleanedUrl.split(".")
                return parts.all { it.toIntOrNull()?.let { num -> num in 0..255 } == true }
            }
            
            // Check if it's a valid domain
            if (domainPattern.matches(cleanedUrl)) {
                return cleanedUrl.length <= 253 && cleanedUrl.contains(".")
            }
            
            // Check for localhost
            if (cleanedUrl.equals("localhost", ignoreCase = true) || 
                cleanedUrl.equals("127.0.0.1", ignoreCase = true)) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun cleanUrl(url: String): String {
        return url.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("ws://")
            .removePrefix("wss://")
            .removeSuffix("/")
    }
    
    private fun showValidationError(message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("❌ Validation Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(title: String, message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun showLoadingDialog(message: String): android.app.AlertDialog {
        val loadingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 40, 40, 40)
            gravity = android.view.Gravity.CENTER
        }
        
        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 20, 0)
            }
        }
        
        val messageText = TextView(this).apply {
            text = message
            textSize = 16f
        }
        
        loadingLayout.addView(progressBar)
        loadingLayout.addView(messageText)
        
        return android.app.AlertDialog.Builder(this)
            .setView(loadingLayout)
            .setCancelable(false)
            .create().apply { show() }
    }
}
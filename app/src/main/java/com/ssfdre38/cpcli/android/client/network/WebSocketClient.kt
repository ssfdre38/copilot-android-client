package com.ssfdre38.cpcli.android.client.network

import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlinx.coroutines.*

data class WebSocketMessage(
    val type: String,
    val message: String? = null,
    val sessionId: String? = null,
    val error: String? = null
)

interface WebSocketListener {
    fun onConnected()
    fun onDisconnected()
    fun onMessageReceived(message: WebSocketMessage)
    fun onError(error: String)
}

class CopilotWebSocketClient(
    private val serverUrl: String,
    private val listener: WebSocketListener
) {
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isConnecting = false
    private var connectionAttempts = 0
    private val maxRetryAttempts = 3
    private val retryDelayMs = 2000L
    
    fun connect() {
        if (isConnecting) {
            android.util.Log.d("WebSocket", "Already attempting to connect")
            return
        }
        
        try {
            isConnecting = true
            android.util.Log.d("WebSocket", "Attempting to connect to: $serverUrl")
            
            // Validate URL format
            if (!isValidWebSocketUrl(serverUrl)) {
                listener.onError("Invalid WebSocket URL format: $serverUrl")
                isConnecting = false
                return
            }
            
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    try {
                        android.util.Log.d("WebSocket", "Connected successfully")
                        isConnecting = false
                        connectionAttempts = 0
                        scope.launch {
                            try {
                                withContext(Dispatchers.Main) {
                                    try {
                                        listener.onConnected()
                                    } catch (e: Exception) {
                                        android.util.Log.e("WebSocket", "Error in onConnected callback", e)
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("WebSocket", "Error switching to Main dispatcher in onOpen", e)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("WebSocket", "Error in onOpen", e)
                    }
                }
                
                override fun onMessage(message: String?) {
                    message?.let { 
                        try {
                            android.util.Log.d("WebSocket", "Received message: $it")
                            val wsMessage = gson.fromJson(it, WebSocketMessage::class.java)
                            scope.launch {
                                try {
                                    withContext(Dispatchers.Main) {
                                        listener.onMessageReceived(wsMessage)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("WebSocket", "Error calling onMessageReceived on Main thread", e)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WebSocket", "Error parsing message", e)
                            scope.launch {
                                try {
                                    withContext(Dispatchers.Main) {
                                        listener.onError("Failed to parse message: ${e.message}")
                                    }
                                } catch (mainError: Exception) {
                                    android.util.Log.e("WebSocket", "Error calling onError on Main thread", mainError)
                                }
                            }
                        }
                    }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    try {
                        android.util.Log.d("WebSocket", "Connection closed: code=$code, reason=$reason, remote=$remote")
                        isConnecting = false
                        
                        // Handle different close codes
                        val errorMessage = when (code) {
                            1000 -> null // Normal closure
                            1001 -> "Server is going away"
                            1002 -> "Protocol error"
                            1003 -> "Unsupported data type"
                            1006 -> "Connection lost unexpectedly"
                            1011 -> "Server error"
                            else -> "Connection closed with code: $code, reason: $reason"
                        }
                        
                        scope.launch {
                            try {
                                withContext(Dispatchers.Main) {
                                    if (errorMessage != null && remote) {
                                        // Only show error for unexpected closures
                                        listener.onError(errorMessage)
                                    } else {
                                        listener.onDisconnected()
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("WebSocket", "Error calling onDisconnected on Main thread", e)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("WebSocket", "Error in onClose", e)
                    }
                }
                
                override fun onError(ex: Exception?) {
                    try {
                        android.util.Log.e("WebSocket", "WebSocket error", ex)
                        isConnecting = false
                        
                        val errorMessage = when {
                            ex?.message?.contains("ENETUNREACH") == true -> 
                                "Network unreachable. Please check your internet connection and server address."
                            ex?.message?.contains("ECONNREFUSED") == true -> 
                                "Connection refused. The server may be offline or the port may be blocked."
                            ex?.message?.contains("timeout") == true -> 
                                "Connection timeout. The server is not responding."
                            ex?.message?.contains("SSL") == true -> 
                                "SSL/TLS error. Check if the server supports secure connections."
                            else -> "Connection failed: ${ex?.message ?: "Unknown error"}"
                        }
                        
                        scope.launch {
                            try {
                                withContext(Dispatchers.Main) {
                                    // Attempt retry for certain errors
                                    if (shouldRetry(ex) && connectionAttempts < maxRetryAttempts) {
                                        connectionAttempts++
                                        android.util.Log.d("WebSocket", "Retrying connection (attempt $connectionAttempts/$maxRetryAttempts)")
                                        
                                        // Delay before retry
                                        delay(retryDelayMs)
                                        connect()
                                    } else {
                                        listener.onError(errorMessage)
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("WebSocket", "Error calling onError on Main thread", e)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("WebSocket", "Error in onError handler", e)
                    }
                }
            }
            
            // Set connection timeout
            webSocketClient?.setConnectionLostTimeout(30) // 30 seconds
            webSocketClient?.connect()
            
        } catch (e: Exception) {
            isConnecting = false
            val errorMessage = when {
                e.message?.contains("Invalid") == true -> "Invalid server URL format"
                e.message?.contains("Unknown host") == true -> "Server not found. Check the server address."
                else -> "Failed to connect: ${e.message}"
            }
            listener.onError(errorMessage)
        }
    }
    
    private fun isValidWebSocketUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            uri.scheme in listOf("ws", "wss") && !uri.host.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun shouldRetry(exception: Exception?): Boolean {
        val message = exception?.message?.lowercase() ?: ""
        return when {
            message.contains("timeout") -> true
            message.contains("connection reset") -> true
            message.contains("network") -> true
            else -> false
        }
    }
    
    fun sendMessage(message: String, sessionId: String? = null) {
        val wsMessage = WebSocketMessage(
            type = "message",
            message = message,
            sessionId = sessionId
        )
        
        val json = gson.toJson(wsMessage)
        webSocketClient?.send(json)
    }
    
    fun sendCommand(command: String, sessionId: String? = null) {
        val wsMessage = WebSocketMessage(
            type = "command",
            message = command,
            sessionId = sessionId
        )
        
        val json = gson.toJson(wsMessage)
        webSocketClient?.send(json)
    }
    
    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
    }
    
    fun isConnected(): Boolean {
        return webSocketClient?.isOpen == true
    }
    
    fun cleanup() {
        disconnect()
        scope.cancel()
    }
}
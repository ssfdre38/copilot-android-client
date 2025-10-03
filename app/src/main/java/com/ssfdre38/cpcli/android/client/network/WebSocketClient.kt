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
    
    fun connect() {
        try {
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    try {
                        android.util.Log.d("WebSocket", "Connection opened")
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
                        android.util.Log.d("WebSocket", "Connection closed: code=$code, reason=$reason")
                        scope.launch {
                            try {
                                withContext(Dispatchers.Main) {
                                    listener.onDisconnected()
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
                        scope.launch {
                            try {
                                withContext(Dispatchers.Main) {
                                    listener.onError(ex?.message ?: "Unknown WebSocket error")
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
            
            webSocketClient?.connect()
            
        } catch (e: Exception) {
            listener.onError("Failed to connect: ${e.message}")
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
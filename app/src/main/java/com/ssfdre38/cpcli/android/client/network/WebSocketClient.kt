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
                    scope.launch(Dispatchers.Main) {
                        listener.onConnected()
                    }
                }
                
                override fun onMessage(message: String?) {
                    message?.let { 
                        try {
                            val wsMessage = gson.fromJson(it, WebSocketMessage::class.java)
                            scope.launch(Dispatchers.Main) {
                                listener.onMessageReceived(wsMessage)
                            }
                        } catch (e: Exception) {
                            scope.launch(Dispatchers.Main) {
                                listener.onError("Failed to parse message: ${e.message}")
                            }
                        }
                    }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    scope.launch(Dispatchers.Main) {
                        listener.onDisconnected()
                    }
                }
                
                override fun onError(ex: Exception?) {
                    scope.launch(Dispatchers.Main) {
                        listener.onError(ex?.message ?: "Unknown WebSocket error")
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
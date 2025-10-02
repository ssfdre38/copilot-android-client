package com.github.copilot.client.network

import android.util.Log
import com.github.copilot.client.model.CopilotRequest
import com.github.copilot.client.model.CopilotResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.TimeUnit

class CopilotWebSocketClient(private val serverUrl: String, private val apiKey: String? = null) {
    
    private val gson = Gson()
    private var webSocketClient: WebSocketClient? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _messages = MutableStateFlow<CopilotResponse?>(null)
    val messages: StateFlow<CopilotResponse?> = _messages
    
    private val _errors = MutableStateFlow<String?>(null)
    val errors: StateFlow<String?> = _errors
    
    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }
    
    fun connect() {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(TAG, "WebSocket connected to $serverUrl")
                    _connectionState.value = ConnectionState.CONNECTED
                    
                    // Send authentication if API key is provided
                    apiKey?.let { key ->
                        val authMessage = mapOf(
                            "type" to "auth",
                            "apiKey" to key
                        )
                        send(gson.toJson(authMessage))
                    }
                }
                
                override fun onMessage(message: String?) {
                    Log.d(TAG, "Received message: $message")
                    message?.let {
                        try {
                            val response = gson.fromJson(it, CopilotResponse::class.java)
                            _messages.value = response
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing message: ${e.message}")
                            // Try to handle as plain text message
                            _messages.value = CopilotResponse(
                                message = it,
                                error = null
                            )
                        }
                    }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket closed: $code - $reason")
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket error", ex)
                    _connectionState.value = ConnectionState.ERROR
                    _errors.value = ex?.message ?: "Unknown connection error"
                }
            }
            
            webSocketClient?.connectBlocking(30, TimeUnit.SECONDS)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect", e)
            _connectionState.value = ConnectionState.ERROR
            _errors.value = e.message ?: "Failed to connect to server"
        }
    }
    
    fun sendMessage(message: String, sessionId: String? = null) {
        val request = CopilotRequest(
            message = message,
            sessionId = sessionId
        )
        
        val jsonMessage = gson.toJson(request)
        webSocketClient?.send(jsonMessage)
        Log.d(TAG, "Sent message: $jsonMessage")
    }
    
    fun disconnect() {
        webSocketClient?.close()
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    fun isConnected(): Boolean {
        return _connectionState.value == ConnectionState.CONNECTED
    }
    
    companion object {
        private const val TAG = "CopilotWebSocketClient"
    }
}
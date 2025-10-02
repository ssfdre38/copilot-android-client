package com.github.copilot.client.model

import java.util.Date

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Date = Date(),
    val isTyping: Boolean = false,
    val serverId: String? = null // For multi-server support
)

enum class MessageSender {
    USER, COPILOT
}

data class ServerConfig(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val url: String,
    val apiKey: String? = null,
    val timeout: Long = 30000L,
    val isDefault: Boolean = false,
    val lastUsed: Date = Date()
) : java.io.Serializable

data class CopilotRequest(
    val message: String,
    val sessionId: String? = null,
    val context: Map<String, Any>? = null
)

data class CopilotResponse(
    val message: String,
    val sessionId: String? = null,
    val error: String? = null,
    val metadata: Map<String, Any>? = null
)
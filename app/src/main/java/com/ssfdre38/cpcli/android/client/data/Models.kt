package com.ssfdre38.cpcli.android.client.data

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val serverId: String
)

data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val port: Int,
    val isDefault: Boolean = false
)

data class AppSettings(
    val isDarkMode: Boolean = false,
    val autoCheckUpdates: Boolean = true,
    val chatHistoryLimit: Int = 1000,
    val defaultServerId: String? = null
)
package com.github.copilot.client.model

data class AppSettings(
    val isDarkMode: Boolean = false,
    val autoUpdatesEnabled: Boolean = true,
    val chatHistoryEnabled: Boolean = true,
    val maxChatHistory: Int = 1000,
    val currentServerId: String? = null,
    val lastUpdateCheck: Long = 0L
)

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isMandatory: Boolean = false,
    val releaseDate: Long = System.currentTimeMillis()
)

data class HelpItem(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val searchKeywords: List<String> = emptyList()
)

data class ChatHistory(
    val serverId: String,
    val messages: List<ChatMessage>
)
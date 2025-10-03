package com.ssfdre38.cpcli.android.client.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val serverId: String,
    val messageType: String = "text",
    val isRead: Boolean = true
) : Parcelable

@Parcelize
data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val port: Int,
    val isDefault: Boolean = false,
    val isSecure: Boolean = true,
    val lastConnected: Long = 0L,
    val description: String = ""
) : Parcelable {
    
    fun getFullUrl(): String {
        val protocol = if (isSecure) "wss" else "ws"
        return if (port == 80 || port == 443) {
            "$protocol://$url"
        } else {
            "$protocol://$url:$port"
        }
    }
    
    fun getDisplayName(): String {
        return if (description.isNotEmpty()) {
            "$name - $description"
        } else {
            name
        }
    }
}

data class AppSettings(
    val isDarkMode: Boolean = false,
    val autoCheckUpdates: Boolean = true,
    val chatHistoryLimit: Int = 1000,
    val defaultServerId: String? = null
)
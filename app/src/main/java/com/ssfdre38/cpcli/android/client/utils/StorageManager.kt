package com.ssfdre38.cpcli.android.client.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.ssfdre38.cpcli.android.client.model.ChatMessage
import com.ssfdre38.cpcli.android.client.model.ServerConfig
import com.ssfdre38.cpcli.android.client.model.AppSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class StorageManager(private val context: Context) {
    
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    
    // Chat History Management
    fun saveChatHistory(serverId: String, messages: List<ChatMessage>) {
        val key = "chat_history_$serverId"
        val json = gson.toJson(messages)
        preferences.edit().putString(key, json).apply()
    }
    
    fun loadChatHistory(serverId: String): List<ChatMessage> {
        val key = "chat_history_$serverId"
        val json = preferences.getString(key, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearChatHistory(serverId: String) {
        val key = "chat_history_$serverId"
        preferences.edit().remove(key).apply()
    }
    
    fun getAllChatHistoryKeys(): List<String> {
        return preferences.all.keys.filter { it.startsWith("chat_history_") }
    }
    
    // Server Management
    fun saveServers(servers: List<ServerConfig>) {
        val json = gson.toJson(servers)
        preferences.edit().putString("saved_servers", json).apply()
    }
    
    fun loadServers(): List<ServerConfig> {
        val json = preferences.getString("saved_servers", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ServerConfig>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveCurrentServerId(serverId: String?) {
        preferences.edit().putString("current_server_id", serverId).apply()
    }
    
    fun getCurrentServerId(): String? {
        return preferences.getString("current_server_id", null)
    }
    
    // App Settings
    fun saveAppSettings(settings: AppSettings) {
        with(preferences.edit()) {
            putBoolean("dark_mode", settings.isDarkMode)
            putBoolean("auto_updates", settings.autoUpdatesEnabled)
            putBoolean("chat_history_enabled", settings.chatHistoryEnabled)
            putInt("max_chat_history", settings.maxChatHistory)
            putString("current_server_id", settings.currentServerId)
            putLong("last_update_check", settings.lastUpdateCheck)
            apply()
        }
    }
    
    fun loadAppSettings(): AppSettings {
        return AppSettings(
            isDarkMode = preferences.getBoolean("dark_mode", false),
            autoUpdatesEnabled = preferences.getBoolean("auto_updates", true),
            chatHistoryEnabled = preferences.getBoolean("chat_history_enabled", true),
            maxChatHistory = preferences.getInt("max_chat_history", 1000),
            currentServerId = preferences.getString("current_server_id", null),
            lastUpdateCheck = preferences.getLong("last_update_check", 0L)
        )
    }
    
    // Cleanup old history based on max limit
    fun cleanupOldHistory(serverId: String, maxMessages: Int) {
        val messages = loadChatHistory(serverId)
        if (messages.size > maxMessages) {
            val recentMessages = messages.takeLast(maxMessages)
            saveChatHistory(serverId, recentMessages)
        }
    }
}
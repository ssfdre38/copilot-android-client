package com.ssfdre38.cpcli.android.client.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class StorageManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("copilot_client", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Chat History
    fun saveChatMessage(message: ChatMessage) {
        val messages = getChatHistory(message.serverId).toMutableList()
        messages.add(message)
        
        // Limit history size
        val settings = getAppSettings()
        if (messages.size > settings.chatHistoryLimit) {
            messages.removeAt(0)
        }
        
        val json = gson.toJson(messages)
        prefs.edit().putString("chat_history_${message.serverId}", json).apply()
    }
    
    fun getChatHistory(serverId: String): List<ChatMessage> {
        val json = prefs.getString("chat_history_$serverId", null) ?: return emptyList()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun clearChatHistory(serverId: String) {
        prefs.edit().remove("chat_history_$serverId").apply()
    }
    
    fun clearAllChatHistory() {
        val keys = prefs.all.keys.filter { it.startsWith("chat_history_") }
        val editor = prefs.edit()
        keys.forEach { editor.remove(it) }
        editor.apply()
    }
    
    // Server Management
    fun saveServerConfig(server: ServerConfig) {
        val servers = getAllServers().toMutableList()
        val existingIndex = servers.indexOfFirst { it.id == server.id }
        
        if (existingIndex >= 0) {
            servers[existingIndex] = server
        } else {
            servers.add(server)
        }
        
        val json = gson.toJson(servers)
        prefs.edit().putString("servers", json).apply()
    }
    
    fun getAllServers(): List<ServerConfig> {
        val json = prefs.getString("servers", null) ?: return emptyList()
        val type = object : TypeToken<List<ServerConfig>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getServerById(id: String): ServerConfig? {
        return getAllServers().find { it.id == id }
    }
    
    fun deleteServer(id: String) {
        val servers = getAllServers().filter { it.id != id }
        val json = gson.toJson(servers)
        prefs.edit().putString("servers", json).apply()
        
        // Also clear chat history for this server
        clearChatHistory(id)
    }
    
    fun getDefaultServer(): ServerConfig? {
        val settings = getAppSettings()
        return settings.defaultServerId?.let { getServerById(it) }
            ?: getAllServers().firstOrNull { it.isDefault }
    }
    
    // App Settings
    fun saveAppSettings(settings: AppSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString("app_settings", json).apply()
    }
    
    fun getAppSettings(): AppSettings {
        val json = prefs.getString("app_settings", null) ?: return AppSettings()
        return gson.fromJson(json, AppSettings::class.java) ?: AppSettings()
    }
    
    // Quick settings
    fun setDarkMode(enabled: Boolean) {
        val settings = getAppSettings().copy(isDarkMode = enabled)
        saveAppSettings(settings)
    }
    
    fun isDarkModeEnabled(): Boolean {
        return getAppSettings().isDarkMode
    }
    
    fun setAutoUpdates(enabled: Boolean) {
        val settings = getAppSettings().copy(autoCheckUpdates = enabled)
        saveAppSettings(settings)
    }
    
    fun isAutoUpdatesEnabled(): Boolean {
        return getAppSettings().autoCheckUpdates
    }
    
    fun setDefaultServer(serverId: String?) {
        val settings = getAppSettings().copy(defaultServerId = serverId)
        saveAppSettings(settings)
    }
    
    // Version tracking
    fun getLastVersionCode(): Int {
        return prefs.getInt("last_version_code", 0)
    }
    
    fun setLastVersionCode(versionCode: Int) {
        prefs.edit().putInt("last_version_code", versionCode).apply()
    }
    
    fun getLastUpdateCheck(): Long {
        return prefs.getLong("last_update_check", 0)
    }
    
    fun setLastUpdateCheck(timestamp: Long) {
        prefs.edit().putLong("last_update_check", timestamp).apply()
    }
}
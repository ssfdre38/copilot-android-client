package com.ssfdre38.cpcli.android.client.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ServerConfigManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "server_configs"
        private const val KEY_SERVERS = "servers"
        private const val KEY_ACTIVE_SERVER = "active_server"
    }
    
    fun getAllServers(): List<ServerConfig> {
        val json = preferences.getString(KEY_SERVERS, null) ?: return getDefaultServers()
        val type = object : TypeToken<List<ServerConfig>>() {}.type
        return gson.fromJson(json, type) ?: getDefaultServers()
    }
    
    fun getActiveServer(): ServerConfig? {
        val activeId = preferences.getString(KEY_ACTIVE_SERVER, null)
        return getAllServers().find { it.id == activeId }
    }
    
    fun addServer(server: ServerConfig): Boolean {
        val servers = getAllServers().toMutableList()
        
        // Check if server with same URL already exists
        if (servers.any { it.url == server.url && it.port == server.port }) {
            return false
        }
        
        servers.add(server)
        saveServers(servers)
        return true
    }
    
    fun updateServer(serverId: String, updatedServer: ServerConfig): Boolean {
        val servers = getAllServers().toMutableList()
        val index = servers.indexOfFirst { it.id == serverId }
        
        if (index != -1) {
            servers[index] = updatedServer.copy(id = serverId)
            saveServers(servers)
            return true
        }
        return false
    }
    
    fun deleteServer(serverId: String): Boolean {
        return try {
            val servers = getAllServers().toMutableList()
            val initialSize = servers.size
            
            // Find the server to delete
            val serverToDeleteIndex = servers.indexOfFirst { it.id == serverId }
            if (serverToDeleteIndex == -1) {
                return false
            }
            
            // Remove the server by index to ensure it's actually removed
            val removedServer = servers.removeAt(serverToDeleteIndex)
            
            // If deleted server was active, clear active server preference immediately
            val activeServerId = preferences.getString(KEY_ACTIVE_SERVER, null)
            if (activeServerId == serverId) {
                preferences.edit()
                    .remove(KEY_ACTIVE_SERVER)
                    .commit()
            }
            
            // Save the updated server list with immediate write
            val saveSuccess = try {
                val json = gson.toJson(servers)
                preferences.edit()
                    .putString(KEY_SERVERS, json)
                    .commit() // Use commit() for immediate persistence
                true
            } catch (e: Exception) {
                false
            }
            
            if (saveSuccess) {
                // Verify the deletion by reloading and checking
                val reloadedServers = getAllServers()
                val deletionSuccessful = reloadedServers.none { it.id == serverId } && 
                                       reloadedServers.size == (initialSize - 1)
                deletionSuccessful
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun setActiveServer(serverId: String?) {
        preferences.edit()
            .putString(KEY_ACTIVE_SERVER, serverId)
            .apply()
    }
    
    fun updateLastConnected(serverId: String) {
        val servers = getAllServers().toMutableList()
        val index = servers.indexOfFirst { it.id == serverId }
        
        if (index != -1) {
            servers[index] = servers[index].copy(lastConnected = System.currentTimeMillis())
            saveServers(servers)
        }
    }
    
    private fun saveServers(servers: List<ServerConfig>) {
        try {
            val json = gson.toJson(servers)
            preferences.edit()
                .putString(KEY_SERVERS, json)
                .commit() // Use commit() instead of apply() for immediate write
        } catch (e: Exception) {
            // If JSON serialization fails, try to save empty list
            preferences.edit()
                .putString(KEY_SERVERS, "[]")
                .commit()
            throw e
        }
    }
    
    private fun getDefaultServers(): List<ServerConfig> {
        return listOf(
            ServerConfig(
                id = UUID.randomUUID().toString(),
                name = "Local Development",
                url = "localhost",
                isSecure = false,
                port = 3002,
                description = "Local development server"
            ),
            ServerConfig(
                id = UUID.randomUUID().toString(),
                name = "Local Secure",
                url = "localhost",
                isSecure = true,
                port = 8443,
                description = "Local secure development server"
            )
        )
    }
}
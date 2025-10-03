package com.ssfdre38.cpcli.android.client.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

data class ExportData(
    val exportDate: String,
    val appVersion: String,
    val servers: List<ServerConfig>,
    val chatHistory: Map<String, List<ChatMessage>>
)

class ImportExportManager(private val context: Context) {
    private val storageManager = StorageManager(context)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    /**
     * Export all data to JSON format
     */
    fun exportAllData(): ExportData {
        val servers = storageManager.getAllServers()
        val chatHistory = mutableMapOf<String, List<ChatMessage>>()
        
        servers.forEach { server ->
            chatHistory[server.id] = storageManager.getChatHistory(server.id)
        }
        
        return ExportData(
            exportDate = dateFormat.format(Date()),
            appVersion = getAppVersion(),
            servers = servers,
            chatHistory = chatHistory
        )
    }
    
    /**
     * Export data to file URI
     */
    fun exportToFile(uri: Uri, includeHistory: Boolean = true): Boolean {
        return try {
            val exportData = if (includeHistory) {
                exportAllData()
            } else {
                ExportData(
                    exportDate = dateFormat.format(Date()),
                    appVersion = getAppVersion(),
                    servers = storageManager.getAllServers(),
                    chatHistory = emptyMap()
                )
            }
            
            val json = gson.toJson(exportData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Import data from file URI
     */
    fun importFromFile(uri: Uri): ImportResult {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return ImportResult.Error("Failed to read file")
            
            val exportData = gson.fromJson(json, ExportData::class.java)
            
            importData(exportData)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("Failed to parse import file: ${e.message}")
        }
    }
    
    /**
     * Import servers and optionally chat history
     */
    fun importData(exportData: ExportData, importHistory: Boolean = true): ImportResult {
        return try {
            var serversImported = 0
            var historyImported = 0
            var serversSkipped = 0
            
            // Import servers
            exportData.servers.forEach { server ->
                val existingServer = storageManager.getServerById(server.id)
                if (existingServer == null) {
                    storageManager.saveServerConfig(server)
                    serversImported++
                } else {
                    serversSkipped++
                }
            }
            
            // Import chat history if requested
            if (importHistory) {
                exportData.chatHistory.forEach { (serverId, messages) ->
                    // Only import if server exists
                    if (storageManager.getServerById(serverId) != null) {
                        messages.forEach { message ->
                            storageManager.saveChatMessage(message)
                            historyImported++
                        }
                    }
                }
            }
            
            ImportResult.Success(serversImported, historyImported, serversSkipped)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("Import failed: ${e.message}")
        }
    }
    
    /**
     * Generate export filename
     */
    fun generateExportFilename(): String {
        return "copilot_backup_${dateFormat.format(Date())}.json"
    }
    
    /**
     * Validate import file format
     */
    fun validateImportFile(uri: Uri): ValidationResult {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return ValidationResult.Invalid("Cannot read file")
            
            val exportData = gson.fromJson(json, ExportData::class.java)
            
            ValidationResult.Valid(
                serverCount = exportData.servers.size,
                historyCount = exportData.chatHistory.values.sumOf { it.size },
                exportDate = exportData.exportDate,
                appVersion = exportData.appVersion
            )
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid file format: ${e.message}")
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

sealed class ImportResult {
    data class Success(
        val serversImported: Int,
        val historyImported: Int,
        val serversSkipped: Int
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}

sealed class ValidationResult {
    data class Valid(
        val serverCount: Int,
        val historyCount: Int,
        val exportDate: String,
        val appVersion: String
    ) : ValidationResult()
    
    data class Invalid(val message: String) : ValidationResult()
}
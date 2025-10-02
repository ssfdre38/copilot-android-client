package com.ssfdre38.cpcli.android.client.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.ssfdre38.cpcli.android.client.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    
    private val currentVersion: String by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
    
    val updateAvailable = MutableLiveData<UpdateInfo?>()
    val checkingForUpdates = MutableLiveData<Boolean>()
    
    private val githubApiUrl = "https://api.github.com/repos/ssfdre38/copilot-android-client/releases/latest"
    
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        checkingForUpdates.postValue(true)
        
        try {
            val url = URL(githubApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            
            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                
                val jsonObject = JSONObject(response)
                val latestVersion = jsonObject.getString("tag_name").removePrefix("v")
                val releaseNotes = jsonObject.getString("body")
                val publishedAt = jsonObject.getString("published_at")
                
                // Find APK asset
                val assets = jsonObject.getJSONArray("assets")
                var downloadUrl = ""
                
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk") && !name.contains("debug")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                
                // If no release APK found, use the direct GitHub raw URL
                if (downloadUrl.isEmpty()) {
                    downloadUrl = "https://github.com/ssfdre38/copilot-android-client/raw/main/copilot-android-client.apk"
                }
                
                val updateInfo = if (isNewerVersion(latestVersion, currentVersion)) {
                    UpdateInfo(
                        version = latestVersion,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes,
                        isMandatory = false,
                        releaseDate = parseDate(publishedAt)
                    )
                } else null
                
                updateAvailable.postValue(updateInfo)
                return@withContext updateInfo
                
            } else {
                return@withContext null
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            checkingForUpdates.postValue(false)
        }
    }
    
    private fun isNewerVersion(remote: String, current: String): Boolean {
        return try {
            val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(remoteParts.size, currentParts.size)
            
            for (i in 0 until maxLength) {
                val remotePart = remoteParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                when {
                    remotePart > currentPart -> return true
                    remotePart < currentPart -> return false
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun parseDate(dateString: String): Long {
        return try {
            // GitHub API returns ISO 8601 format: 2023-12-01T10:30:00Z
            val instant = java.time.Instant.parse(dateString)
            instant.toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    fun getAppVersion(): String = currentVersion
    
    fun getDownloadIntent(downloadUrl: String): android.content.Intent {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.data = Uri.parse(downloadUrl)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }
    
    companion object {
        private const val PREF_LAST_UPDATE_CHECK = "last_update_check"
        private const val PREF_SKIP_VERSION = "skip_version"
        
        fun shouldCheckForUpdates(context: Context): Boolean {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val lastCheck = prefs.getLong(PREF_LAST_UPDATE_CHECK, 0)
            val now = System.currentTimeMillis()
            val oneDayInMs = 24 * 60 * 60 * 1000L
            
            return (now - lastCheck) > oneDayInMs
        }
        
        fun markUpdateChecked(context: Context) {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putLong(PREF_LAST_UPDATE_CHECK, System.currentTimeMillis()).apply()
        }
        
        fun setSkipVersion(context: Context, version: String) {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putString(PREF_SKIP_VERSION, version).apply()
        }
        
        fun getSkipVersion(context: Context): String? {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(PREF_SKIP_VERSION, null)
        }
    }
}
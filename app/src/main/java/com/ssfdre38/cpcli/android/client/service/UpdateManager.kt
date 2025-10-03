package com.ssfdre38.cpcli.android.client.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import com.ssfdre38.cpcli.android.client.BuildConfig

data class ReleaseInfo(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<Asset>
)

data class Asset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

class UpdateManager(private val context: Context) {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/ssfdre38/copilot-android-client/releases/latest"
        private const val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    interface UpdateListener {
        fun onUpdateAvailable(releaseInfo: ReleaseInfo)
        fun onUpdateNotAvailable()
        fun onUpdateError(error: String)
        fun onDownloadProgress(progress: Int)
        fun onDownloadComplete(file: File)
        fun onDownloadError(error: String)
    }
    
    private var listener: UpdateListener? = null
    
    fun setUpdateListener(listener: UpdateListener) {
        this.listener = listener
    }
    
    suspend fun checkForUpdates(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(GITHUB_API_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val releaseInfo = Gson().fromJson(response, ReleaseInfo::class.java)
                    
                    val currentVersion = BuildConfig.VERSION_NAME
                    val latestVersion = releaseInfo.tag_name.removePrefix("v")
                    
                    if (isNewerVersion(latestVersion, currentVersion)) {
                        withContext(Dispatchers.Main) {
                            listener?.onUpdateAvailable(releaseInfo)
                        }
                        true
                    } else {
                        withContext(Dispatchers.Main) {
                            listener?.onUpdateNotAvailable()
                        }
                        false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        listener?.onUpdateError("Server returned: $responseCode")
                    }
                    false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listener?.onUpdateError("Check failed: ${e.message}")
                }
                false
            }
        }
    }
    
    suspend fun downloadUpdate(releaseInfo: ReleaseInfo) {
        withContext(Dispatchers.IO) {
            try {
                val apkAsset = releaseInfo.assets.find { it.name.endsWith(".apk") }
                if (apkAsset == null) {
                    withContext(Dispatchers.Main) {
                        listener?.onDownloadError("No APK found in release")
                    }
                    return@withContext
                }
                
                val downloadUrl = apkAsset.browser_download_url
                val fileName = "copilot-update-${releaseInfo.tag_name}.apk"
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadDir, fileName)
                
                val connection = URL(downloadUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                
                val fileLength = connection.contentLength
                val input = connection.inputStream
                val output = FileOutputStream(file)
                
                val buffer = ByteArray(4096)
                var totalBytes = 0
                var bytes: Int
                
                while (input.read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    totalBytes += bytes
                    
                    if (fileLength > 0) {
                        val progress = (totalBytes * 100 / fileLength)
                        withContext(Dispatchers.Main) {
                            listener?.onDownloadProgress(progress)
                        }
                    }
                }
                
                output.close()
                input.close()
                
                withContext(Dispatchers.Main) {
                    listener?.onDownloadComplete(file)
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listener?.onDownloadError("Download failed: ${e.message}")
                }
            }
        }
    }
    
    fun installUpdate(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            listener?.onDownloadError("Installation failed: ${e.message}")
        }
    }
    
    private fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").map { it.toInt() }
            val currentParts = current.split(".").map { it.toInt() }
            
            for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
                val latestPart = latestParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                when {
                    latestPart > currentPart -> return true
                    latestPart < currentPart -> return false
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
    
    fun saveLastUpdateCheck() {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_update_check", System.currentTimeMillis()).apply()
    }
    
    fun shouldCheckForUpdates(): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0)
        return System.currentTimeMillis() - lastCheck > UPDATE_CHECK_INTERVAL
    }
    
    fun isAutoUpdateEnabled(): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_update_enabled", true)
    }
    
    fun setAutoUpdateEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_update_enabled", enabled).apply()
    }
}
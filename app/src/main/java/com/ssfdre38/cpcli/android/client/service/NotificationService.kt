package com.ssfdre38.cpcli.android.client.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ssfdre38.cpcli.android.client.R
import com.ssfdre38.cpcli.android.client.MainActivity
import com.ssfdre38.cpcli.android.client.data.StorageManager
import com.ssfdre38.cpcli.android.client.network.CopilotWebSocketClient
import com.ssfdre38.cpcli.android.client.network.WebSocketListener
import com.ssfdre38.cpcli.android.client.network.WebSocketMessage
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import kotlinx.coroutines.*

class NotificationService : Service() {
    companion object {
        const val CHANNEL_ID = "copilot_notifications"
        const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val MESSAGE_NOTIFICATION_ID = 1002
        
        fun startService(context: Context) {
            val intent = Intent(context, NotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, NotificationService::class.java)
            context.stopService(intent)
        }
    }
    
    private lateinit var storageManager: StorageManager
    private var serviceJob: Job? = null
    private var webSocketClient: CopilotWebSocketClient? = null
    private var currentServerName: String? = null
    
    override fun onCreate() {
        super.onCreate()
        storageManager = StorageManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
        
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            monitorForMessages()
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        serviceJob?.cancel()
        webSocketClient?.disconnect()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Copilot Notifications"
            val descriptionText = "Notifications for Copilot CLI responses"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Copilot CLI Running")
            .setContentText("Monitoring for messages in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private suspend fun monitorForMessages() {
        try {
            val defaultServer = storageManager.getDefaultServer()
            if (defaultServer != null) {
                currentServerName = defaultServer.name
                connectToServer(defaultServer.url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun connectToServer(serverUrl: String) {
        try {
            webSocketClient = CopilotWebSocketClient(serverUrl, object : WebSocketListener {
                override fun onConnected() {
                    // Connection established
                }
                
                override fun onDisconnected() {
                    // Connection lost
                }
                
                override fun onMessageReceived(message: WebSocketMessage) {
                    message.message?.let { content ->
                        handleIncomingMessage(content)
                    }
                }
                
                override fun onError(error: String) {
                    // Handle error
                }
            })
            webSocketClient?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleIncomingMessage(message: String) {
        // Only show notification if app is not in foreground
        if (!isAppInForeground()) {
            showMessageNotification(message)
        }
        
        // Save message to database
        currentServerName?.let { serverName ->
            val serverConfig = storageManager.getAllServers().find { it.name == serverName }
            serverConfig?.let { server ->
                val chatMessage = ChatMessage(
                    id = "notification_${System.currentTimeMillis()}",
                    content = message,
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    serverId = server.id
                )
                storageManager.saveChatMessage(chatMessage)
            }
        }
    }
    
    private fun showMessageNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val truncatedMessage = if (message.length > 100) {
            message.substring(0, 97) + "..."
        } else {
            message
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New Copilot Response")
            .setContentText(truncatedMessage)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        with(NotificationManagerCompat.from(this)) {
            notify(MESSAGE_NOTIFICATION_ID, notification)
        }
    }
    
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        
        return runningAppProcesses.any { processInfo ->
            processInfo.processName == packageName && 
            processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }
}
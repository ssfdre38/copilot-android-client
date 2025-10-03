package com.ssfdre38.cpcli.android.client.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import java.net.*
import java.util.concurrent.ConcurrentHashMap

data class DiscoveredServer(
    val name: String,
    val host: String,
    val port: Int,
    val responseTime: Long
)

class ServerDiscovery(private val context: Context) {
    private val TAG = "ServerDiscovery"
    private val commonPorts = listOf(3001, 3000, 8080, 8000, 3333, 8888)
    private val discoveredServers = ConcurrentHashMap<String, DiscoveredServer>()
    
    interface DiscoveryListener {
        fun onServerFound(server: DiscoveredServer)
        fun onDiscoveryComplete(servers: List<DiscoveredServer>)
        fun onDiscoveryError(error: String)
    }
    
    private var listener: DiscoveryListener? = null
    private var discoveryJob: Job? = null
    
    fun setDiscoveryListener(listener: DiscoveryListener) {
        this.listener = listener
    }
    
    fun startDiscovery() {
        discoveryJob?.cancel()
        discoveredServers.clear()
        
        discoveryJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val networkInfo = getNetworkInfo()
                if (networkInfo != null) {
                    scanNetwork(networkInfo.subnet, networkInfo.mask)
                } else {
                    withContext(Dispatchers.Main) {
                        listener?.onDiscoveryError("Unable to determine network subnet")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Discovery error", e)
                withContext(Dispatchers.Main) {
                    listener?.onDiscoveryError("Discovery failed: ${e.message}")
                }
            }
        }
    }
    
    fun stopDiscovery() {
        discoveryJob?.cancel()
    }
    
    private data class NetworkInfo(val subnet: String, val mask: Int)
    
    private fun getNetworkInfo(): NetworkInfo? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            if (ipAddress == 0) return null
            
            val ip = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            
            // Most common subnet mask for home networks
            val subnetMask = 24
            val subnet = ip.substringBeforeLast(".") + ".0"
            
            return NetworkInfo(subnet, subnetMask)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get network info", e)
            return null
        }
    }
    
    private suspend fun scanNetwork(subnet: String, mask: Int) {
        val baseIp = subnet.substringBeforeLast(".")
        val startIp = 1
        val endIp = if (mask == 24) 254 else 100 // Limit scan range
        
        val jobs = mutableListOf<Job>()
        
        for (ip in startIp..endIp) {
            val targetIp = "$baseIp.$ip"
            
            val job = CoroutineScope(Dispatchers.IO).launch {
                scanHost(targetIp)
            }
            jobs.add(job)
            
            // Limit concurrent connections
            if (jobs.size >= 20) {
                jobs.joinAll()
                jobs.clear()
            }
        }
        
        // Wait for remaining jobs
        jobs.joinAll()
        
        // Discovery complete
        withContext(Dispatchers.Main) {
            listener?.onDiscoveryComplete(discoveredServers.values.toList())
        }
    }
    
    private suspend fun scanHost(host: String) {
        for (port in commonPorts) {
            try {
                val startTime = System.currentTimeMillis()
                
                withTimeout(2000) { // 2 second timeout per connection
                    val socket = Socket()
                    socket.connect(InetSocketAddress(host, port), 1000)
                    socket.close()
                    
                    val responseTime = System.currentTimeMillis() - startTime
                    
                    // Try to get server info
                    val serverName = getServerName(host, port)
                    val server = DiscoveredServer(serverName, host, port, responseTime)
                    
                    discoveredServers["$host:$port"] = server
                    
                    withContext(Dispatchers.Main) {
                        listener?.onServerFound(server)
                    }
                }
            } catch (e: Exception) {
                // Host/port not reachable, continue scanning
            }
        }
    }
    
    private suspend fun getServerName(host: String, port: Int): String {
        return try {
            // Try to connect and get basic info
            withTimeout(1000) {
                // Simple HTTP check
                val url = URL("http://$host:$port/health")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.requestMethod = "GET"
                
                if (connection.responseCode == 200) {
                    "Copilot CLI Server"
                } else {
                    "Server ($host:$port)"
                }
            }
        } catch (e: Exception) {
            "Unknown Server ($host:$port)"
        }
    }
}
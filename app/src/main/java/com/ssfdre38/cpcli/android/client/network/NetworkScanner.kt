package com.ssfdre38.cpcli.android.client.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

class NetworkScanner {
    
    suspend fun scanForServers(ports: List<Int> = listOf(3001, 8080, 3000, 8000)): List<String> = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress()
        val network = getNetworkPrefix(localIp)
        val servers = mutableListOf<String>()
        
        // Scan common IP ranges in the local network
        for (i in 1..254) {
            val ip = "$network.$i"
            for (port in ports) {
                if (isPortOpen(ip, port, 1000)) {
                    // Test if it's actually a WebSocket server by checking health endpoint
                    if (testWebSocketServer(ip, port)) {
                        servers.add("ws://$ip:$port")
                    }
                }
            }
        }
        
        servers
    }
    
    private fun getLocalIpAddress(): String {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 80))
            val localAddress = socket.localAddress.hostAddress
            socket.close()
            return localAddress ?: "192.168.1.1"
        } catch (e: Exception) {
            return "192.168.1.1"
        }
    }
    
    private fun getNetworkPrefix(ip: String): String {
        val parts = ip.split(".")
        return "${parts[0]}.${parts[1]}.${parts[2]}"
    }
    
    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), timeoutMs)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun testWebSocketServer(ip: String, port: Int): Boolean {
        // Simple test to see if it responds to HTTP health check
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 2000)
            val output = socket.getOutputStream()
            output.write("GET /health HTTP/1.1\r\nHost: $ip:$port\r\n\r\n".toByteArray())
            
            val input = socket.getInputStream()
            val response = ByteArray(1024)
            val bytesRead = input.read(response)
            socket.close()
            
            bytesRead > 0 && String(response, 0, bytesRead).contains("200")
        } catch (e: Exception) {
            false
        }
    }
}
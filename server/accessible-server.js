const WebSocket = require('ws');
const express = require('express');
const https = require('https');
const http = require('http');
const fs = require('fs');

class AccessibleCopilotServer {
    constructor() {
        this.app = express();
        this.clients = new Map();
        
        this.setupExpress();
        this.startServers();
    }
    
    setupExpress() {
        this.app.use(express.json());
        
        // CORS headers
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', '*');
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
            next();
        });
        
        // Health check
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'ok',
                timestamp: new Date().toISOString(),
                connections: this.clients.size,
                version: '3.3.0',
                integration: 'accessible-copilot',
                servers: {
                    httpLocal: 'http://s1.pepperbacks.xyz:3002',
                    httpsLocal: 'https://s1.pepperbacks.xyz:8443',
                    httpExternal: 'http://54.37.254.74:3002',
                    httpsExternal: 'https://54.37.254.74:8443'
                },
                websockets: {
                    wsLocal: 'ws://s1.pepperbacks.xyz:3002',
                    wssLocal: 'wss://s1.pepperbacks.xyz:8443',
                    wsExternal: 'ws://54.37.254.74:3002',
                    wssExternal: 'wss://54.37.254.74:8443'
                }
            });
        });
        
        // Sessions endpoint
        this.app.get('/sessions', (req, res) => {
            const sessions = Array.from(this.clients.values()).map(client => ({
                clientId: client.id,
                type: client.type,
                connected: client.connected
            }));
            
            res.json({
                totalSessions: sessions.length,
                sessions: sessions
            });
        });
    }
    
    startServers() {
        // HTTP Server on port 3002
        const httpServer = http.createServer(this.app);
        httpServer.listen(3002, '0.0.0.0', () => {
            console.log('🌐 HTTP Server listening on 0.0.0.0:3002');
            console.log('📡 External: http://54.37.254.74:3002');
            console.log('📡 Local: http://s1.pepperbacks.xyz:3002');
        });
        
        const httpWss = new WebSocket.Server({ server: httpServer });
        this.setupWebSocketHandlers(httpWss, 'HTTP');
        
        // HTTPS Server on port 8443
        try {
            const sslOptions = {
                key: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/privkey.pem'),
                cert: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/fullchain.pem')
            };
            
            const httpsServer = https.createServer(sslOptions, this.app);
            httpsServer.listen(8443, '0.0.0.0', () => {
                console.log('🔒 HTTPS Server listening on 0.0.0.0:8443');
                console.log('📡 External: https://54.37.254.74:8443');
                console.log('📡 Local: https://s1.pepperbacks.xyz:8443');
                console.log('🔍 Health check: https://54.37.254.74:8443/health');
            });
            
            const httpsWss = new WebSocket.Server({ server: httpsServer });
            this.setupWebSocketHandlers(httpsWss, 'HTTPS');
            
        } catch (error) {
            console.error('❌ Failed to start HTTPS server:', error.message);
            console.log('📝 Falling back to HTTP only...');
        }
    }
    
    setupWebSocketHandlers(wss, serverType) {
        wss.on('connection', (ws, req) => {
            const clientId = `${serverType}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
            console.log(`✅ New ${serverType} client connected: ${clientId}`);
            console.log(`   From: ${req.socket.remoteAddress}:${req.socket.remotePort}`);
            
            this.clients.set(clientId, {
                id: clientId,
                ws: ws,
                type: serverType,
                connected: new Date().toISOString(),
                remoteAddress: req.socket.remoteAddress
            });
            
            // Send welcome message
            ws.send(JSON.stringify({
                type: 'welcome',
                message: `🎉 **${serverType} WebSocket Connected Successfully!**\\n\\nYour connection is working! Server: ${serverType}\\nClient ID: ${clientId}`,
                clientId: clientId,
                serverType: serverType,
                ssl: serverType === 'HTTPS',
                timestamp: new Date().toISOString()
            }));
            
            ws.on('message', (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    console.log(`📨 ${serverType} message from ${clientId}: ${message.message}`);
                    
                    // Echo back the message
                    ws.send(JSON.stringify({
                        type: 'response',
                        message: `✅ **${serverType} Echo**: ${message.message}\\n\\nMessage received successfully! Connection is working perfectly.`,
                        clientId: clientId,
                        serverType: serverType,
                        ssl: serverType === 'HTTPS',
                        timestamp: new Date().toISOString()
                    }));
                    
                } catch (error) {
                    console.error(`❌ ${serverType} message error:`, error);
                    ws.send(JSON.stringify({
                        type: 'error',
                        error: 'Invalid message format',
                        serverType: serverType
                    }));
                }
            });
            
            ws.on('close', () => {
                console.log(`👋 ${serverType} client disconnected: ${clientId}`);
                this.clients.delete(clientId);
            });
            
            ws.on('error', (error) => {
                console.error(`💥 ${serverType} WebSocket error for ${clientId}:`, error);
                this.clients.delete(clientId);
            });
        });
    }
}

console.log('🚀 Starting Accessible Copilot WebSocket Server...');
console.log('🌐 This server will be accessible both locally and externally');
console.log('');

new AccessibleCopilotServer();
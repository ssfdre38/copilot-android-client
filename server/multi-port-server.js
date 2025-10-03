const WebSocket = require('ws');
const express = require('express');
const https = require('https');
const http = require('http');
const fs = require('fs');

class MultiPortCopilotServer {
    constructor() {
        this.app = express();
        this.clients = new Map();
        
        this.setupExpress();
        this.startMultipleServers();
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
                version: '3.2.0',
                integration: 'multi-port-copilot',
                ports: {
                    http: 3002,
                    https: 8443,
                    standardHttps: 443
                },
                endpoints: [
                    'ws://s1.pepperbacks.xyz:3002',
                    'wss://s1.pepperbacks.xyz:8443',
                    'wss://s1.pepperbacks.xyz',
                    'wss://s1.pepperbacks.xyz:443'
                ]
            });
        });
    }
    
    startMultipleServers() {
        // 1. HTTP WebSocket Server (port 3002)
        const httpServer = http.createServer(this.app);
        httpServer.listen(3002, '0.0.0.0', () => {
            console.log('🌐 HTTP WebSocket Server listening on port 3002');
            console.log('📡 WebSocket endpoint: ws://s1.pepperbacks.xyz:3002');
        });
        
        const httpWss = new WebSocket.Server({ server: httpServer });
        this.setupWebSocketHandlers(httpWss, 'HTTP');
        
        // 2. HTTPS WebSocket Server (port 8443)
        try {
            const sslOptions = {
                key: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/privkey.pem'),
                cert: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/fullchain.pem')
            };
            
            const httpsServer = https.createServer(sslOptions, this.app);
            httpsServer.listen(8443, '0.0.0.0', () => {
                console.log('🔒 HTTPS WebSocket Server listening on port 8443');
                console.log('📡 Secure WebSocket endpoint: wss://s1.pepperbacks.xyz:8443');
            });
            
            const httpsWss = new WebSocket.Server({ server: httpsServer });
            this.setupWebSocketHandlers(httpsWss, 'HTTPS-8443');
            
        } catch (error) {
            console.error('❌ Failed to start HTTPS server on 8443:', error.message);
        }
        
        // 3. Standard HTTPS WebSocket Server (port 443) - if not used by nginx
        try {
            const sslOptions443 = {
                key: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/privkey.pem'),
                cert: fs.readFileSync('/etc/letsencrypt/live/s1.pepperbacks.xyz/fullchain.pem')
            };
            
            const express443 = express();
            express443.use(this.app);
            
            const https443Server = https.createServer(sslOptions443, express443);
            https443Server.listen(8080, '0.0.0.0', () => {
                console.log('🔒 HTTPS WebSocket Server listening on port 8080');
                console.log('📡 Secure WebSocket endpoint: wss://s1.pepperbacks.xyz:8080');
            });
            
            const https443Wss = new WebSocket.Server({ server: https443Server });
            this.setupWebSocketHandlers(https443Wss, 'HTTPS-8080');
            
        } catch (error) {
            console.error('❌ Failed to start HTTPS server on 8080:', error.message);
        }
    }
    
    setupWebSocketHandlers(wss, serverType) {
        wss.on('connection', (ws, req) => {
            const clientId = `${serverType}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
            console.log(`✅ New ${serverType} client connected: ${clientId}`);
            
            this.clients.set(clientId, {
                ws: ws,
                type: serverType,
                connected: new Date().toISOString()
            });
            
            // Send welcome message
            ws.send(JSON.stringify({
                type: 'welcome',
                message: `🎉 **Connected to ${serverType} WebSocket Server!**\\n\\nConnection successful via ${serverType}. Your WebSocket connection is working!`,
                clientId: clientId,
                serverType: serverType,
                ssl: serverType.includes('HTTPS'),
                timestamp: new Date().toISOString()
            }));
            
            ws.on('message', (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    console.log(`📨 ${serverType} message from ${clientId}: ${message.message}`);
                    
                    // Echo back the message
                    ws.send(JSON.stringify({
                        type: 'response',
                        message: `✅ **${serverType} Echo**: ${message.message}\\n\\nMessage received successfully via ${serverType} connection!`,
                        clientId: clientId,
                        serverType: serverType,
                        ssl: serverType.includes('HTTPS'),
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

console.log('🚀 Starting Multi-Port Copilot WebSocket Server...');
console.log('🔧 This server will listen on multiple ports for maximum compatibility:');
console.log('   - HTTP WebSocket: port 3002');
console.log('   - HTTPS WebSocket: port 8443');
console.log('   - HTTPS WebSocket: port 8080');
console.log('');

new MultiPortCopilotServer();
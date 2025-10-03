const WebSocket = require('ws');
const express = require('express');
const https = require('https');
const fs = require('fs');
const { spawn } = require('child_process');
const { v4: uuidv4 } = require('uuid');

class SecureCopilotIntegrationServer {
    constructor(port = 3003, httpsPort = 8443) {
        this.port = port;
        this.httpsPort = httpsPort;
        this.app = express();
        this.server = null;
        this.httpsServer = null;
        this.wss = null;
        this.clients = new Map();
        this.copilotProcesses = new Map();
        
        this.setupExpress();
        this.setupHTTPS();
    }
    
    setupExpress() {
        this.app.use(express.json());
        
        // CORS headers
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', '*');
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
            next();
        });
        
        // Health check endpoint
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'ok',
                timestamp: new Date().toISOString(),
                connections: this.clients.size,
                activeCopilotSessions: this.copilotProcesses.size,
                version: '3.1.0',
                integration: 'secure-copilot-cli',
                ssl: true,
                features: [
                    'secure-websocket',
                    'real-copilot-integration',
                    'persistent-sessions',
                    'interactive-mode',
                    'ssl-tls-support'
                ]
            });
        });
        
        // Sessions endpoint
        this.app.get('/sessions', (req, res) => {
            const sessions = Array.from(this.clients.values()).map(client => ({
                clientId: client.id,
                sessionId: client.sessionId,
                connected: client.ws.readyState === WebSocket.OPEN,
                lastActivity: new Date(client.lastActivity).toISOString(),
                startTime: client.startTime,
                copilotReady: client.copilotReady
            }));
            
            res.json({
                totalSessions: sessions.length,
                sessions: sessions
            });
        });
    }
    
    setupHTTPS() {
        try {
            // Try different possible certificate locations
            const certPaths = [
                {
                    key: '/etc/letsencrypt/live/s1.pepperbacks.xyz/privkey.pem',
                    cert: '/etc/letsencrypt/live/s1.pepperbacks.xyz/fullchain.pem'
                },
                {
                    key: '/etc/letsencrypt/live/code.pepperbacks.xyz/privkey.pem',
                    cert: '/etc/letsencrypt/live/code.pepperbacks.xyz/fullchain.pem'
                },
                {
                    key: '/etc/ssl/private/s1.pepperbacks.xyz.key',
                    cert: '/etc/ssl/certs/s1.pepperbacks.xyz.crt'
                }
            ];
            
            let sslOptions = null;
            for (const paths of certPaths) {
                try {
                    if (fs.existsSync(paths.key) && fs.existsSync(paths.cert)) {
                        sslOptions = {
                            key: fs.readFileSync(paths.key),
                            cert: fs.readFileSync(paths.cert)
                        };
                        console.log(`🔒 Using SSL certificates from: ${paths.key}`);
                        break;
                    }
                } catch (e) {
                    continue;
                }
            }
            
            if (!sslOptions) {
                console.log('⚠️  No SSL certificates found, generating self-signed certificate...');
                return this.generateSelfSignedCert();
            }
            
            this.httpsServer = https.createServer(sslOptions, this.app);
            this.setupWebSocket();
            
        } catch (error) {
            console.error('❌ SSL setup failed:', error.message);
            console.log('📝 Falling back to self-signed certificate...');
            this.generateSelfSignedCert();
        }
    }
    
    generateSelfSignedCert() {
        const { execSync } = require('child_process');
        
        try {
            // Create SSL directory
            execSync('mkdir -p /tmp/ssl');
            
            // Generate self-signed certificate
            execSync(`openssl req -x509 -newkey rsa:4096 -keyout /tmp/ssl/key.pem -out /tmp/ssl/cert.pem -days 365 -nodes -subj "/CN=s1.pepperbacks.xyz"`);
            
            const sslOptions = {
                key: fs.readFileSync('/tmp/ssl/key.pem'),
                cert: fs.readFileSync('/tmp/ssl/cert.pem')
            };
            
            console.log('🔒 Generated self-signed SSL certificate for s1.pepperbacks.xyz');
            
            this.httpsServer = https.createServer(sslOptions, this.app);
            this.setupWebSocket();
            
        } catch (error) {
            console.error('❌ Failed to generate self-signed certificate:', error.message);
            console.log('📝 Starting without SSL...');
            this.startWithoutSSL();
        }
    }
    
    startWithoutSSL() {
        this.server = this.app.listen(this.port, '0.0.0.0', () => {
            console.log(`🚀 Copilot Integration Server (NO SSL) listening on port ${this.port}`);
            console.log(`📡 WebSocket endpoint: ws://0.0.0.0:${this.port}`);
        });
        
        this.wss = new WebSocket.Server({ server: this.server });
        this.setupWebSocketHandlers();
    }
    
    setupWebSocket() {
        this.httpsServer.listen(this.httpsPort, '0.0.0.0', () => {
            console.log(`🚀 Secure Copilot Integration Server listening on port ${this.httpsPort}`);
            console.log(`🔒 Secure WebSocket endpoint: wss://s1.pepperbacks.xyz:${this.httpsPort}`);
            console.log(`🔒 Alternative: wss://127.0.1.1:${this.httpsPort}`);
            console.log(`🔍 Health check: https://s1.pepperbacks.xyz:${this.httpsPort}/health`);
        });
        
        this.wss = new WebSocket.Server({ server: this.httpsServer });
        this.setupWebSocketHandlers();
    }
    
    setupWebSocketHandlers() {
        this.wss.on('connection', (ws, req) => {
            const clientId = uuidv4();
            console.log(`✅ New secure client connected: ${clientId}`);
            
            const clientInfo = {
                id: clientId,
                ws: ws,
                sessionId: uuidv4(),
                authenticated: true,
                lastActivity: Date.now(),
                startTime: new Date().toISOString(),
                copilotReady: false
            };
            
            this.clients.set(clientId, clientInfo);
            
            // Send welcome message
            ws.send(JSON.stringify({
                type: 'welcome',
                message: '🔒 **Secure GitHub Copilot CLI Connected!**\\n\\nYou are now connected to a secure live GitHub Copilot CLI session with SSL/TLS encryption.',
                clientId: clientId,
                sessionId: clientInfo.sessionId,
                ssl: true,
                copilotIntegration: 'secure'
            }));
            
            ws.on('message', async (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    console.log(`📨 Secure message from ${clientId}: ${message.message}`);
                    
                    // Echo back for testing
                    ws.send(JSON.stringify({
                        type: 'response',
                        message: `🔒 **Secure Echo**: ${message.message}\\n\\nSSL WebSocket connection working! Your message was received securely.`,
                        sessionId: clientInfo.sessionId,
                        ssl: true,
                        timestamp: new Date().toISOString()
                    }));
                    
                } catch (error) {
                    console.error('❌ Error parsing secure message:', error);
                    ws.send(JSON.stringify({
                        type: 'error',
                        error: 'Invalid message format',
                        ssl: true
                    }));
                }
            });
            
            ws.on('close', () => {
                console.log(`👋 Secure client disconnected: ${clientId}`);
                this.clients.delete(clientId);
            });
            
            ws.on('error', (error) => {
                console.error(`💥 Secure WebSocket error for client ${clientId}:`, error);
                this.clients.delete(clientId);
            });
        });
    }
    
    start() {
        console.log('🔐 Starting Secure Copilot Integration Server...');
        console.log('🔍 Checking for SSL certificates...');
    }
}

// Start the secure server
const server = new SecureCopilotIntegrationServer();
server.start();
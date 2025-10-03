const WebSocket = require('ws');
const express = require('express');
const { spawn } = require('child_process');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

class RealCopilotIntegrationServer {
    constructor(port = 3002) {
        this.port = port;
        this.app = express();
        this.server = null;
        this.wss = null;
        this.clients = new Map();
        this.copilotProcesses = new Map(); // Map of clientId -> copilot process
        
        this.setupExpress();
        this.setupWebSocket();
        this.verifyCopilotAuth();
    }
    
    async verifyCopilotAuth() {
        try {
            console.log('🔍 Checking GitHub Copilot authentication...');
            
            // Check if user is authenticated
            const authCheck = spawn('copilot', ['/user', 'show'], { 
                stdio: ['pipe', 'pipe', 'pipe'],
                env: { ...process.env }
            });
            
            let output = '';
            authCheck.stdout.on('data', (data) => {
                output += data.toString();
            });
            
            authCheck.on('close', (code) => {
                if (code === 0 && output.includes('@')) {
                    console.log('✅ GitHub Copilot is authenticated and ready');
                    console.log('👤 Authenticated user detected in output');
                } else {
                    console.log('⚠️  GitHub Copilot authentication may be required');
                    console.log('📋 Please run: copilot /login');
                }
            });
            
        } catch (error) {
            console.log('❌ Failed to check Copilot auth:', error.message);
        }
    }
    
    setupExpress() {
        this.app.use(express.json());
        
        // Enhanced health check
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'ok',
                timestamp: new Date().toISOString(),
                connections: this.clients.size,
                activeCopilotSessions: this.copilotProcesses.size,
                version: '3.0.0',
                integration: 'real-copilot-cli',
                features: [
                    'real-copilot-integration',
                    'persistent-sessions', 
                    'interactive-mode',
                    'tool-permissions',
                    'context-awareness'
                ]
            });
        });
        
        // Copilot CLI status
        this.app.get('/copilot/status', async (req, res) => {
            try {
                const status = await this.getCopilotStatus();
                res.json(status);
            } catch (error) {
                res.status(500).json({ error: error.message });
            }
        });
        
        // Active sessions info
        this.app.get('/sessions', (req, res) => {
            const sessions = Array.from(this.copilotProcesses.keys()).map(clientId => ({
                clientId,
                connected: this.clients.has(clientId),
                startTime: this.clients.get(clientId)?.startTime || 'unknown'
            }));
            
            res.json({
                totalSessions: sessions.length,
                sessions: sessions
            });
        });
        
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', '*');
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
            next();
        });
    }
    
    async getCopilotStatus() {
        return new Promise((resolve) => {
            const statusProcess = spawn('copilot', ['/session'], {
                stdio: ['pipe', 'pipe', 'pipe'],
                timeout: 5000
            });
            
            let output = '';
            let errorOutput = '';
            
            statusProcess.stdout.on('data', (data) => {
                output += data.toString();
            });
            
            statusProcess.stderr.on('data', (data) => {
                errorOutput += data.toString();
            });
            
            statusProcess.on('close', (code) => {
                resolve({
                    available: code === 0,
                    authenticated: output.includes('User:') || output.includes('@'),
                    output: output || errorOutput,
                    version: 'GitHub Copilot CLI',
                    features: ['interactive-chat', 'code-generation', 'explanations', 'debugging']
                });
            });
            
            statusProcess.on('error', () => {
                resolve({
                    available: false,
                    error: 'Copilot CLI not found or not accessible'
                });
            });
        });
    }
    
    setupWebSocket() {
        this.server = this.app.listen(this.port, '0.0.0.0', () => {
            console.log(`🚀 Real Copilot Integration Server listening on port ${this.port}`);
            console.log(`📡 WebSocket endpoint: ws://0.0.0.0:${this.port}`);
            console.log(`🔍 Health check: http://0.0.0.0:${this.port}/health`);
            console.log(`🤖 Copilot status: http://0.0.0.0:${this.port}/copilot/status`);
            console.log(`📊 Sessions info: http://0.0.0.0:${this.port}/sessions`);
        });
        
        this.wss = new WebSocket.Server({ server: this.server });
        
        this.wss.on('connection', (ws, req) => {
            const clientId = uuidv4();
            console.log(`✅ New client connected: ${clientId}`);
            
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
            
            // Initialize Copilot session for this client
            this.initializeCopilotSession(clientId);
            
            ws.on('message', async (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    await this.handleMessage(clientId, message);
                } catch (error) {
                    console.error('❌ Error parsing message:', error);
                    this.sendError(clientId, 'Invalid message format');
                }
            });
            
            ws.on('close', () => {
                console.log(`👋 Client disconnected: ${clientId}`);
                this.cleanupClient(clientId);
            });
            
            ws.on('error', (error) => {
                console.error(`💥 WebSocket error for client ${clientId}:`, error);
                this.cleanupClient(clientId);
            });
        });
    }
    
    async initializeCopilotSession(clientId) {
        try {
            console.log(`🤖 Initializing Copilot session for client: ${clientId}`);
            
            // Start Copilot in interactive mode with correct options
            const copilotProcess = spawn('copilot', [], {
                stdio: ['pipe', 'pipe', 'pipe'],
                env: { 
                    ...process.env,
                    COPILOT_ALLOW_ALL: 'true'
                }
            });
            
            this.copilotProcesses.set(clientId, copilotProcess);
            
            let buffer = '';
            
            copilotProcess.stdout.on('data', (data) => {
                const output = data.toString();
                buffer += output;
                
                // Process complete responses
                if (buffer.includes('\n') || buffer.includes('> ')) {
                    const lines = buffer.split('\n');
                    buffer = lines.pop() || ''; // Keep incomplete line in buffer
                    
                    for (const line of lines) {
                        if (line.trim()) {
                            this.sendCopilotResponse(clientId, line.trim());
                        }
                    }
                }
            });
            
            copilotProcess.stderr.on('data', (data) => {
                const error = data.toString();
                console.log(`🔍 Copilot stderr for ${clientId}:`, error);
                
                // Don't send auth prompts or setup messages as errors
                if (!error.includes('login') && !error.includes('auth') && error.trim()) {
                    this.sendError(clientId, `Copilot: ${error.trim()}`);
                }
            });
            
            copilotProcess.on('close', (code) => {
                console.log(`🔚 Copilot process closed for ${clientId} with code ${code}`);
                this.copilotProcesses.delete(clientId);
                
                const client = this.clients.get(clientId);
                if (client) {
                    this.sendToClient(clientId, {
                        type: 'copilot_disconnected',
                        message: 'Copilot session ended. You can continue with enhanced responses.',
                        code: code
                    });
                }
            });
            
            copilotProcess.on('error', (error) => {
                console.error(`💥 Copilot process error for ${clientId}:`, error);
                this.sendError(clientId, `Failed to start Copilot: ${error.message}`);
            });
            
            // Send welcome message once Copilot is ready
            setTimeout(() => {
                const client = this.clients.get(clientId);
                if (client) {
                    client.copilotReady = true;
                    this.sendToClient(clientId, {
                        type: 'welcome',
                        message: '🤖 **Real GitHub Copilot CLI Connected!**\n\nYou are now connected to a live GitHub Copilot CLI session. You can:\n\n• Ask coding questions and get real AI assistance\n• Request code explanations and suggestions\n• Get help with debugging and optimization\n• Use natural language to describe what you want to build\n\nType your question or request, and Copilot will respond!',
                        clientId: clientId,
                        sessionId: client.sessionId,
                        copilotIntegration: 'active'
                    });
                }
            }, 2000);
            
        } catch (error) {
            console.error(`Failed to initialize Copilot for ${clientId}:`, error);
            this.sendError(clientId, `Failed to connect to Copilot: ${error.message}`);
        }
    }
    
    async handleMessage(clientId, message) {
        const client = this.clients.get(clientId);
        if (!client) return;
        
        client.lastActivity = Date.now();
        const userMessage = message.message || message.content || '';
        
        console.log(`📨 Message from ${clientId}: ${userMessage}`);
        
        try {
            // Check if we have an active Copilot process
            const copilotProcess = this.copilotProcesses.get(clientId);
            
            if (copilotProcess && !copilotProcess.killed) {
                // Send to real Copilot CLI
                console.log(`🤖 Sending to Copilot CLI: ${userMessage}`);
                copilotProcess.stdin.write(`${userMessage}\n`);
            } else {
                // Fallback to enhanced responses
                console.log(`📝 Using enhanced response for: ${userMessage}`);
                const response = await this.generateEnhancedResponse(userMessage);
                
                this.sendToClient(clientId, {
                    type: 'response',
                    message: `**Enhanced Assistant Response:**\n\n${response}\n\n*Note: Copilot CLI session not available. For full Copilot features, ensure authentication is complete.*`,
                    sessionId: client.sessionId,
                    source: 'enhanced-fallback'
                });
            }
            
        } catch (error) {
            console.error('💥 Error handling message:', error);
            this.sendError(clientId, `Failed to process message: ${error.message}`);
        }
    }
    
    sendCopilotResponse(clientId, response) {
        const client = this.clients.get(clientId);
        if (!client) return;
        
        // Filter out command prompts and focus on actual responses
        if (response.includes('> ') || response.length < 3) {
            return;
        }
        
        this.sendToClient(clientId, {
            type: 'response',
            message: response,
            sessionId: client.sessionId,
            source: 'copilot-cli',
            timestamp: new Date().toISOString()
        });
    }
    
    async generateEnhancedResponse(userMessage) {
        // Simplified enhanced responses for fallback
        const lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.includes('hello') || lowerMessage.includes('hi')) {
            return 'Hello! I\'m here to help with your coding questions. What would you like to work on?';
        }
        
        if (lowerMessage.includes('help')) {
            return 'I can assist with:\n• Code explanations and debugging\n• API development guidance\n• Best practices and optimization\n• Framework-specific questions\n\nWhat specific help do you need?';
        }
        
        return 'I\'m ready to help with your development questions! Please provide more details about what you\'re working on, and I\'ll do my best to assist you.';
    }
    
    sendToClient(clientId, data) {
        const client = this.clients.get(clientId);
        if (client && client.ws.readyState === WebSocket.OPEN) {
            client.ws.send(JSON.stringify(data));
        }
    }
    
    sendError(clientId, error) {
        this.sendToClient(clientId, {
            type: 'error',
            error: error,
            timestamp: new Date().toISOString()
        });
    }
    
    cleanupClient(clientId) {
        // Close Copilot process if it exists
        const copilotProcess = this.copilotProcesses.get(clientId);
        if (copilotProcess && !copilotProcess.killed) {
            console.log(`🧹 Terminating Copilot process for client: ${clientId}`);
            copilotProcess.kill('SIGTERM');
            this.copilotProcesses.delete(clientId);
        }
        
        this.clients.delete(clientId);
    }
    
    // Cleanup inactive connections
    startCleanupInterval() {
        setInterval(() => {
            const now = Date.now();
            const timeout = 15 * 60 * 1000; // 15 minutes
            
            for (const [clientId, client] of this.clients.entries()) {
                if (now - client.lastActivity > timeout) {
                    console.log(`🧹 Cleaning up inactive client: ${clientId}`);
                    client.ws.terminate();
                    this.cleanupClient(clientId);
                }
            }
        }, 60000); // Check every minute
    }
}

// Start the real integration server
const port = process.env.PORT || 3002;
const server = new RealCopilotIntegrationServer(port);
server.startCleanupInterval();

// Graceful shutdown
const shutdown = () => {
    console.log('🛑 Shutting down Real Copilot Integration Server...');
    
    // Cleanup all Copilot processes
    for (const [clientId, process] of server.copilotProcesses.entries()) {
        console.log(`🧹 Terminating Copilot process for ${clientId}`);
        process.kill('SIGTERM');
    }
    
    server.server.close(() => {
        process.exit(0);
    });
};

process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);

module.exports = RealCopilotIntegrationServer;
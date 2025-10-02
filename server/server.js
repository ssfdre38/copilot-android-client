const WebSocket = require('ws');
const express = require('express');
const { v4: uuidv4 } = require('uuid');

class CopilotCliServer {
    constructor(port = 3002) {
        this.port = port;
        this.app = express();
        this.server = null;
        this.wss = null;
        this.clients = new Map();
        
        this.setupExpress();
        this.setupWebSocket();
    }
    
    setupExpress() {
        this.app.use(express.json());
        
        // Health check endpoint
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'ok',
                timestamp: new Date().toISOString(),
                connections: this.clients.size,
                version: '1.0.0'
            });
        });
        
        // CORS headers
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', '*');
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
            next();
        });
    }
    
    setupWebSocket() {
        this.server = this.app.listen(this.port, () => {
            console.log(`🚀 Copilot CLI Server listening on port ${this.port}`);
            console.log(`📡 WebSocket endpoint: ws://localhost:${this.port}`);
            console.log(`🔍 Health check: http://localhost:${this.port}/health`);
        });
        
        this.wss = new WebSocket.Server({ server: this.server });
        
        this.wss.on('connection', (ws, req) => {
            const clientId = uuidv4();
            console.log(`✅ New client connected: ${clientId}`);
            
            const clientInfo = {
                id: clientId,
                ws: ws,
                sessionId: null,
                authenticated: true, // Simplified for demo
                lastActivity: Date.now()
            };
            
            this.clients.set(clientId, clientInfo);
            
            // Send welcome message
            this.sendToClient(clientId, {
                type: 'welcome',
                message: 'Connected to Copilot CLI Server! How can I help you today?',
                clientId: clientId
            });
            
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
    
    async handleMessage(clientId, message) {
        const client = this.clients.get(clientId);
        if (!client) return;
        
        client.lastActivity = Date.now();
        console.log(`📨 Message from ${clientId}:`, message.message || message);
        
        // Set or get session ID
        if (message.sessionId) {
            client.sessionId = message.sessionId;
        } else if (!client.sessionId) {
            client.sessionId = uuidv4();
        }
        
        try {
            const response = await this.generateResponse(message.message || message.content || 'Hello');
            
            this.sendToClient(clientId, {
                type: 'response',
                message: response,
                sessionId: client.sessionId
            });
            
        } catch (error) {
            console.error('💥 Error processing chat message:', error);
            this.sendError(clientId, 'Failed to process message');
        }
    }
    
    async generateResponse(userMessage) {
        // Enhanced AI-like responses based on common queries
        const lowerMessage = userMessage.toLowerCase();
        
        // Programming language specific responses
        if (lowerMessage.includes('python')) {
            return `🐍 Python help:\n\n# Virtual environment\npython -m venv myenv\nsource myenv/bin/activate  # Linux/Mac\nmyenv\\Scripts\\activate  # Windows\n\n# Install packages\npip install requests flask\n\n# Common commands\npython app.py\npython -m pip list`;
        }
        
        if (lowerMessage.includes('javascript') || lowerMessage.includes('node')) {
            return `🟨 JavaScript/Node.js help:\n\n// Package management\nnpm init -y\nnpm install express cors\nnpm start\n\n// Common patterns\nconst express = require('express');\nconst app = express();\napp.listen(3000, () => console.log('Server running'));`;
        }
        
        if (lowerMessage.includes('react')) {
            return `⚛️ React development:\n\n# Create new app\nnpx create-react-app my-app\ncd my-app\nnpm start\n\n# Common commands\nnpm run build\nnpm test\nnpm run eject\n\n// Basic component\nfunction App() {\n  return <div>Hello World</div>;\n}`;
        }
        
        if (lowerMessage.includes('git')) {
            return `🔧 Git commands:\n\n# Basic workflow\ngit init\ngit add .\ngit commit -m "Initial commit"\ngit remote add origin <url>\ngit push -u origin main\n\n# Branching\ngit checkout -b feature-branch\ngit merge feature-branch\n\n# Useful commands\ngit status\ngit log --oneline\ngit diff`;
        }
        
        if (lowerMessage.includes('docker')) {
            return `🐳 Docker commands:\n\n# Build and run\ndocker build -t my-app .\ndocker run -p 8080:8080 my-app\n\n# Management\ndocker ps\ndocker stop <container-id>\ndocker rm <container-id>\ndocker images\ndocker rmi <image-id>\n\n# Docker Compose\ndocker-compose up -d\ndocker-compose down`;
        }
        
        if (lowerMessage.includes('linux') || lowerMessage.includes('bash') || lowerMessage.includes('terminal')) {
            return `🐧 Linux/Bash commands:\n\n# File operations\nls -la          # List files\ncp file1 file2  # Copy\nmv file1 file2  # Move/rename\nrm file         # Delete\nmkdir folder    # Create directory\n\n# Text processing\ngrep "pattern" file\ncat file | grep "search"\nfind . -name "*.js"\n\n# Permissions\nchmod +x script.sh\nsudo chown user:group file`;
        }
        
        if (lowerMessage.includes('android') || lowerMessage.includes('kotlin')) {
            return `🤖 Android/Kotlin development:\n\n# Gradle commands\n./gradlew assembleDebug\n./gradlew installDebug\n./gradlew clean\n\n// Kotlin basics\nclass MainActivity : AppCompatActivity() {\n    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n        setContentView(R.layout.activity_main)\n    }\n}`;
        }
        
        if (lowerMessage.includes('database') || lowerMessage.includes('sql')) {
            return `🗄️ Database/SQL help:\n\n-- Basic queries\nSELECT * FROM users WHERE active = 1;\nINSERT INTO users (name, email) VALUES ('John', 'john@email.com');\nUPDATE users SET name = 'Jane' WHERE id = 1;\nDELETE FROM users WHERE id = 1;\n\n-- Joins\nSELECT u.name, p.title \nFROM users u \nJOIN posts p ON u.id = p.user_id;`;
        }
        
        if (lowerMessage.includes('api') || lowerMessage.includes('rest')) {
            return `🌐 API development:\n\n# HTTP methods\nGET    /api/users      # Retrieve\nPOST   /api/users      # Create\nPUT    /api/users/1    # Update\nDELETE /api/users/1    # Delete\n\n// Express.js example\napp.get('/api/users', (req, res) => {\n  res.json({ users: [] });\n});\n\n# Test with curl\ncurl -X GET http://localhost:3000/api/users`;
        }
        
        if (lowerMessage.includes('deploy') || lowerMessage.includes('production')) {
            return `🚀 Deployment guide:\n\n# Build for production\nnpm run build\ndocker build -t app:latest .\n\n# Environment variables\nexport NODE_ENV=production\nexport PORT=3000\n\n# Process management\npm2 start app.js\npm2 status\npm2 restart app\n\n# Health checks\ncurl http://localhost:3000/health`;
        }
        
        if (lowerMessage.includes('test') || lowerMessage.includes('testing')) {
            return `🧪 Testing strategies:\n\n# Unit tests\nnpm test\njest --watch\n\n# Integration tests\nnpm run test:integration\n\n# E2E tests\nnpx cypress open\n\n// Example test\ntest('should add two numbers', () => {\n  expect(add(2, 3)).toBe(5);\n});`;
        }
        
        // Generic helpful responses
        const genericResponses = [
            `I'm here to help with your development questions! You can ask me about:\n\n• Programming languages (Python, JavaScript, Java, etc.)\n• Development tools (Git, Docker, databases)\n• Framework-specific help (React, Android, etc.)\n• Command line operations\n• Best practices and debugging\n\nWhat would you like to work on?`,
            
            `Hello! I can assist with various development tasks:\n\n🔧 **Development Tools**: Git, Docker, CI/CD\n💻 **Programming**: Python, JavaScript, Java, Kotlin\n🌐 **Web Development**: React, Node.js, APIs\n📱 **Mobile**: Android development\n🗄️ **Databases**: SQL, NoSQL queries\n\nWhat specific challenge are you facing?`,
            
            `Welcome! I'm your coding assistant. I can help you with:\n\n• **Code examples** and snippets\n• **Command line** operations\n• **Debugging** and troubleshooting\n• **Best practices** and patterns\n• **Tool configuration** and setup\n\nJust describe what you're trying to accomplish!`
        ];
        
        return genericResponses[Math.floor(Math.random() * genericResponses.length)];
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
            error: error
        });
    }
    
    cleanupClient(clientId) {
        this.clients.delete(clientId);
    }
    
    // Cleanup inactive connections
    startCleanupInterval() {
        setInterval(() => {
            const now = Date.now();
            const timeout = 5 * 60 * 1000; // 5 minutes
            
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

// Start the server
const port = process.env.PORT || 3002;
const server = new CopilotCliServer(port);
server.startCleanupInterval();

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('🛑 Shutting down server...');
    server.server.close(() => {
        process.exit(0);
    });
});

process.on('SIGINT', () => {
    console.log('🛑 Shutting down server...');
    server.server.close(() => {
        process.exit(0);
    });
});
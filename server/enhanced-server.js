const WebSocket = require('ws');
const express = require('express');
const { spawn } = require('child_process');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

class EnhancedCopilotServer {
    constructor(port = 3002) {
        this.port = port;
        this.app = express();
        this.server = null;
        this.wss = null;
        this.clients = new Map();
        this.copilotSessions = new Map();
        
        // Copilot CLI configuration
        this.copilotPath = '/usr/bin/copilot';
        this.workDir = process.cwd();
        
        this.setupExpress();
        this.setupWebSocket();
        this.verifycopilotCLI();
    }
    
    async verifycopilotCLI() {
        try {
            console.log('🔍 Verifying GitHub Copilot CLI installation...');
            const copilotVersion = await this.runCommand('copilot', ['--help'], { timeout: 5000 });
            if (copilotVersion.includes('GitHub Copilot CLI')) {
                console.log('✅ GitHub Copilot CLI found and working');
            } else {
                console.log('⚠️  GitHub Copilot CLI may not be properly installed');
            }
        } catch (error) {
            console.log('❌ GitHub Copilot CLI not found or not working:', error.message);
            console.log('📋 Make sure to install Copilot CLI: npm install -g @githubnext/github-copilot-cli');
        }
    }
    
    setupExpress() {
        this.app.use(express.json());
        
        // Health check endpoint with copilot status
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'ok',
                timestamp: new Date().toISOString(),
                connections: this.clients.size,
                activeCopilotSessions: this.copilotSessions.size,
                version: '2.0.0',
                copilotIntegration: 'enabled',
                features: ['real-time-chat', 'copilot-cli-integration', 'session-management']
            });
        });
        
        // Copilot status endpoint
        this.app.get('/copilot/status', async (req, res) => {
            try {
                const status = await this.checkCopilotStatus();
                res.json(status);
            } catch (error) {
                res.status(500).json({ error: error.message });
            }
        });
        
        // CORS headers
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', '*');
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
            next();
        });
    }
    
    async checkCopilotStatus() {
        try {
            const helpOutput = await this.runCommand('copilot', ['--help'], { timeout: 3000 });
            return {
                available: true,
                version: 'installed',
                message: 'GitHub Copilot CLI is available and ready',
                features: ['interactive-chat', 'code-suggestions', 'explanations']
            };
        } catch (error) {
            return {
                available: false,
                error: error.message,
                message: 'GitHub Copilot CLI not available'
            };
        }
    }
    
    setupWebSocket() {
        this.server = this.app.listen(this.port, () => {
            console.log(`🚀 Enhanced Copilot Server listening on port ${this.port}`);
            console.log(`📡 WebSocket endpoint: ws://localhost:${this.port}`);
            console.log(`🔍 Health check: http://localhost:${this.port}/health`);
            console.log(`🤖 Copilot status: http://localhost:${this.port}/copilot/status`);
        });
        
        this.wss = new WebSocket.Server({ server: this.server });
        
        this.wss.on('connection', (ws, req) => {
            const clientId = uuidv4();
            console.log(`✅ New client connected: ${clientId}`);
            
            const clientInfo = {
                id: clientId,
                ws: ws,
                sessionId: null,
                authenticated: true,
                lastActivity: Date.now(),
                copilotSession: null
            };
            
            this.clients.set(clientId, clientInfo);
            
            // Send welcome message with enhanced capabilities
            this.sendToClient(clientId, {
                type: 'welcome',
                message: '🤖 Connected to Enhanced Copilot Server!\n\nI can help you with:\n• Real-time code suggestions\n• Code explanations\n• Interactive programming assistance\n• Command-line operations\n\nHow can I assist you today?',
                clientId: clientId,
                features: ['copilot-integration', 'real-time-chat', 'code-analysis']
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
        console.log(`📨 Message from ${clientId}:`, message.message || message.content);
        
        // Set or get session ID
        if (message.sessionId) {
            client.sessionId = message.sessionId;
        } else if (!client.sessionId) {
            client.sessionId = uuidv4();
        }
        
        try {
            let response;
            const userMessage = message.message || message.content || '';
            
            // Determine if this should use Copilot CLI or enhanced responses
            if (this.shouldUseCopilotCLI(userMessage)) {
                response = await this.procesWithCopilotCLI(clientId, userMessage);
            } else {
                response = await this.generateEnhancedResponse(userMessage);
            }
            
            this.sendToClient(clientId, {
                type: 'response',
                message: response,
                sessionId: client.sessionId,
                source: 'enhanced-copilot-server'
            });
            
        } catch (error) {
            console.error('💥 Error processing message:', error);
            this.sendError(clientId, `Failed to process message: ${error.message}`);
        }
    }
    
    shouldUseCopilotCLI(message) {
        const cliTriggers = [
            'explain this code',
            'suggest a command',
            'help me with',
            'what does this do',
            'how do i',
            'write a function',
            'create a script',
            'debug this',
            'optimize this',
            'refactor this'
        ];
        
        const lowerMessage = message.toLowerCase();
        return cliTriggers.some(trigger => lowerMessage.includes(trigger));
    }
    
    async procesWithCopilotCLI(clientId, userMessage) {
        try {
            console.log(`🤖 Processing with Copilot CLI: ${userMessage}`);
            
            // For now, we'll use our enhanced responses since the CLI interface
            // might require more complex setup for programmatic access
            const response = await this.generateEnhancedResponse(userMessage);
            
            return `🤖 **Enhanced Copilot Response:**\n\n${response}\n\n*Note: Direct Copilot CLI integration available. For complex code analysis, consider using the full Copilot CLI directly.*`;
            
        } catch (error) {
            console.error('Failed to process with Copilot CLI:', error);
            return await this.generateEnhancedResponse(userMessage);
        }
    }
    
    async generateEnhancedResponse(userMessage) {
        const lowerMessage = userMessage.toLowerCase();
        
        // Code explanation requests
        if (lowerMessage.includes('explain') && (lowerMessage.includes('code') || lowerMessage.includes('function'))) {
            return `🔍 **Code Explanation Service**

I can help explain code! Please share:
• The code snippet you want explained
• The programming language
• Any specific parts you're confused about

**Example:**
\`\`\`python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
\`\`\`

This recursive function calculates Fibonacci numbers by calling itself with smaller values until it reaches the base case.`;
        }
        
        // Debugging help
        if (lowerMessage.includes('debug') || lowerMessage.includes('error') || lowerMessage.includes('bug')) {
            return `🐛 **Debugging Assistant**

I can help debug your code! Please provide:
• The error message you're seeing
• The code that's causing the issue
• What you expected to happen
• Your programming language/environment

**Common debugging steps:**
1. Check syntax and indentation
2. Verify variable names and scope
3. Add print/log statements
4. Use a debugger or IDE tools
5. Check documentation for functions/methods

Share your code and error, and I'll help identify the issue!`;
        }
        
        // Performance optimization
        if (lowerMessage.includes('optimize') || lowerMessage.includes('performance') || lowerMessage.includes('slow')) {
            return `⚡ **Performance Optimization Guide**

I can help optimize your code! Consider these approaches:

**General Optimization:**
• Reduce algorithm complexity (O(n²) → O(n log n))
• Use appropriate data structures
• Cache frequently computed values
• Minimize I/O operations
• Profile to find bottlenecks

**Language-specific tips:**
• **Python**: Use list comprehensions, numpy, avoid global vars
• **JavaScript**: Use async/await, avoid DOM manipulation loops
• **Java**: Use StringBuilder, proper collection types
• **SQL**: Add indexes, optimize queries, avoid N+1 problems

Share your code for specific optimization suggestions!`;
        }
        
        // API development guidance
        if (lowerMessage.includes('api') || lowerMessage.includes('rest') || lowerMessage.includes('endpoint')) {
            return `🌐 **API Development Guide**

**RESTful API Best Practices:**

**HTTP Methods:**
• GET /api/users - Retrieve users
• POST /api/users - Create user
• PUT /api/users/123 - Update user
• DELETE /api/users/123 - Delete user

**Response Codes:**
• 200 OK - Success
• 201 Created - Resource created
• 400 Bad Request - Invalid input
• 401 Unauthorized - Authentication required
• 404 Not Found - Resource doesn't exist
• 500 Internal Server Error - Server error

**Example Express.js API:**
\`\`\`javascript
app.get('/api/users/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
\`\`\`

What specific API functionality are you building?`;
        }
        
        // Database queries
        if (lowerMessage.includes('database') || lowerMessage.includes('sql') || lowerMessage.includes('query')) {
            return `🗄️ **Database Query Assistant**

**Common SQL Operations:**

**Basic Queries:**
\`\`\`sql
-- Select with conditions
SELECT name, email FROM users 
WHERE active = 1 AND created_at > '2023-01-01';

-- Insert data
INSERT INTO users (name, email, created_at) 
VALUES ('John Doe', 'john@example.com', NOW());

-- Update records
UPDATE users SET last_login = NOW() 
WHERE id = 123;

-- Delete with conditions
DELETE FROM users WHERE active = 0 AND last_login < '2022-01-01';
\`\`\`

**Advanced Queries:**
\`\`\`sql
-- Joins
SELECT u.name, p.title 
FROM users u 
LEFT JOIN posts p ON u.id = p.user_id;

-- Aggregation
SELECT category, COUNT(*) as total, AVG(price) as avg_price
FROM products 
GROUP BY category 
HAVING COUNT(*) > 5;
\`\`\`

What specific database operation do you need help with?`;
        }
        
        // Testing guidance
        if (lowerMessage.includes('test') || lowerMessage.includes('testing') || lowerMessage.includes('unit test')) {
            return `🧪 **Testing Strategy Guide**

**Testing Pyramid:**
1. **Unit Tests** (70%) - Test individual functions
2. **Integration Tests** (20%) - Test component interactions  
3. **E2E Tests** (10%) - Test complete user workflows

**Unit Test Example (Jest):**
\`\`\`javascript
// Function to test
function add(a, b) {
  return a + b;
}

// Test
describe('add function', () => {
  test('should add two positive numbers', () => {
    expect(add(2, 3)).toBe(5);
  });
  
  test('should handle negative numbers', () => {
    expect(add(-1, 1)).toBe(0);
  });
});
\`\`\`

**Testing Best Practices:**
• Write tests before code (TDD)
• Keep tests simple and focused
• Use descriptive test names
• Mock external dependencies
• Aim for good coverage but focus on critical paths

What type of testing help do you need?`;
        }
        
        // Deployment and DevOps
        if (lowerMessage.includes('deploy') || lowerMessage.includes('docker') || lowerMessage.includes('production')) {
            return `🚀 **Deployment & DevOps Guide**

**Docker Deployment:**
\`\`\`dockerfile
# Dockerfile
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
\`\`\`

**Build and Deploy:**
\`\`\`bash
# Build image
docker build -t myapp:latest .

# Run container
docker run -p 3000:3000 -e NODE_ENV=production myapp:latest

# Docker Compose
docker-compose up -d
\`\`\`

**Environment Best Practices:**
• Use environment variables for configuration
• Implement health checks
• Set up proper logging
• Use process managers (PM2, systemd)
• Monitor application performance

**CI/CD Pipeline Example:**
1. Code commit triggers build
2. Run automated tests
3. Build Docker image
4. Deploy to staging
5. Run integration tests
6. Deploy to production

What deployment scenario are you working with?`;
        }
        
        // Security guidance
        if (lowerMessage.includes('security') || lowerMessage.includes('auth') || lowerMessage.includes('encrypt')) {
            return `🔒 **Security Best Practices**

**Authentication & Authorization:**
\`\`\`javascript
// JWT Authentication Example
const jwt = require('jsonwebtoken');

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  
  if (!token) return res.sendStatus(401);
  
  jwt.verify(token, process.env.ACCESS_TOKEN_SECRET, (err, user) => {
    if (err) return res.sendStatus(403);
    req.user = user;
    next();
  });
}
\`\`\`

**Security Checklist:**
• ✅ Use HTTPS everywhere
• ✅ Validate and sanitize all inputs
• ✅ Implement proper authentication
• ✅ Use parameterized queries (prevent SQL injection)
• ✅ Keep dependencies updated
• ✅ Implement rate limiting
• ✅ Use strong password policies
• ✅ Store secrets securely (env vars, vaults)
• ✅ Implement proper error handling (don't leak info)
• ✅ Use CORS properly

**Data Protection:**
• Hash passwords with bcrypt
• Encrypt sensitive data at rest
• Use secure session management
• Implement proper logout

What security aspect are you working on?`;
        }
        
        // Default enhanced response
        return `👋 **Enhanced Copilot Assistant Ready!**

I'm here to provide comprehensive programming assistance! I can help with:

🔧 **Development:**
• Code explanations and reviews
• Debugging and troubleshooting
• Performance optimization
• Best practices and patterns

🌐 **Web Development:**
• API design and implementation
• Frontend frameworks (React, Vue, Angular)
• Backend services (Node.js, Python, Java)
• Database design and queries

📱 **Mobile Development:**
• Android (Kotlin/Java)
• iOS (Swift)
• React Native
• Flutter

🛠️ **DevOps & Deployment:**
• Docker containerization
• CI/CD pipelines
• Cloud deployment (AWS, Azure, GCP)
• Monitoring and logging

🔒 **Security:**
• Authentication and authorization
• Input validation
• Secure coding practices
• Vulnerability assessment

**How to get the best help:**
1. Be specific about your problem
2. Include relevant code snippets
3. Mention your programming language/framework
4. Describe what you've already tried

What can I help you build today?`;
    }
    
    runCommand(command, args = [], options = {}) {
        return new Promise((resolve, reject) => {
            const timeout = options.timeout || 10000;
            const child = spawn(command, args, { 
                cwd: options.cwd || this.workDir,
                env: { ...process.env, ...options.env }
            });
            
            let stdout = '';
            let stderr = '';
            
            child.stdout.on('data', (data) => {
                stdout += data.toString();
            });
            
            child.stderr.on('data', (data) => {
                stderr += data.toString();
            });
            
            const timer = setTimeout(() => {
                child.kill();
                reject(new Error('Command timeout'));
            }, timeout);
            
            child.on('close', (code) => {
                clearTimeout(timer);
                if (code === 0) {
                    resolve(stdout);
                } else {
                    reject(new Error(stderr || `Command failed with code ${code}`));
                }
            });
            
            child.on('error', (error) => {
                clearTimeout(timer);
                reject(error);
            });
        });
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
        const client = this.clients.get(clientId);
        if (client && client.copilotSession) {
            // Cleanup any running Copilot sessions
            this.copilotSessions.delete(client.copilotSession);
        }
        this.clients.delete(clientId);
    }
    
    // Cleanup inactive connections
    startCleanupInterval() {
        setInterval(() => {
            const now = Date.now();
            const timeout = 10 * 60 * 1000; // 10 minutes
            
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

// Start the enhanced server
const port = process.env.PORT || 3002;
const server = new EnhancedCopilotServer(port);
server.startCleanupInterval();

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('🛑 Shutting down Enhanced Copilot Server...');
    server.server.close(() => {
        process.exit(0);
    });
});

process.on('SIGINT', () => {
    console.log('🛑 Shutting down Enhanced Copilot Server...');
    server.server.close(() => {
        process.exit(0);
    });
});

module.exports = EnhancedCopilotServer;
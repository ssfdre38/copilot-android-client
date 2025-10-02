#!/bin/bash

# 🚀 Copilot Android Client - Complete Server Setup Script
# This script sets up the complete server environment for the Copilot Android Client

set -e  # Exit on any error

echo "🤖 Copilot Android Client - Server Setup"
echo "========================================"
echo

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   echo "❌ This script should not be run as root"
   echo "💡 Please run as a regular user with sudo privileges"
   exit 1
fi

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

echo "📋 Checking prerequisites..."

# Check Node.js
if ! command_exists node; then
    echo "❌ Node.js not found. Installing..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt-get install -y nodejs
else
    NODE_VERSION=$(node --version)
    echo "✅ Node.js found: $NODE_VERSION"
fi

# Check npm
if ! command_exists npm; then
    echo "❌ npm not found. Installing..."
    sudo apt-get install -y npm
else
    NPM_VERSION=$(npm --version)
    echo "✅ npm found: $NPM_VERSION"
fi

# Check git
if ! command_exists git; then
    echo "❌ git not found. Installing..."
    sudo apt-get update
    sudo apt-get install -y git
else
    GIT_VERSION=$(git --version)
    echo "✅ git found: $GIT_VERSION"
fi

echo
echo "📦 Setting up project..."

# Create project directory if it doesn't exist
PROJECT_DIR="$HOME/copilot-android-client"
if [ ! -d "$PROJECT_DIR" ]; then
    echo "📁 Creating project directory: $PROJECT_DIR"
    mkdir -p "$PROJECT_DIR"
fi

cd "$PROJECT_DIR"

# Initialize server directory
echo "🔧 Setting up server..."
if [ ! -d "server" ]; then
    mkdir server
fi

cd server

# Create package.json if it doesn't exist
if [ ! -f "package.json" ]; then
    echo "📄 Creating package.json..."
    cat > package.json << 'EOF'
{
  "name": "copilot-android-server",
  "version": "1.0.0",
  "description": "WebSocket server for Copilot Android Client",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js",
    "test": "node test-client.js"
  },
  "keywords": ["copilot", "android", "websocket", "cli"],
  "author": "Copilot Android Client",
  "license": "MIT",
  "dependencies": {
    "ws": "^8.14.0",
    "express": "^4.18.2",
    "uuid": "^9.0.0"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
EOF
fi

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Create server.js if it doesn't exist
if [ ! -f "server.js" ]; then
    echo "🔧 Creating server.js..."
    cat > server.js << 'EOF'
const WebSocket = require('ws');
const express = require('express');
const { v4: uuidv4 } = require('uuid');
const { spawn } = require('child_process');

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
            
            this.clients.set(clientId, {
                ws: ws,
                sessionId: null,
                lastActivity: new Date()
            });
            
            ws.on('message', async (message) => {
                try {
                    const data = JSON.parse(message.toString());
                    await this.handleMessage(clientId, data);
                } catch (error) {
                    console.error(`❌ Error processing message from ${clientId}:`, error);
                    this.sendError(clientId, 'Invalid message format');
                }
            });
            
            ws.on('close', () => {
                console.log(`👋 Client disconnected: ${clientId}`);
                this.clients.delete(clientId);
            });
            
            ws.on('error', (error) => {
                console.error(`🔥 WebSocket error for ${clientId}:`, error);
                this.clients.delete(clientId);
            });
        });
    }
    
    async handleMessage(clientId, data) {
        const client = this.clients.get(clientId);
        if (!client) return;
        
        client.lastActivity = new Date();
        
        if (data.type === 'message') {
            const response = await this.processCopilotRequest(data.message, data.sessionId);
            this.sendMessage(clientId, response, data.sessionId);
        }
    }
    
    async processCopilotRequest(message, sessionId) {
        try {
            // Try to use GitHub Copilot CLI
            const response = await this.callCopilotCli(message);
            return response;
        } catch (error) {
            // Fallback to smart responses
            return this.generateFallbackResponse(message);
        }
    }
    
    async callCopilotCli(message) {
        return new Promise((resolve, reject) => {
            const copilot = spawn('gh', ['copilot', 'suggest', message], {
                timeout: 30000
            });
            
            let output = '';
            let errorOutput = '';
            
            copilot.stdout.on('data', (data) => {
                output += data.toString();
            });
            
            copilot.stderr.on('data', (data) => {
                errorOutput += data.toString();
            });
            
            copilot.on('close', (code) => {
                if (code === 0 && output.trim()) {
                    resolve(output.trim());
                } else {
                    reject(new Error(`Copilot CLI error: ${errorOutput || 'No output'}`));
                }
            });
            
            copilot.on('error', (error) => {
                reject(error);
            });
        });
    }
    
    generateFallbackResponse(message) {
        const lowerMessage = message.toLowerCase();
        
        // Programming language responses
        if (lowerMessage.includes('python') || lowerMessage.includes('.py')) {
            return this.generatePythonResponse(message);
        }
        if (lowerMessage.includes('javascript') || lowerMessage.includes('js') || lowerMessage.includes('node')) {
            return this.generateJavaScriptResponse(message);
        }
        if (lowerMessage.includes('git')) {
            return this.generateGitResponse(message);
        }
        if (lowerMessage.includes('docker')) {
            return this.generateDockerResponse(message);
        }
        if (lowerMessage.includes('linux') || lowerMessage.includes('bash') || lowerMessage.includes('shell')) {
            return this.generateLinuxResponse(message);
        }
        
        // Default helpful response
        return `I understand you're asking about: "${message}"

While I don't have access to the full Copilot CLI right now, here are some general suggestions:

🔍 **For coding questions:**
- Break down your problem into smaller steps
- Check official documentation for the technology you're using
- Look for similar examples on GitHub or Stack Overflow

🛠 **For command-line tasks:**
- Use 'man command-name' to see manual pages
- Try 'command-name --help' for usage information
- Search for "how to [your task] in [your environment]"

💡 **Development tips:**
- Write small, testable functions
- Use version control (git) frequently
- Add comments to explain complex logic
- Test your code with different inputs

Would you like me to help you with a more specific question?`;
    }
    
    generatePythonResponse(message) {
        if (message.includes('install') || message.includes('pip')) {
            return `For Python package management:

\`\`\`bash
# Install a package
pip install package-name

# Install from requirements file
pip install -r requirements.txt

# Create virtual environment
python -m venv myenv
source myenv/bin/activate  # Linux/Mac
myenv\\Scripts\\activate   # Windows

# List installed packages
pip list
\`\`\``;
        }
        
        return `Here's a helpful Python snippet:

\`\`\`python
# Example Python code structure
def main():
    print("Hello, Python!")
    
    # Your code here
    
if __name__ == "__main__":
    main()
\`\`\`

Common Python patterns:
- Use virtual environments for projects
- Follow PEP 8 style guidelines
- Write docstrings for functions
- Handle exceptions gracefully`;
    }
    
    generateJavaScriptResponse(message) {
        return `JavaScript/Node.js help:

\`\`\`javascript
// Modern JavaScript patterns
const myFunction = async () => {
    try {
        const result = await someAsyncOperation();
        return result;
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
};

// ES6+ features
const { destructuring } = someObject;
const newArray = [...oldArray, newItem];
\`\`\`

Node.js tips:
- Use npm or yarn for package management
- Consider using TypeScript for larger projects
- Follow async/await patterns
- Handle errors properly`;
    }
    
    generateGitResponse(message) {
        return `Common Git commands:

\`\`\`bash
# Basic workflow
git status
git add .
git commit -m "Your commit message"
git push

# Branching
git checkout -b feature-branch
git merge feature-branch
git branch -d feature-branch

# Checking history
git log --oneline
git diff

# Undoing changes
git checkout -- filename  # Discard changes
git reset HEAD~1          # Undo last commit
\`\`\`

Git best practices:
- Write clear commit messages
- Use branches for features
- Pull before pushing
- Review changes before committing`;
    }
    
    generateDockerResponse(message) {
        return `Docker commands and tips:

\`\`\`bash
# Basic Docker commands
docker build -t myapp .
docker run -p 8080:80 myapp
docker ps
docker stop container-id

# Docker Compose
docker-compose up -d
docker-compose down
docker-compose logs

# Cleanup
docker system prune
\`\`\`

Dockerfile example:
\`\`\`dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
\`\`\``;
    }
    
    generateLinuxResponse(message) {
        return `Linux/Bash commands:

\`\`\`bash
# File operations
ls -la
mkdir directory-name
cp source destination
mv old-name new-name
rm filename

# Text processing
grep "pattern" filename
cat filename
less filename
head -n 10 filename
tail -f logfile

# System info
ps aux
top
df -h
free -h

# Permissions
chmod 755 filename
chown user:group filename
\`\`\`

Bash scripting tips:
- Use '#!/bin/bash' at the start
- Quote variables: "$variable"
- Check exit codes: $?
- Use 'set -e' for strict mode`;
    }
    
    sendMessage(clientId, message, sessionId) {
        const client = this.clients.get(clientId);
        if (client && client.ws.readyState === WebSocket.OPEN) {
            client.ws.send(JSON.stringify({
                type: 'response',
                message: message,
                sessionId: sessionId,
                timestamp: new Date().toISOString()
            }));
        }
    }
    
    sendError(clientId, error) {
        const client = this.clients.get(clientId);
        if (client && client.ws.readyState === WebSocket.OPEN) {
            client.ws.send(JSON.stringify({
                type: 'error',
                message: error,
                timestamp: new Date().toISOString()
            }));
        }
    }
}

// Start the server
const server = new CopilotCliServer(process.env.PORT || 3002);

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('📤 Received SIGTERM, shutting down gracefully');
    server.server.close(() => {
        console.log('👋 Server closed');
        process.exit(0);
    });
});
EOF
fi

# Create test client
if [ ! -f "test-client.js" ]; then
    echo "🧪 Creating test client..."
    cat > test-client.js << 'EOF'
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:3002');

ws.on('open', function open() {
    console.log('✅ Connected to server');
    
    // Send test message
    ws.send(JSON.stringify({
        type: 'message',
        message: 'Hello, Copilot! How do I create a Python web server?',
        sessionId: 'test-session'
    }));
});

ws.on('message', function message(data) {
    const response = JSON.parse(data.toString());
    console.log('📨 Received:', response.message);
    ws.close();
});

ws.on('error', function error(err) {
    console.error('❌ Error:', err);
});
EOF
fi

cd ..

echo
echo "🔧 Setting up systemd service..."

# Create systemd service file
SERVICE_FILE="server/copilot-server.service"
cat > "$SERVICE_FILE" << EOF
[Unit]
Description=Copilot CLI WebSocket Server
Documentation=https://github.com/ssfdre38/copilot-android-client
After=network.target

[Service]
Type=simple
User=$USER
Group=$USER
WorkingDirectory=$PWD/server
ExecStart=/usr/bin/node $PWD/server/server.js
Restart=always
RestartSec=10
Environment=NODE_ENV=production
Environment=PORT=3002

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=copilot-server

# Security
NoNewPrivileges=true

[Install]
WantedBy=multi-user.target
EOF

# Install systemd service
echo "🔧 Installing systemd service..."
sudo cp "$SERVICE_FILE" /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable copilot-server
sudo systemctl start copilot-server

echo
echo "✅ Setup complete!"
echo
echo "📊 Service Status:"
sudo systemctl status copilot-server --no-pager

echo
echo "🌐 Server Information:"
echo "  📡 WebSocket URL: ws://$(hostname -I | awk '{print $1}'):3002"
echo "  🔍 Health Check: http://$(hostname -I | awk '{print $1}'):3002/health"
echo "  📁 Project Directory: $PWD"
echo
echo "🧪 Test the server:"
echo "  cd $PWD/server && node test-client.js"
echo
echo "📱 In your Android app, use:"
echo "  Server URL: ws://$(hostname -I | awk '{print $1}'):3002"
echo
echo "🔧 Manage the service:"
echo "  Start:   sudo systemctl start copilot-server"
echo "  Stop:    sudo systemctl stop copilot-server"
echo "  Restart: sudo systemctl restart copilot-server"
echo "  Status:  sudo systemctl status copilot-server"
echo "  Logs:    journalctl -u copilot-server -f"
echo
echo "🎉 Your Copilot Android Server is ready!"
EOF
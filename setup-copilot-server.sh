#!/bin/bash

# 🚀 Copilot Android Client - Server Setup Script v1.5.0
# This script sets up the Node.js WebSocket server for the Android client

echo "🚀 Setting up Copilot CLI Server for Android Client v1.5.0"
echo "=========================================================="

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16+ first."
    echo "   Visit: https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node --version | sed 's/v//')
echo "✅ Node.js version: $NODE_VERSION"

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

echo "✅ npm is available"

# Create server directory
SERVER_DIR="copilot-cli-server"
if [ -d "$SERVER_DIR" ]; then
    echo "📁 Server directory already exists"
    cd "$SERVER_DIR"
else
    echo "📁 Creating server directory: $SERVER_DIR"
    mkdir "$SERVER_DIR"
    cd "$SERVER_DIR"
fi

# Initialize package.json if it doesn't exist
if [ ! -f "package.json" ]; then
    echo "📦 Initializing npm package..."
    npm init -y
fi

# Install dependencies
echo "📥 Installing dependencies..."
npm install ws express cors

# Create the server file
echo "🔧 Creating server.js..."
cat > server.js << 'EOF'
const WebSocket = require('ws');
const express = require('express');
const cors = require('cors');
const { spawn } = require('child_process');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({ 
        status: 'healthy', 
        service: 'Copilot CLI Server',
        version: '1.5.0',
        timestamp: new Date().toISOString()
    });
});

// Create HTTP server
const server = require('http').createServer(app);

// Create WebSocket server
const wss = new WebSocket.Server({ server });

console.log('🚀 Starting Copilot CLI Server v1.5.0');
console.log('📱 Compatible with Android Client v1.5.0');

wss.on('connection', (ws, req) => {
    const clientIp = req.socket.remoteAddress;
    console.log(`📱 Android client connected from ${clientIp}`);
    
    // Send welcome message
    ws.send(JSON.stringify({
        type: 'response',
        message: '🎉 Connected to Copilot CLI Server v1.5.0!\n\nYou can now ask questions and get help with coding tasks.\n\nTry asking: "How do I create a REST API in Node.js?"',
        timestamp: new Date().toISOString()
    }));
    
    ws.on('message', async (data) => {
        try {
            const message = JSON.parse(data);
            console.log(`📝 Received: ${message.message}`);
            
            if (message.type === 'message' && message.message) {
                // Try to use GitHub Copilot CLI if available
                try {
                    const copilotProcess = spawn('gh', ['copilot', 'explain', message.message], {
                        stdio: ['pipe', 'pipe', 'pipe']
                    });
                    
                    let response = '';
                    let errorOutput = '';
                    
                    copilotProcess.stdout.on('data', (data) => {
                        response += data.toString();
                    });
                    
                    copilotProcess.stderr.on('data', (data) => {
                        errorOutput += data.toString();
                    });
                    
                    copilotProcess.on('close', (code) => {
                        if (code === 0 && response.trim()) {
                            // Successful Copilot response
                            ws.send(JSON.stringify({
                                type: 'response',
                                message: response.trim(),
                                timestamp: new Date().toISOString()
                            }));
                        } else {
                            // Fallback response
                            sendFallbackResponse(ws, message.message);
                        }
                    });
                    
                    // Timeout after 10 seconds
                    setTimeout(() => {
                        copilotProcess.kill();
                        sendFallbackResponse(ws, message.message);
                    }, 10000);
                    
                } catch (error) {
                    // Fallback if Copilot CLI is not available
                    sendFallbackResponse(ws, message.message);
                }
            }
        } catch (error) {
            console.error('❌ Error processing message:', error);
            ws.send(JSON.stringify({
                type: 'response',
                message: 'Sorry, I encountered an error processing your request. Please try again.',
                timestamp: new Date().toISOString()
            }));
        }
    });
    
    ws.on('close', () => {
        console.log(`📱 Android client disconnected from ${clientIp}`);
    });
    
    ws.on('error', (error) => {
        console.error('❌ WebSocket error:', error);
    });
});

function sendFallbackResponse(ws, userMessage) {
    const responses = [
        `I understand you're asking about: "${userMessage}"\n\nWhile I don't have GitHub Copilot CLI available, I recommend:\n\n1. Check the official documentation\n2. Search for examples on GitHub\n3. Consider using GitHub Copilot in your IDE\n\nFor the best experience, install GitHub CLI with Copilot extension:\n\`gh extension install github/gh-copilot\``,
        
        `Question: "${userMessage}"\n\n🤖 Copilot CLI Response:\n\nTo get the best answers, please ensure you have GitHub Copilot CLI installed:\n\n1. Install GitHub CLI: https://cli.github.com/\n2. Install Copilot extension: \`gh extension install github/gh-copilot\`\n3. Restart this server\n\nFor now, I recommend checking the official documentation or GitHub for examples related to your question.`,
        
        `You asked: "${userMessage}"\n\n💡 Helpful suggestion:\n\nThis server works best with GitHub Copilot CLI installed. Without it, I can only provide basic responses.\n\nTo enable full Copilot functionality:\n- Install GitHub CLI\n- Add Copilot extension\n- Ensure you have Copilot access\n\nIn the meantime, try searching for "${userMessage}" on GitHub or Stack Overflow for community solutions.`
    ];
    
    const randomResponse = responses[Math.floor(Math.random() * responses.length)];
    
    ws.send(JSON.stringify({
        type: 'response',
        message: randomResponse,
        timestamp: new Date().toISOString()
    }));
}

server.listen(PORT, '0.0.0.0', () => {
    console.log(`🌐 Server running on http://0.0.0.0:${PORT}`);
    console.log(`📱 Android clients can connect to: ws://YOUR_IP:${PORT}`);
    console.log(`💻 Health check: http://YOUR_IP:${PORT}/health`);
    console.log('');
    console.log('🔧 Setup Instructions:');
    console.log('1. Install GitHub CLI: https://cli.github.com/');
    console.log('2. Install Copilot: gh extension install github/gh-copilot');
    console.log('3. Restart server for full functionality');
    console.log('');
    console.log('📱 In your Android app:');
    console.log('- Use "Discover Servers" to find this server automatically');
    console.log('- Or manually connect to ws://YOUR_IP:3001');
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down server...');
    server.close(() => {
        console.log('✅ Server stopped');
        process.exit(0);
    });
});
EOF

# Create start script
echo "🔧 Creating start script..."
cat > start-server.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting Copilot CLI Server..."
echo "Press Ctrl+C to stop"
node server.js
EOF

chmod +x start-server.sh

# Create systemd service file
echo "🔧 Creating systemd service file..."
cat > copilot-cli-server.service << EOF
[Unit]
Description=Copilot CLI Server for Android Client
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)
ExecStart=/usr/bin/node server.js
Restart=always
RestartSec=10
Environment=NODE_ENV=production

[Install]
WantedBy=multi-user.target
EOF

echo "✅ Server setup complete!"
echo ""
echo "🚀 Quick Start:"
echo "1. Run: ./start-server.sh"
echo "2. In Android app, use 'Discover Servers' or connect to ws://YOUR_IP:3001"
echo ""
echo "🔧 Auto-start on boot (optional):"
echo "sudo cp copilot-cli-server.service /etc/systemd/system/"
echo "sudo systemctl enable copilot-cli-server"
echo "sudo systemctl start copilot-cli-server"
echo ""
echo "📋 Server Features:"
echo "- ✅ WebSocket communication with Android client"
echo "- ✅ Auto-discovery support (health check endpoint)"
echo "- ✅ GitHub Copilot CLI integration (when available)"
echo "- ✅ Fallback responses when Copilot unavailable"
echo "- ✅ Cross-platform compatibility"
echo ""
echo "📱 Compatible with Android Client v1.5.0+"
echo "🌐 Server will be available at: http://$(hostname -I | awk '{print $1}'):3001"
EOF

chmod +x setup-copilot-server.sh
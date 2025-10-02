const WebSocket = require('ws');

// Test client to verify server functionality
class TestClient {
    constructor(url = 'ws://localhost:3001') {
        this.url = url;
        this.ws = null;
    }
    
    connect() {
        return new Promise((resolve, reject) => {
            this.ws = new WebSocket(this.url);
            
            this.ws.on('open', () => {
                console.log('✅ Connected to server');
                resolve();
            });
            
            this.ws.on('message', (data) => {
                const message = JSON.parse(data.toString());
                console.log('📨 Received:', message.message || message.type);
            });
            
            this.ws.on('error', (error) => {
                console.error('❌ Connection error:', error.message);
                reject(error);
            });
            
            this.ws.on('close', () => {
                console.log('🔌 Connection closed');
            });
        });
    }
    
    sendMessage(text) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const message = {
                type: 'message',
                message: text
            };
            this.ws.send(JSON.stringify(message));
            console.log('📤 Sent:', text);
        }
    }
    
    disconnect() {
        if (this.ws) {
            this.ws.close();
        }
    }
}

// Run test
async function runTest() {
    const client = new TestClient();
    
    try {
        await client.connect();
        
        // Wait a moment for welcome message
        setTimeout(() => {
            client.sendMessage('Hello, Copilot! How do I list files in Linux?');
        }, 1000);
        
        // Test for 5 seconds then disconnect
        setTimeout(() => {
            client.disconnect();
            process.exit(0);
        }, 5000);
        
    } catch (error) {
        console.error('Test failed:', error.message);
        process.exit(1);
    }
}

if (require.main === module) {
    console.log('🧪 Testing Copilot CLI Server...');
    runTest();
}
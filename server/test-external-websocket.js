const WebSocket = require('ws');

console.log('🧪 Testing WebSocket connection to server from external perspective...');

// Test WebSocket connection like the Android app would
const wsUrl = 'ws://54.37.254.74:3002';
console.log(`📡 Connecting to: ${wsUrl}`);

const ws = new WebSocket(wsUrl);

ws.on('open', function open() {
    console.log('✅ WebSocket connected successfully!');
    
    // Send test message like Android app
    const testMessage = {
        type: 'message',
        message: 'Hello from external test client',
        clientId: 'test-client-' + Date.now()
    };
    
    console.log('📤 Sending test message:', JSON.stringify(testMessage));
    ws.send(JSON.stringify(testMessage));
});

ws.on('message', function message(data) {
    console.log('📥 Received from server:');
    try {
        const parsed = JSON.parse(data.toString());
        console.log(JSON.stringify(parsed, null, 2));
    } catch (e) {
        console.log('Raw message:', data.toString());
    }
});

ws.on('close', function close(code, reason) {
    console.log(`👋 Connection closed - Code: ${code}, Reason: ${reason}`);
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
    console.error('Error details:', err);
});

// Close after 15 seconds
setTimeout(() => {
    console.log('🔚 Closing test connection...');
    ws.close();
    process.exit(0);
}, 15000);
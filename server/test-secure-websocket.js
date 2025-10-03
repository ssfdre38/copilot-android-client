const WebSocket = require('ws');

console.log('🔒 Testing Secure WebSocket connection...');

// Test WSS connection
const wssUrl = 'wss://s1.pepperbacks.xyz:8443';
console.log(`📡 Connecting to: ${wssUrl}`);

const ws = new WebSocket(wssUrl, {
    rejectUnauthorized: false  // Accept self-signed certs for testing
});

ws.on('open', function open() {
    console.log('✅ Secure WebSocket connected successfully!');
    
    // Send test message
    const testMessage = {
        type: 'message',
        message: 'Hello from secure test client'
    };
    
    console.log('📤 Sending secure test message:', JSON.stringify(testMessage));
    ws.send(JSON.stringify(testMessage));
});

ws.on('message', function message(data) {
    console.log('📥 Received from secure server:');
    try {
        const parsed = JSON.parse(data.toString());
        console.log(JSON.stringify(parsed, null, 2));
    } catch (e) {
        console.log('Raw message:', data.toString());
    }
});

ws.on('close', function close(code, reason) {
    console.log(`👋 Secure connection closed - Code: ${code}, Reason: ${reason}`);
});

ws.on('error', function error(err) {
    console.error('❌ Secure WebSocket error:', err.message);
    console.error('Error details:', err);
});

// Close after 10 seconds
setTimeout(() => {
    console.log('🔚 Closing secure test connection...');
    ws.close();
    process.exit(0);
}, 10000);
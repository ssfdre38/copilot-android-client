const WebSocket = require('ws');

console.log('🔌 Testing WebSocket connection to localhost:3002...');

const ws = new WebSocket('ws://localhost:3002');

ws.on('open', function open() {
    console.log('✅ Connected to WebSocket server');
    
    // Send a test message
    const testMessage = {
        type: 'message',
        content: 'Hello from test client!',
        timestamp: new Date().toISOString()
    };
    
    console.log('📤 Sending test message:', testMessage);
    ws.send(JSON.stringify(testMessage));
    
    // Close after 2 seconds
    setTimeout(() => {
        console.log('🔌 Closing connection...');
        ws.close();
    }, 2000);
});

ws.on('message', function message(data) {
    console.log('📥 Received:', data.toString());
});

ws.on('close', function close(code, reason) {
    console.log('🔌 Connection closed:', code, reason.toString());
    console.log('✅ WebSocket test completed successfully!');
});

ws.on('error', function error(err) {
    console.log('❌ WebSocket error:', err.message);
});
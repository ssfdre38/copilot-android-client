const WebSocket = require('ws');

console.log('🧪 Testing all WebSocket endpoints...');

const endpoints = [
    'ws://54.37.254.74:3002',
    'wss://54.37.254.74:8443'
];

let testCount = 0;

endpoints.forEach((url, index) => {
    setTimeout(() => {
        console.log(`\n📡 Testing: ${url}`);
        
        const ws = new WebSocket(url, {
            rejectUnauthorized: false  // Accept self-signed certs
        });
        
        ws.on('open', function open() {
            console.log(`✅ Connected to ${url}`);
            
            const testMessage = {
                type: 'message',
                message: `Test from external client - ${url}`
            };
            
            ws.send(JSON.stringify(testMessage));
        });
        
        ws.on('message', function message(data) {
            console.log(`📥 Response from ${url}:`);
            try {
                const parsed = JSON.parse(data.toString());
                console.log(`   Type: ${parsed.type}`);
                console.log(`   Message: ${parsed.message?.substring(0, 100)}...`);
            } catch (e) {
                console.log(`   Raw: ${data.toString().substring(0, 100)}...`);
            }
            
            ws.close();
        });
        
        ws.on('close', function close() {
            console.log(`👋 Closed connection to ${url}`);
            testCount++;
            
            if (testCount === endpoints.length) {
                console.log('\n🏁 All tests completed!');
                process.exit(0);
            }
        });
        
        ws.on('error', function error(err) {
            console.error(`❌ Error connecting to ${url}: ${err.message}`);
            testCount++;
            
            if (testCount === endpoints.length) {
                console.log('\n🏁 All tests completed!');
                process.exit(0);
            }
        });
        
    }, index * 2000); // Stagger tests
});

// Exit after 30 seconds regardless
setTimeout(() => {
    console.log('\n⏰ Test timeout reached');
    process.exit(0);
}, 30000);
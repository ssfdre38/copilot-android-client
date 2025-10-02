#!/bin/bash

echo "🚀 Setting up CP CLI Android Server with Auto-boot"
echo "================================================="

# Check if running as root for systemctl operations
if [ "$EUID" -eq 0 ]; then
    echo "⚠️  Please run this script as a regular user, not root"
    echo "   The script will use sudo when needed"
    exit 1
fi

# Get the current directory
CURRENT_DIR=$(pwd)
SERVER_DIR="$CURRENT_DIR/server"

echo "1. Checking server setup..."

# Check if we're in the right directory
if [ ! -f "server/server.js" ]; then
    echo "❌ Please run this script from the CopilotAndroidClient directory"
    exit 1
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Installing..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

echo "   ✅ Node.js version: $(node --version)"

# Check npm dependencies
echo "2. Installing server dependencies..."
cd server
if [ ! -d "node_modules" ]; then
    npm install
fi

echo "3. Testing server functionality..."
# Test server briefly
timeout 5 npm start > /dev/null 2>&1 &
TEST_PID=$!
sleep 3

if curl -s http://localhost:3002/health | grep -q "ok"; then
    echo "   ✅ Server test successful"
    kill $TEST_PID 2>/dev/null
else
    echo "   ⚠️  Server test inconclusive, proceeding anyway"
    kill $TEST_PID 2>/dev/null
fi

cd ..

echo "4. Setting up systemctl service..."

# Update service file with correct paths
sed -i "s|WorkingDirectory=.*|WorkingDirectory=$SERVER_DIR|g" server/copilot-server.service
sed -i "s|ReadWritePaths=.*|ReadWritePaths=$SERVER_DIR|g" server/copilot-server.service
sed -i "s|ExecStart=.*|ExecStart=/usr/bin/node $SERVER_DIR/server.js|g" server/copilot-server.service

# Stop any existing service
sudo systemctl stop copilot-server.service 2>/dev/null || true

# Copy service file to systemd
sudo cp server/copilot-server.service /etc/systemd/system/

# Reload systemd and enable service
sudo systemctl daemon-reload
sudo systemctl enable copilot-server.service

echo "5. Starting the service..."
sudo systemctl start copilot-server.service

# Check service status
sleep 2
if sudo systemctl is-active --quiet copilot-server.service; then
    echo "   ✅ Service started successfully"
else
    echo "   ❌ Service failed to start, checking logs..."
    sudo journalctl -u copilot-server.service --no-pager -n 10
    exit 1
fi

echo "6. Verifying service..."
sleep 3
if curl -s http://localhost:3002/health | grep -q "ok"; then
    echo "   ✅ Service is responding correctly"
else
    echo "   ❌ Service is not responding"
    sudo systemctl status copilot-server.service
    exit 1
fi

# Get server IP
SERVER_IP=$(hostname -I | awk '{print $1}')

echo ""
echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo "✅ CP CLI Android Server is now running and will auto-start on boot"
echo "✅ Service status: $(sudo systemctl is-active copilot-server.service)"
echo "✅ Server URL: http://localhost:3002"
echo "✅ WebSocket URL: ws://$SERVER_IP:3002"
echo ""
echo "📱 Android App Configuration:"
echo "   Server IP: $SERVER_IP"
echo "   Port: 3002"
echo "   URL: ws://$SERVER_IP:3002"
echo ""
echo "📋 Service Management Commands:"
echo "   sudo systemctl status copilot-server    # Check status"
echo "   sudo systemctl stop copilot-server      # Stop service"
echo "   sudo systemctl start copilot-server     # Start service"
echo "   sudo systemctl restart copilot-server   # Restart service"
echo "   sudo journalctl -u copilot-server -f    # View logs"
echo ""
echo "🔥 Your server is ready for Android client connections!"
echo "📲 Download the Android app from: https://github.com/ssfdre38/copilot-android-client/releases"
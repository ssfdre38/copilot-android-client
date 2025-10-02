#!/bin/bash

echo "🔧 Starting Copilot CLI Server manually..."

cd /home/ubuntu/CopilotAndroidClient/server

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

echo "🚀 Starting server on port 3001..."
echo "✅ Server URL: ws://$(hostname -I | awk '{print $1}'):3001"
echo "✅ Health check: http://$(hostname -I | awk '{print $1}'):3001/health"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Start the server
npm start
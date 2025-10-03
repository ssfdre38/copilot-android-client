#!/bin/bash

# 🚀 Copilot CLI Android Server Quick Setup Script v1.2.0
# This script sets up the WebSocket server for the Android client

set -e

echo "🤖 Copilot CLI Android Server Setup v1.2.0"
echo "=============================================="

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   log_error "This script should not be run as root for security reasons"
   exit 1
fi

# Get the current directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
SERVER_DIR="$SCRIPT_DIR/server"

log_info "Project root: $PROJECT_ROOT"
log_info "Server directory: $SERVER_DIR"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check Node.js installation
log_info "Checking Node.js installation..."
if command_exists node; then
    NODE_VERSION=$(node --version)
    log_success "Node.js found: $NODE_VERSION"
    
    # Check if version is 16 or higher
    NODE_MAJOR_VERSION=$(echo $NODE_VERSION | cut -d'.' -f1 | cut -d'v' -f2)
    if [ "$NODE_MAJOR_VERSION" -lt 16 ]; then
        log_warning "Node.js version 16 or higher is recommended. Current: $NODE_VERSION"
    fi
else
    log_error "Node.js not found. Please install Node.js 16 or higher."
    log_info "Visit: https://nodejs.org/en/download/"
    echo ""
    log_info "On Ubuntu/Debian: curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - && sudo apt-get install -y nodejs"
    exit 1
fi

# Check npm installation
log_info "Checking npm installation..."
if command_exists npm; then
    NPM_VERSION=$(npm --version)
    log_success "npm found: $NPM_VERSION"
else
    log_error "npm not found. Please install npm."
    exit 1
fi

# Install dependencies
log_info "Installing Node.js dependencies..."
cd "$SERVER_DIR"
if npm install; then
    log_success "Dependencies installed successfully"
else
    log_error "Failed to install dependencies"
    exit 1
fi

# Configure firewall (optional)
log_info "Configuring firewall rules..."
if command_exists ufw; then
    UFW_STATUS=$(sudo ufw status | head -1)
    if [[ $UFW_STATUS == *"active"* ]]; then
        log_info "UFW firewall is active, adding rule for port 3002..."
        if sudo ufw allow 3002/tcp comment "Copilot Android Server" 2>/dev/null; then
            log_success "Firewall rule added for port 3002"
        else
            log_warning "Failed to add firewall rule. You may need to manually allow port 3002"
        fi
    else
        log_warning "UFW firewall is inactive. Port 3002 should be accessible."
    fi
else
    log_warning "UFW not found. Make sure port 3002 is accessible."
fi

# Start server manually (not as service for quick testing)
log_info "Starting server..."
cd "$SERVER_DIR"

# Kill any existing process on port 3002
if lsof -Pi :3002 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warning "Port 3002 is already in use. Attempting to stop existing process..."
    sudo pkill -f "node.*server.js" || true
    sleep 2
fi

# Start server in background
log_info "Starting Copilot CLI Android Server..."
nohup node server.js > server.log 2>&1 &
SERVER_PID=$!

# Wait a moment for server to start
sleep 3

# Check if server is running
if kill -0 $SERVER_PID 2>/dev/null; then
    log_success "Server started successfully (PID: $SERVER_PID)"
else
    log_error "Failed to start server"
    if [ -f server.log ]; then
        log_info "Server log:"
        cat server.log
    fi
    exit 1
fi

# Test the server
log_info "Testing server connectivity..."
if curl -f -s "http://localhost:3002/health" > /dev/null; then
    log_success "Server health check passed"
    
    # Show health check response
    HEALTH_RESPONSE=$(curl -s "http://localhost:3002/health")
    log_info "Health check response: $HEALTH_RESPONSE"
else
    log_warning "Health check failed. Checking server logs..."
    if [ -f server.log ]; then
        tail -10 server.log
    fi
fi

# Get server IP addresses
log_info "Getting server network information..."
echo ""
echo "🌐 Server Access Information:"
echo "=============================="

# Local access
echo "📍 Local Access:"
echo "   http://localhost:3002"
echo "   ws://localhost:3002"

# LAN access
echo ""
echo "🏠 LAN Access (from other devices on your network):"
LOCAL_IPS=$(hostname -I 2>/dev/null || ip route get 1 | awk '{print $7; exit}' 2>/dev/null || echo "Unable to determine")
for IP in $LOCAL_IPS; do
    if [[ $IP =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "   http://$IP:3002"
        echo "   ws://$IP:3002"
    fi
done

echo ""
echo "📱 Android App Configuration:"
echo "============================="
echo "In your Android app, use one of these server URLs:"
echo "• For same device testing: localhost:3002"
echo "• For LAN testing: [LAN_IP]:3002"

echo ""
echo "🔧 Server Management:"
echo "===================="
echo "• Server PID: $SERVER_PID"
echo "• Stop server: kill $SERVER_PID"
echo "• View logs: tail -f $SERVER_DIR/server.log"
echo "• Test health: curl http://localhost:3002/health"

echo ""
echo "📋 To set up as a system service (auto-start on boot):"
echo "======================================================"
echo "Run: sudo ./setup-server-autoboot.sh"

echo ""
log_success "🎉 Server is now running!"
log_info "You can now connect your Android app to this server."
log_info "The server log is being written to: $SERVER_DIR/server.log"

# Save PID for easy management
echo $SERVER_PID > "$SERVER_DIR/server.pid"
log_info "Server PID saved to: $SERVER_DIR/server.pid"

echo ""
echo "🚀 To start the Android app:"
echo "============================"
echo "1. Install the APK on your Android device"
echo "2. Open the app"
echo "3. Enter the server URL (use LAN IP for testing from another device)"
echo "4. Tap 'Connect' then 'Start Chat'"
echo "5. Start chatting with Copilot CLI!"

exit 0
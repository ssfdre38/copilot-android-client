#!/bin/bash

# Copilot Android Client - Complete Server Setup Script
# This script sets up the server and configures it for auto-boot

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root. Please run as a regular user."
   exit 1
fi

print_status "Starting Copilot Android Client Server Setup..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    print_warning "Node.js is not installed. Installing Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
    sudo apt-get install -y nodejs
    print_success "Node.js installed successfully"
else
    print_success "Node.js is already installed: $(node --version)"
fi

# Check if we're in the correct directory
if [[ ! -f "server/server.js" ]]; then
    print_error "Please run this script from the copilot-android-client directory"
    exit 1
fi

# Install server dependencies
print_status "Installing server dependencies..."
cd server
npm install
cd ..
print_success "Dependencies installed"

# Test the server
print_status "Testing server startup..."
cd server
timeout 5s node server.js || true
cd ..
print_success "Server test completed"

# Setup systemctl service
print_status "Setting up systemctl service for auto-boot..."

# Copy service file to systemd directory
sudo cp server/copilot-server.service /etc/systemd/system/

# Update systemd and enable the service
sudo systemctl daemon-reload
sudo systemctl enable copilot-server.service

print_success "Service installed and enabled for auto-boot"

# Start the service
print_status "Starting the Copilot server service..."
sudo systemctl start copilot-server.service

# Check service status
sleep 2
if sudo systemctl is-active --quiet copilot-server.service; then
    print_success "Copilot server is running!"
else
    print_error "Failed to start the service. Checking logs..."
    sudo journalctl -u copilot-server.service --no-pager -n 10
    exit 1
fi

# Setup firewall (if ufw is available)
if command -v ufw &> /dev/null; then
    print_status "Configuring firewall..."
    sudo ufw allow 3002/tcp comment "Copilot Android Client"
    print_success "Firewall configured to allow port 3002"
else
    print_warning "UFW not found. Please manually configure your firewall to allow port 3002"
fi

# Get server information
SERVER_IP=$(hostname -I | awk '{print $1}')
print_success "Setup complete!"
echo ""
echo "========================================"
echo "Copilot Android Client Server Info:"
echo "========================================"
echo "Server IP: $SERVER_IP"
echo "Server Port: 3002"
echo "Full URL: http://$SERVER_IP:3002"
echo "WebSocket URL: ws://$SERVER_IP:3002"
echo ""
echo "Service Management Commands:"
echo "- Start:   sudo systemctl start copilot-server"
echo "- Stop:    sudo systemctl stop copilot-server"
echo "- Restart: sudo systemctl restart copilot-server"
echo "- Status:  sudo systemctl status copilot-server"
echo "- Logs:    sudo journalctl -u copilot-server -f"
echo ""
echo "Use this server URL in your Android app:"
echo "$SERVER_IP:3002"
echo "========================================"

print_status "Testing server health endpoint..."
sleep 2
if curl -s http://localhost:3002/health > /dev/null; then
    print_success "Server health check passed!"
    curl -s http://localhost:3002/health | jq '.' 2>/dev/null || curl -s http://localhost:3002/health
else
    print_warning "Server health check failed. The server might still be starting up."
fi

echo ""
print_success "Server setup completed successfully!"
print_status "The server will automatically start on boot."
print_status "You can now configure your Android app to connect to: $SERVER_IP:3002"
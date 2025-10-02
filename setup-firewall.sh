#!/bin/bash

# 🔥 Copilot Android Client - Firewall Setup Script
# This script configures UFW firewall for the Copilot server

echo "🔥 Setting up firewall for Copilot Android Server"
echo "================================================"

# Check if UFW is installed
if ! command -v ufw >/dev/null 2>&1; then
    echo "📦 Installing UFW firewall..."
    sudo apt-get update
    sudo apt-get install -y ufw
fi

echo "🔧 Configuring firewall rules..."

# Reset UFW to defaults
sudo ufw --force reset

# Set default policies
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow SSH (important!)
sudo ufw allow ssh
sudo ufw allow 22/tcp

# Allow the Copilot server port
sudo ufw allow 3002/tcp comment 'Copilot Android Server'

# Allow common web ports (optional)
echo "🌐 Would you like to allow standard web ports (80, 443)? [y/N]"
read -r response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    sudo ufw allow 80/tcp comment 'HTTP'
    sudo ufw allow 443/tcp comment 'HTTPS'
    echo "✅ Web ports allowed"
fi

# Enable UFW
echo "🚀 Enabling firewall..."
sudo ufw --force enable

echo
echo "✅ Firewall configuration complete!"
echo
echo "📊 Current firewall status:"
sudo ufw status verbose

echo
echo "🔒 Security Summary:"
echo "  ✅ SSH access maintained on port 22"
echo "  ✅ Copilot server accessible on port 3002"
echo "  ✅ All other incoming connections blocked"
echo "  ✅ All outgoing connections allowed"
echo
echo "📱 Your Android app can now connect to:"
echo "  ws://$(hostname -I | awk '{print $1}'):3002"
echo
echo "💡 To modify firewall rules later:"
echo "  View status: sudo ufw status"
echo "  Add rule:    sudo ufw allow port/protocol"
echo "  Remove rule: sudo ufw delete allow port/protocol"
echo "  Disable:     sudo ufw disable"
# Server Configuration

## Your Server is Running!

✅ Server URL: ws://$(hostname -I | awk '{print $1}'):3001
✅ Health Check: http://$(hostname -I | awk '{print $1}'):3001/health
✅ Auto-boot: Enabled (starts automatically on system boot)

## Service Management Commands

```bash
# Check status
sudo systemctl status copilot-server

# Stop service
sudo systemctl stop copilot-server

# Start service  
sudo systemctl start copilot-server

# Restart service
sudo systemctl restart copilot-server

# View logs
sudo journalctl -u copilot-server -f

# Disable auto-boot
sudo systemctl disable copilot-server

# Enable auto-boot
sudo systemctl enable copilot-server
```

## Android App Configuration

1. **Server URL**: ws://$(hostname -I | awk '{print $1}'):3001
2. **API Key**: (leave blank - not required)
3. **Auto Connect**: Enable for convenience

## Network Access

- **Local Network**: Other devices can connect using your IP
- **Firewall**: Ensure port 3001 is open
- **Router**: May need port forwarding for external access

## Security Notes

- Server runs on port 3001 (HTTP/WebSocket)
- No authentication required by default
- Add API key authentication if needed
- Consider using HTTPS/WSS for production

package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class HelpActivity : AppCompatActivity() {

    private lateinit var textHelpContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        
        initViews()
        setupContent()
        
        // Set up the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Help & Documentation"
        
        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
    
    private fun initViews() {
        textHelpContent = findViewById(R.id.textHelpContent)
    }
    
    private fun setupContent() {
        val helpContent = """
# 📱 Copilot CLI Android Client - Help Guide

## 🚀 Quick Start

### 1. Setting Up Your Server
- Go to **Settings** > **Server Management**
- Add a new server with your Copilot CLI server details
- Default: `localhost:3002` for local development

### 2. Connecting
- Enter server URL in the main screen
- Press **Connect** to establish connection
- **Start Chat** to begin conversation

### 3. Using Chat Features
- Type messages naturally to interact with Copilot CLI
- Use keyboard shortcuts for CLI operations
- Chat history is automatically saved

## ⌨️ Keyboard Shortcuts

### Essential CLI Keys
- **Ctrl+C**: Interrupt current operation
- **Ctrl+V**: Paste clipboard content  
- **Tab**: Auto-completion (context-aware)
- **Enter**: Execute command/send message
- **Esc**: Escape current operation
- **↑/↓**: Navigate command history
- **⌫**: Backspace for editing

### Usage Tips
- Use **Tab** for code completion suggestions
- **Ctrl+C** to stop running processes
- **↑** to recall previous commands
- **Enter** to execute or send messages

## 🔧 Settings & Configuration

### Dark Mode
- **Settings** > **Appearance** > **Dark Mode**
- Automatically follows system theme
- Battery-friendly for OLED displays

### Auto-Updates
- **Settings** > **Application** > **Auto-Updates**
- Check for new app versions automatically
- Manual check available in settings

### Server Management
- **Add/Edit/Delete** multiple servers
- **Set Default** server for quick access
- Each server maintains separate chat history

### Chat History
- Automatic conversation saving
- Per-server history isolation
- Configurable retention limits
- Search and export capabilities

## 📡 Server Setup

### Local Development
```bash
# Install dependencies
npm install

# Start server
npm start

# Server runs on port 3002 by default
# Access: http://localhost:3002
```

### Production Deployment
```bash
# Using PM2
pm2 start server.js --name copilot-server

# Using Docker
docker build -t copilot-server .
docker run -p 3002:3002 copilot-server

# Using systemctl (Linux)
sudo systemctl enable copilot-server
sudo systemctl start copilot-server
```

### Network Configuration
- **Default Port**: 3002
- **Protocol**: WebSocket (ws://) or Secure (wss://)
- **Firewall**: Allow inbound connections on server port
- **SSL**: Use wss:// for secure connections

## 🐛 Troubleshooting

### Connection Issues
**Problem**: "Failed to connect to server"
- ✅ Verify server is running and accessible
- ✅ Check firewall settings on server
- ✅ Confirm correct IP address and port
- ✅ Try switching between WiFi and mobile data

**Problem**: "Connection timeout"
- ✅ Check network connectivity
- ✅ Verify server is responding (try browser)
- ✅ Increase timeout in app settings

### App Crashes
**Problem**: App closes unexpectedly
- ✅ Check minimum Android version (API 31+)
- ✅ Clear app cache and data
- ✅ Restart device
- ✅ Update to latest app version

**Problem**: Chat messages not sending
- ✅ Verify connection status
- ✅ Check server logs for errors
- ✅ Try reconnecting to server

### Performance Issues
**Problem**: App running slowly
- ✅ Clear chat history if very large
- ✅ Reduce chat history limit in settings
- ✅ Restart app periodically
- ✅ Close other memory-intensive apps

## 🔒 Privacy & Security

### Data Storage
- Chat history stored locally on device
- Server configurations encrypted
- No data sent to external services
- User controls all data retention

### Network Security
- Use **HTTPS/WSS** for production
- **VPN** recommended for remote access
- **Firewall** rules for server protection
- **Authentication** tokens for enterprise use

## 📋 Common Commands

### Git Operations
```bash
git status
git add .
git commit -m "message"
git push origin main
git pull
git checkout -b feature-branch
```

### Docker Commands
```bash
docker ps
docker images
docker build -t app .
docker run -p 8080:8080 app
docker-compose up -d
```

### Node.js Development
```bash
npm init -y
npm install package-name
npm start
npm run build
npm test
```

### System Administration
```bash
ls -la
cd /path/to/directory
chmod +x script.sh
sudo systemctl status service
top
ps aux | grep process
```

## 💡 Tips & Best Practices

### Efficient Usage
- Use **Tab completion** for faster typing
- Set up **default server** for quick access
- Organize servers by **project/environment**
- Regular **backup** of chat history

### Security Best Practices
- Use **unique server names** for identification
- **Regular updates** for security patches
- **Monitor server logs** for suspicious activity
- **Secure network** connections (VPN/SSL)

### Performance Optimization
- **Limit chat history** to reasonable size
- **Close unused connections** to save battery
- **Use dark mode** on OLED screens
- **Background app refresh** management

## 🆘 Getting Help

### In-App Support
- **Settings** > **Help** (this guide)
- **Settings** > **About** > **Report Issue**
- **Settings** > **About** > **View Source**

### Online Resources
- **GitHub Repository**: Bug reports and feature requests
- **Documentation**: Latest setup guides
- **Community**: Discussion and support

### Contact Information
- **Issues**: GitHub Issues page
- **Features**: Pull requests welcome
- **General**: Community discussions

---

**Made with ❤️ for developers who want to code anywhere, anytime**

*Last updated: Version 1.1.3*
        """.trimIndent()
        
        textHelpContent.text = helpContent
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
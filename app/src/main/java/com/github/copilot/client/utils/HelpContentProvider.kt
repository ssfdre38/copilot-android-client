package com.github.copilot.client.utils

import com.github.copilot.client.model.HelpItem

object HelpContentProvider {
    
    fun getAllHelpItems(): List<HelpItem> = listOf(
        // Getting Started
        HelpItem(
            id = "getting_started",
            title = "Getting Started",
            content = """
# Getting Started with Copilot Android Client

Welcome to the Copilot Android Client! This app allows you to connect to a Copilot CLI server and interact with GitHub Copilot from your mobile device.

## First Setup
1. **Install the app** on your Android device
2. **Configure server connection** in Settings
3. **Connect** to your server
4. **Start chatting** with Copilot!

## Requirements
- Android 12+ (API 31+)
- Network connection to your server
- Valid server running on port 3002 (default)

## Quick Tips
- Use the quick action buttons for common commands
- Enable dark mode in Settings for better night viewing
- Chat history is automatically saved
- Multiple servers can be configured
            """.trimIndent(),
            category = "Setup",
            searchKeywords = listOf("start", "setup", "install", "configure", "first", "begin")
        ),
        
        // Connection Setup
        HelpItem(
            id = "connection_setup",
            title = "Server Connection Setup",
            content = """
# Server Connection Setup

## Server URL Format
Enter your server URL in one of these formats:
- `ws://your-server-ip:3002`
- `wss://your-domain.com:3002` (for SSL)
- `http://your-server-ip:3002` (will auto-convert to WebSocket)

## Common Connection Issues
1. **Connection Failed**: Check if server is running
2. **Timeout**: Verify firewall settings on server
3. **Authentication Error**: Check API key if required

## Server Requirements
- Node.js server running the Copilot bridge
- Port 3002 open (default)
- Network accessible from your device

## Testing Connection
Use the "Test Connection" button in settings to verify your configuration before saving.
            """.trimIndent(),
            category = "Connection",
            searchKeywords = listOf("server", "connection", "url", "websocket", "port", "setup", "configure")
        ),
        
        // Features
        HelpItem(
            id = "features_overview",
            title = "App Features",
            content = """
# App Features Overview

## Chat Interface
- **Real-time messaging** with Copilot
- **Message history** saved automatically
- **Typing indicators** when Copilot is responding
- **Copy messages** by long-pressing

## Quick Actions
- **Tab**: Auto-completion
- **Escape**: Cancel current operation
- **Ctrl+C**: Interrupt command
- **Up/Down**: Navigate command history
- **Clear**: Clear terminal screen

## Dark Mode
- **Auto-detection** based on system settings
- **Manual toggle** in Settings
- **Battery friendly** for OLED displays

## Multi-Server Support
- **Save multiple servers** for different projects
- **Quick switching** between servers
- **Per-server chat history**
- **Default server** setting
            """.trimIndent(),
            category = "Features",
            searchKeywords = listOf("features", "chat", "history", "dark", "mode", "servers", "actions", "buttons")
        ),
        
        // Troubleshooting
        HelpItem(
            id = "troubleshooting",
            title = "Troubleshooting",
            content = """
# Troubleshooting Guide

## Connection Problems
**Problem**: Cannot connect to server
**Solutions**:
- Verify server is running: `sudo systemctl status copilot-server`
- Check server URL format
- Ensure port 3002 is open
- Try using IP address instead of hostname

**Problem**: Connection drops frequently
**Solutions**:
- Check network stability
- Verify server resources
- Restart the server service

## App Issues
**Problem**: App crashes or freezes
**Solutions**:
- Force close and restart app
- Clear app cache in Android settings
- Reinstall app if persistent

**Problem**: Messages not sending
**Solutions**:
- Check connection status
- Restart WebSocket connection
- Verify server response

## Performance Issues
**Problem**: App is slow or laggy
**Solutions**:
- Enable dark mode to reduce battery usage
- Clear old chat history
- Close other apps to free memory
- Restart your device
            """.trimIndent(),
            category = "Support",
            searchKeywords = listOf("problem", "issue", "troubleshoot", "error", "crash", "slow", "fix", "help")
        ),
        
        // Server Management
        HelpItem(
            id = "server_management",
            title = "Server Management",
            content = """
# Server Management Guide

## Adding Servers
1. Go to **Settings** → **Servers**
2. Tap **Add Server**
3. Enter server details:
   - Name (for identification)
   - URL (WebSocket endpoint)
   - API Key (if required)
4. **Test Connection** before saving
5. **Save** and optionally set as default

## Managing Multiple Servers
- **Switch servers**: Use dropdown in main screen
- **Edit server**: Long-press server in list
- **Delete server**: Swipe server in list
- **Set default**: Check "Default" when editing

## Server Status
- 🟢 **Connected**: Ready to use
- 🟡 **Connecting**: Attempting connection
- 🔴 **Disconnected**: Connection failed
- ⚫ **Offline**: No network available

## Backup & Sync
- Server configurations are saved locally
- Export/Import feature available in Settings
- Chat history is per-server basis
            """.trimIndent(),
            category = "Servers",
            searchKeywords = listOf("server", "manage", "add", "delete", "switch", "multiple", "backup", "export")
        ),
        
        // Privacy & Security
        HelpItem(
            id = "privacy_security",
            title = "Privacy & Security",
            content = """
# Privacy & Security

## Data Storage
- **Chat history**: Stored locally on device
- **Server configs**: Encrypted and stored locally
- **No cloud sync**: All data stays on your device
- **API keys**: Securely stored using Android Keystore

## Network Security
- **WebSocket connections**: Can use SSL/TLS (wss://)
- **No data collection**: App doesn't send analytics
- **Local processing**: No data sent to third parties
- **Server communication**: Only with your configured servers

## Permissions
- **Internet**: Required for server communication
- **Network State**: To detect connectivity
- **No sensitive permissions**: No camera, location, contacts, etc.

## Best Practices
- Use SSL/TLS connections when possible
- Keep API keys secure
- Use VPN for remote connections
- Regularly update the app
- Monitor server access logs
            """.trimIndent(),
            category = "Security",
            searchKeywords = listOf("privacy", "security", "data", "encryption", "ssl", "permissions", "safe")
        ),
        
        // Updates
        HelpItem(
            id = "app_updates",
            title = "App Updates",
            content = """
# App Updates

## Auto-Updates
- **Check for updates**: Automatic daily check
- **Download**: Manual download from GitHub
- **Install**: Side-load APK file
- **Notifications**: Update alerts in app

## Manual Updates
1. Go to **Settings** → **About**
2. Tap **Check for Updates**
3. Download new APK if available
4. Install over existing app (data preserved)

## Update Process
- **Check**: App checks GitHub releases
- **Compare**: Version comparison with current
- **Download**: Direct APK download link
- **Install**: Standard Android APK install

## Release Channels
- **Stable**: Main releases (recommended)
- **Beta**: Pre-release testing versions
- **Debug**: Development builds

## Backup Before Updates
- Settings are preserved during updates
- Chat history is maintained
- Server configurations kept
- Consider manual backup for safety
            """.trimIndent(),
            category = "Updates",
            searchKeywords = listOf("update", "upgrade", "version", "download", "install", "apk", "release")
        ),
        
        // Advanced Usage
        HelpItem(
            id = "advanced_usage",
            title = "Advanced Usage",
            content = """
# Advanced Usage

## Custom Commands
- **Terminal commands**: Full shell access through server
- **Copilot queries**: Natural language programming questions
- **File operations**: Navigate and edit files remotely
- **Git operations**: Commit, push, pull through terminal

## Keyboard Shortcuts
- **Volume Up + Power**: Take screenshot
- **Back button**: Minimize terminal
- **Home button**: Background app
- **Recent button**: Switch between apps

## Integration Tips
- **Development workflow**: Use with IDE on desktop
- **Code review**: Quick queries about code
- **Learning**: Ask Copilot to explain concepts
- **Debugging**: Get help with error messages

## Performance Optimization
- **Clear history**: Regularly clean old chats
- **Close unused connections**: Disconnect when not needed
- **Background limits**: Android may limit background activity
- **Network optimization**: Use WiFi when possible

## Automation
- **Quick actions**: Set up custom command buttons
- **Session persistence**: Server maintains context
- **Command history**: Navigate previous commands
- **Batch operations**: Send multiple commands
            """.trimIndent(),
            category = "Advanced",
            searchKeywords = listOf("advanced", "commands", "shortcuts", "integration", "automation", "performance")
        )
    )
    
    fun getHelpItemsByCategory(category: String): List<HelpItem> {
        return getAllHelpItems().filter { it.category == category }
    }
    
    fun searchHelpItems(query: String): List<HelpItem> {
        val searchTerm = query.lowercase()
        return getAllHelpItems().filter { item ->
            item.title.lowercase().contains(searchTerm) ||
            item.content.lowercase().contains(searchTerm) ||
            item.searchKeywords.any { keyword -> keyword.lowercase().contains(searchTerm) }
        }
    }
    
    fun getCategories(): List<String> {
        return getAllHelpItems().map { it.category }.distinct().sorted()
    }
}
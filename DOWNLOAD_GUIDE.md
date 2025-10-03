# 📱 Copilot Android Client - Download Guide

## 🚀 Latest Release - v1.2.2 (Android 15 Compatible)

### 📥 Direct Download Links

**🎯 Recommended - Android 15 Compatible:**
```
https://github.com/ssfdre38/copilot-android-client/releases/download/v1.2.2/copilot-android-client-v1.2.2-android15-release.apk
```
- **Size**: 13.0 MB
- **Type**: Optimized release build
- **Android**: 12+ (API 31+) - Optimized for Android 15
- **Package**: com.ssfdre38.cpcli.android.client

## 📋 System Requirements

- **Android Version**: 12+ (API 31+) **[Android 15 Compatible]**
- **Storage**: ~20 MB free space
- **Network**: Internet connection to reach server
- **Permissions**: Network access, notifications

## 🔧 Installation Steps

### For Android 15 Users:
1. **Download APK** - Use the direct link above
2. **Enable Unknown Apps** - Settings → Security → Install unknown apps → [Your Browser] → Allow
3. **Install APK** - Tap the downloaded file and follow prompts
4. **Grant Permissions** - Allow network and notification permissions
5. **Open App** - Launch "Copilot Client" from your app drawer
6. **Configure Server** - Enter your server details

### ⚠️ Android 15 Installation Notes

If you get "package appears to be invalid":
- Make sure you downloaded the v1.2.2 APK (not older versions)
- Restart your device and try again
- Ensure sufficient storage space
- Verify "Install unknown apps" is enabled for your download source

## 🖥️ Server Information

### Your Server Details
- **Server URL**: `ws://s1.pepperbacks.xyz:3002`
- **Status**: ✅ Running and configured
- **Auto-boot**: ✅ Enabled (systemctl service)
- **Health Check**: http://s1.pepperbacks.xyz:3002/health

### Test Connection
You can test your server connectivity:
```bash
curl http://s1.pepperbacks.xyz:3002/health
```

## ✨ App Features

### 🔥 Core Features
- ✅ **Multi-server Support** - Connect to multiple Copilot instances
- ✅ **Dark Mode** - Complete theme customization with auto-detection
- ✅ **Chat History** - Persistent conversation storage per server
- ✅ **Auto-updates** - Automatic update checking and download
- ✅ **Offline Help** - Complete documentation and guides
- ✅ **Quick Actions** - Common keystroke buttons for terminal operations

### 🎯 Quick Action Buttons Available
- 💡 **Explain Code** - "Please explain this code:"
- ⚡ **Shell Command** - "I need a shell command to:"
- 🔧 **Git Help** - "Help me with git to:"
- 🐛 **Debug** - "Debug this error:"
- ⚡ **Optimize** - "How can I optimize this code:"
- 🧪 **Write Tests** - "Write unit tests for:"
- 📝 **Documentation** - "Add documentation for:"
- 🔄 **Refactor** - "How should I refactor:"
- 🔒 **Security Review** - "Review security of:"
- ⚡ **Performance** - "Improve performance of:"

### 📱 Server Connection Setup
In the app, configure your connection:
1. Open Settings → Manage Servers
2. Add New Server
3. Enter details:
   - **Name**: Your Server
   - **URL**: `ws://s1.pepperbacks.xyz:3002`
   - **Auto-connect**: ✅ (optional)

## 🆘 Troubleshooting

### Common Issues

**❌ Connection Failed**
- Verify server is accessible: `curl http://s1.pepperbacks.xyz:3002/health`
- Check your internet connection
- Try switching from WiFi to mobile data (or vice versa)

**❌ App Won't Install**
- Enable "Install from Unknown Sources" in Android settings
- Check available storage (need ~20MB)
- Clear Downloads folder if full

**❌ Server Not Responding**
- Server status: `systemctl status copilot-server`
- Check server logs: `journalctl -u copilot-server -f`
- Restart server: `sudo systemctl restart copilot-server`

### Getting Help
- Use the built-in Help & Documentation (Settings → Help & Documentation)
- Check GitHub issues: https://github.com/ssfdre38/copilot-android-client/issues
- All help content is available offline in the app

## 🔄 Updates

The app checks for updates automatically. You can also:
- **Manual Check**: Settings → Check for Updates
- **Auto-updates**: Enabled by default
- **GitHub Releases**: https://github.com/ssfdre38/copilot-android-client/releases

---

**🎉 Ready to code anywhere, anytime!**
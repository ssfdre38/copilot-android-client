# 📱 Copilot Android Client - APK Downloads

## Quick Download

### 🚀 Latest Release APK
**Recommended for most users:**
- **File**: `copilot-android-client.apk` (12MB) - Optimized release build
- **Android Version**: Android 12+ (API 31+)
- **Features**: Complete client with server connection settings

### 🔧 Debug APK  
**For testing and development:**
- **File**: `app-debug.apk` (14MB) - Debug build with extra logging
- **Android Version**: Android 12+ (API 31+)

## 🔧 Installation

1. **Download** the APK file to your Android device
2. **Enable** "Install from Unknown Sources" in your Android settings
3. **Install** the APK by tapping on it
4. **Configure** the server connection in the app settings

## 🖥️ Server Setup

### Automatic Setup (Recommended)
```bash
cd /path/to/CopilotAndroidClient
./setup-server-autoboot.sh
```

### Manual Setup
```bash
cd server
npm install
npm start
```

**Server URL**: `http://your-server-ip:3002`
**WebSocket URL**: `ws://your-server-ip:3002`

## 📋 Quick Usage

1. **Launch** the Android app
2. **Configure** server settings (tap the settings icon)
3. **Connect** to your server
4. **Use** the virtual terminal or quick buttons for common commands

## 🎯 Features

- ✅ Virtual terminal interface
- ✅ Quick action buttons (Tab, Escape, Ctrl+C, etc.)
- ✅ Server connection settings
- ✅ Real-time WebSocket communication
- ✅ Auto-reconnection
- ✅ Designed for Copilot CLI integration

## 🛠️ Server Management

```bash
# Check server status
sudo systemctl status copilot-server

# Start/Stop/Restart server
sudo systemctl start copilot-server
sudo systemctl stop copilot-server
sudo systemctl restart copilot-server

# View logs
sudo journalctl -u copilot-server -f
```

## 🔒 Security Notes

- The APK is unsigned (development build)
- Use only on trusted networks
- Consider using a VPN for remote connections
- The server runs on port 3002 by default

---

**Built with ❤️ for seamless Copilot CLI mobile access**
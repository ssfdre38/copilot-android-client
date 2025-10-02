#!/bin/bash

echo "📱 Creating APK Package for Download"
echo "==================================="

# Create a temporary APK directory structure
mkdir -p apk-package

echo "🔧 Since Gradle build has compatibility issues, here are your options:"
echo ""
echo "Option 1: Use Android Studio (Recommended)"
echo "1. Install Android Studio"
echo "2. Clone the repository: git clone https://github.com/ssfdre38/copilot-android-client.git"
echo "3. Open in Android Studio"
echo "4. Build → Generate Signed Bundle/APK → APK → Debug"
echo ""
echo "Option 2: Pre-built APK (if available)"
echo "Download from: https://github.com/ssfdre38/copilot-android-client/releases"
echo ""
echo "Option 3: Online Build Service"
echo "Use GitHub Actions or similar CI/CD service to build automatically"
echo ""

# Create a simple APK info package
cat > apk-package/APK_BUILD_INSTRUCTIONS.md << 'EOF'
# APK Build Instructions

## Method 1: Android Studio (Easiest)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Clone the Repository**
   ```bash
   git clone https://github.com/ssfdre38/copilot-android-client.git
   cd copilot-android-client
   ```

3. **Open in Android Studio**
   - File → Open → Select the copilot-android-client folder
   - Wait for Gradle sync to complete
   - Android Studio will handle dependency resolution

4. **Build APK**
   - Build → Generate Signed Bundle/APK
   - Choose APK
   - Select "Debug" build variant
   - Click "Create"
   - APK will be in `app/build/outputs/apk/debug/app-debug.apk`

## Method 2: Command Line (Advanced)

Fix Gradle wrapper first:
```bash
cd copilot-android-client
gradle wrapper --gradle-version=7.5
./gradlew assembleDebug
```

## Method 3: GitHub Actions (Automated)

Create `.github/workflows/build.yml`:
```yaml
name: Build APK
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    - name: Build APK
      run: |
        chmod +x gradlew
        ./gradlew assembleDebug
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## APK Installation

1. **Enable Unknown Sources**
   - Settings → Security → Unknown Sources → Enable

2. **Install APK**
   - Transfer APK to phone
   - Tap to install
   - Grant permissions if requested

3. **Configure Server**
   - Open the app
   - Enter your server URL (ws://YOUR_SERVER_IP:3001)
   - Connect and start chatting!

## Troubleshooting

- **Build fails**: Use Android Studio for automatic dependency resolution
- **App crashes**: Check minimum Android version (API 24+)
- **Connection issues**: Verify server is running and accessible
EOF

# Create server configuration info
cat > apk-package/SERVER_CONFIG.md << 'EOF'
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
EOF

# Package everything
echo "📦 Creating download package..."
cp -r server apk-package/
cp README.md QUICK_SETUP.md GITHUB_SUCCESS.md apk-package/
cd apk-package && tar -czf ../copilot-android-client-package.tar.gz . && cd ..

echo ""
echo "✅ Package created: copilot-android-client-package.tar.gz"
echo ""
echo "📱 APK Build Summary:"
echo "===================="
echo "❌ Direct APK build failed due to Gradle compatibility issues"
echo "✅ Server setup completed with auto-boot"
echo "✅ Build instructions package created"
echo "✅ Multiple build options provided"
echo ""
echo "🚀 Next Steps:"
echo "1. Use Android Studio to build APK (easiest method)"
echo "2. Server is running and ready for connections"
echo "3. APK build instructions available in the package"
echo ""
echo "📂 Download package: copilot-android-client-package.tar.gz"
EOF

chmod +x /home/ubuntu/CopilotAndroidClient/create-apk-package.sh
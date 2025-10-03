#!/bin/bash

# Build Stable Debug APK that installs properly
# This script builds a debug APK with proper signing for installation

set -e  # Exit on any error

echo "Building Stable Installable Debug APK..."

# Clean previous builds
./gradlew clean

# Build debug APK (which has proper signing)
./gradlew assembleDebug

# Get version info from build.gradle
VERSION=$(grep "versionName" app/build.gradle | grep -o '"[^"]*"' | tr -d '"')
VERSION_CODE=$(grep "versionCode" app/build.gradle | grep -o '[0-9]*')

echo "Version: $VERSION (Code: $VERSION_CODE)"

# Copy the built APK with version name
cp app/build/outputs/apk/debug/app-debug.apk "copilot-android-client-v${VERSION}-stable-debug.apk"

# Create APK info file
cat > "copilot-android-client-v${VERSION}-stable-debug.apk.info" << EOF
APK Information
===============
Version: $VERSION
Version Code: $VERSION_CODE
Package: com.ssfdre38.cpcli.android.client
Target SDK: 34 (Android 14)
Min SDK: 24 (Android 7.0)
Build Type: Debug (Signed)
Signing: Android Debug Keystore
Installation: Should install properly on all Android devices

Features:
- Fixed APK installation issues
- Proper Android signing
- Works on Android 7.0+
- Material Design 3 UI
- Tablet support
- WebSocket connection to Copilot CLI
- Auto server discovery
- Chat history
- Dark mode
- Multi-server support

Installation Instructions:
1. Enable "Install from unknown sources" in Android settings
2. Download and install this APK
3. Open the app and configure your server connection
4. Enjoy GitHub Copilot CLI on your Android device!
EOF

echo "Build completed successfully!"
echo "APK: copilot-android-client-v${VERSION}-stable-debug.apk"
echo "Info: copilot-android-client-v${VERSION}-stable-debug.apk.info"

# Verify the APK is properly built
if [ -f "copilot-android-client-v${VERSION}-stable-debug.apk" ]; then
    echo "✅ APK built successfully: $(ls -lh copilot-android-client-v${VERSION}-stable-debug.apk | awk '{print $5}')"
else
    echo "❌ APK build failed!"
    exit 1
fi
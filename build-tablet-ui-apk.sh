#!/bin/bash

# Build APK for Copilot Android Client - Tablet UI Optimized
# Version 1.3.0

set -e

echo "🚀 Building Copilot Android Client v1.3.0 with Tablet UI..."
echo "============================================================"

# Check if we're in the right directory
if [ ! -f "app/build.gradle" ]; then
    echo "❌ Please run this script from the project root directory"
    exit 1
fi

echo "1. Cleaning previous builds..."
./gradlew clean

echo "2. Building release APK (Tablet UI optimized)..."
./gradlew assembleRelease --no-daemon --console=plain || {
    echo "❌ Build failed. Check the error messages above."
    exit 1
}

# Get version info
VERSION=$(grep "versionName" app/build.gradle | grep -o '"[^"]*"' | tr -d '"')
VERSION_CODE=$(grep "versionCode" app/build.gradle | grep -o '[0-9]\+')

echo ""
echo "3. Build completed successfully! 🎉"
echo "=================================="

# Find the release APK
RELEASE_APK=$(find app/build/outputs/apk/release -name "*.apk" | head -1)

if [ -n "$RELEASE_APK" ]; then
    # Copy APK with proper naming
    APK_NAME="copilot-android-client-v${VERSION}-tablet-ui-release.apk"
    cp "$RELEASE_APK" "$APK_NAME"
    
    APK_SIZE=$(du -h "$APK_NAME" | cut -f1)
    echo "📦 APK created: $APK_NAME"
    echo "📏 Size: $APK_SIZE"
    echo "🎯 Version: $VERSION (code: $VERSION_CODE)"
    echo "📱 Target: Android 14 (API 34)"
    echo "📱 Min: Android 12 (API 31)"
    echo "📟 Features: Tablet UI, Landscape mode, Responsive design"
    
    # Create APK info file
    cat > "${APK_NAME}.info" << EOF
APK Information:
================
Version: $VERSION
Version Code: $VERSION_CODE
Target SDK: 34 (Android 14)
Min SDK: 31 (Android 12)
Built: $(date)
Signed: Debug signature for compatibility
Package: com.ssfdre38.cpcli.android.client

New Features:
=============
- Tablet optimized layouts (sw600dp)
- Landscape mode with two-column design
- Enhanced keyboard buttons for tablets
- Responsive design for all screen sizes
- Better touch targets and spacing
- Improved typography for larger screens

Installation Notes:
===================
- Compatible with Android 12 and above
- Optimized for 7"+ tablets
- Enable "Install from Unknown Sources" 
- Self-signed for compatibility
EOF

    echo ""
    echo "📱 Installation instructions:"
    echo "1. Enable 'Install Unknown Apps' in Android settings"
    echo "2. Transfer $APK_NAME to your device"
    echo "3. Install the APK"
    echo "4. Configure your Copilot server URL in the app"
    echo ""
    echo "🚀 Ready for tablets and phones!"
    
else
    echo "⚠️  APK file not found. Build may have failed."
    exit 1
fi
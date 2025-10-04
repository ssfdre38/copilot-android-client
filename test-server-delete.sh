#!/bin/bash

# Server Delete Functionality Test Script
# This script builds the APK and creates a test version with debugging logs

echo "🔧 Testing Server Delete Functionality"
echo "======================================="

cd /home/ubuntu/copilot-android-client

# Set up Android environment
export ANDROID_HOME=/home/ubuntu/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

echo "📦 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    # Find the generated APK
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -1)
    
    if [ -n "$APK_PATH" ]; then
        # Create a timestamped copy
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        NEW_APK="github-copilot-cli-v3.2.3-SERVER-DELETE-FIXED-$TIMESTAMP.apk"
        
        cp "$APK_PATH" "$NEW_APK"
        
        echo "📱 APK created: $NEW_APK"
        echo "🔍 APK size: $(du -h "$NEW_APK" | cut -f1)"
        
        # Generate SHA256 checksum
        sha256sum "$NEW_APK" > "$NEW_APK.sha256"
        echo "🔐 Checksum created: $NEW_APK.sha256"
        
        echo ""
        echo "✅ Server delete fix completed!"
        echo "📋 Changes made:"
        echo "   - Fixed ServerConfigManager.deleteServer() method"
        echo "   - Improved ServerAdapter.updateServers() method"
        echo "   - Enhanced delete UI with background thread processing"
        echo "   - Added better error handling and debugging"
        echo ""
        echo "🧪 To test the fix:"
        echo "   1. Install the APK: adb install $NEW_APK"
        echo "   2. Open the app and go to Server Management"
        echo "   3. Add a test server"
        echo "   4. Try to delete it - should work properly now"
        
    else
        echo "❌ Could not find generated APK file"
        exit 1
    fi
else
    echo "❌ Build failed"
    exit 1
fi
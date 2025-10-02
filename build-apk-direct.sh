#!/bin/bash

echo "🚀 Building APK for Android 12+ devices..."
echo "========================================="

# Set Android SDK environment
export ANDROID_HOME=/home/ubuntu/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0

cd /home/ubuntu/CopilotAndroidClient

echo "1. Checking Android SDK setup..."
if [ ! -d "$ANDROID_HOME/platforms/android-34" ]; then
    echo "   Installing Android SDK components..."
    yes | sdkmanager --licenses >/dev/null 2>&1
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1
fi

echo "2. Cleaning previous builds..."
./gradlew clean >/dev/null 2>&1

echo "3. Building debug APK..."
./gradlew assembleDebug --no-daemon --console=plain

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 APK Build Successful!"
    echo "======================="
    
    APK_PATH=$(find . -name "app-debug.apk" | head -1)
    if [ -n "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📦 APK Location: $APK_PATH"
        echo "📏 APK Size: $APK_SIZE"
        echo ""
        echo "📱 Installation Instructions:"
        echo "1. Download the APK file"
        echo "2. Enable 'Unknown Sources' in Android settings"
        echo "3. Install the APK on your Android 12+ device"
        echo "4. Configure server URL: ws://YOUR_SERVER_IP:3001"
        echo ""
        echo "✅ Ready to use enhanced Copilot CLI on mobile!"
    else
        echo "⚠️  APK file not found in expected location"
    fi
else
    echo "❌ APK build failed"
    echo "Check the error messages above for troubleshooting"
fi
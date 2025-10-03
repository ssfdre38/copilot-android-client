#!/bin/bash

# Android Emulator APK Testing Script
# This script starts an emulator and tests the APK installation and basic functionality

set -e

echo "🤖 Starting Android Emulator APK Testing..."

# Set up Android environment
export ANDROID_HOME=/home/ubuntu/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

# Check if KVM is available
if [ ! -w /dev/kvm ]; then
    echo "⚠️  Warning: KVM not available. You may need to:"
    echo "   sudo usermod -a -G kvm \$USER"
    echo "   Then log out and back in"
    echo ""
fi

# Start emulator in background
echo "📱 Starting Android emulator..."
nohup $ANDROID_HOME/emulator/emulator -avd test_device -no-window -no-audio -no-boot-anim > emulator_test.log 2>&1 &
EMULATOR_PID=$!

echo "⏳ Waiting for emulator to start (this may take 2-3 minutes)..."
timeout 300 bash -c 'until $ANDROID_HOME/platform-tools/adb devices | grep -q "device$"; do sleep 5; done' || {
    echo "❌ Emulator failed to start within 5 minutes"
    kill $EMULATOR_PID 2>/dev/null || true
    exit 1
}

echo "✅ Emulator started successfully!"

# Wait for emulator to be fully ready
echo "⏳ Waiting for emulator to be fully ready..."
$ANDROID_HOME/platform-tools/adb wait-for-device
sleep 30

# Find the latest APK
APK_FILE=$(ls -t copilot-android-client-v*.apk 2>/dev/null | head -1)
if [ -z "$APK_FILE" ]; then
    echo "❌ No APK file found! Please build the APK first."
    kill $EMULATOR_PID 2>/dev/null || true
    exit 1
fi

echo "📦 Installing APK: $APK_FILE"
$ANDROID_HOME/platform-tools/adb install "$APK_FILE"

echo "🚀 Launching app..."
$ANDROID_HOME/platform-tools/adb shell am start -n com.ssfdre38.cpcli.android.client/.MainActivity

echo "⏳ Waiting for app to launch..."
sleep 10

# Check if app is running
if $ANDROID_HOME/platform-tools/adb shell pidof com.ssfdre38.cpcli.android.client > /dev/null; then
    echo "✅ App launched successfully and is running!"
    
    # Capture screenshot
    echo "📸 Taking screenshot..."
    $ANDROID_HOME/platform-tools/adb exec-out screencap -p > app_screenshot.png
    echo "Screenshot saved as app_screenshot.png"
    
    # Get basic app info
    echo "📊 App information:"
    $ANDROID_HOME/platform-tools/adb shell dumpsys package com.ssfdre38.cpcli.android.client | grep -E "(versionName|versionCode|targetSdk)"
    
    echo ""
    echo "🧪 Basic functionality test completed!"
    echo "The app appears to be working correctly."
    echo ""
    echo "Manual testing steps:"
    echo "1. ✅ App launches without crashing"
    echo "2. ⏳ Try pressing 'Start Chat' button"
    echo "3. ⏳ Test server connection settings"
    echo "4. ⏳ Check if keyboard shortcuts work"
    echo ""
    echo "To view the emulator:"
    echo "   $ANDROID_HOME/emulator/emulator -avd test_device"
    echo ""
    echo "To stop the emulator:"
    echo "   $ANDROID_HOME/platform-tools/adb emu kill"
    
else
    echo "❌ App failed to launch or crashed immediately"
    echo "Checking logcat for errors..."
    $ANDROID_HOME/platform-tools/adb logcat -t 50 | grep -i "copilot\|crash\|error"
    kill $EMULATOR_PID 2>/dev/null || true
    exit 1
fi

echo ""
echo "🏁 Testing completed! Emulator is still running for manual testing."
echo "Run '$ANDROID_HOME/platform-tools/adb emu kill' to stop the emulator when done."
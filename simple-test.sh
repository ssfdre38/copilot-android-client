#!/bin/bash

# Simple APK Testing Script
# This script tests the APK on a lightweight emulator setup

set -e

# Environment setup
export ANDROID_HOME=/home/ubuntu/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Simple APK Testing Setup${NC}"

# Check if emulator is available
if ! command -v emulator &> /dev/null; then
    echo -e "${RED}Emulator not found. Install Android emulator first.${NC}"
    exit 1
fi

# List available AVDs
echo -e "${YELLOW}Available Android Virtual Devices:${NC}"
emulator -list-avds

# Quick device check for testing
echo -e "${YELLOW}Checking ADB devices:${NC}"
adb devices

# Build APK for testing
echo -e "${YELLOW}Building debug APK for testing...${NC}"
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo -e "${GREEN}Debug APK built successfully: $APK_PATH${NC}"
    echo -e "${YELLOW}File size: $(du -h "$APK_PATH" | cut -f1)${NC}"
    
    # Check APK structure
    echo -e "${YELLOW}APK structure check:${NC}"
    unzip -l "$APK_PATH" | head -20
    
    echo -e "${GREEN}APK ready for manual testing${NC}"
    echo -e "${YELLOW}To test manually:${NC}"
    echo "1. Start an emulator: emulator -avd test_device &"
    echo "2. Wait for boot, then install: adb install -r $APK_PATH"
    echo "3. Launch app: adb shell am start -n com.ssfdre38.cpcli.android.client/.MainActivity"
else
    echo -e "${RED}Failed to build APK${NC}"
    exit 1
fi
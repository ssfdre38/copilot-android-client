#!/bin/bash

# APK Testing Script with Android Emulator
# This script builds and tests the APK in an emulator before release

set -e

# Environment setup
export ANDROID_HOME=/home/ubuntu/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting APK build and test process...${NC}"

# Function to wait for emulator to be ready
wait_for_emulator() {
    echo -e "${YELLOW}Waiting for emulator to boot...${NC}"
    while [ "`adb shell getprop sys.boot_completed | tr -d '\r' `" != "1" ] ; do 
        sleep 2
    done
    echo -e "${GREEN}Emulator is ready!${NC}"
}

# Function to install and test APK
test_apk() {
    local apk_path=$1
    echo -e "${YELLOW}Testing APK: $apk_path${NC}"
    
    if [ ! -f "$apk_path" ]; then
        echo -e "${RED}APK file not found: $apk_path${NC}"
        return 1
    fi
    
    # Install APK
    echo -e "${YELLOW}Installing APK...${NC}"
    adb install -r "$apk_path"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}APK installed successfully!${NC}"
        
        # Try to launch the app
        echo -e "${YELLOW}Launching app...${NC}"
        adb shell am start -n com.ssfdre38.cpcli.android.client/.MainActivity
        
        # Wait a moment for app to load
        sleep 5
        
        # Check if app is running
        APP_PID=$(adb shell pidof com.ssfdre38.cpcli.android.client)
        if [ ! -z "$APP_PID" ]; then
            echo -e "${GREEN}App started successfully! PID: $APP_PID${NC}"
            
            # Take a screenshot for verification
            adb shell screencap -p /sdcard/app_test.png
            adb pull /sdcard/app_test.png ./test_screenshot.png
            echo -e "${GREEN}Screenshot saved as test_screenshot.png${NC}"
            
            # Test basic functionality - check if main activity is visible
            sleep 3
            ACTIVITY=$(adb shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp' | head -1)
            echo -e "${YELLOW}Current focus: $ACTIVITY${NC}"
            
            return 0
        else
            echo -e "${RED}App failed to start!${NC}"
            return 1
        fi
    else
        echo -e "${RED}APK installation failed!${NC}"
        return 1
    fi
}

# Build the APK first
echo -e "${YELLOW}Building APK...${NC}"
./gradlew assembleDebug

# Start emulator in background
echo -e "${YELLOW}Starting emulator...${NC}"
nohup emulator -avd test_device -no-window -no-audio > emulator.log 2>&1 &
EMULATOR_PID=$!

# Wait for emulator to be ready
wait_for_emulator

# Test the newly built APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
test_apk "$APK_PATH"

# Clean up
echo -e "${YELLOW}Stopping emulator...${NC}"
adb emu kill
wait $EMULATOR_PID 2>/dev/null || true

echo -e "${GREEN}APK testing complete!${NC}"
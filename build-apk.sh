#!/bin/bash

echo "📱 Building Copilot Android Client APK"
echo "===================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle" ]; then
    echo "❌ Please run this script from the project root directory"
    exit 1
fi

echo "1. Checking build requirements..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install OpenJDK 11 or later."
    exit 1
fi

# Check Android SDK (if available)
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Using Gradle wrapper only."
else
    echo "✅ Android SDK found at $ANDROID_HOME"
fi

echo "2. Setting up Gradle wrapper..."
if [ ! -f "gradlew" ]; then
    echo "   Creating Gradle wrapper..."
    gradle wrapper --gradle-version=8.0 || {
        echo "❌ Failed to create Gradle wrapper"
        exit 1
    }
fi

chmod +x gradlew

echo "3. Building debug APK..."
echo "   This may take several minutes on first build..."

# Build the APK
./gradlew assembleDebug --no-daemon --console=plain || {
    echo "❌ Build failed. Check the error messages above."
    echo ""
    echo "Common solutions:"
    echo "- Ensure you have Java 11+ installed"
    echo "- Check your internet connection for dependency downloads"
    echo "- Try: ./gradlew clean assembleDebug"
    exit 1
}

echo ""
echo "4. Build completed successfully! 🎉"
echo "================================="

# Find the APK
APK_PATH=$(find . -name "*.apk" -path "*/outputs/apk/debug/*" | head -1)

if [ -n "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "📦 APK created: $APK_PATH"
    echo "📏 Size: $APK_SIZE"
    echo ""
    echo "📱 Installation instructions:"
    echo "1. Enable 'Unknown Sources' in Android settings"
    echo "2. Transfer APK to your device"
    echo "3. Install the APK"
    echo "4. Configure your Copilot server URL in the app"
    echo ""
    echo "🚀 Ready to use!"
else
    echo "⚠️  APK file not found. Build may have completed without creating APK."
fi

echo ""
echo "Additional commands:"
echo "- Clean build: ./gradlew clean"
echo "- Install to connected device: ./gradlew installDebug"
echo "- View tasks: ./gradlew tasks"
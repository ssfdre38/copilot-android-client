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

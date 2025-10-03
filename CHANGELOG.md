# Changelog

## Version 1.3.1 - Critical Crash Fix (2024-12-19)

### 🔧 Critical Fixes
- **App Crash Fixed**: Resolved critical crash when starting chat activity
- **Error Handling**: Added comprehensive error handling throughout the app
- **WebSocket Issues**: Fixed WebSocket connection and initialization problems
- **UI Stability**: Enhanced UI stability for all device sizes
- **Exception Safety**: Added proper exception handling for missing UI elements

### 🔧 Compatibility Improvements
- **Android Support**: Extended support from Android 7.0+ (was Android 12+)
- **Android 15**: Improved Android 15 compatibility
- **Target API**: Updated for better compatibility across Android versions
- **Thread Safety**: Added proper UI thread handling for WebSocket callbacks

### 🛠️ Technical Changes
- Added try-catch blocks for critical operations
- Improved WebSocket listener implementation with runOnUiThread
- Better handling of optional UI elements in ChatActivity
- Enhanced error reporting and debugging capabilities

## Version 1.3.0 - Tablet UI Optimization (2024-10-03)

### 🆕 New Features
- **Tablet Support**: Dedicated layouts for tablets (sw600dp screen width qualifier)
- **Landscape Mode**: Optimized two-column layout for landscape orientation
- **Enhanced Keyboard**: Additional Clear and History buttons on tablet layouts
- **Responsive Design**: Adaptive layouts that scale properly across device sizes
- **Better Spacing**: Improved margins, padding, and touch targets for larger screens

### 🔧 Improvements
- **Build Compatibility**: Reduced target SDK to 34 for better Android 15 compatibility
- **Button Sizing**: Larger keyboard buttons on tablets for easier interaction
- **Layout Optimization**: Centered content with max-width constraints for better readability
- **Typography**: Larger text sizes on tablets for improved readability
- **Touch Targets**: Increased button sizes and spacing for tablet use

### 🛠️ Technical Changes
- Added tablet-specific resource qualifiers (layout-sw600dp, values-sw600dp)
- Created landscape-specific layouts (layout-land)
- Added dimension resources for consistent spacing across screen sizes
- Improved keyboard button styling with TabletKeyboardButton style
- Enhanced ChatActivity to handle optional tablet buttons gracefully

### 📱 Device Support
- **Phones**: Android 12+ (unchanged)
- **Tablets**: Optimized for 7"+ tablets with proper scaling
- **Foldables**: Better support for large unfolded screens
- **Landscape**: Improved landscape mode experience

### 🔧 Bug Fixes
- Fixed missing dimension resources that could cause crashes
- Improved build compatibility with different Android versions
- Fixed layout issues on larger screens

---

## Version 1.2.2 - Android 15 Compatibility (2024-09-30)

### 🔧 Improvements
- **Android 15 Support**: Updated for compatibility with Android 15
- **Package Naming**: Changed to com.ssfdre38.cpcli.android.client to avoid trademark issues
- **Trademark Compliance**: Added proper attribution to GitHub and Microsoft

### 🛠️ Technical Changes
- Updated targetSdk to 35 for Android 15
- Added proper multidex support
- Improved package signing

---

## Version 1.2.0 - Core Functionality (2024-09-29)

### ✅ Working Features
- **App Launch**: Stable startup and navigation
- **UI Navigation**: Chat, settings, and main screens
- **Keyboard Shortcuts**: All essential CLI keys
- **Material Design**: Modern Android 12+ styling

### 🔧 Improvements
- Fixed app crashes on startup
- Improved WebSocket connection handling
- Enhanced keyboard button responsiveness
- Better error handling and user feedback

---

## Version 1.1.0 - Initial Release (2024-09-28)

### 🆕 Initial Features
- Basic Android app structure
- WebSocket client for Copilot CLI
- Material Design 3 UI
- Essential keyboard shortcuts
- Server connection management
# 🤖 Copilot Android Client

A native Android application that provides a mobile interface for GitHub Copilot CLI interaction, featuring keyboard shortcuts and a clean Material Design UI.

## 📋 Project Overview

This Android app serves as a mobile client for interacting with GitHub Copilot CLI through a WebSocket server. The current version focuses on stable UI functionality and keyboard shortcuts for enhanced copilot-cli interaction.

### 🔧 Components
1. **Android Client** - Native Android app with Material Design 3 UI
2. **Server Setup Scripts** - Automated Node.js WebSocket server deployment
3. **Keyboard Shortcuts** - Essential keys for CLI interaction (Ctrl+C, Tab, Enter, etc.)

### ✨ Current Features
- 📱 **Native Android App** with modern Material Design 3 UI
- ⌨️ **Keyboard Shortcuts** for copilot-cli commands (Ctrl+C, Ctrl+V, Tab, Enter, Esc, Arrow keys)
- 🔌 **Connection UI** for server configuration
- ⚙️ **Settings Screen** with future feature placeholders
- 🚀 **Stable Architecture** - Fixed crash issues from previous versions
- 📱 **Android 12+** support (API 31 and above)

## 🚀 Quick Start

### Download APK
📥 **[Download Latest Release (v1.2.0)](https://github.com/ssfdre38/copilot-android-client/releases/tag/v1.2.0)**

- **Release APK** (13.2 MB) - Recommended for general use
- **Debug APK** (15.5 MB) - For testing and development

### Prerequisites
- **Android Device**: Android 12 (API 31) or higher
- **Server**: Node.js 16+ (for WebSocket server setup)
- **Optional**: GitHub CLI with Copilot extension

### 1-Minute Setup
```bash
# Download APK from releases page
# Install on Android device
# For server setup (coming in next release):
cd copilot-android-client/server
./setup-complete-server.sh
```

## 📱 Current App Status (v1.2.0)

### ✅ Working Features
- **App Launch** - Stable startup and navigation between screens
- **UI Navigation** - Smooth navigation between main, chat, and settings screens  
- **Keyboard Shortcuts** - All essential keys for CLI interaction:
  - Ctrl+C, Ctrl+V for copy/paste operations
  - Tab for auto-completion suggestions
  - Enter to send commands
  - Esc, Arrow Up/Down for command history
  - Backspace for editing
- **WebSocket Connection** - Real-time communication with copilot-cli server
- **Chat Functionality** - Send/receive messages to/from copilot-cli
- **Multi-server Support** - Add, edit, delete, and manage multiple servers
- **Chat History** - Automatic saving and viewing of conversation history
- **Dark Mode** - Theme switching with system detection
- **Auto-updates** - Manual update checking (automated coming soon)
- **Settings Management** - Comprehensive configuration options
- **Offline Help** - Built-in documentation and troubleshooting guide

### 🚧 In Development (Next Release)
- **Auto Server Discovery** - Detect servers on local network
- **Push Notifications** - Background response alerts
- **File Operations** - Upload/download code files
- **Syntax Highlighting** - Code display with proper formatting

### 🔧 Technical Details
- **Target SDK**: Android 12+ (API 31)
- **Architecture**: MVP with findViewById (stable and reliable)
- **Package**: `com.ssfdre38.cpcli.android.client`
- **Build**: Gradle 8.2, Android Gradle Plugin 8.1.0
- **WebSocket**: Real-time communication support
- **Storage**: Local SQLite for chat history and settings

## 🖥️ Server Features

### Core Functionality
- **WebSocket Server**: Handles multiple concurrent connections
- **Copilot Integration**: Interfaces with GitHub Copilot CLI
- **Session Management**: Maintains conversation context per client
- **Health Monitoring**: Built-in health check endpoints

### Smart Responses
- **Context-Aware**: Recognizes programming languages and tools
- **Fallback Mode**: Provides helpful responses when Copilot unavailable
- **Command Suggestions**: Common CLI operations and best practices

## 🔧 Architecture

```
┌─────────────────┐    WebSocket    ┌─────────────────┐    CLI     ┌─────────────┐
│  Android Client │ ──────────────→ │  Node.js Server │ ─────────→ │ Copilot CLI │
│                 │ ←────────────── │                 │ ←───────── │             │
└─────────────────┘    JSON/Text    └─────────────────┘  Response  └─────────────┘
```

### Communication Protocol
```json
// Client → Server
{
  "type": "message",
  "message": "How do I deploy a React app?",
  "sessionId": "optional-uuid"
}

// Server → Client  
{
  "type": "response",
  "message": "To deploy a React app, you can...",
  "sessionId": "uuid"
}
```

## 🛠️ Development

### Android Development
```bash
# Open in Android Studio
android-studio CopilotAndroidClient

# Or build from command line
./gradlew assembleDebug
./gradlew installDebug
```

### Server Development  
```bash
cd server
npm install
npm start           # Production
npm run dev         # Development with auto-reload
```

### Testing
```bash
# Test server connectivity
node test-client.js

# Check health endpoint
curl http://localhost:3001/health

# Android testing
adb logcat | grep CopilotClient
```

## 📦 Deployment

### Development (Local Network)
1. Start server: `cd server && npm start`
2. Find local IP: `ifconfig` or `ipconfig`
3. Use IP in Android app: `ws://192.168.1.100:3001`

### Production
- Deploy server with PM2, Docker, or cloud platform
- Use HTTPS/WSS for secure connections
- Implement proper authentication
- Set up monitoring and logging

## 🔐 Security Considerations

- **Transport Security**: Use WSS in production
- **Authentication**: Implement API key validation
- **Input Validation**: Sanitize all user inputs
- **Rate Limiting**: Prevent abuse and DoS attacks
- **Network Security**: Firewall and access controls

## 🐛 Troubleshooting

### Common Issues

**Connection Failed**
- Verify server is running and accessible
- Check firewall settings on server
- Ensure correct IP address and port

**App Crashes**
- Check minimum Android API level (24+)
- Verify all dependencies are installed
- Review logcat output for stack traces

**No Responses**
- Server logs show connection but no responses
- Check if Copilot CLI is properly configured
- Fallback responses should work regardless

## 📚 Documentation

- **README.md**: Project overview and setup
- **DEPLOYMENT.md**: Production deployment guide
- **API Documentation**: WebSocket protocol details
- **Android Docs**: UI components and architecture
- **Server Docs**: Node.js implementation details

## 🔮 Development Roadmap

### ✅ v1.2.0 - Full Networking & Features (Current)
- [x] **WebSocket Client** - Real-time connection to copilot-cli server
- [x] **Chat Functionality** - Send/receive messages to/from copilot-cli
- [x] **Multi-server Support** - Add, edit, delete server configurations
- [x] **Chat History** - Automatic saving and viewing per server
- [x] **Dark Mode** - Theme switching with system detection
- [x] **Auto-updates** - Manual update checking functionality
- [x] **Offline Help** - Complete built-in documentation
- [x] **Settings Management** - Comprehensive configuration options
- [x] **Data Management** - Clear history, export/import settings

### ✅ v1.1.2 - Stability Release (Previous)
- [x] **Fixed App Crashes** - Resolved critical startup and navigation issues
- [x] **Stable UI Architecture** - Simplified, reliable findViewById-based approach
- [x] **Keyboard Shortcuts** - Essential CLI interaction keys (Ctrl+C, Tab, Enter, Esc, Arrows)
- [x] **Basic Navigation** - Main, Chat, and Settings screens working
- [x] **Material Design 3** - Modern Android UI with proper theming
- [x] **Android 12+ Support** - Target SDK 34, minimum API 31
- [x] **Trademark Compliance** - Non-infringing package naming and proper attributions

### 📋 v1.3.0 - Enhanced Features (Next Release)
- [ ] **Auto Server Discovery** - Detect servers on local network automatically
- [ ] **Automated Updates** - Background checking and download of new versions
- [ ] **Import/Export** - Backup and restore server configurations and chat history
- [ ] **Search** - Find specific messages across chat history
- [ ] **Notifications** - Background alerts for server responses
- [ ] **Performance Optimizations** - Better memory usage and battery life

### 🎯 v1.4.0 - Advanced Features  
- [ ] **File Operations** - Upload/download code files through chat
- [ ] **Voice Input** - Speech-to-text for hands-free interaction
- [ ] **Syntax Highlighting** - Code display with proper formatting
- [ ] **Hardware Keyboard** - Enhanced shortcuts for external keyboards
- [ ] **Tablet UI** - Optimized interface for larger screens
- [ ] **Plugin System** - Custom extensions and integrations

### 🔮 Future Considerations
- [ ] **File Operations** - Upload/download code files
- [ ] **Voice Input** - Speech-to-text for hands-free interaction
- [ ] **Syntax Highlighting** - Code display with proper formatting
- [ ] **Push Notifications** - Background response alerts
- [ ] **Tablet UI** - Optimized interface for larger screens
- [ ] **Hardware Keyboard** - Enhanced shortcuts for external keyboards

### 🎯 Key Features Overview

#### 🖥️ **Multi-Server Management**
- Save unlimited server configurations
- Quick switching between servers
- Default server setting
- Per-server chat history
- Server status indicators
- Import/Export configurations

#### 🌙 **Dark Mode & Themes**
- System automatic detection
- Manual light/dark toggle
- Material Design 3 compliance
- Battery-friendly OLED optimization
- Persistent theme settings

#### 💾 **Chat History**
- Automatic conversation saving
- Per-server history isolation
- Configurable history limits (100-5000 messages)
- Search through chat history
- Export conversation logs
- Clear history options

#### 🔄 **Auto-Updates**
- Daily automatic update checks
- Manual update checking
- Direct APK download
- Version comparison
- Skip version option
- Release notes display

#### 📚 **Offline Help System**
- Complete setup guides
- Troubleshooting documentation
- Feature explanations
- Search functionality
- Category organization
- No internet required

## 👥 Contributing

We welcome contributions! Please see our contributing guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup
```bash
# Clone the repo
git clone https://github.com/your-username/copilot-android-client
cd copilot-android-client

# Run setup script
./setup.sh

# Start development server
cd server && npm run dev
```

## 📄 License & Legal

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Trademark Notices
- **GitHub®** is a registered trademark of Microsoft Corporation
- **Copilot™** is a trademark of Microsoft Corporation  
- **Android™** is a trademark of Google LLC
- **Material Design™** is a trademark of Google LLC

This application is not affiliated with, endorsed by, or sponsored by Microsoft Corporation, GitHub, Inc., or Google LLC. All trademarks and registered trademarks are the property of their respective owners.

## 🔄 Version History

### v1.2.0 (Current)
- **🔗 WebSocket Integration**: Full real-time communication with copilot-cli server
- **💬 Chat Functionality**: Complete chat interface with message history
- **🔧 Multi-server Support**: Add, edit, delete multiple server configurations
- **📚 Chat History**: Automatic conversation saving with per-server isolation
- **🌙 Dark Mode**: Theme switching with system detection and manual toggle
- **🔄 Update Management**: Manual update checking with GitHub releases integration
- **📖 Offline Help**: Comprehensive built-in documentation and troubleshooting
- **⚙️ Settings Overhaul**: Complete settings redesign with data management
- **🛠️ Stability Improvements**: Enhanced error handling and connection management

### v1.1.2 (Previous)
- **🐛 Critical Bug Fix**: Fixed app crash when pressing "Start Chat" button
- **🔗 WebSocket Connection**: Resolved WebSocket connection issues preventing chat functionality
- **⚡ Performance**: Removed blocking WebSocket connection calls that caused ANR crashes
- **🔧 Stability**: Improved connection state management and error handling

### v1.0.2
- Multi-server support enhancements
- Dark mode improvements
- Chat history optimizations

### v1.0.1
- Initial feature-complete release
- Core functionality implementation

## 🆔 App Information

- **Package Name**: `com.ssfdre38.cpcli.android.client`
- **Minimum Android**: API 31 (Android 12)
- **Target Android**: API 34 (Android 14)
- **Architecture**: ARM64, ARM, x86_64

## 🙏 Acknowledgments

- **GitHub Copilot Team** for the amazing AI assistant
- **Android Team** for Material Design guidelines
- **Node.js Community** for excellent WebSocket libraries
- **Open Source Contributors** who make projects like this possible

---

**Made with ❤️ for developers who want to code anywhere, anytime**
# 🚀 Quick Setup Guide - Copilot Android Client

## For Server Administrators

### Deploy Your Own Server
```bash
# Clone/download the project
cd CopilotAndroidClient/server

# Install dependencies
npm install

# Start server (default port 3001)
npm start

# OR start on custom port
PORT=8080 npm start

# Your server will be available at:
# ws://localhost:3001 (local testing)
# ws://YOUR_IP:3001 (network access)
```

### Server Features
- **Smart AI responses** for programming questions
- **Multiple language support** (Python, JavaScript, Git, Docker, etc.)
- **Session management** for conversation context
- **Health monitoring** at `/health` endpoint
- **Configurable ports** via environment variables

## For Android Users

### 1. Install the App
- Build the APK using Android Studio
- Or use the provided Gradle build: `./gradlew assembleDebug`
- Install on your Android device

### 2. Configure Your Server
Open the app and enter your server details:

#### Local Network Server
```
Server URL: ws://192.168.1.100:3001
API Key: (leave blank if not required)
```

#### Cloud/Remote Server  
```
Server URL: wss://your-domain.com:3001
API Key: your-secret-key (if required)
```

#### Auto-Discovery
- Tap "Scan Network" to find local servers automatically
- Tap "Test Localhost" for local development

### 3. Connect and Chat
1. Tap "Connect to Server"
2. Wait for "Connected" status
3. Tap "Start Chat"
4. Begin asking programming questions!

## Example Conversations

**User**: "How do I create a Python virtual environment?"

**Copilot**: 
```
🐍 Python help:

# Virtual environment
python -m venv myenv
source myenv/bin/activate  # Linux/Mac
myenv\Scripts\activate  # Windows

# Install packages  
pip install requests flask
```

**User**: "Git commands for beginners"

**Copilot**:
```
🔧 Git commands:

# Basic workflow
git init
git add .
git commit -m "Initial commit"
git push -u origin main

# Branching
git checkout -b feature-branch
git merge feature-branch
```

## Configuration Options

### Server Settings (Android App)
- **Server URL**: Any WebSocket endpoint (ws:// or wss://)
- **API Key**: Optional authentication
- **Auto Connect**: Automatically connect on app start
- **Connection Timeout**: How long to wait for connection

### Network Requirements
- Server and Android device must be able to communicate
- Default port 3001 (configurable)
- WebSocket protocol (ws:// or wss://)

## Troubleshooting

### Connection Issues
1. **Verify server is running**: Check server logs
2. **Check network connectivity**: Ping the server IP
3. **Firewall settings**: Ensure port is open
4. **URL format**: Must start with ws:// or wss://

### Server Issues
1. **Port already in use**: Try different port with `PORT=8080 npm start`
2. **Dependencies**: Run `npm install` in server directory
3. **Node.js version**: Requires Node.js 16+

### Android Issues
1. **App crashes**: Check logcat for errors
2. **Connection timeout**: Increase timeout in settings
3. **Network permissions**: Ensure app has internet permission

## Advanced Usage

### Secure Deployment (HTTPS/WSS)
```bash
# Add SSL certificates to server
const options = {
  key: fs.readFileSync('private-key.pem'),
  cert: fs.readFileSync('certificate.pem')
};
```

### Multiple Servers
- Configure different servers for different purposes
- Switch between development and production servers
- Share server configurations with team members

### API Authentication
```bash
# Set API key on server
export API_KEY="your-secure-api-key"
npm start
```

## Support

- **Documentation**: See README.md and DEPLOYMENT.md
- **Server Health**: Visit `http://your-server:port/health`
- **Logs**: Check server console output for errors
- **Testing**: Use included test scripts to validate setup

---

**Enjoy chatting with Copilot on your Android device!** 🤖📱
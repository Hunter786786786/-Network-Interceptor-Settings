# 🔧 Setup Instructions

## Quick Start

### Option 1: Android Studio (Recommended for beginners)

1. **Download & Install**
   ```bash
   # Download this ZIP file
   # Extract to a folder
   ```

2. **Open in Android Studio**
   ```
   File → Open → Select the project folder
   ```

3. **Sync Gradle**
   ```
   Click "Sync Now" when prompted
   Or: File → Sync Project with Gradle Files
   ```

4. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

### Option 2: Command Line

```bash
# Navigate to project folder
cd NetworkInterceptor

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Find APK at:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release.apk
```

## Prerequisites

### Install Java 17

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# macOS
brew install openjdk@17

# Windows
# Download from: https://adoptium.net/
```

### Install Android SDK

```bash
# Download Android Studio
# https://developer.android.com/studio

# Or use command line tools only
# https://developer.android.com/studio/command-line
```

### Set Environment Variables

```bash
# Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## GitHub Actions Setup

### 1. Create Repository

```bash
# Create new repository on GitHub
# Don't initialize with README
```

### 2. Push Code

```bash
# Initialize git
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit"

# Add remote
git remote add origin https://github.com/YOUR_USERNAME/NetworkInterceptor.git

# Push
git push -u origin main
```

### 3. Enable Actions

1. Go to repository on GitHub
2. Click "Actions" tab
3. Click "I understand my workflows, go ahead and enable them"

### 4. Download Built APK

1. Go to Actions tab
2. Click on latest workflow run
3. Scroll down to "Artifacts"
4. Download `release-apk`

## Troubleshooting

### Gradle Sync Failed

```bash
# Clear Gradle cache
./gradlew clean

# Rebuild
./gradlew build --refresh-dependencies
```

### Missing Gradle Wrapper

```bash
# Generate wrapper
cd NetworkInterceptor
gradle wrapper --gradle-version 8.2
```

### Build Failed - SDK Not Found

```bash
# Set SDK path in local.properties
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

## LSPosed Installation

### 1. Prerequisites

- Rooted device with Magisk
- Android 8.0 or higher

### 2. Install LSPosed

```bash
# Download LSPosed from GitHub
# https://github.com/LSPosed/LSPosed/releases

# Install via Magisk
Magisk → Modules → Install from storage → Select LSPosed ZIP

# Reboot device
```

### 3. Install Module

```bash
# Install APK
adb install app-release.apk

# Or copy to device and install manually
```

### 4. Configure LSPosed

1. Open LSPosed Manager app
2. Go to "Modules" tab
3. Find "Network Interceptor"
4. Enable the module
5. Set scope to: `delivery.samurai.android`
6. Force stop target app

### 5. Configure Module

1. Open "Network Interceptor" app
2. Enable "Master Switch"
3. Set distance limits
4. Tap "Save Settings"

## Verification

### Check if Hook is Working

```bash
# View logs
adb logcat -s "NetworkInterceptor:D"

# You should see:
# === Network Interceptor Loaded ===
# Target: delivery.samurai.android
```

### Test Order Detection

When target app receives an order, you should see:
```
📦 Order event detected: Allocation
Order ID: xxx
Pickup: x.xkm
Delivery: x.xkm
```

## Need Help?

- 📖 Read the [README](README.md)
- 🐛 Check [Issues](https://github.com/YOUR_USERNAME/NetworkInterceptor/issues)
- 💬 Join discussions

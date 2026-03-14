# 🌐 Network Interceptor - Xposed Module

> **⚠️ Educational Purpose Only** - Learn Xposed Framework & Network Interception

[![Build Status](https://github.com/YOUR_USERNAME/NetworkInterceptor/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/NetworkInterceptor/actions/workflows/build.yml)

## 📱 UI Preview

```
┌─────────────────────────────────────┐
│  Network Interceptor Settings       │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐  │
│  │ Master Switch          [🟢ON] │  │
│  │ Enable/Disable interception   │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ Max Pickup Distance           │  │
│  │ ●━━━━━━━━━━○───────  5.0km    │  │
│  │ 0km              10km         │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ Max Delivery Distance         │  │
│  │ ●━━━━━━━━━━━━━━━━○── 10.0km   │  │
│  │ 0km              15km         │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     [  Save Settings  ]       │  │
│  └───────────────────────────────┘  │
│                                     │
│     📚 Educational Purpose Only     │
│     LSPosed Xposed Module           │
│     Target: delivery.samurai.android│
│                                     │
└─────────────────────────────────────┘
```

## 🎯 Features

- ✅ **Master Switch** - Enable/Disable interception
- 📏 **Distance Sliders** - Set max pickup/delivery distances
- ⚡ **Background Processing** - Non-blocking coroutine-based
- 🔌 **OkHttp WebSocket Hook** - Intercept network messages
- 📊 **Socket.io Support** - Handle real-time events
- 🎨 **Material Design UI** - Clean and modern interface

## 📋 Requirements

| Component | Version |
|-----------|---------|
| Android | 8.0+ (API 26+) |
| Root Access | Required |
| LSPosed | Latest |
| Target App | delivery.samurai.android |

## 🚀 Installation

### 1. Setup LSPosed

```bash
# 1. Install Magisk (if not already installed)
# 2. Download LSPosed ZIP from GitHub
# 3. Magisk > Modules > Install from storage
# 4. Reboot device
```

### 2. Install Module

```bash
# 1. Install the APK
adb install app-release.apk

# 2. Open LSPosed Manager
# 3. Go to Modules tab
# 4. Enable "Network Interceptor"
# 5. Select scope: delivery.samurai.android
# 6. Force stop target app
```

### 3. Configure Settings

1. Open "Network Interceptor" app
2. Toggle **Master Switch** ON
3. Adjust distance sliders
4. Tap **Save Settings**

## 🏗️ Project Structure

```
NetworkInterceptor/
├── .github/
│   └── workflows/
│       └── build.yml          # CI/CD Pipeline
├── app/
│   ├── src/main/
│   │   ├── java/com/example/interceptor/
│   │   │   ├── XposedHook.kt       # Main hook logic
│   │   │   └── SettingsActivity.kt # Settings UI
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_settings.xml
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── README.md
```

## 🔧 Building from Source

### Local Build

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/NetworkInterceptor.git
cd NetworkInterceptor

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release.apk
```

### GitHub Actions (Auto Build)

1. Fork this repository
2. Push to `main` branch
3. GitHub Actions will automatically build
4. Download APK from Actions tab

## 📊 How It Works

### Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Target App                           │
│  ┌─────────────┐    ┌─────────────┐    ┌────────────┐  │
│  │   Server    │───▶│   OkHttp    │───▶│    UI      │  │
│  │             │    │  WebSocket  │    │   Dialog   │  │
│  └─────────────┘    └──────┬──────┘    └────────────┘  │
│                            │                            │
│                            │ (Hook intercepts here)     │
│                            ▼                            │
│  ┌─────────────────────────────────────────────────┐   │
│  │              Xposed Hook (Our Module)            │   │
│  │  1. Parse JSON message                          │   │
│  │  2. Extract order data (id, distances)          │   │
│  │  3. Check against user preferences              │   │
│  │  4. Log decision                                │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Purpose |
|-----------|---------|
| `XposedHook.kt` | Main hook logic, intercepts network calls |
| `SettingsActivity.kt` | UI for configuring preferences |
| `XSharedPreferences` | Share settings between app and module |
| `Coroutines` | Background processing without blocking UI |

## 📝 Code Examples

### Adding New Criteria

```kotlin
// In XposedHook.kt, modify shouldAcceptOrder()

// Add minimum payout check
val minPayout = prefs.getFloat("min_payout", 5.0f)
if (order.payout < minPayout) {
    YukiHookAPI.loggerI("Payout too low: ${order.payout} < $minPayout")
    return false
}
```

### Adding New Field

```kotlin
// In OrderData data class
data class OrderData(
    val id: String,
    val pickupDistanceInKm: Double,
    val deliveryDistanceInKm: Double,
    val restaurantName: String,
    val payout: Double,
    val newField: String  // Add your field
)
```

## 🐛 Debugging

### View Logs

```bash
# Filter for our module logs
adb logcat -s "NetworkInterceptor:D" "*:S"

# Full log with context
adb logcat | grep NetworkInterceptor
```

### Common Issues

| Issue | Solution |
|-------|----------|
| Hook not working | Check LSPosed scope is set correctly |
| Preferences not loading | Ensure `makeWorldReadable()` is called |
| Slow performance | Check coroutine scope configuration |
| Build fails | Update Android Studio and SDK |

## 📚 Learning Resources

### Xposed Framework
- [Xposed Framework](https://github.com/rovo89/XposedBridge)
- [LSPosed (Modern Xposed)](https://github.com/LSPosed/LSPosed)

### YukiHookAPI
- [YukiHookAPI Documentation](https://github.com/HighCapable/YukiHookAPI)
- [API Reference](https://highcapable.github.io/YukiHookAPI/)

### Android Networking
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Socket.io Client](https://socket.io/docs/v4/client-api/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## ⚖️ Disclaimer

This software is for **educational purposes only**. The developer is not responsible for:
- Any misuse of this software
- Violation of any app's Terms of Service
- Any damage to your device

Use at your own risk.

## 📄 License

```
MIT License

Copyright (c) 2024 NetworkInterceptor

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<p align="center">
  Made with ❤️ for educational purposes
</p>

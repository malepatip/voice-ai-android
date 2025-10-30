# Voice AI Android 🎤

[![Build APK](https://github.com/malepatip/voice-ai-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/malepatip/voice-ai-android/actions/workflows/build-apk.yml)

Voice AI Android Application with Hybrid SLM Architecture (Gemini Nano + Llama 3.2)

## 🚀 Quick Start

### Download Latest APK (No Build Required!)

**Option 1: GitHub Releases** ⭐ *Best for sharing & installing!*
1. **Go to [Releases](https://github.com/malepatip/voice-ai-android/releases)**
2. **Download** the latest APK directly
3. **Install** on your Android device (or share the download link!)
4. To create a release: `git tag v1.0.0 && git push --tags`

**Option 2: GitHub Actions Artifacts**
1. **Go to [Actions Tab](https://github.com/malepatip/voice-ai-android/actions)**
2. **Click latest "Build and Upload APK" run** (green checkmark ✅)
3. **Scroll to "Artifacts"** section
4. **Download** `voice-ai-android-debug-[number].zip`
5. **Extract and install** on your Android device

See **[GitHub Actions APK Guide](docs/GITHUB_ACTIONS_APK.md)** for detailed instructions.

## 📱 Features

- **Hybrid SLM Architecture**: On-device (Gemini Nano) + Backend (Llama 3.2)
- **Emotion-Aware TTS**: Cartesia Sonic 3 for expressive voice synthesis
- **Privacy-First**: Most processing stays on-device
- **Cost-Effective**: Oracle Cloud A1 free tier (€0/month)
- **Intelligent Routing**: Automatic decision between on-device and backend

## 🏗️ Architecture

```
Voice Input → STT → Intent (Gemini Nano) → Emotion Detection
                                    ↓
                          Router Logic (Smart)
                    ↓                           ↓
              On-Device                    Backend
            (Gemini Nano)               (Llama 3.2 ARM)
                    ↓                           ↓
                          Response Generation
                                    ↓
                    TTS (Sonic 3 / On-Device)
                                    ↓
                            Audio Output
```

## 📚 Documentation

- **[SLM Architecture](docs/SLM_ARCHITECTURE.md)** - Complete hybrid SLM design
- **[Sonic 3 TTS Integration](docs/SONIC3_TTS_INTEGRATION.md)** - Emotion-aware voice synthesis
- **[Sonic 3 Quick Start](docs/SONIC3_QUICKSTART.md)** - 30-min implementation guide
- **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** - ARM Kubernetes deployment
- **[APK Testing Guide](docs/APK_TESTING_GUIDE.md)** - Install and test on Android
- **[GitHub Actions APK](docs/GITHUB_ACTIONS_APK.md)** - Download pre-built APKs

## 🛠️ Build Locally (Optional)

If you want to build yourself:

```bash
# Clone repository
git clone https://github.com/malepatip/voice-ai-android.git
cd voice-ai-android

# Build debug APK
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

**Requirements**: Android SDK, JDK 17+

See **[Quick Build Guide](QUICK_BUILD.md)** for details.

## 🎯 Current Status

### ✅ Completed
- [x] Hybrid SLM architecture design
- [x] Domain layer (entities, repositories, use cases)
- [x] Router logic (on-device vs backend)
- [x] Evals framework
- [x] Skills framework
- [x] TTS integration (Sonic 3)
- [x] Kubernetes deployment configs
- [x] GitHub Actions CI/CD
- [x] **GitHub Releases for APK distribution** ⭐ *NEW!*
- [x] Comprehensive documentation

### 🚧 In Progress
- [ ] Android Gemini Nano integration
- [ ] Voice recording (STT)
- [ ] Backend Llama 3.2 deployment
- [ ] Emotion detection model
- [ ] Skills engine implementation
- [ ] End-to-end testing

## 🧪 Testing

### Install on Android

1. **Download APK** from [GitHub Actions artifacts](https://github.com/malepatip/voice-ai-android/actions)
2. **Upload to Google Drive** (or transfer directly)
3. **Enable "Install from unknown sources"** on your device
4. **Install APK**
5. **Grant permissions** (Microphone, Internet)
6. **Launch and test!**

See **[APK Testing Guide](docs/APK_TESTING_GUIDE.md)** for step-by-step instructions.

## 📦 Components

| Component | Technology | Status |
|-----------|-----------|---------|
| **On-Device AI** | Gemini Nano | 🚧 In Progress |
| **Backend AI** | Llama 3.2 (1B/3B) | 🚧 In Progress |
| **TTS** | Cartesia Sonic 3 | ✅ Integrated |
| **STT** | Google Speech Recognition | 🚧 Planned |
| **Emotion Detection** | wav2vec2-emotion | 🚧 Planned |
| **Backend** | ARM Kubernetes (Oracle A1) | ✅ Configured |
| **Architecture** | MVVM + Clean Architecture | ✅ Implemented |
| **DI** | Hilt | ✅ Configured |
| **UI** | Jetpack Compose | ✅ Minimal UI |

## 🔧 Tech Stack

### Android
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 34 (Android 14)
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt/Dagger

### AI/ML
- **On-Device**: Google AI Edge SDK (Gemini Nano)
- **Backend**: llama.cpp (Llama 3.2 on ARM)
- **TTS**: Cartesia Sonic 3 API
- **Emotion**: wav2vec2-emotion (TFLite)

### Infrastructure
- **Backend**: Node.js + Express
- **Orchestration**: Kubernetes (K3s)
- **Cloud**: Oracle Cloud A1 (Free Tier)
- **Cache**: Redis
- **CI/CD**: GitHub Actions

## 💰 Cost

- **Oracle Cloud A1**: €0/month (free tier)
- **Sonic 3 TTS**: €0-5/month (free tier: 10k chars, Pro: €5 for 100k)
- **Total**: **€0-5/month** for moderate use

## 🤝 Contributing

Currently in active development. Stay tuned for contribution guidelines!

## 📄 License

[Add your license here]

## 🔗 Links

- **Repository**: https://github.com/malepatip/voice-ai-android
- **Actions (Download APK)**: https://github.com/malepatip/voice-ai-android/actions
- **Issues**: https://github.com/malepatip/voice-ai-android/issues

---

**Built with** 🤖 [Claude Code](https://claude.com/claude-code)

# Quick Build Instructions for Voice AI Android

## Prerequisites Check

Before building, ensure you have:
- ✅ JDK 17+ installed
- ✅ Android SDK installed (or Android Studio)
- ✅ Git repository cloned

## Step 1: Initialize Gradle Wrapper (First Time Only)

If the `gradlew` file doesn't exist, initialize it:

```bash
# Navigate to project
cd /home/user/voice-ai-android

# Initialize gradle wrapper (if not exists)
gradle wrapper --gradle-version 8.2

# Or if you have Android Studio installed:
# The wrapper should already be present
```

Make gradlew executable:
```bash
chmod +x gradlew
```

## Step 2: Quick Build

### Build Debug APK:
```bash
./gradlew assembleDebug
```

### Build Release APK (unsigned):
```bash
./gradlew assembleRelease
```

## Step 3: Find Your APK

```bash
# Debug APK location
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Release APK location
ls -lh app/build/outputs/apk/release/app-release-unsigned.apk
```

## Step 4: Upload to Google Drive

1. Open https://drive.google.com
2. Upload the APK from the location above
3. Follow the rest of the steps in `/docs/APK_TESTING_GUIDE.md`

## Troubleshooting

### "gradlew: command not found"

**Solution**: Initialize the gradle wrapper first (see Step 1)

### "SDK location not found"

**Solution**: Create `local.properties`:
```bash
cat > local.properties <<EOF
sdk.dir=/path/to/your/android-sdk
# Or on Linux usually:
# sdk.dir=/home/user/Android/Sdk
EOF
```

### "Permission denied: ./gradlew"

**Solution**:
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Build fails with "Java version" error

**Solution**: Ensure JDK 17+ is installed:
```bash
java -version
# Should show: java version "17" or higher

# If not, install JDK 17:
# Ubuntu/Debian:
sudo apt-get install openjdk-17-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### First build takes long (5-10 minutes)

**Expected**: Gradle downloads dependencies on first build.
Subsequent builds will be much faster (30-60 seconds).

## Quick Reference

```bash
# Clean build
./gradlew clean

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Run tests
./gradlew test

# Check for issues
./gradlew check

# List all tasks
./gradlew tasks
```

## Current Status

The app currently has:
- ✅ Minimal MainActivity with placeholder UI
- ✅ VoiceAIApplication class
- ✅ AndroidManifest with permissions
- ✅ Domain layer entities and interfaces
- ✅ Build configuration

**What's Next**:
- Implement Sonic 3 TTS (see `/docs/SONIC3_QUICKSTART.md`)
- Add voice recording functionality
- Integrate Gemini Nano
- Deploy Llama 3.2 backend

## Need Help?

See detailed documentation:
- `/docs/APK_TESTING_GUIDE.md` - Complete testing guide
- `/docs/SONIC3_QUICKSTART.md` - Sonic 3 TTS implementation
- `/docs/SLM_ARCHITECTURE.md` - Overall architecture

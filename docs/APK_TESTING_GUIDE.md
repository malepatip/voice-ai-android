# APK Testing Guide - Google Drive Method

## Overview
This guide shows you how to build your Voice AI Android APK, upload it to Google Drive, and install it on your Android device for testing.

## Prerequisites
- Android device (Android 8.0+ recommended)
- Google Drive account
- Internet connection on both devices

## Step 1: Build the APK

### Option A: Debug APK (Recommended for Testing)

```bash
# Navigate to project directory
cd /home/user/voice-ai-android

# Build debug APK
./gradlew assembleDebug

# APK will be created at:
# app/build/outputs/apk/debug/app-debug.apk
```

**Expected output**:
```
BUILD SUCCESSFUL in 1m 23s
```

### Option B: Release APK (For Production Testing)

```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# APK will be created at:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

**Note**: Release APKs need signing for distribution. For quick testing, use debug APK.

## Step 2: Locate Your APK

```bash
# Find the APK file
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Output example:
# -rw-r--r-- 1 user user 45M Oct 30 12:34 app-debug.apk
```

**APK Location**:
```
/home/user/voice-ai-android/app/build/outputs/apk/debug/app-debug.apk
```

## Step 3: Upload to Google Drive

### Using Web Browser

1. **Open Google Drive**:
   - Visit: https://drive.google.com
   - Sign in with your Google account

2. **Create a Folder** (Optional but recommended):
   - Click "New" → "Folder"
   - Name it: "Voice AI APKs"
   - Open the folder

3. **Upload APK**:
   - Click "New" → "File upload"
   - Navigate to: `/home/user/voice-ai-android/app/build/outputs/apk/debug/`
   - Select `app-debug.apk`
   - Wait for upload to complete (may take 1-2 minutes)

4. **Get Shareable Link**:
   - Right-click on `app-debug.apk`
   - Click "Share" or "Get link"
   - Set to "Anyone with the link can view"
   - Copy the link
   - **Or**: Note the file location in your Drive

### Using Command Line (Alternative)

If you have `rclone` or `gdrive` CLI tool:

```bash
# Using rclone (if configured)
rclone copy app/build/outputs/apk/debug/app-debug.apk gdrive:VoiceAI-APKs/

# Using gdrive CLI
gdrive upload app/build/outputs/apk/debug/app-debug.apk
```

## Step 4: Download on Android Device

### Method 1: Direct Download (Easiest)

1. **Open Google Drive App** on your Android device
2. **Navigate** to the folder where you uploaded the APK
3. **Tap** on `app-debug.apk`
4. **Tap** "Download" or the download icon
5. Wait for download to complete

### Method 2: Using Browser

1. **Open Chrome** (or any browser) on your Android device
2. **Go to**: https://drive.google.com
3. **Sign in** with the same Google account
4. **Find** the APK file
5. **Tap** the three dots (⋮) next to the file
6. **Select** "Download"

### Method 3: Using Shared Link

1. **On your computer**: Copy the shareable link from Step 3
2. **On your Android device**:
   - Open the link in Chrome
   - Tap "Download" when prompted
   - Wait for download

## Step 5: Enable Installation from Unknown Sources

Before installing, you need to allow installations from outside the Play Store.

### Android 8.0 - 12:
1. **Open Settings**
2. Go to **Apps & notifications** → **Advanced** → **Special app access**
3. Tap **Install unknown apps**
4. Select **Chrome** (or **Files** app, depending on where you're installing from)
5. Toggle **Allow from this source** ON

### Android 13+:
1. **Open Settings**
2. Go to **Security & privacy** → **More security settings**
3. Tap **Install apps from unknown sources**
4. Select **Chrome** or **Files**
5. Toggle **Allow from this source** ON

**Security Note**: You can disable this after installing the APK.

## Step 6: Install the APK

### From Downloads:
1. **Open** the **Files** app or **Downloads** app
2. **Navigate** to the Downloads folder
3. **Tap** on `app-debug.apk`
4. A prompt will appear: **"Do you want to install this application?"**
5. **Review permissions** (if shown)
6. Tap **Install**
7. Wait for installation (5-15 seconds)
8. Tap **Open** to launch the app

### From Notification:
1. When download completes, **pull down** the notification shade
2. **Tap** on the download notification
3. Follow steps 4-8 above

## Step 7: Launch and Test

### First Launch:
1. **Find the app** in your app drawer: "Voice AI" or "VoiceAIAndroid"
2. **Tap** to open
3. **Grant permissions** when prompted:
   - Microphone (required for voice input)
   - Storage (if needed)
   - Internet (for Sonic 3 API)

### Test Sonic 3 TTS:
1. Open the app
2. If you implemented the test activity, you should see:
   - Text input field
   - "Speak with Sonic 3" button
3. **Enter text**: "Hello! I'm testing Sonic 3 TTS."
4. **Tap** "Speak with Sonic 3"
5. **Listen** for audio output (~2-3 seconds)

### Expected Behavior:
- ✅ Audio plays with clear voice
- ✅ Latency: ~200-300ms
- ✅ Emotional tone (if emotion parameter set)
- ✅ No crashes or errors

## Troubleshooting

### Issue 1: "App not installed" Error

**Cause**: Conflicting package or signature mismatch

**Solution**:
```bash
# Uninstall previous version first
adb uninstall com.voiceai  # Replace with your package name

# Or on device:
# Settings → Apps → Voice AI → Uninstall
```

Then try installing again.

### Issue 2: "Package appears to be corrupt"

**Cause**: Incomplete download or corrupted file

**Solution**:
1. Delete the downloaded APK from your phone
2. Re-download from Google Drive
3. Verify file size matches the original:
   ```bash
   # On computer
   ls -lh app/build/outputs/apk/debug/app-debug.apk
   # Note the size (e.g., 45M)
   ```
4. On phone: Check downloaded file size matches

### Issue 3: "Install blocked" or "Installation not allowed"

**Cause**: "Install from unknown sources" not enabled

**Solution**:
- Follow Step 5 again carefully
- Make sure you enable it for the correct app (Chrome/Files)
- Restart the installation

### Issue 4: App crashes on launch

**Cause**: Missing dependencies or API key not configured

**Solution**:
1. Check LogCat for errors:
   ```bash
   adb logcat -s VoiceAI:V AndroidRuntime:E
   ```
2. Common issues:
   - API key not set → Add to `local.properties`
   - Missing permissions → Grant in Settings → Apps → Voice AI → Permissions
   - Network error → Check internet connection

### Issue 5: No audio output

**Cause**: Volume too low or audio permissions not granted

**Solution**:
1. **Turn up volume** on your device
2. Check **Settings → Apps → Voice AI → Permissions → Microphone** (granted)
3. Try different audio output:
   - Speaker
   - Wired headphones
   - Bluetooth (disconnect and try speaker first)

### Issue 6: "Sonic 3 API error: 401 Unauthorized"

**Cause**: Invalid or missing API key

**Solution**:
1. Verify API key in `local.properties`:
   ```properties
   SONIC3_API_KEY=cartesia_api_xxxxxxxx
   ```
2. Rebuild APK:
   ```bash
   ./gradlew clean assembleDebug
   ```
3. Re-upload to Google Drive
4. Re-download and install

## Build Variants

### Debug vs Release

| Feature | Debug APK | Release APK |
|---------|-----------|-------------|
| **Size** | Larger (~45MB) | Smaller (~30MB) |
| **Speed** | Slower | Faster |
| **Debugging** | Enabled | Disabled |
| **Signing** | Auto-signed | Needs manual signing |
| **Obfuscation** | No | Yes (if configured) |
| **Best for** | Development/Testing | Production |

**Recommendation**: Use **Debug APK** for testing.

## APK Information

### Check APK Details:

```bash
# On computer
./gradlew assembleDebug --info | grep "APK"

# Or use aapt
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Key info:
# - package name: com.voiceai
# - versionCode: 1
# - versionName: 1.0
# - minSdkVersion: 26 (Android 8.0)
# - targetSdkVersion: 34 (Android 14)
```

### Renaming APK (Optional):

```bash
# Rename for easier identification
mv app/build/outputs/apk/debug/app-debug.apk \
   app/build/outputs/apk/debug/voice-ai-v1.0-debug.apk
```

## Quick Reference Card

### Build & Upload Workflow:

```bash
# 1. Build
./gradlew assembleDebug

# 2. Verify
ls -lh app/build/outputs/apk/debug/app-debug.apk

# 3. Upload to Google Drive (manual via browser)

# 4. On Android:
#    - Download from Drive
#    - Enable unknown sources
#    - Install
#    - Grant permissions
#    - Test!
```

## Advanced: Continuous Testing

### Script for Quick Rebuild:

```bash
#!/bin/bash
# build-and-copy.sh

# Build
./gradlew assembleDebug

# Copy to shared location
cp app/build/outputs/apk/debug/app-debug.apk ~/Downloads/

# Optional: Auto-upload to Drive with rclone
# rclone copy ~/Downloads/app-debug.apk gdrive:VoiceAI-APKs/ --update

echo "✅ APK ready at: ~/Downloads/app-debug.apk"
echo "📤 Upload to Google Drive to test on device"
```

Make executable:
```bash
chmod +x build-and-copy.sh
./build-and-copy.sh
```

## Alternative: USB Debugging (Faster)

If you have a USB cable:

```bash
# Enable USB debugging on device:
# Settings → About phone → Tap "Build number" 7 times → Back → Developer options → USB debugging

# Connect USB cable

# Verify connection
adb devices

# Install directly
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.voiceai/.MainActivity

# View logs
adb logcat -s VoiceAI:V
```

**Benefit**: No need for Google Drive, instant installation!

## Testing Checklist

After installing, verify:

- [ ] App launches without crashes
- [ ] Permissions granted (microphone, internet)
- [ ] UI loads correctly
- [ ] Text input works
- [ ] Sonic 3 API responds (check for audio output)
- [ ] Audio plays clearly
- [ ] Latency acceptable (<500ms)
- [ ] Emotion parameters work (test happy, sad, etc.)
- [ ] Error handling works (test offline mode)
- [ ] App doesn't crash when backgrounded

## Monitoring During Testing

### View Real-Time Logs:

If USB connected:
```bash
# Filter for your app
adb logcat | grep "VoiceAI"

# Or specific tags
adb logcat -s VoiceAI:V Sonic3:V AndroidRuntime:E
```

### Monitor Network Traffic:

```bash
# Check API calls
adb logcat | grep "OkHttp\|Retrofit"
```

### Monitor Performance:

```bash
# Check memory usage
adb shell dumpsys meminfo com.voiceai

# Check battery usage
adb shell dumpsys batterystats --charged com.voiceai
```

## Support & Resources

### If you encounter issues:
1. Check LogCat output
2. Verify API key configuration
3. Test network connectivity
4. Review permissions in Settings
5. Try rebuilding from clean state:
   ```bash
   ./gradlew clean assembleDebug
   ```

### Useful Commands:

```bash
# Uninstall app
adb uninstall com.voiceai

# Clear app data
adb shell pm clear com.voiceai

# Reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.voiceai/.MainActivity
```

---

## Summary

1. ✅ Build APK: `./gradlew assembleDebug`
2. ✅ Upload to Google Drive
3. ✅ Download on Android device
4. ✅ Enable "Install from unknown sources"
5. ✅ Install APK
6. ✅ Grant permissions
7. ✅ Test Sonic 3 TTS!

**Estimated time**: 10-15 minutes (first time)

**Ready to build?** Run `./gradlew assembleDebug` and follow the steps above!

---

**Last Updated**: 2025-10-30
**For**: Voice AI Android with Sonic 3 TTS

# Building APK with GitHub Actions

## Overview
This project uses GitHub Actions to automatically build APKs on every push. No local Android SDK required!

## How It Works

Every time you push code to the repository:
1. 🤖 GitHub Actions automatically starts building
2. ☁️ APK is built in the cloud (takes ~3-5 minutes)
3. 📦 APK is uploaded as an artifact
4. ⬇️ You can download it from GitHub

## Download Your APK

### Method 1: From Actions Tab (Web)

1. **Go to your repository** on GitHub:
   ```
   https://github.com/malepatip/voice-ai-android
   ```

2. **Click "Actions" tab** at the top

3. **Find the latest workflow run**:
   - Look for "Build and Upload APK"
   - Green checkmark ✅ = successful build
   - Click on the workflow run

4. **Scroll down to "Artifacts" section**:
   - You'll see: `voice-ai-android-debug-[number]`
   - Example: `voice-ai-android-debug-42`

5. **Click to download**:
   - Downloads as a ZIP file
   - Extract the ZIP to get the APK

6. **Upload to Google Drive**:
   - Go to https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf
   - Upload the extracted APK
   - Then download on your Android device

### Method 2: Using GitHub CLI

If you have GitHub CLI installed:

```bash
# Login (first time only)
gh auth login

# List available artifacts
gh run list --workflow=build-apk.yml

# Download latest artifact
gh run download --name voice-ai-android-debug-42

# Or download from specific run
gh run download 123456789
```

### Method 3: Direct Download (with GitHub token)

If you want to automate downloads:

```bash
# Get your GitHub token from: https://github.com/settings/tokens
# Then use the GitHub API

REPO="malepatip/voice-ai-android"
TOKEN="your_github_token"
RUN_ID="latest"

# Download artifact
curl -L -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/$REPO/actions/artifacts/$ARTIFACT_ID/zip" \
  -o apk.zip
```

## Understanding the Workflow

### When Does It Build?

The workflow automatically runs on:
- ✅ **Push to any `claude/**` branch** (like your current branch)
- ✅ **Push to `main` or `develop` branches**
- ✅ **Pull requests to `main`**
- ✅ **Manual trigger** (workflow_dispatch)

### Build Time

- **First build**: ~5-8 minutes (downloads dependencies)
- **Subsequent builds**: ~3-5 minutes (uses cache)

### What Gets Built?

- **APK Type**: Debug (signed with debug keystore)
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Size**: ~45-50 MB
- **Retention**: Artifacts stored for 30 days

## Viewing Build Status

### In GitHub UI

1. **Commit Badge**:
   - Each commit shows a status icon
   - ✅ = Build passed
   - ❌ = Build failed
   - 🟡 = Build in progress

2. **Actions Tab**:
   - See all workflow runs
   - View logs and download artifacts

### Build Summary

After each build, you'll see:
```
🎉 Build Successful!

📦 APK Information
- Size: 45M
- Build Number: #42
- Branch: claude/slm-i-focus-011CUbxHNMKE2qmmjqhroWyY
- Commit: 7ed9a76...

📥 Download APK
1. Go to the Actions tab
2. Click on this workflow run
3. Scroll down to Artifacts
4. Download: voice-ai-android-debug-42
```

## Troubleshooting

### Build Fails

**Check the logs**:
1. Go to Actions tab
2. Click on the failed run
3. Click on "Build Debug APK" job
4. Expand the failed step to see errors

**Common issues**:
- **Gradle build error**: Check `build.gradle.kts` syntax
- **Missing dependencies**: Usually auto-resolved on retry
- **Out of memory**: Workflow has 7GB RAM limit

**Solution**: Push a fix and workflow will auto-retry

### Can't Find Artifact

**Ensure**:
- Build completed successfully (green checkmark)
- You're logged into GitHub
- Artifact hasn't expired (30 days retention)

### Download is Too Slow

**Alternative**:
- Use GitHub CLI (faster)
- Wait for off-peak hours
- Download directly on Android if repo is public

## Upload to Google Drive

### Manual Method (Current)

1. **Download APK from GitHub Actions**
2. **Go to your Google Drive folder**:
   ```
   https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf
   ```
3. **Click "New" → "File upload"**
4. **Select the APK**
5. **Wait for upload**
6. **Download on Android device** (see `/docs/APK_TESTING_GUIDE.md`)

### Automated Method (Advanced)

If you want GitHub Actions to auto-upload to Google Drive:

1. **Set up Google Drive API credentials**
2. **Add secrets to GitHub**:
   - `Settings` → `Secrets` → `Actions`
   - Add: `GDRIVE_CREDENTIALS`
   - Add: `GDRIVE_FOLDER_ID`

3. **Modify workflow** (uncomment the upload-to-drive job)

4. **APK will auto-upload** after each build

*Note: This requires additional setup with Google Cloud Console*

## Workflow Configuration

### File Location
```
.github/workflows/build-apk.yml
```

### Customize Triggers

Edit the workflow file to change when it runs:

```yaml
on:
  push:
    branches:
      - 'claude/**'  # Your branches
      - main
      - develop
  pull_request:
    branches:
      - main
  workflow_dispatch:  # Manual trigger
```

### Manual Trigger

You can manually trigger a build:
1. Go to **Actions** tab
2. Click **"Build and Upload APK"** workflow
3. Click **"Run workflow"** button
4. Select branch
5. Click **"Run workflow"**

## APK Naming

APKs are named with build number:
```
voice-ai-android-debug-1.zip   (First build)
voice-ai-android-debug-2.zip   (Second build)
voice-ai-android-debug-42.zip  (42nd build)
```

This helps you track which version you're testing.

## Security Notes

### Debug vs Release

- **Debug APK** (current):
  - Signed with debug keystore
  - Larger size (~45MB)
  - Easier to test
  - **Do not distribute publicly**

- **Release APK** (future):
  - Requires signing key
  - Smaller size (~30MB)
  - Optimized and obfuscated
  - For production distribution

### Secrets in Workflow

- Workflow doesn't expose secrets
- API keys not included in APK (requires `local.properties`)
- Debug keystore is public (safe for testing)

## Next Steps

### After Downloading APK

1. **Upload to Google Drive** (manual)
2. **Download on Android device**
3. **Enable "Install from unknown sources"**
4. **Install APK**
5. **Test the app**
6. **Report issues** by creating GitHub issues

### Improving the Workflow

Want to enhance the workflow?

- **Auto-upload to Drive**: Add Google Drive integration
- **Send notifications**: Add Slack/Discord webhooks
- **Create releases**: Automatically create GitHub releases
- **Build release APK**: Add release variant builds
- **Run tests**: Add automated testing step

See the commented sections in `build-apk.yml` for examples.

## Support

### Getting Help

If you encounter issues:
1. Check the **Actions logs** for errors
2. Review **`/docs/APK_TESTING_GUIDE.md`** for testing help
3. Create a **GitHub issue** with workflow run link
4. Check **GitHub Actions documentation**: https://docs.github.com/actions

### Useful Links

- **Your Repository**: https://github.com/malepatip/voice-ai-android
- **Actions Tab**: https://github.com/malepatip/voice-ai-android/actions
- **Your Google Drive Folder**: https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf

---

## Quick Reference

### Download APK (3 Steps)
```
1. GitHub → Actions → Latest Run
2. Scroll to Artifacts → Click to download
3. Extract ZIP → Upload to Google Drive
```

### Install on Android (5 Steps)
```
1. Download from Google Drive
2. Enable "Install from unknown sources"
3. Tap APK file
4. Tap "Install"
5. Grant permissions
```

### Build Status
```
✅ Green = Success (download APK)
❌ Red = Failed (check logs)
🟡 Yellow = In Progress (wait)
```

---

**Last Updated**: 2025-10-30
**Workflow File**: `.github/workflows/build-apk.yml`
**APK Retention**: 30 days

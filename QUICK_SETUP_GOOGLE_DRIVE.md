# Quick Setup: Google Drive Auto-Upload (5 Minutes)

## What Was Added

✅ **Automatic APK upload to Google Drive** after each successful build
✅ Works on `main` and `claude/*` branches
✅ Each build creates a uniquely named APK file

## Setup (Required - 5 Minutes)

### Step 1: Create Service Account (2 minutes)

1. Go to: https://console.cloud.google.com/
2. Create project → Enable "Google Drive API"
3. Create Service Account → Download JSON key

### Step 2: Share Drive Folder (1 minute)

1. Open JSON file, copy the `client_email` (looks like: `xyz@project.iam.gserviceaccount.com`)
2. Go to Google Drive folder: https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf
3. Click "Share" → Paste email → Give "Editor" access → Share

### Step 3: Add GitHub Secrets (2 minutes)

1. Go to: https://github.com/malepatip/voice-ai-android/settings/secrets/actions
2. Click "New repository secret"

**Add Secret 1:**
- Name: `GOOGLE_DRIVE_CREDENTIALS`
- Value: Paste **entire contents** of JSON file

**Add Secret 2:**
- Name: `GOOGLE_DRIVE_FOLDER_ID`
- Value: `1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf`

### Step 4: Test It! (5 minutes)

1. Push a commit to your branch
2. Watch Actions: https://github.com/malepatip/voice-ai-android/actions
3. After build succeeds, check Google Drive - APK should be there! 🎉

## How to Install APK on Your Phone

1. **Open Google Drive app** on Android
2. **Find the APK**: `voice-ai-android-debug-build-{number}.apk`
3. **Tap to install** (enable "Install unknown apps" if prompted)
4. **Done!** 🎉

## Files Changed

- `.github/workflows/build-apk.yml` - Added Google Drive upload step
- `GOOGLE_DRIVE_SETUP.md` - Detailed setup guide
- `QUICK_SETUP_GOOGLE_DRIVE.md` - This quick reference

## What Happens Now

Every successful build will:
1. ✅ Build APK
2. ✅ Upload to GitHub Artifacts (as before)
3. ✅ **Upload to Google Drive** (NEW!)
4. ✅ Show download links in build summary

## APK Naming

Format: `voice-ai-android-debug-build-{number}.apk`

Examples:
- Build #42: `voice-ai-android-debug-build-42.apk`
- Build #43: `voice-ai-android-debug-build-43.apk`

Each build creates a new file (no overwriting).

## Troubleshooting

**Upload step fails?**
- Check that secrets are added correctly
- Verify Google Drive folder is shared with service account email
- See `GOOGLE_DRIVE_SETUP.md` for detailed troubleshooting

**Want to test without pushing?**
- Go to Actions → "Build and Upload APK" → "Run workflow"
- Select your branch → Click "Run workflow"

---

**Next Action**: Add the two secrets to GitHub, then push a commit to test!

**Need detailed help?** See `GOOGLE_DRIVE_SETUP.md`

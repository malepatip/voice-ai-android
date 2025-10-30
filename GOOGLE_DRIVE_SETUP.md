# Google Drive Upload Setup for GitHub Actions

This guide explains how to set up automatic APK uploads to Google Drive from GitHub Actions.

## Overview

The workflow automatically uploads the built APK to your Google Drive folder after each successful build on `main` or `claude/*` branches.

## Setup Steps

### 1. Create Google Cloud Project & Enable Drive API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or use existing)
3. Enable the **Google Drive API**:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click "Enable"

### 2. Create Service Account

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "Service Account"
3. Name it: `github-actions-drive-upload`
4. Click "Create and Continue"
5. Skip optional steps and click "Done"

### 3. Generate Service Account Key

1. Click on the service account you just created
2. Go to the "Keys" tab
3. Click "Add Key" > "Create new key"
4. Choose **JSON** format
5. Click "Create" - a JSON file will be downloaded

**⚠️ IMPORTANT**: Keep this file secure! Never commit it to Git!

### 4. Share Google Drive Folder with Service Account

1. Open the JSON file you downloaded
2. Find the `client_email` field (looks like: `name@project.iam.gserviceaccount.com`)
3. Go to your Google Drive folder: https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf
4. Click "Share"
5. Paste the service account email
6. Give it **Editor** permissions
7. Click "Share"

### 5. Add Secrets to GitHub Repository

1. Go to your GitHub repository: https://github.com/malepatip/voice-ai-android
2. Click "Settings" > "Secrets and variables" > "Actions"
3. Click "New repository secret"

**Add these two secrets:**

#### Secret 1: GOOGLE_DRIVE_CREDENTIALS
- **Name**: `GOOGLE_DRIVE_CREDENTIALS`
- **Value**: Copy the **entire contents** of the downloaded JSON file
  ```json
  {
    "type": "service_account",
    "project_id": "your-project",
    "private_key_id": "...",
    "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
    "client_email": "github-actions-drive-upload@your-project.iam.gserviceaccount.com",
    "client_id": "...",
    ...
  }
  ```

#### Secret 2: GOOGLE_DRIVE_FOLDER_ID
- **Name**: `GOOGLE_DRIVE_FOLDER_ID`
- **Value**: `1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf`
  (This is the folder ID from your Google Drive URL)

### 6. Test the Workflow

1. Push a commit to the `claude/*` branch or `main`
2. Go to Actions tab: https://github.com/malepatip/voice-ai-android/actions
3. Watch the build complete
4. The "Upload to Google Drive" step should succeed ✅
5. Check your Google Drive folder - the APK should be there!

## Workflow Details

### When Does Upload Happen?

The workflow uploads to Google Drive when:
- ✅ Build succeeds
- ✅ Branch is `main` OR starts with `claude/`

### APK Naming

Files are named: `voice-ai-android-debug-build-{build_number}.apk`

Example: `voice-ai-android-debug-build-42.apk`

### Overwrite Behavior

`overwrite: "false"` means each build creates a new file (no overwriting).

To change this behavior, edit `.github/workflows/build-apk.yml` line 73:
```yaml
overwrite: "true"  # Will overwrite previous file with same name
```

## Troubleshooting

### Error: "insufficient authentication scopes"

**Solution**: The service account needs proper Drive API permissions.
1. Check that Google Drive API is enabled
2. Verify the service account has the correct JSON key
3. Ensure the folder is shared with the service account email

### Error: "The caller does not have permission"

**Solution**: Share the Google Drive folder with the service account email (from JSON's `client_email`).

### Error: "Invalid credentials"

**Solution**:
1. Verify `GOOGLE_DRIVE_CREDENTIALS` secret contains the complete JSON
2. Make sure JSON is valid (no extra spaces, proper formatting)
3. Generate a new key if needed

### Upload Step is Skipped

**Solution**: Check the `if` condition in the workflow:
- Must be on `main` or `claude/*` branch
- Build must have succeeded

To enable on all branches, change line 66:
```yaml
if: success()  # Upload on all branches
```

## Security Best Practices

✅ **DO:**
- Keep service account JSON secure
- Only give Editor access (not Owner)
- Use specific folder ID (not root drive)
- Rotate keys periodically

❌ **DON'T:**
- Commit service account JSON to Git
- Share secrets publicly
- Give unnecessary permissions

## Alternative: Manual Setup via rclone

If you prefer not to use service accounts, you can also:
1. Use rclone with OAuth authentication
2. Set up a personal access token
3. Use GitHub Actions with rclone secret

See [rclone documentation](https://rclone.org/drive/) for details.

## Need Help?

If you encounter issues:
1. Check the GitHub Actions logs for detailed error messages
2. Verify all secrets are set correctly
3. Test the service account access manually using `gcloud` CLI
4. Ensure the Google Drive folder exists and is accessible

---

**Status**: 🟢 Ready to use after secrets are configured

**Last Updated**: 2025-10-30

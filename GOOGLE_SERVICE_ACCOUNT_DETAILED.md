# Google Service Account Setup - Detailed Step-by-Step

## Part 1: Create Project & Enable API

### Step 1.1: Go to Google Cloud Console
1. Open: https://console.cloud.google.com/
2. Sign in with your Google account

### Step 1.2: Create a New Project
1. Look at the **top bar** - you'll see the project selector dropdown (next to "Google Cloud")
2. Click on the **project dropdown** (it might say "Select a project" or show your current project name)
3. A popup appears - click **"NEW PROJECT"** button (top right of the popup)
4. Enter project details:
   - **Project name**: `voice-ai-android-uploads` (or any name you like)
   - **Location**: Leave as default (No organization)
5. Click **"CREATE"** button
6. Wait 10-20 seconds for project creation
7. You'll see a notification when it's ready - click **"SELECT PROJECT"**

### Step 1.3: Enable Google Drive API
1. In the **left sidebar**, click on **"☰" (hamburger menu)** if it's collapsed
2. Find and click: **"APIs & Services"** → **"Library"**
   - OR go directly to: https://console.cloud.google.com/apis/library
3. In the search box at the top, type: **"Google Drive API"**
4. Click on **"Google Drive API"** from the results
5. Click the blue **"ENABLE"** button
6. Wait for it to enable (5-10 seconds)
7. You'll see "API enabled" ✅

## Part 2: Create Service Account

### Step 2.1: Navigate to Service Accounts
1. In the **left sidebar**, click: **"☰"** → **"APIs & Services"** → **"Credentials"**
   - OR go directly to: https://console.cloud.google.com/apis/credentials
2. At the top of the page, click the **"+ CREATE CREDENTIALS"** button
3. From the dropdown, select: **"Service account"**

### Step 2.2: Create Service Account - Page 1
You'll see a form with 3 pages:

**Page 1: Service account details**
1. **Service account name**: Enter `github-actions-uploader`
   - (The service account ID will auto-fill: `github-actions-uploader@...`)
2. **Service account description**: Enter `Uploads APKs from GitHub Actions to Google Drive`
3. Click **"CREATE AND CONTINUE"** button

### Step 2.3: Create Service Account - Page 2
**Page 2: Grant this service account access to project**
1. **Select a role** dropdown:
   - You can skip this (leave blank) OR select "Basic" → "Editor"
   - Not critical for Drive access
2. Click **"CONTINUE"** button

### Step 2.4: Create Service Account - Page 3
**Page 3: Grant users access to this service account**
1. Leave all fields blank (optional section)
2. Click **"DONE"** button

### Step 2.5: You're back at Credentials page
You should now see your service account in the list:
- Name: `github-actions-uploader`
- Email: `github-actions-uploader@your-project.iam.gserviceaccount.com`

## Part 3: Download JSON Key

### Step 3.1: Create and Download Key
1. On the **Credentials** page, find your service account in the list
2. Click on the **service account email** (the blue link)
3. You're now on the service account details page
4. Click the **"KEYS"** tab (near the top)
5. Click **"ADD KEY"** dropdown button
6. Select **"Create new key"**
7. A popup appears:
   - **Key type**: Select **"JSON"** (should be selected by default)
8. Click **"CREATE"** button
9. A JSON file will automatically download to your computer
   - Filename: `your-project-xxxxx-xxxxxx.json`
   - ⚠️ **IMPORTANT**: Keep this file secure! Don't share it!

### Step 3.2: Verify JSON File
1. Open the downloaded JSON file with a text editor
2. You should see something like this:
```json
{
  "type": "service_account",
  "project_id": "voice-ai-android-uploads",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIE...\n-----END PRIVATE KEY-----\n",
  "client_email": "github-actions-uploader@voice-ai-android-uploads.iam.gserviceaccount.com",
  "client_id": "123456789...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  ...
}
```
3. **Find and copy the `client_email`** value - you'll need this in the next step!

## Part 4: Share Google Drive Folder

### Step 4.1: Get Service Account Email
From your JSON file, copy the `client_email` value.
Example: `github-actions-uploader@voice-ai-android-uploads.iam.gserviceaccount.com`

### Step 4.2: Share Drive Folder
1. Go to your Google Drive folder: https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf
2. Click the **"Share"** button (top right)
3. In the "Add people and groups" field:
   - **Paste the service account email** you copied
   - Press Enter
4. A dropdown appears next to the email:
   - Change from "Viewer" to **"Editor"**
5. **UNCHECK** "Notify people" (service account doesn't need notification)
6. Click **"Share"** button
7. If a warning appears about sharing outside your org, click **"Share anyway"**

✅ **Done!** The service account now has access to upload files to your Drive folder.

## Part 5: Add Secrets to GitHub

### Step 5.1: Navigate to GitHub Secrets
1. Go to: https://github.com/malepatip/voice-ai-android/settings/secrets/actions
2. You should see "Actions secrets and variables" page

### Step 5.2: Add Secret 1 - Credentials
1. Click **"New repository secret"** button (green button, top right)
2. **Name**: Enter exactly `GOOGLE_DRIVE_CREDENTIALS` (all caps, underscores)
3. **Secret**:
   - Open your downloaded JSON file
   - **Copy the ENTIRE contents** (everything from `{` to `}`)
   - Paste into the "Secret" field
4. Click **"Add secret"** button

### Step 5.3: Add Secret 2 - Folder ID
1. Click **"New repository secret"** again
2. **Name**: Enter exactly `GOOGLE_DRIVE_FOLDER_ID` (all caps, underscores)
3. **Secret**: Enter `1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf`
4. Click **"Add secret"** button

### Step 5.4: Verify Secrets
You should now see 2 secrets in the list:
- `GOOGLE_DRIVE_CREDENTIALS` (Updated X seconds ago)
- `GOOGLE_DRIVE_FOLDER_ID` (Updated X seconds ago)

## ✅ Setup Complete!

### Test It:
1. Push any commit to your branch
2. Watch: https://github.com/malepatip/voice-ai-android/actions
3. After build succeeds, check your Google Drive folder
4. You should see the APK! 🎉

---

## Troubleshooting

### Can't find "APIs & Services"?
- Click the **☰ hamburger menu** (top left)
- Scroll down the sidebar
- Look for "APIs & Services" section

### Can't find "Service account" option?
- Make sure you're at: https://console.cloud.google.com/apis/credentials
- Click "+ CREATE CREDENTIALS" at the top
- If "Service account" is greyed out, make sure your project is selected (check top bar)

### Download didn't work?
- Check your browser's download folder
- Try a different browser (Chrome works best)
- Make sure pop-ups aren't blocked

### Need More Help?
Let me know which step you're stuck on and I can provide more detailed instructions!

---

**Quick Links**:
- Google Cloud Console: https://console.cloud.google.com/
- Credentials Page: https://console.cloud.google.com/apis/credentials
- Your GitHub Secrets: https://github.com/malepatip/voice-ai-android/settings/secrets/actions
- Your Google Drive Folder: https://drive.google.com/drive/folders/1htKA6gdQoOqRN2XlSabW72hZy9j1RKJf

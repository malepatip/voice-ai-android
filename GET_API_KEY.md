# How to Get Cartesia Sonic 3 API Key

## Step 1: Sign Up

1. Go to: https://cartesia.ai
2. Click **"Sign Up"** or **"Get Started"**
3. Create account with email
4. **Free tier**: 10,000 characters/month

## Step 2: Get API Key

1. After signup, go to: https://play.cartesia.ai/
2. Click your profile (top right)
3. Click **"API Keys"**
4. Click **"Create New API Key"**
5. Copy the key (looks like: `sk-...`)

## Step 3: Add to App

**Option A: For testing (easiest)**

Edit `data/src/main/kotlin/com/voiceai/data/repository/SimpleTTSRepository.kt`:

Find this line:
```kotlin
private val apiKey = "YOUR_API_KEY_HERE"
```

Replace with your actual key:
```kotlin
private val apiKey = "sk-your-actual-key-here"
```

**Option B: Production (secure)**

Add to `local.properties`:
```
SONIC3_API_KEY=sk-your-actual-key-here
```

Then update build.gradle.kts to read from there.

## Step 4: Test

1. Rebuild the app
2. Install on your device
3. Type text in the field
4. Click "🔊 Speak"
5. You should hear the text spoken!

---

**That's it!** Your app now has working text-to-speech! 🎉

package com.voiceai

import android.app.Application

/**
 * Application class for Voice AI Android
 *
 * TODO: Add Hilt initialization when ready:
 * @HiltAndroidApp
 * class VoiceAIApplication : Application() { ... }
 *
 * For now, this is a minimal placeholder to allow the app to build.
 */
class VoiceAIApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Initialize components when implemented:
        // - Hilt dependency injection
        // - Timber logging
        // - Crash reporting (optional)
        // - Performance monitoring (optional)

        // Placeholder initialization
        initializePlaceholder()
    }

    private fun initializePlaceholder() {
        // Log app initialization
        android.util.Log.d(TAG, "Voice AI Application initialized")
        android.util.Log.d(TAG, "Architecture: Hybrid SLM (Gemini Nano + Llama 3.2)")
        android.util.Log.d(TAG, "TTS: Cartesia Sonic 3")
    }

    companion object {
        private const val TAG = "VoiceAIApp"
    }
}

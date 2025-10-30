# Cartesia Sonic 3 TTS Integration Guide

## Overview

Cartesia Sonic 3 is a state-of-the-art Text-to-Speech (TTS) model announced in January 2025, designed for real-time conversational AI with emotional expressiveness. This guide analyzes how to integrate Sonic 3 into the Voice AI Android hybrid SLM architecture.

## Sonic 3 Key Features

### 1. Ultra-Low Latency ⚡
- **Model latency**: 90ms
- **End-to-end latency**: 190ms
- **Industry leading**: Fastest TTS currently available
- **Perfect for**: Real-time voice conversations

### 2. Emotional Expression 🎭
- **Full range of emotions**: Joy, sadness, anger, surprise, etc.
- **Laughter generation**: Can naturally laugh in responses
- **Contextual emotion**: Maintains emotional tone across conversation
- **API control**: Fine-grained emotion parameters via SSML tags

### 3. Multilingual Support 🌍
- **42 languages** supported
- **Language switching**: Change voices between languages (Pro plan)
- **Localization**: Natural accent and pronunciation

### 4. Technical Architecture
- **State Space Models (SSMs)**: Unlike Transformer-based models
- **Context retention**: Remembers conversation "topic and vibe"
- **No constant reprocessing**: More efficient than traditional models
- **Real-time optimization**: Built specifically for conversational AI

### 5. Control Parameters
- **Volume**: Adjustable output volume
- **Speed**: Variable speech rate (0.5x to 2x)
- **Emotion**: Granular emotional control
- **SSML support**: Advanced markup for prosody control

## Pricing & Plans

### Free Tier
- **10,000 credits/month**
- **1 credit per character** (standard TTS)
- **1 parallel request**
- **15 languages** access
- **Limitations**:
  - ❌ No commercial use
  - ❌ No voice cloning
  - ❌ No language localization features

### Pro Plan ($5/month)
- **100,000 credits/month** (~100,000 characters)
- **1 credit per character** (standard TTS)
- **1.5 credits per character** (Pro Voice Cloning)
- **3 parallel requests**
- **42 languages** access
- **Features**:
  - ✅ Commercial use allowed
  - ✅ Instant voice cloning
  - ✅ Language switching
  - ✅ Full API access

### Usage Estimates

**Example conversational AI app**:
- Average response: 100 characters
- 1,000 responses/day = 100,000 characters
- **Free tier**: 100 responses (limited)
- **Pro plan**: 1,000 responses/day

**Cost comparison** (per 1M characters):
- Sonic 3: ~$50 (at $5 for 100k credits)
- ElevenLabs: ~$60-300 (depending on tier)
- Google Cloud TTS: ~$4-16 (cheaper but less expressive)
- On-device TTS: $0 (but lower quality)

## Integration Strategies for Voice AI Android

### Strategy 1: Hybrid TTS (Recommended)
Use **on-device TTS for simple responses** + **Sonic 3 for emotion-aware responses**

#### When to use On-Device TTS:
- Simple command confirmations: "Timer set for 5 minutes"
- Low emotion context: Neutral responses
- Offline mode
- Battery saving mode
- Privacy-critical responses

#### When to use Sonic 3:
- Emotion-aware conversations (detected sad/angry/happy user)
- Complex, engaging responses
- When expressiveness matters (storytelling, empathy)
- When user explicitly requests high-quality voice
- Languages not supported on-device

#### Architecture Integration:
```
[AI Response] → [Emotion Context]
       ↓
[TTS Router] → Decision:
       ↓                    ↓
 [On-Device TTS]    [Sonic 3 API]
       ↓                    ↓
   [Audio Output] ← [Audio Output]
```

### Strategy 2: Sonic 3 Only
Use Sonic 3 for all TTS (requires network)

**Pros**:
- ✅ Consistent high-quality voice
- ✅ Emotional expressiveness
- ✅ Ultra-low latency (190ms)

**Cons**:
- ❌ Requires network always
- ❌ Monthly cost (~$5/month for moderate use)
- ❌ Privacy: audio generated in cloud

### Strategy 3: On-Device Only
Use Android native TTS (current plan)

**Pros**:
- ✅ Free
- ✅ Offline capable
- ✅ Privacy-preserving

**Cons**:
- ❌ No emotional expressiveness
- ❌ Lower voice quality
- ❌ Limited language support

## Recommended: Hybrid Approach

### Implementation Plan

#### 1. Add TTS Router Logic

```kotlin
// domain/src/main/kotlin/com/voiceai/domain/router/TTSRouter.kt
package com.voiceai.domain.router

import com.voiceai.domain.entity.Emotion
import com.voiceai.domain.entity.AIResponse

sealed class TTSDecision {
    object OnDevice : TTSDecision()
    data class Sonic3(val emotion: String? = null, val speed: Float = 1.0f) : TTSDecision()
}

class TTSRouter {
    fun route(
        response: AIResponse,
        emotion: Emotion?,
        networkAvailable: Boolean,
        userPreference: TTSPreference = TTSPreference.BALANCED
    ): TTSDecision {
        // Force on-device if offline
        if (!networkAvailable) {
            return TTSDecision.OnDevice
        }

        // Privacy-first mode
        if (userPreference == TTSPreference.PRIVACY_FIRST) {
            return TTSDecision.OnDevice
        }

        // Use Sonic 3 for emotion-aware responses
        if (emotion?.isSignificant() == true) {
            return TTSDecision.Sonic3(
                emotion = mapEmotionToSonic3(emotion),
                speed = 1.0f
            )
        }

        // Use Sonic 3 for long, complex responses
        if (response.content.length > 200) {
            return TTSDecision.Sonic3()
        }

        // Default: on-device for simple responses
        return TTSDecision.OnDevice
    }

    private fun mapEmotionToSonic3(emotion: Emotion): String {
        return when (emotion.primary) {
            EmotionType.HAPPY -> "cheerful"
            EmotionType.SAD -> "empathetic"
            EmotionType.ANGRY -> "calm" // Counter emotion for de-escalation
            EmotionType.FEARFUL -> "reassuring"
            EmotionType.SURPRISED -> "excited"
            else -> "neutral"
        }
    }
}

enum class TTSPreference {
    PRIVACY_FIRST,  // Always on-device
    BALANCED,       // Smart routing
    QUALITY_FIRST   // Prefer Sonic 3
}
```

#### 2. Add Sonic 3 Repository Interface

```kotlin
// domain/src/main/kotlin/com/voiceai/domain/repository/TTSRepository.kt
package com.voiceai.domain.repository

interface TTSRepository {
    /**
     * Synthesize speech using on-device TTS
     */
    suspend fun synthesizeOnDevice(
        text: String,
        language: String = "en-US",
        pitch: Float = 1.0f,
        rate: Float = 1.0f
    ): Result<ByteArray>

    /**
     * Synthesize speech using Sonic 3 API
     */
    suspend fun synthesizeSonic3(
        text: String,
        voiceId: String = "default",
        emotion: String? = null,
        speed: Float = 1.0f,
        outputFormat: AudioFormat = AudioFormat.PCM_16KHZ
    ): Result<ByteArray>

    /**
     * Check if Sonic 3 API is available
     */
    suspend fun isSonic3Available(): Boolean
}

enum class AudioFormat {
    PCM_16KHZ,
    PCM_24KHZ,
    MP3,
    OGG
}
```

#### 3. Implement Sonic 3 API Client

```kotlin
// data/src/main/kotlin/com/voiceai/data/api/Sonic3ApiClient.kt
package com.voiceai.data.api

import retrofit2.http.*

interface Sonic3ApiClient {
    @POST("tts/stream")
    suspend fun synthesize(
        @Header("X-API-Key") apiKey: String,
        @Body request: Sonic3Request
    ): Response<ByteArray>
}

data class Sonic3Request(
    val text: String,
    val voice: VoiceConfig = VoiceConfig(),
    val output_format: String = "pcm_16000"
)

data class VoiceConfig(
    val mode: String = "id",
    val id: String = "default",
    val speed: Float = 1.0f,
    val emotion: String? = null
)
```

#### 4. Implement Repository

```kotlin
// data/src/main/kotlin/com/voiceai/data/repository/TTSRepositoryImpl.kt
package com.voiceai.data.repository

import android.speech.tts.TextToSpeech
import com.voiceai.data.api.Sonic3ApiClient
import com.voiceai.domain.repository.TTSRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TTSRepositoryImpl @Inject constructor(
    private val sonic3Client: Sonic3ApiClient,
    private val textToSpeech: TextToSpeech,
    private val sonic3ApiKey: String
) : TTSRepository {

    override suspend fun synthesizeOnDevice(
        text: String,
        language: String,
        pitch: Float,
        rate: Float
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            // Implementation using Android TTS
            // This would use TextToSpeech.synthesizeToFile()
            Result.success(ByteArray(0)) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun synthesizeSonic3(
        text: String,
        voiceId: String,
        emotion: String?,
        speed: Float,
        outputFormat: AudioFormat
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val request = Sonic3Request(
                text = text,
                voice = VoiceConfig(
                    id = voiceId,
                    speed = speed,
                    emotion = emotion
                ),
                output_format = when (outputFormat) {
                    AudioFormat.PCM_16KHZ -> "pcm_16000"
                    AudioFormat.PCM_24KHZ -> "pcm_24000"
                    AudioFormat.MP3 -> "mp3"
                    AudioFormat.OGG -> "ogg"
                }
            )

            val response = sonic3Client.synthesize(sonic3ApiKey, request)

            if (response.isSuccessful) {
                Result.success(response.body() ?: ByteArray(0))
            } else {
                Result.failure(Exception("Sonic 3 API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isSonic3Available(): Boolean {
        return try {
            // Ping health endpoint or check API key validity
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

#### 5. Update Use Case to Include TTS

```kotlin
// domain/src/main/kotlin/com/voiceai/domain/usecase/ProcessVoiceInputUseCase.kt
// Add TTS step at the end

suspend operator fun invoke(...): Result<VoiceProcessingResult> {
    // ... existing STT, Intent, Emotion, Routing, Inference logic ...

    // NEW: TTS Routing and Synthesis
    val ttsDecision = ttsRouter.route(
        response = response,
        emotion = emotion,
        networkAvailable = context.networkAvailable,
        userPreference = userPreference.toTTSPreference()
    )

    val ttsStartTime = System.currentTimeMillis()
    val audioOutput = when (ttsDecision) {
        is TTSDecision.OnDevice -> {
            ttsRepository.synthesizeOnDevice(response.content)
        }
        is TTSDecision.Sonic3 -> {
            ttsRepository.synthesizeSonic3(
                text = response.content,
                emotion = ttsDecision.emotion,
                speed = ttsDecision.speed
            )
        }
    }
    val ttsLatency = System.currentTimeMillis() - ttsStartTime

    // Update metrics with TTS latency
    val finalMetrics = processingMetrics.copy(
        ttsLatencyMs = ttsLatency,
        totalLatencyMs = processingMetrics.totalLatencyMs + ttsLatency
    )

    return Result.success(
        VoiceProcessingResult(
            response = response,
            audioOutput = audioOutput.getOrNull(),
            emotion = emotion,
            intent = intent,
            routeDecision = routeDecision,
            ttsDecision = ttsDecision,
            metrics = finalMetrics
        )
    )
}
```

## Cost Analysis: Hybrid vs Sonic 3 Only

### Scenario: Moderate Usage (1,000 conversations/month)

**Assumptions**:
- Average response length: 100 characters
- 30% require emotional TTS (300 conversations)
- 70% use on-device TTS (700 conversations)

#### Hybrid Approach (Recommended):
- Sonic 3 usage: 300 × 100 = 30,000 characters
- Cost: Free tier (10,000 credits) + Pro plan needed
- **Monthly cost**: $5/month (Pro plan)
- **Characters used**: 30,000 / 100,000 available (30% utilization)

#### Sonic 3 Only:
- Sonic 3 usage: 1,000 × 100 = 100,000 characters
- Cost: Exactly at Pro plan limit
- **Monthly cost**: $5/month
- **Characters used**: 100,000 / 100,000 available (100% utilization)

#### On-Device Only:
- **Monthly cost**: $0
- **Quality**: Lower, no emotion

### Scenario: Heavy Usage (10,000 conversations/month)

**Assumptions**:
- Average response length: 100 characters
- 30% require emotional TTS (3,000 conversations)
- 70% use on-device TTS (7,000 conversations)

#### Hybrid Approach:
- Sonic 3 usage: 300,000 characters
- Cost: Pro plan (100k) + additional credits
- Additional: 200,000 × $0.00005 per character = $10
- **Monthly cost**: $5 (Pro) + $10 (overage) = **$15/month**

#### Sonic 3 Only:
- Sonic 3 usage: 1,000,000 characters
- Cost: Pro plan (100k) + 900k additional
- Additional: 900,000 × $0.00005 = $45
- **Monthly cost**: $5 + $45 = **$50/month**

### Recommendation: Hybrid Approach ✅

**Pros**:
- 70% cost savings vs Sonic 3 only (heavy usage)
- Maintains emotional expressiveness where needed
- Offline capability
- Privacy-preserving for simple responses
- Scales cost-effectively

**Cons**:
- Requires implementation of routing logic (already done in architecture)
- Quality variance between on-device and Sonic 3

## Integration Steps

### Week 1: Foundation
1. Add Retrofit dependency for Sonic 3 API
2. Implement `TTSRepository` interface
3. Implement `TTSRouter` with decision logic
4. Add Sonic 3 API client

### Week 2: Android Implementation
1. Set up Android TextToSpeech for on-device
2. Implement `TTSRepositoryImpl` with both engines
3. Add API key configuration (secrets management)
4. Create audio playback pipeline

### Week 3: Integration
1. Update `ProcessVoiceInputUseCase` to include TTS
2. Wire up dependency injection (Hilt)
3. Add UI controls for TTS preference
4. Implement audio streaming for low latency

### Week 4: Testing & Optimization
1. Test both TTS engines
2. Measure latency (target: <200ms total)
3. Optimize audio buffering
4. Add fallback mechanisms

## Updated Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Android Application                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │ Voice Input  │───▶│ STT Engine   │───▶│   Router     │      │
│  │ (AudioRecord)│    │ (On-device)  │    │   Logic      │      │
│  └──────────────┘    └──────────────┘    └──────┬───────┘      │
│                                                   │               │
│                            ┌──────────────────────┼───────────┐  │
│                            ▼                      ▼           │  │
│                   ┌────────────────┐    ┌─────────────────┐  │  │
│                   │ Gemini Nano    │    │  Backend API    │  │  │
│                   │ (On-device)    │    │  Client         │  │  │
│                   │ - Quick intent │    │  (Llama 3.2)    │  │  │
│                   └────────┬───────┘    └────────┬────────┘  │  │
│                            │                     │           │  │
│                            └──────────┬──────────┘           │  │
│                                       ▼                      │  │
│                            ┌──────────────────┐             │  │
│                            │ Response Handler │             │  │
│                            │ + Emotion Layer  │             │  │
│                            └────────┬─────────┘             │  │
│                                     ▼                       │  │
│                            ┌──────────────────┐             │  │
│                            │   TTS Router     │ ← NEW!      │  │
│                            └────────┬─────────┘             │  │
│                                     │                       │  │
│                    ┌────────────────┼────────────────┐      │  │
│                    ▼                ▼                ▼      │  │
│           ┌────────────────┐ ┌──────────────┐             │  │
│           │ On-Device TTS  │ │  Sonic 3 API │ ← NEW!      │  │
│           │ (Simple/Fast)  │ │  (Emotional) │             │  │
│           └────────┬───────┘ └──────┬───────┘             │  │
│                    │                │                      │  │
│                    └────────┬───────┘                      │  │
│                             ▼                              │  │
│                  ┌──────────────────┐                      │  │
│                  │ Audio Playback   │                      │  │
│                  └──────────────────┘                      │  │
└─────────────────────────────────────────────────────────────────┘
```

## Security & Privacy Considerations

### API Key Management
```kotlin
// Store Sonic 3 API key securely
// app/src/main/kotlin/com/voiceai/config/ApiConfig.kt
object ApiConfig {
    // DO NOT hardcode! Use BuildConfig or secure storage
    val SONIC3_API_KEY: String
        get() = BuildConfig.SONIC3_API_KEY

    const val SONIC3_BASE_URL = "https://api.cartesia.ai"
}

// In app/build.gradle.kts
android {
    defaultConfig {
        // Read from local.properties
        buildConfigField("String", "SONIC3_API_KEY", "\"${project.findProperty("SONIC3_API_KEY")}\"")
    }
}
```

### Privacy Best Practices
1. **User consent**: Inform users when Sonic 3 is used (cloud processing)
2. **Data retention**: Cartesia's policy on audio data
3. **Opt-out**: Allow users to force on-device TTS only
4. **Audit logging**: Track when Sonic 3 API is called

## Comparison: Sonic 3 vs Alternatives

| Feature | Sonic 3 | ElevenLabs | Google TTS | On-Device |
|---------|---------|------------|------------|-----------|
| **Latency** | 190ms ⚡ | 300-500ms | 200-400ms | 100-200ms |
| **Emotion** | ✅ Full range | ✅ Good | ❌ Limited | ❌ None |
| **Languages** | 42 | 29 | 120+ | 50+ |
| **Cost/1M chars** | ~$50 | $60-300 | $4-16 | $0 |
| **Voice cloning** | ✅ Instant | ✅ High quality | ❌ No | ❌ No |
| **Real-time** | ✅ Optimized | ⚠️ Moderate | ⚠️ Moderate | ✅ Yes |
| **Offline** | ❌ No | ❌ No | ❌ No | ✅ Yes |
| **Privacy** | ⚠️ Cloud | ⚠️ Cloud | ⚠️ Cloud | ✅ Local |

## Conclusion

### Recommendation: Adopt Hybrid TTS Strategy

**Use Sonic 3 for**:
- Emotion-aware conversational responses
- Complex, engaging content
- When user explicitly requests high-quality voice
- Premium features (if offering paid tier)

**Use On-Device TTS for**:
- Simple confirmations and commands
- Offline mode
- Privacy-sensitive responses
- Battery-saving mode

### Expected Benefits
1. **Quality**: Best-in-class emotional TTS when needed
2. **Cost**: ~70% cheaper than Sonic 3-only approach
3. **Privacy**: Most responses stay on-device
4. **Performance**: Average latency <300ms (well within 800ms target)
5. **User experience**: Adaptive quality based on context

### Next Steps
1. Sign up for Cartesia Sonic 3 (start with free tier)
2. Implement TTS router and repository (see code above)
3. Integrate with existing architecture
4. Test with emotion-aware responses
5. Monitor usage and costs

---

**Document Version**: 1.0
**Last Updated**: 2025-10-30
**Status**: Ready for Implementation 🚀

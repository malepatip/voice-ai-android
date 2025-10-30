package com.voiceai.domain.router

import com.voiceai.domain.entity.Emotion
import com.voiceai.domain.entity.EmotionType
import com.voiceai.domain.entity.AIResponse

/**
 * Routes TTS requests between on-device and Sonic 3 API.
 * Optimizes for quality, cost, and privacy based on context.
 */
interface TTSRouter {
    /**
     * Determine the best TTS engine for the given response
     */
    fun route(
        response: AIResponse,
        emotion: Emotion?,
        networkAvailable: Boolean,
        batteryLevel: Float,
        userPreference: TTSPreference = TTSPreference.BALANCED
    ): TTSDecision

    /**
     * Evaluate a past TTS routing decision
     */
    suspend fun evaluateDecision(
        decision: TTSDecision,
        metrics: TTSMetrics
    ): TTSEvaluation
}

/**
 * TTS routing decision
 */
sealed class TTSDecision {
    /**
     * Use on-device Android TTS
     */
    object OnDevice : TTSDecision() {
        override fun toString() = "OnDevice(Android TTS)"
    }

    /**
     * Use Cartesia Sonic 3 API
     */
    data class Sonic3(
        val emotion: String? = null,
        val speed: Float = 1.0f,
        val voiceId: String = "default"
    ) : TTSDecision() {
        override fun toString() = "Sonic3(emotion=$emotion, speed=$speed)"
    }
}

/**
 * User preference for TTS quality vs privacy
 */
enum class TTSPreference {
    PRIVACY_FIRST,  // Always use on-device
    BALANCED,       // Smart routing based on context
    QUALITY_FIRST   // Prefer Sonic 3 when available
}

/**
 * Default implementation of TTS router
 */
class DefaultTTSRouter : TTSRouter {

    override fun route(
        response: AIResponse,
        emotion: Emotion?,
        networkAvailable: Boolean,
        batteryLevel: Float,
        userPreference: TTSPreference
    ): TTSDecision {
        // Priority 1: Offline mode - must use on-device
        if (!networkAvailable) {
            return TTSDecision.OnDevice
        }

        // Priority 2: Privacy-first preference
        if (userPreference == TTSPreference.PRIVACY_FIRST) {
            return TTSDecision.OnDevice
        }

        // Priority 3: Low battery - preserve power
        if (batteryLevel < 0.15f) {
            return TTSDecision.OnDevice
        }

        // Decision logic for Balanced and Quality-First modes
        return when {
            // Use Sonic 3 for emotion-aware responses
            shouldUseSonic3ForEmotion(emotion, userPreference) -> {
                TTSDecision.Sonic3(
                    emotion = mapEmotionToSonic3(emotion!!),
                    speed = calculateSpeed(emotion)
                )
            }

            // Use Sonic 3 for long, complex responses
            shouldUseSonic3ForLength(response, userPreference) -> {
                TTSDecision.Sonic3()
            }

            // Quality-first mode: prefer Sonic 3
            userPreference == TTSPreference.QUALITY_FIRST -> {
                TTSDecision.Sonic3()
            }

            // Default: on-device for simple responses
            else -> TTSDecision.OnDevice
        }
    }

    override suspend fun evaluateDecision(
        decision: TTSDecision,
        metrics: TTSMetrics
    ): TTSEvaluation {
        val optimalDecision = determineOptimalDecision(metrics)
        val wasOptimal = decision.toString() == optimalDecision.toString()

        return TTSEvaluation(
            decision = decision,
            wasOptimal = wasOptimal,
            optimalDecision = optimalDecision,
            actualLatency = metrics.latencyMs,
            estimatedLatency = estimateLatency(decision),
            quality = metrics.userPerceivedQuality,
            cost = estimateCost(decision, metrics.charactersGenerated)
        )
    }

    /**
     * Should use Sonic 3 based on emotion significance
     */
    private fun shouldUseSonic3ForEmotion(
        emotion: Emotion?,
        preference: TTSPreference
    ): Boolean {
        if (emotion == null) return false

        return when (preference) {
            TTSPreference.BALANCED -> {
                // Use Sonic 3 for significant emotions
                emotion.isSignificant() && emotion.primary.requiresEmpathy()
            }
            TTSPreference.QUALITY_FIRST -> {
                // Use Sonic 3 for any detectable emotion
                emotion.confidence > 0.5f
            }
            TTSPreference.PRIVACY_FIRST -> false
        }
    }

    /**
     * Should use Sonic 3 based on response length/complexity
     */
    private fun shouldUseSonic3ForLength(
        response: AIResponse,
        preference: TTSPreference
    ): Boolean {
        val length = response.content.length

        return when (preference) {
            TTSPreference.BALANCED -> length > 200
            TTSPreference.QUALITY_FIRST -> length > 100
            TTSPreference.PRIVACY_FIRST -> false
        }
    }

    /**
     * Map emotion to Sonic 3 emotion parameter
     */
    private fun mapEmotionToSonic3(emotion: Emotion): String {
        return when (emotion.primary) {
            EmotionType.HAPPY -> "cheerful"
            EmotionType.SAD -> "empathetic"
            EmotionType.ANGRY -> "calm" // Counter-emotion for de-escalation
            EmotionType.FEARFUL -> "reassuring"
            EmotionType.SURPRISED -> "excited"
            EmotionType.FRUSTRATED -> "patient"
            EmotionType.CONFUSED -> "clear"
            else -> "neutral"
        }
    }

    /**
     * Calculate speech speed based on emotion
     */
    private fun calculateSpeed(emotion: Emotion): Float {
        return when {
            emotion.primary == EmotionType.ANGRY -> 0.9f // Slower for angry users
            emotion.primary == EmotionType.CONFUSED -> 0.85f // Slower for clarity
            emotion.arousal > 0.7f -> 1.1f // Faster for high arousal
            else -> 1.0f
        }
    }

    /**
     * Determine optimal decision in hindsight
     */
    private fun determineOptimalDecision(metrics: TTSMetrics): TTSDecision {
        // If latency was too high, should have used on-device
        if (metrics.latencyMs > 500) {
            return TTSDecision.OnDevice
        }

        // If quality was poor, should have used Sonic 3
        if (metrics.userPerceivedQuality < 3.0f) {
            return TTSDecision.Sonic3()
        }

        // Otherwise, current decision was fine
        return when {
            metrics.latencyMs < 200 -> TTSDecision.OnDevice
            else -> TTSDecision.Sonic3()
        }
    }

    /**
     * Estimate latency for a given decision
     */
    private fun estimateLatency(decision: TTSDecision): Long {
        return when (decision) {
            is TTSDecision.OnDevice -> 150L // ~150ms for on-device
            is TTSDecision.Sonic3 -> 190L + 30L // 190ms Sonic 3 + 30ms network
        }
    }

    /**
     * Estimate cost for TTS generation
     */
    private fun estimateCost(decision: TTSDecision, characters: Int): Float {
        return when (decision) {
            is TTSDecision.OnDevice -> 0.0f // Free
            is TTSDecision.Sonic3 -> {
                // 1 credit per character, $5 per 100,000 credits
                characters * 0.00005f
            }
        }
    }
}

/**
 * Metrics for TTS generation
 */
data class TTSMetrics(
    val latencyMs: Long,
    val charactersGenerated: Int,
    val audioLengthMs: Long,
    val userPerceivedQuality: Float = 0.0f, // 0-5 scale
    val bytesGenerated: Int = 0
)

/**
 * Evaluation of a TTS routing decision
 */
data class TTSEvaluation(
    val decision: TTSDecision,
    val wasOptimal: Boolean,
    val optimalDecision: TTSDecision,
    val actualLatency: Long,
    val estimatedLatency: Long,
    val quality: Float,
    val cost: Float
) {
    fun toReport(): String {
        return """
            TTS Router Evaluation:
            - Decision: $decision
            - Optimal: $wasOptimal
            - Latency: ${actualLatency}ms (estimated: ${estimatedLatency}ms)
            - Quality: ${"%.1f".format(quality)}/5.0
            - Cost: $${"%.4f".format(cost)}
            ${if (!wasOptimal) "\nRecommendation: Use $optimalDecision instead" else ""}
        """.trimIndent()
    }
}

package com.voiceai.domain.entity

/**
 * Represents emotional state detected from voice input.
 * Replaces Hume AI with local emotion detection.
 */
data class Emotion(
    val primary: EmotionType,
    val confidence: Float,
    val all: Map<EmotionType, Float> = emptyMap(),
    val intensity: Float = 0.5f, // 0.0 to 1.0
    val valence: Float = 0.0f, // -1.0 (negative) to 1.0 (positive)
    val arousal: Float = 0.0f // -1.0 (calm) to 1.0 (excited)
) {
    /**
     * Determines if the emotion is strong enough to affect response generation
     */
    fun isSignificant(): Boolean = confidence > 0.6f && intensity > 0.4f

    /**
     * Returns a human-readable description
     */
    fun describe(): String = "${primary.displayName} (${(confidence * 100).toInt()}%)"
}

/**
 * Basic emotion types (Ekman's 6 basic emotions + neutral)
 */
enum class EmotionType(val displayName: String) {
    HAPPY("Happy"),
    SAD("Sad"),
    ANGRY("Angry"),
    FEARFUL("Fearful"),
    SURPRISED("Surprised"),
    DISGUSTED("Disgusted"),
    NEUTRAL("Neutral"),
    CONFUSED("Confused"), // Additional for voice AI
    FRUSTRATED("Frustrated"); // Additional for voice AI

    fun requiresEmpathy(): Boolean = when (this) {
        SAD, ANGRY, FEARFUL, FRUSTRATED -> true
        else -> false
    }

    fun suggestsPositiveResponse(): Boolean = when (this) {
        HAPPY, SURPRISED -> true
        else -> false
    }
}

/**
 * Result of emotion detection process
 */
sealed class EmotionDetectionResult {
    data class Success(
        val emotion: Emotion,
        val processingTimeMs: Long,
        val audioLengthMs: Long,
        val model: String // "emotion2vec" or "wav2vec2-emotion"
    ) : EmotionDetectionResult()

    data class Failure(
        val error: EmotionDetectionError,
        val message: String
    ) : EmotionDetectionResult()

    /**
     * Returns neutral emotion as fallback
     */
    fun getOrDefault(): Emotion = when (this) {
        is Success -> emotion
        is Failure -> Emotion(
            primary = EmotionType.NEUTRAL,
            confidence = 1.0f,
            intensity = 0.5f
        )
    }
}

enum class EmotionDetectionError {
    AUDIO_TOO_SHORT,
    AUDIO_TOO_NOISY,
    MODEL_UNAVAILABLE,
    TIMEOUT,
    INVALID_INPUT,
    UNKNOWN_ERROR
}

/**
 * Emotion-aware response configuration
 */
data class EmotionResponseConfig(
    val emotion: Emotion,
    val adjustTone: Boolean = true,
    val provideSupportiveResponse: Boolean = false,
    val avoidHumor: Boolean = false,
    val increaseWarmth: Boolean = false
) {
    companion object {
        fun fromEmotion(emotion: Emotion): EmotionResponseConfig {
            return when {
                emotion.primary.requiresEmpathy() -> EmotionResponseConfig(
                    emotion = emotion,
                    adjustTone = true,
                    provideSupportiveResponse = true,
                    avoidHumor = true,
                    increaseWarmth = true
                )
                emotion.primary.suggestsPositiveResponse() -> EmotionResponseConfig(
                    emotion = emotion,
                    adjustTone = true,
                    provideSupportiveResponse = false,
                    avoidHumor = false,
                    increaseWarmth = true
                )
                else -> EmotionResponseConfig(
                    emotion = emotion,
                    adjustTone = false
                )
            }
        }
    }
}

package com.voiceai.domain.entity

/**
 * Represents a classified user intent from voice input.
 * Used by the router to determine processing strategy.
 */
data class Intent(
    val type: IntentType,
    val confidence: Float,
    val parameters: Map<String, Any> = emptyMap(),
    val requiresToolUse: Boolean = false,
    val isConversational: Boolean = false,
    val complexityScore: Float = 0.0f
) {
    /**
     * Determines if this intent can be handled on-device
     */
    fun isSimple(): Boolean {
        return type.isSimple() &&
               confidence > 0.85f &&
               !requiresToolUse &&
               complexityScore < 0.3f
    }

    /**
     * Determines if this intent requires backend processing
     */
    fun requiresBackend(): Boolean {
        return requiresToolUse ||
               isConversational ||
               complexityScore > 0.6f ||
               type.requiresBackend()
    }
}

/**
 * Categories of user intents
 */
enum class IntentType {
    // Simple commands (on-device capable)
    SET_TIMER,
    SET_ALARM,
    CANCEL_TIMER,
    SIMPLE_QUERY,
    WEATHER_CHECK,
    TIME_QUERY,

    // Complex tasks (backend preferred)
    COMPLEX_QUERY,
    MULTI_STEP_TASK,
    CONVERSATION,
    REASONING_TASK,

    // Skill-based (requires tool use)
    WEB_SEARCH,
    CALENDAR_OPERATION,
    CALCULATION,
    TRANSLATION,

    // System commands
    SETTINGS_CHANGE,
    NAVIGATION,

    // Fallback
    UNKNOWN;

    fun isSimple(): Boolean = when (this) {
        SET_TIMER, SET_ALARM, CANCEL_TIMER,
        SIMPLE_QUERY, TIME_QUERY, SETTINGS_CHANGE -> true
        else -> false
    }

    fun requiresBackend(): Boolean = when (this) {
        COMPLEX_QUERY, MULTI_STEP_TASK, CONVERSATION,
        REASONING_TASK, WEB_SEARCH, TRANSLATION -> true
        else -> false
    }
}

/**
 * Result of intent classification process
 */
sealed class IntentClassificationResult {
    data class Success(
        val intent: Intent,
        val processingTimeMs: Long,
        val model: String // "gemini_nano" or "llama_3.2"
    ) : IntentClassificationResult()

    data class Failure(
        val error: IntentClassificationError,
        val message: String
    ) : IntentClassificationResult()
}

enum class IntentClassificationError {
    LOW_CONFIDENCE,
    MODEL_UNAVAILABLE,
    TIMEOUT,
    INVALID_INPUT,
    UNKNOWN_ERROR
}

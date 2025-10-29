package com.voiceai.domain.entity

import java.util.UUID

/**
 * Represents a conversation session between user and voice AI
 */
data class ConversationSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val turns: List<ConversationTurn> = emptyList(),
    val startedAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis(),
    val metadata: ConversationMetadata = ConversationMetadata()
) {
    fun addTurn(turn: ConversationTurn): ConversationSession {
        return copy(
            turns = turns + turn,
            lastActivityAt = System.currentTimeMillis()
        )
    }

    fun isActive(): Boolean {
        val inactivityMs = System.currentTimeMillis() - lastActivityAt
        return inactivityMs < 30 * 60 * 1000 // 30 minutes
    }

    fun getTurnCount(): Int = turns.size

    fun getRecentTurns(count: Int = 10): List<ConversationTurn> {
        return turns.takeLast(count)
    }
}

/**
 * Single turn in a conversation
 */
data class ConversationTurn(
    val turnId: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val emotion: Emotion? = null,
    val intent: Intent? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val processingMetrics: ProcessingMetrics? = null
) {
    enum class Role {
        USER,
        ASSISTANT,
        SYSTEM
    }
}

/**
 * Metadata about the conversation
 */
data class ConversationMetadata(
    val totalTurns: Int = 0,
    val userPreferences: Map<String, Any> = emptyMap(),
    val topics: List<String> = emptyList(),
    val dominantEmotion: EmotionType? = null,
    val averageLatencyMs: Long = 0
)

/**
 * Metrics for a single processing cycle
 */
data class ProcessingMetrics(
    val sttLatencyMs: Long = 0,
    val intentClassificationMs: Long = 0,
    val emotionDetectionMs: Long = 0,
    val routingDecisionMs: Long = 0,
    val inferenceLatencyMs: Long = 0,
    val ttsLatencyMs: Long = 0,
    val totalLatencyMs: Long = 0,
    val route: RouteDecision? = null,
    val model: String = ""
) {
    fun toSummary(): String {
        return """
            Total: ${totalLatencyMs}ms
            - STT: ${sttLatencyMs}ms
            - Intent: ${intentClassificationMs}ms
            - Emotion: ${emotionDetectionMs}ms
            - Route: ${routingDecisionMs}ms
            - Inference: ${inferenceLatencyMs}ms
            - TTS: ${ttsLatencyMs}ms
            Model: $model via $route
        """.trimIndent()
    }

    fun meetsLatencyTarget(target: LatencyRequirement): Boolean {
        return when (target) {
            LatencyRequirement.LOW -> totalLatencyMs < 200
            LatencyRequirement.NORMAL -> totalLatencyMs < 600
            LatencyRequirement.RELAXED -> totalLatencyMs < 1000
        }
    }
}

/**
 * Voice input with audio data
 */
data class VoiceInput(
    val audioData: ByteArray,
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val encoding: AudioEncoding = AudioEncoding.PCM_16BIT,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VoiceInput

        if (!audioData.contentEquals(other.audioData)) return false
        if (sampleRate != other.sampleRate) return false
        if (channels != other.channels) return false
        if (encoding != other.encoding) return false
        if (durationMs != other.durationMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = audioData.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + encoding.hashCode()
        result = 31 * result + durationMs.hashCode()
        return result
    }
}

enum class AudioEncoding {
    PCM_16BIT,
    PCM_8BIT,
    PCM_FLOAT,
    OPUS,
    AAC
}

/**
 * AI response with metadata
 */
data class AIResponse(
    val content: String,
    val emotion: Emotion? = null,
    val confidence: Float = 1.0f,
    val toolCalls: List<ToolCall> = emptyList(),
    val metadata: ResponseMetadata = ResponseMetadata()
)

data class ResponseMetadata(
    val model: String = "",
    val tokensGenerated: Int = 0,
    val finishReason: FinishReason = FinishReason.COMPLETE,
    val safetyFiltered: Boolean = false
)

enum class FinishReason {
    COMPLETE,
    LENGTH_LIMIT,
    SAFETY_FILTER,
    ERROR,
    TIMEOUT
}

/**
 * Tool/skill call from AI
 */
data class ToolCall(
    val toolName: String,
    val parameters: Map<String, Any>,
    val callId: String = UUID.randomUUID().toString()
)

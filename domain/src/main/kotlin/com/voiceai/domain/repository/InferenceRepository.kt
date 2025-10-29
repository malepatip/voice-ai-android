package com.voiceai.domain.repository

import com.voiceai.domain.entity.*

/**
 * Repository for AI inference operations.
 * Abstracts the underlying inference engines (Gemini Nano, Llama 3.2).
 */
interface InferenceRepository {
    /**
     * Classify intent from text input
     */
    suspend fun classifyIntent(
        text: String,
        useOnDevice: Boolean = true
    ): IntentClassificationResult

    /**
     * Generate response for given input
     */
    suspend fun generateResponse(
        input: String,
        context: ConversationSession,
        emotion: Emotion? = null,
        useOnDevice: Boolean = false
    ): Result<AIResponse>

    /**
     * Detect emotion from audio
     */
    suspend fun detectEmotion(
        audioData: ByteArray,
        sampleRate: Int = 16000
    ): EmotionDetectionResult

    /**
     * Check if on-device model is available
     */
    suspend fun isOnDeviceModelAvailable(): Boolean

    /**
     * Check if backend is reachable
     */
    suspend fun isBackendAvailable(): Boolean
}

/**
 * Repository for conversation management
 */
interface ConversationRepository {
    /**
     * Create new conversation session
     */
    suspend fun createSession(userId: String): ConversationSession

    /**
     * Get conversation session by ID
     */
    suspend fun getSession(sessionId: String): ConversationSession?

    /**
     * Update conversation session
     */
    suspend fun updateSession(session: ConversationSession): Result<Unit>

    /**
     * Add turn to conversation
     */
    suspend fun addTurn(sessionId: String, turn: ConversationTurn): Result<Unit>

    /**
     * Get active sessions for user
     */
    suspend fun getActiveSessions(userId: String): List<ConversationSession>

    /**
     * Delete session
     */
    suspend fun deleteSession(sessionId: String): Result<Unit>

    /**
     * Delete old sessions (privacy compliance)
     */
    suspend fun deleteOldSessions(olderThanDays: Int = 30): Result<Int>
}

/**
 * Repository for skills/tools
 */
interface SkillRepository {
    /**
     * Get all available skills
     */
    suspend fun getAvailableSkills(): List<SkillInfo>

    /**
     * Execute skill by name
     */
    suspend fun executeSkill(
        skillName: String,
        parameters: Map<String, Any>,
        context: com.voiceai.domain.skills.SkillExecutionContext
    ): com.voiceai.domain.skills.SkillResult
}

/**
 * Metadata about a skill
 */
data class SkillInfo(
    val name: String,
    val description: String,
    val parameters: List<ParameterInfo>,
    val estimatedLatencyMs: Long
)

data class ParameterInfo(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean
)

/**
 * Repository for metrics and evaluations
 */
interface MetricsRepository {
    /**
     * Log routing decision and metrics
     */
    suspend fun logRoutingMetrics(metrics: RouteMetrics): Result<Unit>

    /**
     * Log processing metrics
     */
    suspend fun logProcessingMetrics(metrics: ProcessingMetrics): Result<Unit>

    /**
     * Get routing accuracy over time
     */
    suspend fun getRoutingAccuracy(
        startTime: Long,
        endTime: Long
    ): Float

    /**
     * Get average latency by route type
     */
    suspend fun getAverageLatency(
        routeType: String,
        startTime: Long,
        endTime: Long
    ): Long

    /**
     * Get user satisfaction scores
     */
    suspend fun getUserSatisfactionScores(
        startTime: Long,
        endTime: Long
    ): List<Float>
}

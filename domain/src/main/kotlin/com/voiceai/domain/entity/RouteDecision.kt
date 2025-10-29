package com.voiceai.domain.entity

/**
 * Represents routing decision for inference processing.
 * Determines whether to use on-device (Gemini Nano) or backend (Llama 3.2).
 */
sealed class RouteDecision {
    /**
     * Process entirely on-device with Gemini Nano
     */
    object OnDevice : RouteDecision() {
        override fun toString() = "OnDevice(Gemini Nano)"
    }

    /**
     * Process on backend with specified model
     */
    data class Backend(
        val model: BackendModel,
        val reason: String = ""
    ) : RouteDecision() {
        override fun toString() = "Backend($model, reason='$reason')"
    }

    /**
     * Hybrid approach - multiple stages
     */
    data class Hybrid(
        val stages: List<ProcessingStage>,
        val reason: String = ""
    ) : RouteDecision() {
        override fun toString() = "Hybrid(${stages.size} stages, reason='$reason')"
    }

    /**
     * Fallback when decision cannot be made
     */
    data class Fallback(
        val defaultRoute: RouteDecision,
        val reason: String
    ) : RouteDecision()
}

/**
 * Backend model options
 */
enum class BackendModel(
    val displayName: String,
    val contextWindow: Int,
    val estimatedLatencyMs: Long
) {
    LLAMA_3_2_1B(
        displayName = "Llama 3.2 1B",
        contextWindow = 2048,
        estimatedLatencyMs = 200
    ),
    LLAMA_3_2_3B(
        displayName = "Llama 3.2 3B",
        contextWindow = 4096,
        estimatedLatencyMs = 350
    );

    fun isFast(): Boolean = estimatedLatencyMs < 250
}

/**
 * Processing stage in hybrid approach
 */
data class ProcessingStage(
    val stage: Stage,
    val model: Model,
    val purpose: String
)

enum class Stage {
    INTENT_CLASSIFICATION,
    TOOL_USE,
    RESPONSE_GENERATION,
    REFINEMENT
}

sealed class Model {
    object GeminiNano : Model() {
        override fun toString() = "Gemini Nano"
    }
    data class LlamaBackend(val variant: BackendModel) : Model() {
        override fun toString() = variant.displayName
    }
}

/**
 * Context used for routing decisions
 */
data class RoutingContext(
    val intent: Intent,
    val emotion: Emotion? = null,
    val conversationHistory: List<ConversationTurn> = emptyList(),
    val networkAvailable: Boolean = true,
    val batteryLevel: Float = 1.0f, // 0.0 to 1.0
    val latencyRequirement: LatencyRequirement = LatencyRequirement.NORMAL,
    val userPreference: UserPreference = UserPreference.BALANCED
) {
    fun requiresQuickResponse(): Boolean =
        latencyRequirement == LatencyRequirement.LOW ||
        batteryLevel < 0.2f

    fun canUseBackend(): Boolean =
        networkAvailable &&
        batteryLevel > 0.1f &&
        userPreference != UserPreference.PRIVACY_FIRST
}

enum class LatencyRequirement {
    LOW,      // < 200ms
    NORMAL,   // < 600ms
    RELAXED   // < 1000ms
}

enum class UserPreference {
    PRIVACY_FIRST,  // Prefer on-device
    BALANCED,       // Smart routing
    QUALITY_FIRST   // Prefer backend for quality
}

/**
 * Metrics for evaluating routing decisions
 */
data class RouteMetrics(
    val decision: RouteDecision,
    val actualLatencyMs: Long,
    val wasOptimal: Boolean,
    val userSatisfaction: Float? = null,
    val cost: Float = 0.0f // Compute cost estimate
) {
    fun toEvalEntry(): String {
        return "Decision: $decision, Latency: ${actualLatencyMs}ms, " +
               "Optimal: $wasOptimal, Satisfaction: ${userSatisfaction ?: "N/A"}"
    }
}

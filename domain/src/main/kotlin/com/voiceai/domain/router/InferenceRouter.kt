package com.voiceai.domain.router

import com.voiceai.domain.entity.*

/**
 * Routes inference requests between on-device (Gemini Nano) and backend (Llama 3.2).
 * This is a critical component for optimizing latency, cost, and privacy.
 */
interface InferenceRouter {
    /**
     * Determine the best route for processing the given input
     */
    suspend fun route(context: RoutingContext): RouteDecision

    /**
     * Evaluate a past routing decision (for learning and optimization)
     */
    suspend fun evaluateDecision(
        decision: RouteDecision,
        metrics: RouteMetrics
    ): RouterEvaluation
}

/**
 * Default implementation of the inference router
 */
class DefaultInferenceRouter : InferenceRouter {

    override suspend fun route(context: RoutingContext): RouteDecision {
        // Priority 1: Offline mode - must use on-device
        if (!context.networkAvailable) {
            return RouteDecision.OnDevice
        }

        // Priority 2: Privacy-first preference
        if (context.userPreference == UserPreference.PRIVACY_FIRST) {
            return if (context.intent.requiresBackend()) {
                RouteDecision.Fallback(
                    defaultRoute = RouteDecision.OnDevice,
                    reason = "User prefers privacy but intent requires backend"
                )
            } else {
                RouteDecision.OnDevice
            }
        }

        // Priority 3: Low battery - preserve power
        if (context.batteryLevel < 0.2f) {
            return RouteDecision.OnDevice
        }

        val intent = context.intent

        // Decision tree based on intent characteristics
        return when {
            // Fast path: High confidence simple intent
            intent.isSimple() && intent.confidence > 0.9f -> {
                RouteDecision.OnDevice
            }

            // Complex reasoning required
            intent.type == IntentType.REASONING_TASK ||
            intent.complexityScore > 0.7f -> {
                RouteDecision.Backend(
                    model = BackendModel.LLAMA_3_2_3B,
                    reason = "Complex reasoning required"
                )
            }

            // Tool use required
            intent.requiresToolUse -> {
                RouteDecision.Backend(
                    model = BackendModel.LLAMA_3_2_1B,
                    reason = "Skill execution required"
                )
            }

            // Conversational with emotion awareness
            intent.isConversational && context.emotion?.isSignificant() == true -> {
                RouteDecision.Backend(
                    model = BackendModel.LLAMA_3_2_3B,
                    reason = "Emotion-aware conversation"
                )
            }

            // Latency critical
            context.requiresQuickResponse() -> {
                RouteDecision.OnDevice
            }

            // Multi-turn conversation
            context.conversationHistory.size > 3 -> {
                RouteDecision.Backend(
                    model = BackendModel.LLAMA_3_2_3B,
                    reason = "Long conversation context"
                )
            }

            // Quality-first preference
            context.userPreference == UserPreference.QUALITY_FIRST -> {
                RouteDecision.Backend(
                    model = BackendModel.LLAMA_3_2_3B,
                    reason = "User prefers quality"
                )
            }

            // Hybrid approach for medium complexity
            intent.complexityScore in 0.3f..0.6f -> {
                RouteDecision.Hybrid(
                    stages = listOf(
                        ProcessingStage(
                            stage = Stage.INTENT_CLASSIFICATION,
                            model = Model.GeminiNano,
                            purpose = "Initial classification"
                        ),
                        ProcessingStage(
                            stage = Stage.RESPONSE_GENERATION,
                            model = Model.LlamaBackend(BackendModel.LLAMA_3_2_1B),
                            purpose = "Response generation"
                        )
                    ),
                    reason = "Medium complexity - hybrid approach"
                )
            }

            // Default: on-device for balanced approach
            else -> RouteDecision.OnDevice
        }
    }

    override suspend fun evaluateDecision(
        decision: RouteDecision,
        metrics: RouteMetrics
    ): RouterEvaluation {
        val optimalRoute = determineOptimalRoute(metrics)
        val wasOptimal = decision.toString() == optimalRoute.toString()

        val efficiency = calculateEfficiency(decision, metrics)
        val cost = estimateCost(decision)

        return RouterEvaluation(
            decision = decision,
            wasOptimal = wasOptimal,
            optimalDecision = optimalRoute,
            efficiency = efficiency,
            actualLatency = metrics.actualLatencyMs,
            estimatedLatency = estimateLatency(decision),
            cost = cost,
            userSatisfaction = metrics.userSatisfaction,
            recommendations = generateRecommendations(decision, metrics)
        )
    }

    /**
     * Determine what the optimal route would have been in hindsight
     */
    private fun determineOptimalRoute(metrics: RouteMetrics): RouteDecision {
        // If latency was good and user was satisfied, the decision was optimal
        if (metrics.actualLatencyMs < 300 && (metrics.userSatisfaction ?: 0.8f) > 0.7f) {
            return metrics.decision
        }

        // If latency was too high, should have used on-device
        if (metrics.actualLatencyMs > 800) {
            return RouteDecision.OnDevice
        }

        // If user satisfaction was low, try different model
        if ((metrics.userSatisfaction ?: 1.0f) < 0.5f) {
            return when (metrics.decision) {
                is RouteDecision.OnDevice -> RouteDecision.Backend(
                    BackendModel.LLAMA_3_2_3B,
                    "Quality improvement needed"
                )
                is RouteDecision.Backend -> RouteDecision.OnDevice
                else -> metrics.decision
            }
        }

        return metrics.decision
    }

    /**
     * Calculate routing efficiency (0.0 to 1.0)
     */
    private fun calculateEfficiency(
        decision: RouteDecision,
        metrics: RouteMetrics
    ): Float {
        val latencyScore = when {
            metrics.actualLatencyMs < 200 -> 1.0f
            metrics.actualLatencyMs < 400 -> 0.8f
            metrics.actualLatencyMs < 600 -> 0.6f
            metrics.actualLatencyMs < 800 -> 0.4f
            else -> 0.2f
        }

        val satisfactionScore = metrics.userSatisfaction ?: 0.7f
        val costScore = 1.0f - (metrics.cost / 10.0f).coerceIn(0.0f, 1.0f)

        return (latencyScore * 0.4f + satisfactionScore * 0.4f + costScore * 0.2f)
    }

    /**
     * Estimate latency for a given route
     */
    private fun estimateLatency(decision: RouteDecision): Long {
        return when (decision) {
            is RouteDecision.OnDevice -> 150L
            is RouteDecision.Backend -> decision.model.estimatedLatencyMs + 60L // network overhead
            is RouteDecision.Hybrid -> {
                decision.stages.sumOf { stage ->
                    when (stage.model) {
                        is Model.GeminiNano -> 50L
                        is Model.LlamaBackend -> stage.model.variant.estimatedLatencyMs
                    }
                } + 60L // network overhead
            }
            is RouteDecision.Fallback -> estimateLatency(decision.defaultRoute)
        }
    }

    /**
     * Estimate compute cost for a route
     */
    private fun estimateCost(decision: RouteDecision): Float {
        return when (decision) {
            is RouteDecision.OnDevice -> 0.0f // Free on-device
            is RouteDecision.Backend -> when (decision.model) {
                BackendModel.LLAMA_3_2_1B -> 0.5f
                BackendModel.LLAMA_3_2_3B -> 1.0f
            }
            is RouteDecision.Hybrid -> {
                decision.stages.sumOf { stage ->
                    when (stage.model) {
                        is Model.GeminiNano -> 0.0
                        is Model.LlamaBackend -> when (stage.model.variant) {
                            BackendModel.LLAMA_3_2_1B -> 0.5
                            BackendModel.LLAMA_3_2_3B -> 1.0
                        }
                    }
                }.toFloat()
            }
            is RouteDecision.Fallback -> estimateCost(decision.defaultRoute)
        }
    }

    /**
     * Generate recommendations for future routing
     */
    private fun generateRecommendations(
        decision: RouteDecision,
        metrics: RouteMetrics
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (metrics.actualLatencyMs > 800) {
            recommendations.add("Consider using on-device for faster response")
        }

        if (metrics.userSatisfaction != null && metrics.userSatisfaction < 0.6f) {
            recommendations.add("Consider using more capable model for better quality")
        }

        if (metrics.cost > 5.0f) {
            recommendations.add("High cost - consider optimizing or caching")
        }

        return recommendations
    }
}

/**
 * Evaluation result for a routing decision
 */
data class RouterEvaluation(
    val decision: RouteDecision,
    val wasOptimal: Boolean,
    val optimalDecision: RouteDecision,
    val efficiency: Float,
    val actualLatency: Long,
    val estimatedLatency: Long,
    val cost: Float,
    val userSatisfaction: Float?,
    val recommendations: List<String>
) {
    fun toReport(): String {
        return """
            Router Evaluation:
            - Decision: $decision
            - Optimal: $wasOptimal
            - Efficiency: ${(efficiency * 100).toInt()}%
            - Latency: ${actualLatency}ms (estimated: ${estimatedLatency}ms)
            - Cost: $cost
            - Satisfaction: ${userSatisfaction?.let { "${(it * 100).toInt()}%" } ?: "N/A"}
            ${if (recommendations.isNotEmpty()) "\nRecommendations:\n${recommendations.joinToString("\n") { "  - $it" }}" else ""}
        """.trimIndent()
    }
}

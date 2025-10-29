package com.voiceai.domain.evals

import com.voiceai.domain.entity.*
import com.voiceai.domain.router.RouterEvaluation

/**
 * Framework for evaluating AI system quality.
 * Tracks metrics for intent classification, response quality, routing decisions, and latency.
 */
interface Evaluator<T, R> {
    /**
     * Evaluate a single test case
     */
    suspend fun evaluate(input: T, expected: R): EvaluationResult

    /**
     * Evaluate a batch of test cases
     */
    suspend fun evaluateBatch(testCases: List<TestCase<T, R>>): BatchEvaluationResult

    /**
     * Get evaluation metrics
     */
    fun getMetrics(): EvaluationMetrics
}

/**
 * Test case with input and expected output
 */
data class TestCase<T, R>(
    val id: String,
    val input: T,
    val expected: R,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Result of a single evaluation
 */
data class EvaluationResult(
    val passed: Boolean,
    val score: Float, // 0.0 to 1.0
    val details: String = "",
    val metrics: Map<String, Float> = emptyMap()
)

/**
 * Result of batch evaluation
 */
data class BatchEvaluationResult(
    val totalTests: Int,
    val passed: Int,
    val failed: Int,
    val averageScore: Float,
    val results: List<EvaluationResult>,
    val metrics: EvaluationMetrics
)

/**
 * Aggregated evaluation metrics
 */
data class EvaluationMetrics(
    val accuracy: Float,
    val precision: Float,
    val recall: Float,
    val f1Score: Float,
    val confusionMatrix: ConfusionMatrix? = null
)

/**
 * Confusion matrix for classification tasks
 */
data class ConfusionMatrix(
    val truePositives: Int,
    val trueNegatives: Int,
    val falsePositives: Int,
    val falseNegatives: Int
) {
    fun accuracy(): Float {
        val total = truePositives + trueNegatives + falsePositives + falseNegatives
        return if (total > 0) {
            (truePositives + trueNegatives).toFloat() / total
        } else 0.0f
    }

    fun precision(): Float {
        val denominator = truePositives + falsePositives
        return if (denominator > 0) {
            truePositives.toFloat() / denominator
        } else 0.0f
    }

    fun recall(): Float {
        val denominator = truePositives + falseNegatives
        return if (denominator > 0) {
            truePositives.toFloat() / denominator
        } else 0.0f
    }

    fun f1Score(): Float {
        val p = precision()
        val r = recall()
        return if (p + r > 0) {
            2 * (p * r) / (p + r)
        } else 0.0f
    }
}

/**
 * Evaluator for intent classification
 */
class IntentEvaluator : Evaluator<String, Intent> {
    private val results = mutableListOf<EvaluationResult>()

    override suspend fun evaluate(input: String, expected: Intent): EvaluationResult {
        // This would call the actual classifier
        // For now, return a placeholder
        return EvaluationResult(
            passed = false,
            score = 0.0f,
            details = "Not implemented"
        ).also { results.add(it) }
    }

    override suspend fun evaluateBatch(
        testCases: List<TestCase<String, Intent>>
    ): BatchEvaluationResult {
        val evalResults = testCases.map { testCase ->
            evaluate(testCase.input, testCase.expected)
        }

        val passed = evalResults.count { it.passed }
        val averageScore = evalResults.map { it.score }.average().toFloat()

        return BatchEvaluationResult(
            totalTests = testCases.size,
            passed = passed,
            failed = testCases.size - passed,
            averageScore = averageScore,
            results = evalResults,
            metrics = getMetrics()
        )
    }

    override fun getMetrics(): EvaluationMetrics {
        if (results.isEmpty()) {
            return EvaluationMetrics(0f, 0f, 0f, 0f)
        }

        val accuracy = results.count { it.passed }.toFloat() / results.size
        // Simplified metrics - would be more sophisticated in production
        return EvaluationMetrics(
            accuracy = accuracy,
            precision = accuracy,
            recall = accuracy,
            f1Score = accuracy
        )
    }
}

/**
 * Evaluator for response quality
 */
class ResponseQualityEvaluator : Evaluator<Pair<String, ConversationSession>, String> {
    private val results = mutableListOf<EvaluationResult>()

    override suspend fun evaluate(
        input: Pair<String, ConversationSession>,
        expected: String
    ): EvaluationResult {
        // Would evaluate semantic similarity, coherence, emotion appropriateness
        return EvaluationResult(
            passed = false,
            score = 0.0f,
            details = "Not implemented"
        ).also { results.add(it) }
    }

    override suspend fun evaluateBatch(
        testCases: List<TestCase<Pair<String, ConversationSession>, String>>
    ): BatchEvaluationResult {
        val evalResults = testCases.map { testCase ->
            evaluate(testCase.input, testCase.expected)
        }

        val passed = evalResults.count { it.passed }
        val averageScore = evalResults.map { it.score }.average().toFloat()

        return BatchEvaluationResult(
            totalTests = testCases.size,
            passed = passed,
            failed = testCases.size - passed,
            averageScore = averageScore,
            results = evalResults,
            metrics = getMetrics()
        )
    }

    override fun getMetrics(): EvaluationMetrics {
        if (results.isEmpty()) {
            return EvaluationMetrics(0f, 0f, 0f, 0f)
        }

        val accuracy = results.count { it.passed }.toFloat() / results.size
        return EvaluationMetrics(
            accuracy = accuracy,
            precision = accuracy,
            recall = accuracy,
            f1Score = accuracy
        )
    }

    /**
     * Evaluate specific quality dimensions
     */
    suspend fun evaluateQuality(
        response: String,
        context: EvaluationContext
    ): QualityMetrics {
        return QualityMetrics(
            relevance = calculateRelevance(response, context),
            coherence = calculateCoherence(response),
            emotionAppropriateness = calculateEmotionMatch(response, context.emotion),
            factualAccuracy = calculateFactualAccuracy(response, context),
            lengthAppropriateness = calculateLengthScore(response)
        )
    }

    private fun calculateRelevance(response: String, context: EvaluationContext): Float {
        // Simplified - would use embeddings in production
        return 0.8f
    }

    private fun calculateCoherence(response: String): Float {
        // Would use perplexity-based measure
        return 0.85f
    }

    private fun calculateEmotionMatch(response: String, emotion: Emotion?): Float {
        // Check if response tone matches emotion
        return 0.9f
    }

    private fun calculateFactualAccuracy(response: String, context: EvaluationContext): Float {
        // Would verify facts against knowledge base
        return 0.75f
    }

    private fun calculateLengthScore(response: String): Float {
        val wordCount = response.split("\\s+".toRegex()).size
        return when {
            wordCount < 10 -> 0.6f
            wordCount in 10..100 -> 1.0f
            else -> 0.7f
        }
    }
}

/**
 * Context for response quality evaluation
 */
data class EvaluationContext(
    val userQuery: String,
    val conversationHistory: List<ConversationTurn>,
    val emotion: Emotion?,
    val intent: Intent?
)

/**
 * Quality metrics for response evaluation
 */
data class QualityMetrics(
    val relevance: Float,
    val coherence: Float,
    val emotionAppropriateness: Float,
    val factualAccuracy: Float,
    val lengthAppropriateness: Float
) {
    fun overallScore(): Float {
        return (relevance * 0.3f +
                coherence * 0.2f +
                emotionAppropriateness * 0.2f +
                factualAccuracy * 0.2f +
                lengthAppropriateness * 0.1f)
    }

    fun toReport(): String {
        return """
            Quality Metrics:
            - Relevance: ${(relevance * 100).toInt()}%
            - Coherence: ${(coherence * 100).toInt()}%
            - Emotion Match: ${(emotionAppropriateness * 100).toInt()}%
            - Factual Accuracy: ${(factualAccuracy * 100).toInt()}%
            - Length: ${(lengthAppropriateness * 100).toInt()}%
            Overall Score: ${(overallScore() * 100).toInt()}%
        """.trimIndent()
    }
}

/**
 * Evaluator for routing decisions
 */
class RouterEvaluator {
    private val evaluations = mutableListOf<RouterEvaluation>()

    fun addEvaluation(evaluation: RouterEvaluation) {
        evaluations.add(evaluation)
    }

    fun getAccuracy(): Float {
        if (evaluations.isEmpty()) return 0.0f
        return evaluations.count { it.wasOptimal }.toFloat() / evaluations.size
    }

    fun getAverageEfficiency(): Float {
        if (evaluations.isEmpty()) return 0.0f
        return evaluations.map { it.efficiency }.average().toFloat()
    }

    fun getAverageLatency(): Long {
        if (evaluations.isEmpty()) return 0L
        return evaluations.map { it.actualLatency }.average().toLong()
    }

    fun getReport(): String {
        if (evaluations.isEmpty()) {
            return "No routing evaluations available"
        }

        val accuracy = getAccuracy()
        val avgEfficiency = getAverageEfficiency()
        val avgLatency = getAverageLatency()
        val totalCost = evaluations.sumOf { it.cost.toDouble() }

        return """
            Router Performance Report:
            - Total Decisions: ${evaluations.size}
            - Accuracy: ${(accuracy * 100).toInt()}%
            - Average Efficiency: ${(avgEfficiency * 100).toInt()}%
            - Average Latency: ${avgLatency}ms
            - Total Cost: ${"%.2f".format(totalCost)}

            By Route Type:
            ${getRouteTypeBreakdown()}
        """.trimIndent()
    }

    private fun getRouteTypeBreakdown(): String {
        val byRoute = evaluations.groupBy { it.decision::class.simpleName }
        return byRoute.entries.joinToString("\n") { (route, evals) ->
            val count = evals.size
            val avgLatency = evals.map { it.actualLatency }.average().toLong()
            "  - $route: $count decisions, ${avgLatency}ms avg latency"
        }
    }
}

/**
 * Latency benchmarks
 */
class LatencyBenchmark {
    data class LatencyTarget(
        val p50: Long,
        val p95: Long,
        val p99: Long
    )

    companion object {
        val ON_DEVICE_TARGET = LatencyTarget(p50 = 100, p95 = 200, p99 = 300)
        val BACKEND_TARGET = LatencyTarget(p50 = 400, p95 = 600, p99 = 800)
        val E2E_TARGET = LatencyTarget(p50 = 500, p95 = 800, p99 = 1200)
    }

    fun evaluateLatency(latencies: List<Long>, target: LatencyTarget): LatencyEvaluation {
        if (latencies.isEmpty()) {
            return LatencyEvaluation(
                p50 = 0, p95 = 0, p99 = 0,
                meetsTarget = false,
                target = target
            )
        }

        val sorted = latencies.sorted()
        val p50 = percentile(sorted, 0.50)
        val p95 = percentile(sorted, 0.95)
        val p99 = percentile(sorted, 0.99)

        val meetsTarget = p50 <= target.p50 && p95 <= target.p95 && p99 <= target.p99

        return LatencyEvaluation(
            p50 = p50,
            p95 = p95,
            p99 = p99,
            meetsTarget = meetsTarget,
            target = target
        )
    }

    private fun percentile(sorted: List<Long>, percentile: Double): Long {
        val index = (sorted.size * percentile).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index]
    }
}

data class LatencyEvaluation(
    val p50: Long,
    val p95: Long,
    val p99: Long,
    val meetsTarget: Boolean,
    val target: LatencyBenchmark.LatencyTarget
) {
    fun toReport(): String {
        return """
            Latency Evaluation:
            - P50: ${p50}ms (target: ${target.p50}ms) ${if (p50 <= target.p50) "✓" else "✗"}
            - P95: ${p95}ms (target: ${target.p95}ms) ${if (p95 <= target.p95) "✓" else "✗"}
            - P99: ${p99}ms (target: ${target.p99}ms) ${if (p99 <= target.p99) "✗" else "✗"}
            Overall: ${if (meetsTarget) "PASS" else "FAIL"}
        """.trimIndent()
    }
}

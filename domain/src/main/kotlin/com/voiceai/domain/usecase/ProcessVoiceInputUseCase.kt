package com.voiceai.domain.usecase

import com.voiceai.domain.entity.*
import com.voiceai.domain.repository.ConversationRepository
import com.voiceai.domain.repository.InferenceRepository
import com.voiceai.domain.repository.MetricsRepository
import com.voiceai.domain.router.InferenceRouter
import javax.inject.Inject

/**
 * Main use case for processing voice input end-to-end.
 * Orchestrates: STT → Intent → Emotion → Routing → Inference → TTS
 */
class ProcessVoiceInputUseCase @Inject constructor(
    private val inferenceRepository: InferenceRepository,
    private val conversationRepository: ConversationRepository,
    private val metricsRepository: MetricsRepository,
    private val inferenceRouter: InferenceRouter
) {
    suspend operator fun invoke(
        voiceInput: VoiceInput,
        sessionId: String,
        userId: String,
        userPreference: UserPreference = UserPreference.BALANCED
    ): Result<VoiceProcessingResult> {
        val startTime = System.currentTimeMillis()
        val metrics = ProcessingMetrics()

        return try {
            // 1. Get conversation session
            val session = conversationRepository.getSession(sessionId)
                ?: conversationRepository.createSession(userId)

            // 2. Speech-to-Text (assumed to be done before this use case in voice module)
            // For now, we'll assume text is extracted separately
            // In practice, this would call STTRepository

            // 3. Emotion detection (parallel with STT in real implementation)
            val emotionStartTime = System.currentTimeMillis()
            val emotionResult = inferenceRepository.detectEmotion(
                voiceInput.audioData,
                voiceInput.sampleRate
            )
            val emotion = emotionResult.getOrDefault()
            val emotionLatency = System.currentTimeMillis() - emotionStartTime

            // Note: Text would come from STT, using placeholder for now
            val text = "placeholder_text" // TODO: Get from STT

            // 4. Intent classification
            val intentStartTime = System.currentTimeMillis()
            val intentResult = inferenceRepository.classifyIntent(text, useOnDevice = true)
            val intent = when (intentResult) {
                is IntentClassificationResult.Success -> intentResult.intent
                is IntentClassificationResult.Failure -> {
                    // Fallback to default intent
                    Intent(
                        type = IntentType.UNKNOWN,
                        confidence = 0.0f
                    )
                }
            }
            val intentLatency = System.currentTimeMillis() - intentStartTime

            // 5. Routing decision
            val routingStartTime = System.currentTimeMillis()
            val routingContext = RoutingContext(
                intent = intent,
                emotion = emotion,
                conversationHistory = session.turns,
                networkAvailable = inferenceRepository.isBackendAvailable(),
                userPreference = userPreference
            )
            val routeDecision = inferenceRouter.route(routingContext)
            val routingLatency = System.currentTimeMillis() - routingStartTime

            // 6. Generate response
            val inferenceStartTime = System.currentTimeMillis()
            val useOnDevice = routeDecision is RouteDecision.OnDevice
            val responseResult = inferenceRepository.generateResponse(
                input = text,
                context = session,
                emotion = emotion,
                useOnDevice = useOnDevice
            )
            val inferenceLatency = System.currentTimeMillis() - inferenceStartTime

            val response = responseResult.getOrThrow()

            // 7. Update conversation
            val userTurn = ConversationTurn(
                role = ConversationTurn.Role.USER,
                content = text,
                emotion = emotion,
                intent = intent
            )
            val assistantTurn = ConversationTurn(
                role = ConversationTurn.Role.ASSISTANT,
                content = response.content,
                processingMetrics = metrics.copy(
                    emotionDetectionMs = emotionLatency,
                    intentClassificationMs = intentLatency,
                    routingDecisionMs = routingLatency,
                    inferenceLatencyMs = inferenceLatency,
                    totalLatencyMs = System.currentTimeMillis() - startTime
                )
            )

            conversationRepository.addTurn(sessionId, userTurn)
            conversationRepository.addTurn(sessionId, assistantTurn)

            // 8. Log metrics
            val totalLatency = System.currentTimeMillis() - startTime
            val processingMetrics = ProcessingMetrics(
                emotionDetectionMs = emotionLatency,
                intentClassificationMs = intentLatency,
                routingDecisionMs = routingLatency,
                inferenceLatencyMs = inferenceLatency,
                totalLatencyMs = totalLatency,
                route = routeDecision,
                model = response.metadata.model
            )
            metricsRepository.logProcessingMetrics(processingMetrics)

            Result.success(
                VoiceProcessingResult(
                    response = response,
                    emotion = emotion,
                    intent = intent,
                    routeDecision = routeDecision,
                    metrics = processingMetrics
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Result of voice processing pipeline
 */
data class VoiceProcessingResult(
    val response: AIResponse,
    val emotion: Emotion,
    val intent: Intent,
    val routeDecision: RouteDecision,
    val metrics: ProcessingMetrics
)

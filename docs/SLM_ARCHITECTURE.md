# Hybrid SLM Architecture for Voice AI Android

## Executive Summary
This document outlines the architecture for a hybrid Small Language Model (SLM) system that replaces Hume AI with on-device Gemini Nano and backend Llama 3.2 models, optimized for voice command understanding, intent classification, and emotion-aware conversational AI.

## Architecture Overview

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
│                   │ - Simple cmds  │    │  - Complex AI   │  │  │
│                   │ - <50ms        │    │  - Skills       │  │  │
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
│                            │ TTS Engine       │             │  │
│                            │ (On-device)      │             │  │
│                            └──────────────────┘             │  │
└─────────────────────────────────────────────────────────────────┘
                                     │
                                     │ HTTPS/gRPC
                                     ▼
┌─────────────────────────────────────────────────────────────────┐
│               ARM Kubernetes Backend (VPS)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐                   │
│  │  Llama 3.2 3B    │    │  Llama 3.2 1B    │                   │
│  │  (vLLM/llama.cpp)│    │  (Fast fallback) │                   │
│  │  - Conversations │    │  - Quick queries │                   │
│  │  - Complex tasks │    │  - Load shedding │                   │
│  └────────┬─────────┘    └────────┬─────────┘                   │
│           │                       │                              │
│           └───────────┬───────────┘                              │
│                       ▼                                          │
│            ┌──────────────────────┐                              │
│            │   Skills Engine      │                              │
│            │   - Function calling │                              │
│            │   - Tool use         │                              │
│            │   - API integration  │                              │
│            └──────────┬───────────┘                              │
│                       │                                          │
│            ┌──────────▼───────────┐                              │
│            │  Emotion Analyzer    │                              │
│            │  (wav2vec2/emotion)  │                              │
│            │  - Audio emotion     │                              │
│            │  - Text sentiment    │                              │
│            └──────────┬───────────┘                              │
│                       │                                          │
│            ┌──────────▼───────────┐                              │
│            │   Redis Cache        │                              │
│            │   - Context memory   │                              │
│            │   - Response cache   │                              │
│            └──────────────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. On-Device Components (Android)

#### 1.1 Gemini Nano Integration
**Technology**: Google AI Edge SDK / MediaPipe LLM Inference API
**Model**: Gemini Nano (quantized, ~2GB)
**Responsibilities**:
- Quick intent classification (<50ms)
- Simple voice commands (timers, alarms, weather queries)
- Offline functionality
- Privacy-preserving local processing

**Implementation**:
```kotlin
// Location: /app/src/main/kotlin/com/voiceai/gemini/GeminiNanoClient.kt
class GeminiNanoClient @Inject constructor() {
    private val inferenceEngine: InferenceEngine

    suspend fun classifyIntent(text: String): Intent {
        return inferenceEngine.generateContent(
            prompt = """Classify this voice command: "$text"
            Categories: [simple_query, complex_task, conversation, command]
            Response format: {intent: "", confidence: 0.0-1.0}"""
        )
    }

    suspend fun handleSimpleCommand(text: String): Response {
        // Fast on-device response generation
    }
}
```

**Resource Requirements**:
- RAM: 2-3GB for model + inference
- Storage: 2GB for quantized model
- Latency: 30-80ms for intent classification

#### 1.2 Speech-to-Text (STT)
**Technology**: On-device options
- **Primary**: Google Speech Recognition API (on-device mode)
- **Alternative**: Whisper.cpp (tiny/base models, ~75MB)

**Configuration**:
```kotlin
// Location: /voice/src/main/kotlin/com/voiceai/stt/STTEngine.kt
class STTEngine @Inject constructor() {
    // 16kHz mono audio input
    // Continuous recognition with VAD
    // Streaming results for low latency
}
```

**Performance Target**: <100ms first-token latency

#### 1.3 Emotion Detection (Local)
**Technology**:
- **Lightweight model**: emotion2vec-tiny (~50MB)
- **Alternative**: Fine-tuned wav2vec2-base-emotion

**Features**:
- Real-time emotion classification from audio
- 6 basic emotions: happy, sad, angry, neutral, surprised, fearful
- Confidence scores

```kotlin
// Location: /voice/src/main/kotlin/com/voiceai/emotion/EmotionDetector.kt
class EmotionDetector @Inject constructor() {
    suspend fun analyzeAudio(audioBuffer: ByteArray): EmotionResult {
        // Process 3-second audio windows
        // Return dominant emotion + confidence
    }
}
```

#### 1.4 Router Logic
**Purpose**: Decide on-device vs backend routing

**Decision Criteria**:
```kotlin
// Location: /domain/src/main/kotlin/com/voiceai/router/InferenceRouter.kt
sealed class RouteDecision {
    object OnDevice : RouteDecision()
    data class Backend(val model: BackendModel) : RouteDecision()
    data class Hybrid(val stages: List<Stage>) : RouteDecision()
}

class InferenceRouter {
    fun route(intent: Intent, context: ConversationContext): RouteDecision {
        return when {
            // Fast path: simple commands
            intent.confidence > 0.9 && intent.type.isSimple() -> OnDevice

            // Offline mode
            !networkAvailable -> OnDevice

            // Complex reasoning required
            intent.requiresToolUse() || intent.conversational() -> Backend(Llama3B)

            // Latency critical
            context.requiresQuickResponse() -> OnDevice

            // Default: hybrid approach
            else -> Hybrid(listOf(OnDevice, Backend(Llama1B)))
        }
    }
}
```

**Metrics Tracked**:
- Routing accuracy (eval against ground truth)
- Latency by route
- User satisfaction by route
- Fallback frequency

### 2. Backend Components (ARM Kubernetes)

#### 2.1 Llama 3.2 Deployment
**Models**:
- **Primary**: Llama 3.2 3B (Q4_K_M quantized, ~2GB)
- **Fallback**: Llama 3.2 1B (Q4_K_M quantized, ~800MB)

**Inference Engine Options**:
1. **llama.cpp** (Recommended for ARM)
   - Native ARM optimization
   - Low memory footprint
   - Supports quantization
   ```bash
   # Run with 4-bit quantization
   ./llama-server -m llama-3.2-3b-q4_k_m.gguf \
     --host 0.0.0.0 --port 8080 \
     --ctx-size 2048 --batch-size 512 \
     --threads 4 --gpu-layers 0
   ```

2. **vLLM** (If GPU available)
   - Higher throughput
   - Continuous batching
   - PagedAttention optimization

**Resource Allocation (Kubernetes)**:
```yaml
# Per pod
resources:
  requests:
    memory: "3Gi"
    cpu: "2000m"
  limits:
    memory: "4Gi"
    cpu: "3000m"
```

**Deployment Strategy**:
- 2 replicas for 3B model (HA)
- 1 replica for 1B model (fallback)
- HPA: scale to 4 pods based on queue depth
- PDB: maintain 1 replica during updates

#### 2.2 Skills Engine
**Purpose**: Function calling and tool use

**Architecture**:
```javascript
// Location: /backend/src/skills/skillsEngine.js
class SkillsEngine {
  constructor() {
    this.skills = {
      weather: new WeatherSkill(),
      calendar: new CalendarSkill(),
      timer: new TimerSkill(),
      webSearch: new WebSearchSkill(),
      calculation: new CalculationSkill()
    };
  }

  async execute(llmResponse) {
    // Parse function call from LLM
    const functionCall = this.parseFunctionCall(llmResponse);

    // Execute skill
    const result = await this.skills[functionCall.name]
      .execute(functionCall.parameters);

    // Return result to LLM for natural language response
    return result;
  }
}
```

**Skills Framework**:
```kotlin
// Location: /domain/src/main/kotlin/com/voiceai/skills/Skill.kt
interface Skill {
    val name: String
    val description: String
    val parameters: List<Parameter>

    suspend fun execute(params: Map<String, Any>): SkillResult
    suspend fun validate(params: Map<String, Any>): ValidationResult
}

// Example implementation
class WeatherSkill : Skill {
    override val name = "get_weather"
    override val description = "Get current weather for a location"
    override val parameters = listOf(
        Parameter("location", ParameterType.STRING, required = true),
        Parameter("units", ParameterType.STRING, required = false)
    )

    override suspend fun execute(params: Map<String, Any>): SkillResult {
        val location = params["location"] as String
        // Call weather API
        return SkillResult.Success(weatherData)
    }
}
```

**Prompt Engineering for Function Calling**:
```
System: You are a helpful assistant with access to these tools:
- get_weather(location: str, units: str = "metric")
- set_timer(duration: int, unit: str, label: str = "")
- search_web(query: str, max_results: int = 5)

When you need to use a tool, respond with:
TOOL: tool_name
PARAMS: {"param1": "value1", "param2": "value2"}

User: {user_query}
```

#### 2.3 Emotion-Aware Response Generation
**Backend Model**: Fine-tuned Llama 3.2 with emotion conditioning

**Training Approach**:
1. Base: Llama 3.2 3B
2. Fine-tune with emotion-conditioned responses
3. Dataset: EmpatheticDialogues + custom data

**Prompt Template**:
```
System: You are an empathetic voice assistant. The user's emotional state is: {emotion} (confidence: {confidence}).
Respond appropriately considering their emotional state.

User: {user_query}
Assistant:
```

**Emotion Integration**:
```javascript
// Backend service
async function generateEmotionAwareResponse(text, emotion) {
  const prompt = `
System: User emotion: ${emotion.primary} (${emotion.confidence})
Detected emotions: ${emotion.all.join(', ')}

Respond empathetically and appropriately.

User: ${text}
Assistant:`;

  const response = await llama3_2.generate(prompt, {
    temperature: 0.7,
    max_tokens: 256,
    stop: ['\n\nUser:', '\n\n']
  });

  return response;
}
```

#### 2.4 Context Management (Redis)
**Purpose**: Maintain conversation history and context

**Schema**:
```javascript
// Redis keys structure
conversation:{user_id}:{session_id} = {
  messages: [
    {role: 'user', content: '...', emotion: '...', timestamp: ...},
    {role: 'assistant', content: '...', timestamp: ...}
  ],
  metadata: {
    started: timestamp,
    last_activity: timestamp,
    total_turns: int,
    user_preferences: {...}
  }
}

// Context TTL: 30 minutes of inactivity
```

**Context Pruning Strategy**:
- Keep last 10 turns in memory
- Summarize older context with Llama 1B
- Store summaries for long-term memory

### 3. Evals Framework

#### 3.1 Intent Classification Accuracy
**Metrics**:
- Precision, Recall, F1 per intent class
- Confusion matrix
- Confidence calibration

**Test Dataset**:
- 1000+ labeled voice commands
- Covers all intent categories
- Includes edge cases and ambiguous queries

```kotlin
// Location: /domain/src/main/kotlin/com/voiceai/evals/IntentEvaluator.kt
class IntentEvaluator {
    fun evaluate(model: IntentClassifier, testSet: List<TestCase>): EvalResult {
        val predictions = testSet.map { model.classify(it.input) }
        return EvalResult(
            accuracy = calculateAccuracy(predictions, testSet),
            precisionRecallF1 = calculatePRF1(predictions, testSet),
            confusionMatrix = buildConfusionMatrix(predictions, testSet)
        )
    }
}
```

#### 3.2 Response Quality
**Metrics**:
- Relevance (human eval + automated)
- Coherence (perplexity-based)
- Emotion appropriateness
- Factual accuracy (for knowledge tasks)
- Latency (p50, p95, p99)

**Automated Evals**:
```python
# Location: /backend/evals/response_quality.py
def evaluate_response_quality(response, ground_truth, emotion_context):
    scores = {
        'semantic_similarity': calculate_cosine_similarity(response, ground_truth),
        'emotion_match': check_emotion_appropriateness(response, emotion_context),
        'coherence': calculate_perplexity(response),
        'length_appropriate': check_length(response)
    }
    return aggregate_score(scores)
```

#### 3.3 Latency Benchmarks
**Targets**:
- On-device intent classification: <50ms
- On-device simple response: <150ms
- Backend complex query: <500ms
- End-to-end (voice-to-voice): <800ms

**Monitoring**:
```kotlin
class LatencyMonitor {
    fun trackPipeline() {
        metrics.track("stt_latency", sttDuration)
        metrics.track("intent_classification_latency", intentDuration)
        metrics.track("llm_inference_latency", inferenceDuration)
        metrics.track("tts_latency", ttsDuration)
        metrics.track("e2e_latency", totalDuration)
    }
}
```

#### 3.4 Router Accuracy
**Evaluation**:
- Compare routing decisions vs optimal route (hindsight)
- Measure unnecessary backend calls
- Track user satisfaction by route type

```kotlin
class RouterEvaluator {
    fun evaluateDecision(
        routeDecision: RouteDecision,
        actualLatency: Long,
        userSatisfaction: Rating
    ): RouterMetrics {
        // Was this the optimal route?
        val optimalRoute = determineOptimalRoute(actualLatency, userSatisfaction)
        val wasOptimal = routeDecision == optimalRoute

        return RouterMetrics(
            accuracy = if (wasOptimal) 1.0 else 0.0,
            latency = actualLatency,
            satisfaction = userSatisfaction.score
        )
    }
}
```

### 4. Data Flow

#### 4.1 Simple Command (On-Device Only)
```
User: "Set a timer for 5 minutes"
  ↓
[STT] → "set a timer for 5 minutes" (80ms)
  ↓
[Intent Classification - Gemini Nano] → intent: "set_timer", confidence: 0.95 (40ms)
  ↓
[Router] → RouteDecision.OnDevice (5ms)
  ↓
[Gemini Nano] → "I've set a timer for 5 minutes" (60ms)
  ↓
[TTS] → Audio output (100ms)

Total: ~285ms
```

#### 4.2 Complex Query (Backend)
```
User: "What's the weather like today and should I bring an umbrella?"
  ↓
[STT] → text (80ms)
  ↓
[Intent Classification - Gemini Nano] → intent: "complex_query", confidence: 0.85 (40ms)
  ↓
[Router] → RouteDecision.Backend(Llama3B) (5ms)
  ↓
[Backend API Call] → (30ms network)
  ↓
[Llama 3.2 3B + Weather Skill] → response with weather data (250ms)
  ↓
[Response] ← API response (30ms network)
  ↓
[TTS] → Audio output (100ms)

Total: ~535ms
```

#### 4.3 Emotion-Aware Conversation (Hybrid)
```
User: [distressed voice] "I'm really worried about my presentation tomorrow"
  ↓
[STT] → text (80ms)
  ↓
[Emotion Detection] → emotion: "anxious", confidence: 0.82 (50ms) [parallel with STT]
  ↓
[Intent Classification - Gemini Nano] → intent: "emotional_support", confidence: 0.90 (40ms)
  ↓
[Router] → RouteDecision.Backend(Llama3B) (5ms)
  ↓
[Backend API Call] with emotion context (30ms)
  ↓
[Llama 3.2 3B with emotion conditioning] → empathetic response (280ms)
  ↓
[Response] ← API response (30ms)
  ↓
[TTS with appropriate tone] → Audio output (120ms)

Total: ~635ms
```

## Performance Optimization

### ARM-Specific Optimizations
1. **NEON SIMD instructions**: Leverage ARM NEON for matrix operations
2. **Quantization**: Use 4-bit (Q4_K_M) for optimal size/quality tradeoff
3. **Memory layout**: Optimize for ARM cache hierarchy
4. **Thread affinity**: Pin inference threads to big cores

### Model Optimization
1. **Llama 3.2 Quantization**:
   ```bash
   # Using llama.cpp
   ./quantize llama-3.2-3b-instruct.gguf llama-3.2-3b-q4_k_m.gguf Q4_K_M
   ```
   - FP16: ~6GB
   - Q8: ~3GB
   - Q4_K_M: ~2GB (recommended)
   - Q4_K_S: ~1.7GB (slight quality loss)

2. **Gemini Nano**: Pre-quantized by Google (INT4/INT8 mixed precision)

3. **Emotion Model**: Convert to TFLite with INT8 quantization (~15MB)

### Caching Strategy
1. **Common intent responses**: Cache in-app (10MB limit)
2. **Backend responses**: Redis with 5-minute TTL
3. **Model outputs**: Cache by (input_hash, model_version)

### Batch Processing (Backend)
- Continuous batching for multiple concurrent requests
- Dynamic batch sizes based on queue depth
- Priority queue: latency-critical requests first

## Deployment Architecture

### Kubernetes Resources

#### 1. Llama 3.2 3B Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: llama-3b-inference
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: llama-server
        image: your-registry/llama-cpp-server:arm64
        args:
          - "-m"
          - "/models/llama-3.2-3b-q4_k_m.gguf"
          - "--ctx-size"
          - "2048"
          - "--threads"
          - "4"
        resources:
          requests:
            memory: "3Gi"
            cpu: "2000m"
          limits:
            memory: "4Gi"
            cpu: "3000m"
        volumeMounts:
        - name: model-storage
          mountPath: /models
          readOnly: true
      volumes:
      - name: model-storage
        persistentVolumeClaim:
          claimName: llm-models-pvc
      nodeSelector:
        kubernetes.io/arch: arm64
```

#### 2. Model Storage PVC
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: llm-models-pvc
spec:
  accessModes:
    - ReadOnlyMany
  resources:
    requests:
      storage: 10Gi  # Space for multiple model versions
```

#### 3. Skills Service Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: skills-engine
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: skills-service
        image: your-registry/skills-engine:arm64
        env:
        - name: LLAMA_3B_ENDPOINT
          value: "http://llama-3b-service:8080"
        - name: REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
```

### Android App Size Budget
- Base APK: ~15MB
- Gemini Nano model: ~2GB (downloaded on-demand)
- Emotion model: ~15MB (bundled)
- TTS voices: ~50MB (downloaded)
- **Total first install**: ~80MB (without Gemini Nano)
- **With on-device AI**: ~2.1GB

**Strategy**:
- Ship minimal APK
- Download Gemini Nano on WiFi only (user consent)
- Lazy-load TTS voices
- Enable "Lite mode" without on-device models

## Security & Privacy

### On-Device Privacy
- All voice processing stays local by default
- User consent required for backend calls
- No audio uploaded unless explicitly needed
- Conversation history encrypted at rest (Android Keystore)

### Backend Security
- mTLS for Android ↔ Backend communication
- Audio transmitted over HTTPS only
- Redis data encrypted in transit
- No persistent storage of raw audio
- User data retention: 30 days max

### Model Safety
- Content filtering on Llama outputs
- Toxicity detection (lightweight classifier)
- User feedback for safety issues

## Cost Analysis

### ARM VPS Costs (Oracle Cloud A1 Free Tier)
- 4 OCPU (ARM cores): Free
- 24GB RAM: Free
- 200GB storage: Free

**Fits**:
- Llama 3.2 3B (2 replicas): 6GB
- Llama 3.2 1B (1 replica): 1GB
- Redis: 1GB
- System overhead: 3GB
- Skills services: 2GB
- Total: ~13GB (comfortable margin)

### Alternative (if exceeding free tier):
- Hetzner CAX11 (2 ARM cores, 4GB RAM): €4.50/month
- Can run Llama 3.2 1B only

### Android Costs
- Google AI Edge (Gemini Nano): Free (subject to fair use)
- Speech Recognition: Free (on-device)
- TTS: Free (on-device)

**Total Cost**: €0/month (Oracle free tier) or €4.50/month (Hetzner)

## Migration from Hume AI

### Phase 1: Parallel Running (1 week)
- Keep Hume AI integration
- Add SLM pipeline alongside
- A/B test: 80% SLM, 20% Hume AI
- Compare quality metrics

### Phase 2: Full Migration (1 week)
- Switch to 100% SLM
- Remove Hume AI client code
- Update infrastructure configs
- Monitor for regressions

### Phase 3: Optimization (2 weeks)
- Fine-tune router logic
- Optimize latency
- Improve emotion detection
- Expand skills library

## Success Metrics

### Quantitative
- **Latency**: p95 < 600ms (voice-to-voice)
- **Accuracy**: Intent classification > 95%
- **Availability**: 99.5% uptime
- **Cost**: <€5/month for 1000 users

### Qualitative
- User satisfaction: >4.2/5 stars
- Emotion appropriateness: >85% human eval
- Natural conversation flow
- Privacy confidence: >90% users comfortable

## Next Steps

1. **Week 1**: Set up Android project with Gemini Nano
2. **Week 2**: Deploy Llama 3.2 on ARM Kubernetes
3. **Week 3**: Implement router and skills framework
4. **Week 4**: Add emotion detection and evals
5. **Week 5**: End-to-end integration and testing
6. **Week 6**: Performance optimization and tuning

---

**Document Version**: 1.0
**Last Updated**: 2025-10-29
**Authors**: Claude + User
**Status**: Architecture Approved - Implementation Ready

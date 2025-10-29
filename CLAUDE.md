# Voice AI Android Application with ARM Kubernetes Backend

## Project Overview
Building a sophisticated Android voice AI application with hybrid SLM (Small Language Model) architecture for voice command understanding, intent classification, and emotion-aware conversational AI. This project uses on-device Gemini Nano and backend Llama 3.2 models hosted on ARM VPS with Kubernetes, focusing on learning evals, routers, and skills logic while maintaining cost-effectiveness and privacy.

## Architecture Constraints & Decisions
- **Target**: Android native application (Kotlin/Java)
- **Voice AI**: Hybrid SLM approach (on-device + backend)
  - **On-device**: Gemini Nano for fast intent classification and simple commands
  - **Backend**: Llama 3.2 (1B/3B) for complex reasoning and conversational AI
- **Backend**: ARM VPS with Kubernetes (Oracle Cloud A1 free tier recommended)
- **Development**: Multi-module MVVM + Clean Architecture
- **Goals**: Learn evals, routers, skills logic, and emotion-aware AI implementation
- **Privacy-First**: Local processing by default, backend only when necessary

## Current Phase: SLM Implementation (Weeks 1-6)
### Infrastructure Tasks:
1. Provision ARM VPS (Oracle Cloud A1 or Hetzner ARM64)
2. Deploy single-node Kubernetes cluster optimized for ARM
3. Deploy Llama 3.2 (1B/3B) with llama.cpp for ARM optimization
4. Set up model storage and serving infrastructure
5. Configure Redis for context management
6. Set up monitoring and logging (latency, accuracy metrics)

### Android Tasks:
1. Integrate Gemini Nano via Google AI Edge SDK
2. Implement on-device Speech-to-Text (Google Speech Recognition)
3. Build router logic for on-device vs backend decision-making
4. Create voice capture pipeline (16kHz mono audio)
5. Implement emotion detection (on-device lightweight model)
6. Set up MVVM architecture with StateFlow for reactive streams
7. Configure Hilt dependency injection

### AI/ML Tasks:
1. Implement intent classification with Gemini Nano
2. Build skills framework (function calling and tool use)
3. Create emotion-aware response generation with Llama 3.2
4. Develop evals framework for quality metrics
5. Optimize routing logic based on latency and complexity

## Key Implementation Guidelines

### For ARM Kubernetes:
- Use `kubernetes.io/arch: arm64` node selectors
- Build multi-arch container images (arm64/amd64)
- Leverage ARM's linear scalability for voice processing
- Configure proper resource limits for ARM performance characteristics

### For Android Development:
- Target SDK 34+ with Kotlin coroutines for async voice processing
- Use Jetpack Compose for reactive UI updates
- Implement proper audio permissions and background processing
- Design for <200ms latency for on-device, <600ms for backend queries
- Optimize APK size: ship minimal app, download models on-demand
- Implement offline-first architecture with graceful degradation

### For SLM Integration:
- **Gemini Nano**: Use Google AI Edge SDK for on-device inference (<50ms)
- **Llama 3.2**: Deploy with llama.cpp on ARM (Q4_K_M quantization)
- **Router Logic**: Intelligent decision-making for on-device vs backend
- **Skills Framework**: Implement function calling and tool use patterns
- **Emotion AI**: Local lightweight models for privacy-preserving emotion detection
- **Context Management**: Redis-based conversation history with intelligent pruning

### For Performance Optimization:
- Leverage ARM NEON SIMD for on-device audio processing
- Use 4-bit quantization (Q4_K_M) for optimal size/quality tradeoff
- Implement continuous batching for backend inference
- Cache common responses and intents locally
- Monitor and optimize p95 latency across the pipeline

## Code Style & Patterns
- Kotlin coding conventions with explicit types for clarity
- Repository pattern for data layer
- Use case pattern for business logic
- Single responsibility principle for AI skills modules
- Comprehensive error handling with sealed classes

## Testing Strategy
- Unit tests for all AI processing components (intent classification, routing)
- Integration tests for Gemini Nano and Llama 3.2 API clients
- Android instrumentation tests for voice capture and STT
- ARM-specific performance benchmarks (latency, throughput, memory)
- Evals framework for intent accuracy, response quality, emotion appropriateness
- Router decision accuracy evaluation (optimal path analysis)
- Load testing for backend inference under concurrent requests
- A/B testing for on-device vs backend performance comparison

## Security & Privacy
- **Privacy-First Design**: All voice processing stays local by default
- On-device Gemini Nano for sensitive queries (no network calls)
- User consent required for backend calls
- Encrypted HTTPS/gRPC for Android ↔ Backend communication
- No persistent storage of raw audio without explicit user consent
- Conversation history encrypted at rest (Android Keystore)
- GDPR compliance for voice data (ARM VPS in EU recommended)
- 30-day max retention policy for user data
- Content safety: toxicity detection and filtering on all LLM outputs

## Development Workflow
1. Create feature branches for each component
2. Use Claude Code for iterative development and debugging
3. Document ARM deployment specifics in separate CLAUDE.md files
4. Implement comprehensive logging for debugging ARM performance
5. Test on both local development and ARM deployment environments

## Current Blockers & Questions
[Update this section as development progresses]

## Next Steps for Claude Code
1. **Week 1**: Set up Android project with Gemini Nano integration
   - Add Google AI Edge SDK dependencies
   - Implement on-device STT and TTS
   - Create basic voice capture pipeline
2. **Week 2**: Deploy Llama 3.2 on ARM Kubernetes
   - Build llama.cpp Docker image for ARM
   - Deploy with quantized models (Q4_K_M)
   - Set up model storage PVC
3. **Week 3**: Implement router and intent classification
   - Build routing logic (on-device vs backend)
   - Implement intent classifier with Gemini Nano
   - Create backend API client
4. **Week 4**: Add skills framework and emotion detection
   - Implement function calling patterns
   - Deploy emotion detection model
   - Create skills engine on backend
5. **Week 5**: Build evals framework
   - Intent classification accuracy metrics
   - Response quality evaluation
   - Router decision analysis
6. **Week 6**: End-to-end integration and optimization
   - Performance tuning (latency, memory)
   - A/B testing on-device vs backend
   - Production readiness checks

## Resource Links
- **SLM Architecture**: See `/docs/SLM_ARCHITECTURE.md` for detailed design
- **Gemini Nano**: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference
- **Llama 3.2**: https://github.com/meta-llama/llama-models/blob/main/models/llama3_2/MODEL_CARD.md
- **llama.cpp**: https://github.com/ggerganov/llama.cpp (ARM optimization)
- **ARM Kubernetes Best Practices**: https://kubernetes.io/docs/setup/production-environment/
- **Android Voice Processing**: https://developer.android.com/media/optimize/low-latency-audio
- **Oracle Cloud A1 Setup**: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier.htm
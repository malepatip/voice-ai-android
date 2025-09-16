# Voice AI Android Application with ARM Kubernetes Backend

## Project Overview
Building a sophisticated Android voice AI application with emotion analysis using Hume AI free tier and ARM VPS hosting with Kubernetes. This project focuses on learning evals, routers, and skills logic while maintaining cost-effectiveness.

## Architecture Constraints & Decisions
- **Target**: Android native application (Kotlin/Java)
- **Voice AI**: Hume AI EVI free tier (10 minutes/month, non-commercial)
- **Backend**: ARM VPS with Kubernetes (Oracle Cloud A1 free tier recommended)
- **Development**: Multi-module MVVM + Clean Architecture
- **Goals**: Learn evals, routers, skills logic implementation

## Current Phase: Phase 1 - Foundation (Weeks 1-3)
### Infrastructure Tasks:
1. Provision ARM VPS (Oracle Cloud A1 or Hetzner ARM64)
2. Deploy single-node Kubernetes cluster optimized for ARM
3. Configure multi-arch container registry
4. Set up basic monitoring and logging

### Android Tasks:
1. Create multi-module Android project structure
2. Implement basic AudioRecord voice capture (16kHz for Hume AI compatibility)
3. Set up MVVM architecture with StateFlow for reactive streams
4. Configure Hilt dependency injection
5. Create initial voice processing pipeline

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
- Design for <200ms latency voice processing pipeline

### For Hume AI Integration:
- Implement usage tracking to stay within 10-minute free tier
- Build session management with intelligent batching
- Create fallback mechanisms for local emotion detection
- Design API client with rate limiting and error handling

## Code Style & Patterns
- Kotlin coding conventions with explicit types for clarity
- Repository pattern for data layer
- Use case pattern for business logic
- Single responsibility principle for AI skills modules
- Comprehensive error handling with sealed classes

## Testing Strategy
- Unit tests for all AI processing components
- Integration tests for Hume AI API client
- Android instrumentation tests for voice capture
- ARM-specific performance benchmarks
- Mock strategies for free tier API limits

## Security & Privacy
- Local audio processing where possible
- Encrypted WebSocket connections to ARM backend
- GDPR compliance for voice data (ARM VPS in EU recommended)
- No persistent storage of voice data without user consent

## Development Workflow
1. Create feature branches for each component
2. Use Claude Code for iterative development and debugging
3. Document ARM deployment specifics in separate CLAUDE.md files
4. Implement comprehensive logging for debugging ARM performance
5. Test on both local development and ARM deployment environments

## Current Blockers & Questions
[Update this section as development progresses]

## Next Steps for Claude Code
1. Start with Android project setup and basic voice recording
2. Implement ARM Kubernetes cluster deployment
3. Create Hume AI client with usage tracking
4. Build basic voice processing pipeline
5. Set up CI/CD for multi-arch deployments

## Resource Links
- Hume AI EVI Documentation: https://dev.hume.ai/docs/empathic-voice-interface-evi/overview
- ARM Kubernetes Best Practices: [link to documentation]
- Android Voice Processing Guidelines: [link to Android docs]
- Oracle Cloud A1 Setup: [link to ARM deployment guide]
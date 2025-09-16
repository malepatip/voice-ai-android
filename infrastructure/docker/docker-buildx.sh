#!/bin/bash

# Multi-arch Docker build script for ARM and AMD64
# Optimized for Hetzner ARM VPS deployment

set -euo pipefail

# Configuration
REGISTRY=${REGISTRY:-"ghcr.io/malepatip"}
PROJECT=${PROJECT:-"voice-ai"}
VERSION=${VERSION:-"latest"}
PLATFORMS="linux/arm64,linux/amd64"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Check if buildx is available
check_buildx() {
    log "Checking Docker buildx availability..."
    if ! docker buildx version >/dev/null 2>&1; then
        error "Docker buildx is not available. Please install Docker 19.03+ with buildx support."
    fi
    log "Docker buildx is available"
}

# Create and use buildx builder
setup_builder() {
    log "Setting up multi-arch builder..."

    # Remove existing builder if it exists
    docker buildx rm voice-ai-builder 2>/dev/null || true

    # Create new builder with ARM and AMD64 support
    docker buildx create \
        --name voice-ai-builder \
        --driver docker-container \
        --platform $PLATFORMS \
        --bootstrap

    # Use the new builder
    docker buildx use voice-ai-builder

    log "Multi-arch builder 'voice-ai-builder' created and activated"
}

# Build and push voice-api service
build_voice_api() {
    log "Building voice-api for $PLATFORMS..."

    docker buildx build \
        --platform $PLATFORMS \
        --file infrastructure/docker/Dockerfile.voice-api \
        --tag $REGISTRY/$PROJECT/voice-api:$VERSION \
        --tag $REGISTRY/$PROJECT/voice-api:latest \
        --push \
        --cache-from type=registry,ref=$REGISTRY/$PROJECT/voice-api:cache \
        --cache-to type=registry,ref=$REGISTRY/$PROJECT/voice-api:cache,mode=max \
        --build-arg BUILDPLATFORM=linux/amd64 \
        --build-arg TARGETPLATFORM=linux/arm64 \
        .

    log "voice-api built and pushed successfully"
}

# Build and push voice-processor service
build_voice_processor() {
    log "Building voice-processor for $PLATFORMS..."

    docker buildx build \
        --platform $PLATFORMS \
        --file infrastructure/docker/Dockerfile.voice-processor \
        --tag $REGISTRY/$PROJECT/voice-processor:$VERSION \
        --tag $REGISTRY/$PROJECT/voice-processor:latest \
        --push \
        --cache-from type=registry,ref=$REGISTRY/$PROJECT/voice-processor:cache \
        --cache-to type=registry,ref=$REGISTRY/$PROJECT/voice-processor:cache,mode=max \
        --build-arg BUILDPLATFORM=linux/amd64 \
        --build-arg TARGETPLATFORM=linux/arm64 \
        .

    log "voice-processor built and pushed successfully"
}

# Inspect built images
inspect_images() {
    log "Inspecting built images..."

    echo "=== voice-api image details ==="
    docker buildx imagetools inspect $REGISTRY/$PROJECT/voice-api:$VERSION

    echo "=== voice-processor image details ==="
    docker buildx imagetools inspect $REGISTRY/$PROJECT/voice-processor:$VERSION
}

# Cleanup builder
cleanup() {
    log "Cleaning up builder..."
    docker buildx rm voice-ai-builder 2>/dev/null || true
}

# Main execution
main() {
    log "Starting multi-arch build process for Voice AI services"

    # Validate environment
    if [[ -z "${REGISTRY}" ]]; then
        warn "REGISTRY not set, using default: ghcr.io/malepatip"
    fi

    check_buildx
    setup_builder

    # Build services
    build_voice_api
    build_voice_processor

    # Show results
    inspect_images

    log "Multi-arch build completed successfully!"
    log "Images pushed to:"
    log "  - $REGISTRY/$PROJECT/voice-api:$VERSION"
    log "  - $REGISTRY/$PROJECT/voice-processor:$VERSION"

    cleanup
}

# Handle script arguments
case "${1:-all}" in
    "voice-api")
        check_buildx
        setup_builder
        build_voice_api
        cleanup
        ;;
    "voice-processor")
        check_buildx
        setup_builder
        build_voice_processor
        cleanup
        ;;
    "all"|"")
        main
        ;;
    "inspect")
        inspect_images
        ;;
    *)
        echo "Usage: $0 [voice-api|voice-processor|all|inspect]"
        echo "  voice-api      - Build only voice-api service"
        echo "  voice-processor - Build only voice-processor service"
        echo "  all            - Build all services (default)"
        echo "  inspect        - Inspect existing images"
        exit 1
        ;;
esac
#!/bin/bash

# Deployment script for malepatip's Voice AI Infrastructure
# Pre-configured with correct GitHub Container Registry settings

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Configuration
REGISTRY="ghcr.io/malepatip"
NAMESPACE="voice-ai"

# Check GitHub token
check_github_token() {
    if [ -z "${GITHUB_TOKEN:-}" ]; then
        error "GITHUB_TOKEN environment variable not set. Please run: export GITHUB_TOKEN=your_token"
    fi
    log "GitHub token found"
}

# Login to GitHub Container Registry
github_login() {
    log "Logging into GitHub Container Registry..."
    echo $GITHUB_TOKEN | docker login ghcr.io -u malepatip --password-stdin
    if [ $? -eq 0 ]; then
        log "✅ Successfully logged into GitHub Container Registry"
    else
        error "❌ Failed to login to GitHub Container Registry"
    fi
}

# Build and push images
build_images() {
    log "Building multi-arch images for ARM64 and AMD64..."

    info "Registry: $REGISTRY"
    info "Expected images:"
    info "  - ghcr.io/malepatip/voice-ai/voice-api:latest"
    info "  - ghcr.io/malepatip/voice-ai/voice-processor:latest"

    # Run the build script
    ./infrastructure/docker/docker-buildx.sh all

    if [ $? -eq 0 ]; then
        log "✅ Images built and pushed successfully"
    else
        error "❌ Failed to build images"
    fi
}

# Deploy voice API service
deploy_voice_api() {
    log "Deploying voice-api service to ARM64 nodes..."

    # Apply the deployment
    kubectl apply -f infrastructure/k8s/voice-api-deployment.yaml

    if [ $? -eq 0 ]; then
        log "✅ Voice API deployment applied"
    else
        error "❌ Failed to deploy voice API"
    fi

    # Wait for deployment
    log "Waiting for voice-api deployment to be ready..."
    kubectl wait --for=condition=available deployment/voice-api -n $NAMESPACE --timeout=300s

    if [ $? -eq 0 ]; then
        log "✅ Voice API deployment is ready"
    else
        warn "⚠️ Voice API deployment may still be starting"
    fi
}

# Verify deployment
verify_deployment() {
    log "Verifying deployment..."

    # Check pods
    info "Checking pods:"
    kubectl get pods -n $NAMESPACE -o custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName,IMAGE:.spec.containers[0].image

    # Check services
    info "Checking services:"
    kubectl get services -n $NAMESPACE

    # Check ARM64 placement
    info "Verifying ARM64 node placement:"
    local voice_api_pods=$(kubectl get pods -n $NAMESPACE -l app=voice-api --no-headers | wc -l)
    if [ "$voice_api_pods" -gt 0 ]; then
        kubectl get pods -n $NAMESPACE -l app=voice-api -o jsonpath='{.items[*].spec.nodeName}' | grep -q "ubuntu-4gb-fsn1-2" && \
            log "✅ Voice API pods are running on ARM64 node" || \
            warn "⚠️ Voice API pods may not be on expected ARM64 node"
    else
        warn "⚠️ No voice-api pods found"
    fi
}

# Show status
show_status() {
    log "🎯 Final Status Summary"
    echo "========================"

    echo "📦 Images:"
    echo "  - ghcr.io/malepatip/voice-ai/voice-api:latest"
    echo "  - ghcr.io/malepatip/voice-ai/voice-processor:latest"

    echo -e "\n🏗️ Infrastructure:"
    kubectl get all -n $NAMESPACE

    echo -e "\n🎉 Next steps:"
    echo "  1. Check GitHub Packages: https://github.com/malepatip?tab=packages"
    echo "  2. Monitor pods: kubectl get pods -n voice-ai -w"
    echo "  3. Check logs: kubectl logs -l app=voice-api -n voice-ai"
    echo "  4. Test Hume AI integration when ready"
}

# Main execution
main() {
    case "${1:-all}" in
        "login")
            check_github_token
            github_login
            ;;
        "build")
            check_github_token
            github_login
            build_images
            ;;
        "deploy")
            deploy_voice_api
            verify_deployment
            ;;
        "status")
            show_status
            ;;
        "all")
            check_github_token
            github_login
            build_images
            deploy_voice_api
            verify_deployment
            show_status
            log "🎉 Complete deployment finished!"
            ;;
        *)
            echo "Usage: $0 [login|build|deploy|status|all]"
            echo ""
            echo "Commands:"
            echo "  login   - Login to GitHub Container Registry"
            echo "  build   - Build and push Docker images"
            echo "  deploy  - Deploy voice-api service"
            echo "  status  - Show deployment status"
            echo "  all     - Run complete deployment (default)"
            echo ""
            echo "Prerequisites:"
            echo "  export GITHUB_TOKEN=your_github_token"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
#!/bin/bash

# Deployment script for Voice AI ARM Kubernetes infrastructure
# Optimized for Hetzner VPS deployment

set -euo pipefail

# Configuration
NAMESPACE=${NAMESPACE:-"voice-ai"}
KUBECONFIG=${KUBECONFIG:-"$HOME/.kube/config"}
TERRAFORM_DIR="infrastructure/terraform"
K8S_DIR="infrastructure/k8s"
DOCKER_DIR="infrastructure/docker"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        error "kubectl is not installed or not in PATH"
    fi

    # Check terraform
    if ! command -v terraform &> /dev/null; then
        error "terraform is not installed or not in PATH"
    fi

    # Check docker
    if ! command -v docker &> /dev/null; then
        error "docker is not installed or not in PATH"
    fi

    # Check kubeconfig
    if ! kubectl cluster-info &> /dev/null; then
        error "Cannot connect to Kubernetes cluster. Check your kubeconfig at: $KUBECONFIG"
    fi

    # Check ARM nodes
    local arm_nodes=$(kubectl get nodes -l kubernetes.io/arch=arm64 --no-headers 2>/dev/null | wc -l)
    if [ "$arm_nodes" -eq 0 ]; then
        warn "No ARM64 nodes detected in the cluster"
        info "If you have ARM nodes, make sure they are labeled with kubernetes.io/arch=arm64"
    else
        log "Found $arm_nodes ARM64 nodes in the cluster"
    fi

    log "Prerequisites check completed"
}

# Deploy Terraform infrastructure
deploy_terraform() {
    log "Deploying Terraform infrastructure..."

    cd "$TERRAFORM_DIR"

    # Initialize Terraform
    terraform init

    # Validate configuration
    terraform validate

    # Plan deployment
    log "Creating Terraform plan..."
    terraform plan -out=voice-ai.tfplan

    # Apply if plan looks good
    log "Applying Terraform configuration..."
    terraform apply voice-ai.tfplan

    # Output results
    terraform output

    cd - > /dev/null
    log "Terraform deployment completed"
}

# Deploy Kubernetes manifests
deploy_kubernetes() {
    log "Deploying Kubernetes manifests..."

    # Apply namespace and RBAC first
    log "Creating namespace and basic resources..."
    kubectl apply -f "$K8S_DIR/namespace.yaml"

    # Apply ARM optimizations
    log "Applying ARM optimizations..."
    kubectl apply -f "$K8S_DIR/arm-node-affinity.yaml"
    kubectl apply -f "$K8S_DIR/arm-optimizations.yaml"

    # Apply cost optimization configs
    log "Applying cost optimization configurations..."
    kubectl apply -f "$K8S_DIR/cost-optimization.yaml"

    # Wait for namespace to be ready
    kubectl wait --for=condition=Active namespace/$NAMESPACE --timeout=60s

    # Apply voice API deployment
    log "Deploying voice API service..."
    kubectl apply -f "$K8S_DIR/voice-api-deployment.yaml"

    # Wait for deployments to be ready
    log "Waiting for deployments to be ready..."
    kubectl wait --for=condition=available deployment/voice-api -n $NAMESPACE --timeout=300s

    log "Kubernetes deployment completed"
}

# Build and push Docker images
build_images() {
    log "Building multi-arch Docker images..."

    # Make sure the build script is executable
    chmod +x "$DOCKER_DIR/docker-buildx.sh"

    # Build images
    "$DOCKER_DIR/docker-buildx.sh" all

    log "Docker images built and pushed"
}

# Verify deployment
verify_deployment() {
    log "Verifying deployment..."

    # Check namespace
    info "Checking namespace:"
    kubectl get namespace $NAMESPACE

    # Check deployments
    info "Checking deployments:"
    kubectl get deployments -n $NAMESPACE

    # Check pods
    info "Checking pods:"
    kubectl get pods -n $NAMESPACE -o wide

    # Check services
    info "Checking services:"
    kubectl get services -n $NAMESPACE

    # Check resource quotas
    info "Checking resource quotas:"
    kubectl describe resourcequota -n $NAMESPACE

    # Check node placement
    info "Checking pod node placement:"
    kubectl get pods -n $NAMESPACE -o custom-columns=NAME:.metadata.name,NODE:.spec.nodeName,ARCH:.spec.nodeSelector

    # Test connectivity
    log "Testing service connectivity..."
    local api_pod=$(kubectl get pods -n $NAMESPACE -l app=voice-api -o jsonpath='{.items[0].metadata.name}')
    if [ ! -z "$api_pod" ]; then
        kubectl port-forward -n $NAMESPACE pod/$api_pod 8080:3000 &
        local pf_pid=$!
        sleep 3

        if curl -s -f http://localhost:8080/health > /dev/null; then
            log "Health check passed"
        else
            warn "Health check failed"
        fi

        kill $pf_pid 2>/dev/null || true
    fi

    log "Deployment verification completed"
}

# Clean up deployment
cleanup() {
    log "Cleaning up deployment..."

    # Remove Kubernetes resources
    kubectl delete -f "$K8S_DIR/" --ignore-not-found=true

    # Remove namespace (this will clean up everything)
    kubectl delete namespace $NAMESPACE --ignore-not-found=true

    # Destroy Terraform resources
    cd "$TERRAFORM_DIR"
    terraform destroy -auto-approve
    cd - > /dev/null

    log "Cleanup completed"
}

# Show deployment status
status() {
    log "Voice AI Deployment Status"
    echo "=========================="

    # Namespace status
    echo "Namespace:"
    kubectl get namespace $NAMESPACE 2>/dev/null || echo "  Not found"

    # Resource status
    echo -e "\nResources in $NAMESPACE:"
    kubectl get all -n $NAMESPACE 2>/dev/null || echo "  No resources found"

    # Resource usage
    echo -e "\nResource Usage:"
    kubectl top pods -n $NAMESPACE 2>/dev/null || echo "  Metrics not available"

    # ARM node status
    echo -e "\nARM64 Nodes:"
    kubectl get nodes -l kubernetes.io/arch=arm64 2>/dev/null || echo "  No ARM64 nodes found"
}

# Show usage
usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy      - Full deployment (terraform + kubernetes + images)"
    echo "  terraform   - Deploy only Terraform resources"
    echo "  kubernetes  - Deploy only Kubernetes manifests"
    echo "  images      - Build and push Docker images"
    echo "  verify      - Verify current deployment"
    echo "  status      - Show deployment status"
    echo "  cleanup     - Remove all resources"
    echo "  help        - Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE   - Kubernetes namespace (default: voice-ai)"
    echo "  KUBECONFIG  - Path to kubeconfig file (default: ~/.kube/config)"
    echo ""
    echo "Examples:"
    echo "  $0 deploy                    # Full deployment"
    echo "  NAMESPACE=test $0 kubernetes # Deploy to test namespace"
    echo "  $0 status                    # Check deployment status"
}

# Main execution
main() {
    case "${1:-deploy}" in
        "deploy")
            check_prerequisites
            deploy_terraform
            build_images
            deploy_kubernetes
            verify_deployment
            log "Full deployment completed successfully!"
            ;;
        "terraform")
            check_prerequisites
            deploy_terraform
            ;;
        "kubernetes")
            check_prerequisites
            deploy_kubernetes
            ;;
        "images")
            check_prerequisites
            build_images
            ;;
        "verify")
            verify_deployment
            ;;
        "status")
            status
            ;;
        "cleanup")
            cleanup
            ;;
        "help"|"-h"|"--help")
            usage
            ;;
        *)
            error "Unknown command: $1"
            usage
            ;;
    esac
}

# Execute main function with all arguments
main "$@"
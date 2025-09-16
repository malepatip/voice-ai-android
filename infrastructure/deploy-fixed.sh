#!/bin/bash

# Fixed deployment script for Voice AI ARM Kubernetes infrastructure
# Addresses all identified deployment issues

set -euo pipefail

# Configuration
NAMESPACE=${NAMESPACE:-"voice-ai"}
KUBECONFIG=${KUBECONFIG:-"$HOME/.kube/config"}
TERRAFORM_DIR="infrastructure/terraform"
K8S_DIR="infrastructure/k8s"

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

# Deploy Terraform infrastructure with fixes
deploy_terraform_fixed() {
    log "Deploying fixed Terraform infrastructure..."

    cd "$TERRAFORM_DIR"

    # Initialize Terraform
    terraform init

    # Validate configuration
    terraform validate

    # Plan deployment
    log "Creating Terraform plan..."
    terraform plan -out=voice-ai-fixed.tfplan

    # Apply with timeout handling
    log "Applying Terraform configuration (with PVC timeout fixes)..."
    terraform apply -auto-approve voice-ai-fixed.tfplan

    # Output results
    terraform output

    cd - > /dev/null
    log "Terraform deployment completed"
}

# Deploy Kubernetes manifests with error handling
deploy_kubernetes_fixed() {
    log "Deploying fixed Kubernetes manifests..."

    # Clean up any previous failed deployments
    log "Cleaning up previous failed deployments..."
    kubectl delete deployment voice-api -n $NAMESPACE --ignore-not-found=true
    kubectl delete deployment arm-template -n $NAMESPACE --ignore-not-found=true

    # Apply namespace and basic resources first
    log "Creating namespace and basic resources..."
    kubectl apply -f "$K8S_DIR/namespace.yaml" || warn "Namespace may already exist"

    # Apply fixed ARM configurations
    log "Applying fixed ARM configurations..."
    kubectl apply -f "$K8S_DIR/arm-node-affinity-fixed.yaml"
    kubectl apply -f "$K8S_DIR/arm-optimizations.yaml"

    # Apply basic cost optimization without problematic CRDs
    log "Applying cost optimization configurations (without CRDs)..."
    kubectl apply -f "$K8S_DIR/cost-optimization-basic.yaml"

    # Wait for namespace to be ready
    kubectl wait --for=condition=Active namespace/$NAMESPACE --timeout=60s

    # Deploy placeholder voice API (with real image)
    log "Deploying placeholder voice API service..."
    kubectl apply -f "$K8S_DIR/voice-api-placeholder.yaml"

    # Wait for deployment to be ready
    log "Waiting for placeholder deployment to be ready..."
    kubectl wait --for=condition=available deployment/voice-api-placeholder -n $NAMESPACE --timeout=300s

    log "Fixed Kubernetes deployment completed"
}

# Verify deployment with detailed checks
verify_deployment_detailed() {
    log "Performing detailed deployment verification..."

    # Check namespace
    info "Checking namespace:"
    kubectl get namespace $NAMESPACE

    # Check all resources
    info "Checking all resources:"
    kubectl get all -n $NAMESPACE

    # Check configmaps and secrets
    info "Checking configurations:"
    kubectl get configmaps,secrets,pvc -n $NAMESPACE

    # Check resource quotas and limits
    info "Checking resource management:"
    kubectl describe resourcequota -n $NAMESPACE
    kubectl describe limitrange -n $NAMESPACE

    # Check node placement and ARM optimization
    info "Checking ARM node placement:"
    kubectl get pods -n $NAMESPACE -o custom-columns=NAME:.metadata.name,NODE:.spec.nodeName,IMAGE:.spec.containers[0].image,STATUS:.status.phase

    # Check events for any issues
    info "Checking recent events:"
    kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -10

    # Test placeholder service connectivity
    log "Testing placeholder service connectivity..."
    local placeholder_pod=$(kubectl get pods -n $NAMESPACE -l app=voice-api,version=placeholder -o jsonpath='{.items[0].metadata.name}')
    if [ ! -z "$placeholder_pod" ] && [ "$placeholder_pod" != "null" ]; then
        info "Testing connectivity to placeholder pod: $placeholder_pod"
        kubectl port-forward -n $NAMESPACE pod/$placeholder_pod 8081:80 &
        local pf_pid=$!
        sleep 3

        if curl -s -f http://localhost:8081/ > /dev/null; then
            log "✅ Placeholder service connectivity test passed"
        else
            warn "❌ Placeholder service connectivity test failed"
        fi

        kill $pf_pid 2>/dev/null || true
    else
        warn "No placeholder pod found for connectivity testing"
    fi

    log "Detailed verification completed"
}

# Show deployment summary
deployment_summary() {
    log "🎯 Deployment Summary"
    echo "===================="

    # Overall status
    echo "📊 Resource Overview:"
    kubectl get all -n $NAMESPACE --no-headers | wc -l | xargs echo "  Total resources:"

    # ARM optimization status
    echo -e "\n🔧 ARM Optimizations:"
    if kubectl get configmap arm-performance-config -n $NAMESPACE >/dev/null 2>&1; then
        echo "  ✅ ARM performance configurations applied"
    else
        echo "  ❌ ARM performance configurations missing"
    fi

    # Storage status
    echo -e "\n💾 Storage:"
    kubectl get pvc -n $NAMESPACE --no-headers | while read name status volume capacity access storage_class age; do
        echo "  📁 $name: $status ($capacity on $storage_class)"
    done

    # Cost optimization status
    echo -e "\n💰 Cost Optimization:"
    if kubectl get cronjob -n $NAMESPACE >/dev/null 2>&1; then
        local cleanup_jobs=$(kubectl get cronjob -n $NAMESPACE --no-headers | wc -l)
        echo "  ✅ $cleanup_jobs cleanup jobs configured"
    else
        echo "  ❌ No cleanup jobs found"
    fi

    # Next steps
    echo -e "\n🚀 Next Steps:"
    echo "  1. Build and push Docker images using: ./infrastructure/docker/docker-buildx.sh"
    echo "  2. Update deployments to use custom images"
    echo "  3. Test Hume AI integration"
    echo "  4. Monitor resource usage and costs"
}

# Main execution with error handling
main() {
    case "${1:-deploy}" in
        "deploy")
            check_prerequisites
            deploy_terraform_fixed
            deploy_kubernetes_fixed
            verify_deployment_detailed
            deployment_summary
            log "🎉 Fixed deployment completed successfully!"
            ;;
        "terraform")
            check_prerequisites
            deploy_terraform_fixed
            ;;
        "kubernetes")
            check_prerequisites
            deploy_kubernetes_fixed
            ;;
        "verify")
            verify_deployment_detailed
            ;;
        "summary")
            deployment_summary
            ;;
        "clean")
            log "Cleaning up previous deployments..."
            kubectl delete namespace $NAMESPACE --ignore-not-found=true
            cd "$TERRAFORM_DIR" && terraform destroy -auto-approve && cd - > /dev/null
            log "Cleanup completed"
            ;;
        *)
            echo "Usage: $0 [deploy|terraform|kubernetes|verify|summary|clean]"
            exit 1
            ;;
    esac
}

# Execute main function
main "$@"
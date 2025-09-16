# Voice AI ARM Kubernetes Infrastructure

This directory contains the complete infrastructure setup for deploying your Voice AI application on ARM-based Kubernetes clusters, specifically optimized for Hetzner VPS deployment.

## 🏗️ Architecture Overview

- **Target Platform**: ARM64 Kubernetes cluster on Hetzner VPS
- **Optimization Focus**: Cost efficiency and ARM-specific performance
- **Services**: Voice API, Voice Processor, monitoring, and cost controls
- **Resource Management**: Conservative allocation with burst capabilities

## 📁 Directory Structure

```
infrastructure/
├── terraform/              # Terraform configurations
│   ├── main.tf             # Main Terraform resources
│   ├── variables.tf        # Input variables
│   ├── outputs.tf          # Output values
│   └── terraform.tfvars.example
├── k8s/                    # Kubernetes manifests
│   ├── namespace.yaml      # Namespace and resource quotas
│   ├── arm-node-affinity.yaml    # ARM64 node selection
│   ├── arm-optimizations.yaml    # ARM performance configs
│   ├── voice-api-deployment.yaml # Main API service
│   └── cost-optimization.yaml    # Cost control measures
├── docker/                 # Docker configurations
│   ├── Dockerfile.voice-api      # API service image
│   ├── Dockerfile.voice-processor # Voice processing image
│   └── docker-buildx.sh          # Multi-arch build script
├── deploy.sh              # Main deployment script
└── README.md              # This file
```

## 🚀 Quick Start

### Prerequisites

1. **Hetzner VPS** with ARM64 Kubernetes cluster
2. **kubectl** configured for your cluster
3. **Terraform** v1.0+
4. **Docker** with buildx support
5. **Hume AI API key**

### 1. Configure Terraform Variables

```bash
cp infrastructure/terraform/terraform.tfvars.example infrastructure/terraform/terraform.tfvars
```

Edit the values:
```hcl
# Your Hetzner cluster configuration
kubeconfig_path = "~/.kube/config"
kube_context    = "hetzner-cluster"

# Resource limits for cost control
cpu_quota    = "2000m"  # 2 CPU cores
memory_quota = "4Gi"    # 4GB RAM

# Your Hume AI API key
hume_api_key = "your-hume-api-key-here"
```

### 2. Deploy Everything

```bash
# Full deployment
./infrastructure/deploy.sh deploy

# Or step by step
./infrastructure/deploy.sh terraform   # Deploy Terraform resources
./infrastructure/deploy.sh images      # Build multi-arch images
./infrastructure/deploy.sh kubernetes  # Deploy K8s manifests
```

### 3. Verify Deployment

```bash
./infrastructure/deploy.sh verify
./infrastructure/deploy.sh status
```

## 🎯 ARM64 Optimizations

### CPU Optimizations
- **NEON SIMD**: Enabled for audio processing
- **CPU Affinity**: ARM64-specific scheduling
- **Performance Governor**: Optimized for ARM cores

### Memory Optimizations
- **Cache-aware Buffers**: 64-byte cache line alignment
- **Efficient Allocation**: Conservative requests with burst limits
- **Memory Compression**: Enabled for cost efficiency

### Audio Processing
- **Buffer Sizes**: 4096 bytes (ARM cache-optimized)
- **FFT Size**: 1024 (ARM-efficient)
- **Threading**: 4 worker threads for typical ARM cores

## 💰 Cost Optimization Features

### Resource Management
- **Resource Quotas**: Prevent runaway costs
- **Vertical Pod Autoscaler**: Right-size containers
- **Horizontal Pod Autoscaler**: Scale based on demand

### Monitoring & Alerts
- **CPU Usage Alerts**: >80% for 5 minutes
- **Memory Alerts**: >85% of limits
- **Hume AI Usage**: Track free tier consumption

### Automated Cleanup
- **Audio Cache**: Cleaned every 6 hours
- **Log Rotation**: Daily with compression
- **Storage Limits**: 10Gi default allocation

## 🔧 Configuration Details

### Node Selection
The deployment automatically selects ARM64 nodes:
```yaml
nodeSelector:
  kubernetes.io/arch: arm64
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/arch
          operator: In
          values: ["arm64"]
```

### Resource Limits
Conservative allocation for cost control:
```yaml
resources:
  requests:
    cpu: "100m"      # 0.1 CPU cores
    memory: "128Mi"  # 128MB RAM
  limits:
    cpu: "500m"      # 0.5 CPU cores burst
    memory: "512Mi"  # 512MB RAM limit
```

### Hume AI Integration
Optimized for free tier usage:
- 10-minute monthly limit tracking
- Rate limiting: 10 requests/minute
- 16kHz mono audio format
- WebSocket compression enabled

## 📊 Monitoring

### Metrics Endpoints
- **Prometheus**: `/metrics` on port 9090
- **Health Check**: `/health` on main port
- **Readiness**: `/ready` on main port

### Key Metrics
- ARM CPU utilization and temperature
- Memory usage and compression ratios
- Audio processing latency
- Hume AI API usage tracking

## 🛠️ Troubleshooting

### Common Issues

1. **No ARM64 nodes found**
   ```bash
   kubectl get nodes -l kubernetes.io/arch=arm64
   kubectl label node <node-name> kubernetes.io/arch=arm64
   ```

2. **Resource quota exceeded**
   ```bash
   kubectl describe resourcequota -n voice-ai
   # Adjust limits in terraform/variables.tf
   ```

3. **Image pull failures**
   ```bash
   # Rebuild images for ARM64
   ./infrastructure/docker/docker-buildx.sh all
   ```

4. **High latency**
   ```bash
   # Check ARM optimizations
   kubectl get configmap arm-performance-config -n voice-ai -o yaml
   ```

### Debugging Commands

```bash
# Check pod placement
kubectl get pods -n voice-ai -o wide

# View ARM optimization status
kubectl logs -n voice-ai -l app=voice-api

# Monitor resource usage
kubectl top pods -n voice-ai

# Check Hume AI integration
kubectl port-forward -n voice-ai svc/voice-api-service 8080:80
curl http://localhost:8080/health
```

## 🔄 Updates and Maintenance

### Update Images
```bash
./infrastructure/docker/docker-buildx.sh all
kubectl rollout restart deployment/voice-api -n voice-ai
```

### Scale Resources
```bash
# Edit terraform/terraform.tfvars
vim infrastructure/terraform/terraform.tfvars

# Apply changes
./infrastructure/deploy.sh terraform
```

### Clean Up
```bash
# Remove everything
./infrastructure/deploy.sh cleanup
```

## 📈 Performance Tuning

### For Higher Workloads
Adjust `terraform.tfvars`:
```hcl
cpu_quota    = "4000m"  # 4 CPU cores
memory_quota = "8Gi"    # 8GB RAM
```

### For Lower Costs
```hcl
cpu_quota    = "1000m"  # 1 CPU core
memory_quota = "2Gi"    # 2GB RAM
```

## 🔐 Security

- Non-root containers with security contexts
- Network policies for traffic isolation
- Secret management via Kubernetes secrets
- Read-only root filesystems where possible

## 📚 Additional Resources

- [Hetzner Kubernetes Documentation](https://docs.hetzner.com/cloud/kubernetes/)
- [ARM Kubernetes Best Practices](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
- [Hume AI API Documentation](https://dev.hume.ai/docs/empathic-voice-interface-evi/overview)

---

For questions or issues, check the troubleshooting section or create an issue in the project repository.
# 🚀 Quick Setup Guide for Voice AI Android

## 🔧 Prerequisites
- Docker with buildx support
- kubectl configured for your Hetzner cluster
- Terraform installed
- GitHub account

## ⚡ Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/malepatip/voice-ai-android.git
cd voice-ai-android
```

### 2. Set Up Secrets (LOCALLY ONLY)
```bash
# Copy terraform template and add your secrets
cp infrastructure/terraform/terraform.tfvars.secure-example infrastructure/terraform/terraform.tfvars

# Edit with your real values (NEVER COMMIT THIS FILE)
# - Get Hume AI credentials from: https://dev.hume.ai
# - Generate JWT secret with: openssl rand -hex 32
vim infrastructure/terraform/terraform.tfvars
```

### 3. Create GitHub Token
1. Go to: https://github.com/settings/tokens
2. Create token with `write:packages`, `read:packages` permissions
3. Export token:
```bash
export GITHUB_TOKEN=ghp_your_token_here
```

### 4. Deploy Everything
```bash
# One command deployment
./deploy-malepatip.sh all
```

This will:
- ✅ Login to GitHub Container Registry
- ✅ Build ARM64/AMD64 Docker images
- ✅ Push to ghcr.io/malepatip/voice-ai/*
- ✅ Deploy to your Hetzner ARM cluster
- ✅ Verify ARM64 node placement

## 📋 Manual Steps

### Deploy Infrastructure Only:
```bash
# Deploy Terraform resources
./deploy-malepatip.sh deploy

# Check status
kubectl get all -n voice-ai
```

### Build Images Only:
```bash
# Build and push Docker images
./deploy-malepatip.sh build
```

### Check Status:
```bash
./deploy-malepatip.sh status
```

## 🎯 Expected Results

After deployment:
- **Namespace**: `voice-ai` with resource quotas
- **Images**: Available at ghcr.io/malepatip/voice-ai/*
- **Pods**: Running on ARM64 nodes
- **Storage**: 10Gi PVC for audio processing
- **Secrets**: Hume AI credentials stored securely

## 🔍 Troubleshooting

### Image Build Fails:
```bash
# Setup buildx
docker buildx create --use
```

### Pod Won't Start:
```bash
# Check events
kubectl describe pod -l app=voice-api -n voice-ai

# Check logs
kubectl logs -l app=voice-api -n voice-ai
```

### ARM64 Not Working:
```bash
# Check node labels
kubectl get nodes --show-labels | grep arch

# Verify pod placement
kubectl get pods -n voice-ai -o wide
```

## 📚 Documentation

- [Complete Setup Guide](GITHUB_SETUP.md)
- [Security Guidelines](SECURITY.md)
- [Infrastructure Details](infrastructure/README.md)

## 🆘 Quick Commands

```bash
# Check cluster connection
kubectl cluster-info

# Watch deployment
kubectl get pods -n voice-ai -w

# Port forward for testing
kubectl port-forward -n voice-ai svc/voice-api-service 8080:80

# Check resource usage
kubectl top pods -n voice-ai
```
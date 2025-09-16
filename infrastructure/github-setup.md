# GitHub Container Registry Setup for Voice AI

## 🐙 GitHub Setup Steps

### 1. Create GitHub Repository
```bash
# In your GitHub account, create a new repository
# Repository name: voice-ai-android (or your preferred name)
```

### 2. Update Registry Configuration
Replace `your-github-username` with your actual GitHub username in:

**File: `infrastructure/docker/docker-buildx.sh`**
```bash
REGISTRY="ghcr.io/YOUR_GITHUB_USERNAME"
```

**File: `infrastructure/k8s/voice-api-deployment.yaml`**
```yaml
image: ghcr.io/YOUR_GITHUB_USERNAME/voice-ai/voice-api:latest
```

### 3. Create GitHub Personal Access Token
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Create new token with these permissions:
   - `write:packages` (to push images)
   - `read:packages` (to pull images)
   - `repo` (for private repos)

### 4. Login to GitHub Container Registry
```bash
# Export your token
export GITHUB_TOKEN=your_personal_access_token

# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

### 5. Build and Push Images
```bash
# Update the registry in the script first
export REGISTRY="ghcr.io/YOUR_GITHUB_USERNAME"

# Build and push multi-arch images
./infrastructure/docker/docker-buildx.sh all
```

### 6. Configure Kubernetes Image Pull Secrets (if private repo)
```bash
# Create image pull secret
kubectl create secret docker-registry github-registry \
  --docker-server=ghcr.io \
  --docker-username=YOUR_GITHUB_USERNAME \
  --docker-password=$GITHUB_TOKEN \
  --namespace=voice-ai

# Update deployment to use the secret (if needed)
```

## 🚀 Deploy Voice API Service

Once images are built and pushed:

```bash
# Deploy the actual voice-api service
kubectl apply -f infrastructure/k8s/voice-api-deployment.yaml

# Check deployment status
kubectl get pods -n voice-ai -w
```

## 🔍 Verify ARM64 Deployment

```bash
# Check that pods are running on ARM64 nodes
kubectl get pods -n voice-ai -o custom-columns=NAME:.metadata.name,NODE:.spec.nodeName,ARCH:.spec.nodeSelector,IMAGE:.spec.containers[0].image

# Check node selectors are working
kubectl describe pod -l app=voice-api -n voice-ai | grep -A5 "Node-Selectors"
```

## 📋 Example Commands with Your Username

Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username:

```bash
# Build command
REGISTRY="ghcr.io/YOUR_GITHUB_USERNAME" ./infrastructure/docker/docker-buildx.sh

# Expected image names:
# ghcr.io/YOUR_GITHUB_USERNAME/voice-ai/voice-api:latest
# ghcr.io/YOUR_GITHUB_USERNAME/voice-ai/voice-processor:latest
```
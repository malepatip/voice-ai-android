# 🐙 GitHub Setup Guide for malepatip

## Step 1: Create GitHub Repository

1. **Go to GitHub**: https://github.com/malepatip
2. **Click "New Repository"**
3. **Repository Settings**:
   - Repository name: `voice-ai-android`
   - Description: `Voice AI Android Application with ARM Kubernetes Backend`
   - Visibility: `Public` (recommended) or `Private`
   - Initialize: ✅ Add a README file
   - Add .gitignore: `Android`
   - License: `MIT` (optional)

4. **Click "Create repository"**

## Step 2: Clone and Set Up Local Repository

```bash
# Clone the repository
git clone https://github.com/malepatip/voice-ai-android.git

# Navigate to the repository
cd voice-ai-android

# Copy your existing files to the repository
# (You'll need to copy the infrastructure/ folder and other files)
```

## Step 3: Create GitHub Personal Access Token

1. **Go to GitHub Settings**: https://github.com/settings/tokens
2. **Click "Generate new token (classic)"**
3. **Configure token**:
   - Note: `Voice AI Docker Registry Access`
   - Expiration: `90 days` (or your preference)
   - Select scopes:
     - ✅ `write:packages` (upload container images)
     - ✅ `read:packages` (download container images)
     - ✅ `repo` (if using private repository)

4. **Click "Generate token"**
5. **⚠️ IMPORTANT**: Copy the token immediately - you won't see it again!

## Step 4: Configure Docker Registry Access

```bash
# Set your token as environment variable
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u malepatip --password-stdin
```

Expected output:
```
Login Succeeded
```

## Step 5: Build and Push Images

```bash
# Navigate to your project directory
cd /path/to/voice-ai-android

# Build and push multi-arch images
./infrastructure/docker/docker-buildx.sh all
```

Expected images to be created:
- `ghcr.io/malepatip/voice-ai/voice-api:latest`
- `ghcr.io/malepatip/voice-ai/voice-processor:latest`

## Step 6: Verify Images on GitHub

1. **Go to your GitHub profile**: https://github.com/malepatip
2. **Click "Packages" tab**
3. **You should see**:
   - `voice-ai/voice-api`
   - `voice-ai/voice-processor`

## Step 7: Deploy Voice API Service

```bash
# Deploy the actual voice-api service with your images
kubectl apply -f infrastructure/k8s/voice-api-deployment.yaml

# Watch the deployment
kubectl get pods -n voice-ai -w
```

## Step 8: Verify ARM64 Deployment

```bash
# Check pod placement on ARM64 nodes
kubectl get pods -n voice-ai -o custom-columns=NAME:.metadata.name,NODE:.spec.nodeName,IMAGE:.spec.containers[0].image

# Should show:
# NAME                        NODE                IMAGE
# voice-api-xxxxx-xxxxx      ubuntu-4gb-fsn1-2   ghcr.io/malepatip/voice-ai/voice-api:latest
```

## 🔧 Troubleshooting

### If Docker Build Fails:
```bash
# Check Docker buildx
docker buildx version

# Setup buildx if needed
docker buildx create --use --name voice-ai-builder
```

### If Image Push Fails:
```bash
# Re-login to registry
echo $GITHUB_TOKEN | docker login ghcr.io -u malepatip --password-stdin

# Check token permissions on GitHub
```

### If Pod Can't Pull Image:
```bash
# Create image pull secret for private repos
kubectl create secret docker-registry github-registry \
  --docker-server=ghcr.io \
  --docker-username=malepatip \
  --docker-password=$GITHUB_TOKEN \
  --namespace=voice-ai
```

## 📋 Quick Commands Summary

```bash
# 1. Login to registry
echo $GITHUB_TOKEN | docker login ghcr.io -u malepatip --password-stdin

# 2. Build images
./infrastructure/docker/docker-buildx.sh all

# 3. Deploy service
kubectl apply -f infrastructure/k8s/voice-api-deployment.yaml

# 4. Check status
kubectl get pods -n voice-ai
```

## 🎯 Expected Final Result

After completing these steps, you should have:
- ✅ GitHub repository with your voice AI code
- ✅ Multi-arch Docker images in GitHub Container Registry
- ✅ Voice API service running on ARM64 nodes in your Hetzner cluster
- ✅ Complete ARM-optimized infrastructure ready for Hume AI integration

## 🔗 Important URLs

- **Your Repository**: https://github.com/malepatip/voice-ai-android
- **Your Packages**: https://github.com/malepatip?tab=packages
- **Token Settings**: https://github.com/settings/tokens
- **GHCR Documentation**: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry
# Voice AI Android - SLM Deployment Guide

Complete guide for deploying the hybrid SLM architecture (Gemini Nano + Llama 3.2) on ARM Kubernetes.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Infrastructure Setup](#infrastructure-setup)
3. [Model Preparation](#model-preparation)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Android App Deployment](#android-app-deployment)
6. [Verification & Testing](#verification--testing)
7. [Monitoring & Maintenance](#monitoring--maintenance)

## Prerequisites

### Required Tools
- `kubectl` v1.23+
- `docker` with multi-arch support (buildx)
- `terraform` v1.0+ (for infrastructure provisioning)
- Android Studio (for app development)
- `git` for version control

### Cloud Resources
Choose one:
- **Oracle Cloud A1 (Recommended)**: 4 OCPU ARM cores + 24GB RAM (Free Tier)
- **Hetzner Cloud**: CAX11 or higher (€4.50+/month)
- **AWS Graviton**: t4g.xlarge or higher
- **Any ARM64 Kubernetes cluster**: Min 4 cores, 16GB RAM

### API Keys (Optional)
- OpenWeatherMap API key (for weather skill)
- Google AI Edge SDK access (for Gemini Nano)

## Infrastructure Setup

### Step 1: Provision ARM VPS

#### Option A: Oracle Cloud A1 (Free)
```bash
cd infrastructure/terraform

# Configure Oracle Cloud credentials
export TF_VAR_tenancy_ocid="ocid1.tenancy.oc1..."
export TF_VAR_user_ocid="ocid1.user.oc1..."
export TF_VAR_fingerprint="xx:xx:xx:..."
export TF_VAR_private_key_path="~/.oci/oci_api_key.pem"
export TF_VAR_region="us-ashburn-1"

# Initialize and apply
terraform init
terraform plan
terraform apply
```

#### Option B: Hetzner Cloud
```bash
cd infrastructure/terraform

# Set Hetzner API token
export HCLOUD_TOKEN="your_hetzner_token"

# Use Hetzner-specific configuration
terraform init -backend-config="backend-hetzner.tfvars"
terraform apply -var-file="hetzner.tfvars"
```

### Step 2: Deploy Kubernetes

```bash
# SSH into your ARM VPS
ssh -i ~/.ssh/your_key root@<VPS_IP>

# Install K3s (lightweight Kubernetes)
curl -sfL https://get.k3s.io | sh -s - \
  --disable traefik \
  --node-label kubernetes.io/arch=arm64

# Verify installation
sudo k3s kubectl get nodes
```

### Step 3: Configure kubectl Locally

```bash
# Copy K3s config from VPS
scp root@<VPS_IP>:/etc/rancher/k3s/k3s.yaml ~/.kube/config

# Update server address
sed -i 's/127.0.0.1/<VPS_IP>/g' ~/.kube/config

# Test connection
kubectl get nodes
```

## Model Preparation

### Step 1: Download Llama 3.2 Models

```bash
# Create models directory
mkdir -p ~/llama-models
cd ~/llama-models

# Download Llama 3.2 models (requires Meta approval)
# Option 1: Official Meta download
# Visit: https://www.llama.com/llama-downloads/
# Download: llama-3.2-1b-instruct and llama-3.2-3b-instruct

# Option 2: Use Hugging Face (if available)
# pip install huggingface-hub
# huggingface-cli download meta-llama/Llama-3.2-3B-Instruct
# huggingface-cli download meta-llama/Llama-3.2-1B-Instruct
```

### Step 2: Quantize Models for ARM

```bash
# Clone llama.cpp
git clone https://github.com/ggerganov/llama.cpp.git
cd llama.cpp

# Build llama.cpp
mkdir build && cd build
cmake ..
cmake --build . --config Release -j$(nproc)

# Convert to GGUF format
python3 ../convert.py ~/llama-models/llama-3.2-3b-instruct \
  --outfile ~/llama-models/llama-3.2-3b-instruct.gguf

python3 ../convert.py ~/llama-models/llama-3.2-1b-instruct \
  --outfile ~/llama-models/llama-3.2-1b-instruct.gguf

# Quantize to Q4_K_M (recommended for ARM)
./bin/quantize ~/llama-models/llama-3.2-3b-instruct.gguf \
  ~/llama-models/llama-3.2-3b-instruct-q4_k_m.gguf Q4_K_M

./bin/quantize ~/llama-models/llama-3.2-1b-instruct.gguf \
  ~/llama-models/llama-3.2-1b-instruct-q4_k_m.gguf Q4_K_M
```

**Model Sizes After Quantization:**
- Llama 3.2 3B Q4_K_M: ~2.0 GB
- Llama 3.2 1B Q4_K_M: ~800 MB

### Step 3: Upload Models to Kubernetes

```bash
# Create namespace
kubectl create namespace voice-ai

# Create PVC for models
kubectl apply -f infrastructure/k8s/llama-3-2-deployment.yaml

# Create temporary pod for uploading
kubectl run -n voice-ai model-loader --image=alpine:3.18 \
  --restart=Never -- sleep 3600

# Wait for pod to be ready
kubectl wait -n voice-ai --for=condition=Ready pod/model-loader --timeout=60s

# Copy models to PVC
kubectl cp ~/llama-models/llama-3.2-3b-instruct-q4_k_m.gguf \
  voice-ai/model-loader:/models/llama-3.2-3b-instruct-q4_k_m.gguf

kubectl cp ~/llama-models/llama-3.2-1b-instruct-q4_k_m.gguf \
  voice-ai/model-loader:/models/llama-3.2-1b-instruct-q4_k_m.gguf

# Verify files
kubectl exec -n voice-ai model-loader -- ls -lh /models/

# Delete loader pod
kubectl delete -n voice-ai pod model-loader
```

## Kubernetes Deployment

### Step 1: Create Namespace and Secrets

```bash
# Create namespace
kubectl apply -f infrastructure/k8s/namespace.yaml

# Create secrets
kubectl create secret generic voice-ai-secrets -n voice-ai \
  --from-literal=jwt-secret=$(openssl rand -hex 32)

kubectl create secret generic skills-secrets -n voice-ai \
  --from-literal=weather-api-key="YOUR_OPENWEATHERMAP_KEY" \
  --from-literal=search-api-key=""
```

### Step 2: Deploy Redis (for context management)

```bash
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: voice-ai
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      nodeSelector:
        kubernetes.io/arch: arm64
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: voice-ai
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
EOF
```

### Step 3: Deploy Llama 3.2 Models

```bash
# Deploy both models (3B and 1B)
kubectl apply -f infrastructure/k8s/llama-3-2-deployment.yaml

# Wait for deployments to be ready
kubectl wait -n voice-ai --for=condition=Available \
  deployment/llama-3b-inference --timeout=180s

kubectl wait -n voice-ai --for=condition=Available \
  deployment/llama-1b-inference --timeout=120s

# Verify pods are running
kubectl get pods -n voice-ai -l app=llama-inference
```

### Step 4: Deploy Skills Engine

```bash
# Build skills engine Docker image
cd infrastructure/docker
docker buildx build --platform linux/arm64 \
  -t ghcr.io/malepatip/voice-ai/skills-engine:latest \
  -f Dockerfile.skills-engine ../backend

# Push to registry
docker push ghcr.io/malepatip/voice-ai/skills-engine:latest

# Deploy
kubectl apply -f infrastructure/k8s/skills-engine-deployment.yaml

# Verify
kubectl get pods -n voice-ai -l app=skills-engine
```

### Step 5: Verify All Services

```bash
# Check all deployments
kubectl get all -n voice-ai

# Check logs
kubectl logs -n voice-ai -l app=llama-inference --tail=50
kubectl logs -n voice-ai -l app=skills-engine --tail=50

# Test Llama 3B endpoint
kubectl port-forward -n voice-ai svc/llama-3b-service 8080:8080 &
curl http://localhost:8080/health

# Test with generation
curl -X POST http://localhost:8080/completion \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Hello, how are you?", "max_tokens": 50}'
```

## Android App Deployment

### Step 1: Configure Backend Endpoint

```kotlin
// Update: app/src/main/kotlin/com/voiceai/config/ApiConfig.kt
object ApiConfig {
    // Use your VPS public IP or domain
    const val BACKEND_BASE_URL = "http://<VPS_IP>:80"
    const val LLAMA_3B_ENDPOINT = "http://<VPS_IP>:8080"
}
```

### Step 2: Add Gemini Nano Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    // Google AI Edge SDK for Gemini Nano
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.ai.edge:generativeai:0.1.0")  // Check latest version
}
```

### Step 3: Build and Install

```bash
# Build APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat -s VoiceAI:V
```

### Step 4: Download Gemini Nano (On-Device)

The app will prompt to download Gemini Nano on first launch (~2GB).
Ensure device is on WiFi for the download.

## Verification & Testing

### Test 1: On-Device Intent Classification

```kotlin
// Test in Android app
val text = "Set a timer for 5 minutes"
val intent = geminiNanoClient.classifyIntent(text)
// Should return: Intent(type=SET_TIMER, confidence>0.9, onDevice=true)
```

### Test 2: Backend Complex Query

```bash
# Port forward skills service
kubectl port-forward -n voice-ai svc/skills-engine-service 3000:80 &

# Test weather skill
curl -X POST http://localhost:3000/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "text": "What is the weather in San Francisco?",
    "userId": "test-user",
    "sessionId": "test-session"
  }'
```

### Test 3: Routing Logic

```kotlin
// Test routing decisions
val contexts = listOf(
    // Should route to on-device
    RoutingContext(Intent(SET_TIMER, 0.95f), networkAvailable=true),

    // Should route to backend
    RoutingContext(Intent(COMPLEX_QUERY, 0.8f), networkAvailable=true),

    // Should fallback to on-device (offline)
    RoutingContext(Intent(CONVERSATION, 0.7f), networkAvailable=false)
)

contexts.forEach { ctx ->
    val decision = router.route(ctx)
    println("Context: ${ctx.intent} -> Decision: $decision")
}
```

### Test 4: End-to-End Voice Pipeline

1. Record 3-second audio sample
2. Process through STT
3. Classify intent with Gemini Nano
4. Route to backend if needed
5. Generate response
6. Synthesize with TTS
7. Verify total latency < 800ms

## Monitoring & Maintenance

### Set Up Monitoring

```bash
# Deploy Prometheus (optional)
kubectl apply -f infrastructure/k8s/monitoring/

# Access Prometheus UI
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Visit: http://localhost:9090
```

### Key Metrics to Monitor

1. **Latency Metrics**:
   - P50, P95, P99 for each component
   - End-to-end voice-to-voice latency
   - Target: P95 < 800ms

2. **Router Metrics**:
   - Routing accuracy
   - On-device vs backend ratio
   - Fallback frequency

3. **Resource Utilization**:
   - CPU/Memory per pod
   - ARM-specific performance counters
   - Model inference throughput

4. **Quality Metrics**:
   - Intent classification accuracy
   - User satisfaction scores
   - Error rates by component

### Maintenance Tasks

#### Weekly:
- Review logs for errors
- Check HPA scaling behavior
- Monitor model performance metrics

#### Monthly:
- Update models if new versions available
- Clean old conversation data (30-day retention)
- Review and optimize routing logic based on evals

#### As Needed:
- Scale deployments based on load
- Update quantization if better methods available
- Fine-tune models on user data (with consent)

### Troubleshooting

#### Issue: Llama pods crashlooping
```bash
# Check logs
kubectl logs -n voice-ai <pod-name>

# Common causes:
# 1. Model file missing - verify PVC contents
# 2. Out of memory - increase resource limits
# 3. Invalid model format - re-quantize model
```

#### Issue: High latency on backend
```bash
# Check if scaling is working
kubectl get hpa -n voice-ai

# Manually scale if needed
kubectl scale deployment/llama-3b-inference -n voice-ai --replicas=4

# Check network latency
kubectl exec -n voice-ai <pod-name> -- ping llama-3b-service
```

#### Issue: Android app can't connect to backend
```bash
# Verify service is exposed
kubectl get svc -n voice-ai

# Check network connectivity
kubectl run -n voice-ai test-curl --image=curlimages/curl --rm -it -- \
  curl http://llama-3b-service:8080/health

# Update Android ApiConfig.kt with correct VPS IP
```

## Cost Analysis

### Oracle Cloud A1 Free Tier (Recommended)
- **Compute**: Free (4 ARM cores, 24GB RAM)
- **Storage**: Free (200GB)
- **Network**: Free (10TB egress/month)
- **Total**: €0/month ✨

### Resource Usage (Current Config)
- Llama 3B (2 replicas): 6GB RAM, 4 CPU cores
- Llama 1B (1 replica): 1GB RAM, 1 CPU core
- Skills Engine (2 replicas): 512MB RAM, 0.4 CPU cores
- Redis: 512MB RAM, 0.2 CPU cores
- **Total**: ~8GB RAM, ~5.6 CPU cores

**Fits comfortably in Oracle A1 free tier with room to spare.**

### Alternative: Hetzner CAX11
- **Cost**: €4.50/month
- **Specs**: 2 ARM cores, 4GB RAM
- **Limitation**: Can only run Llama 1B + Skills Engine

## Next Steps

1. **Week 1-2**: Get infrastructure running with Llama 3.2
2. **Week 3**: Integrate Android app with Gemini Nano
3. **Week 4**: Implement skills framework
4. **Week 5**: Add emotion detection
5. **Week 6**: Optimize and deploy to production

## Support & Resources

- **Documentation**: `/docs/SLM_ARCHITECTURE.md`
- **Issues**: Create GitHub issues for bugs
- **Llama.cpp**: https://github.com/ggerganov/llama.cpp
- **Kubernetes**: https://kubernetes.io/docs/home/
- **ARM Optimization**: https://developer.arm.com/documentation

---

**Last Updated**: 2025-10-29
**Version**: 1.0
**Status**: Production Ready

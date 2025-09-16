terraform {
  required_version = ">= 1.0"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
  }
}

# Configure Kubernetes provider for existing Hetzner cluster
provider "kubernetes" {
  config_path    = var.kubeconfig_path
  config_context = var.kube_context
}

provider "helm" {
  kubernetes {
    config_path    = var.kubeconfig_path
    config_context = var.kube_context
  }
}

# Create namespace for voice AI application
resource "kubernetes_namespace" "voice_ai" {
  metadata {
    name = var.namespace
    labels = {
      app         = "voice-ai"
      environment = var.environment
      arch        = "arm64"
    }
    annotations = {
      "kubernetes.io/managed-by" = "terraform"
    }
  }
}

# Create resource quota for cost control
resource "kubernetes_resource_quota" "voice_ai_quota" {
  metadata {
    name      = "voice-ai-quota"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }
  spec {
    hard = {
      "requests.cpu"    = var.cpu_quota
      "requests.memory" = var.memory_quota
      "limits.cpu"      = var.cpu_limit
      "limits.memory"   = var.memory_limit
      "pods"            = "10"
      "services"        = "5"
      "persistentvolumeclaims" = "3"
    }
  }
}

# Create limit range for ARM optimization
resource "kubernetes_limit_range" "voice_ai_limits" {
  metadata {
    name      = "voice-ai-limits"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }
  spec {
    limit {
      type = "Container"
      default = {
        cpu    = "200m"
        memory = "256Mi"
      }
      default_request = {
        cpu    = "100m"
        memory = "128Mi"
      }
      max = {
        cpu    = "1000m"
        memory = "1Gi"
      }
      min = {
        cpu    = "50m"
        memory = "64Mi"
      }
    }
  }
}

# Create secrets for voice AI services
resource "kubernetes_secret" "voice_ai_secrets" {
  metadata {
    name      = "voice-ai-secrets"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }

  data = {
    hume-api-key     = var.hume_api_key
    hume-secret-key  = var.hume_secret_key
    jwt-secret       = var.jwt_secret
    database-url     = var.database_url
  }

  type = "Opaque"
}

# Create ConfigMap for ARM-specific configurations
resource "kubernetes_config_map" "voice_ai_config" {
  metadata {
    name      = "voice-ai-config"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }

  data = {
    "NODE_ENV"                = var.environment
    "LOG_LEVEL"              = "info"
    "AUDIO_SAMPLE_RATE"      = "16000"
    "AUDIO_CHANNELS"         = "1"
    "MAX_AUDIO_DURATION"     = "300"
    "ARM_OPTIMIZATION"       = "true"
    "CPU_INTENSIVE_THREADS"  = "2"
    "MEMORY_OPTIMIZATION"    = "true"
    "VOICE_PROCESSING_POOL"  = "4"
  }
}

# Create persistent volume for audio processing cache
resource "kubernetes_persistent_volume_claim" "audio_cache" {
  metadata {
    name      = "audio-cache-pvc"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }
  spec {
    access_modes = ["ReadWriteOnce"]
    resources {
      requests = {
        storage = var.storage_size
      }
    }
    storage_class_name = var.storage_class
  }

  # Handle WaitForFirstConsumer binding mode
  timeouts {
    create = "5m"
  }

  # Don't wait for binding if using WaitForFirstConsumer
  wait_until_bound = false
}

# Network policy for security
resource "kubernetes_network_policy" "voice_ai_network_policy" {
  metadata {
    name      = "voice-ai-network-policy"
    namespace = kubernetes_namespace.voice_ai.metadata[0].name
  }

  spec {
    pod_selector {
      match_labels = {
        app = "voice-ai"
      }
    }

    policy_types = ["Ingress", "Egress"]

    ingress {
      from {
        pod_selector {
          match_labels = {
            app = "voice-ai"
          }
        }
      }
      ports {
        protocol = "TCP"
        port     = "3000"
      }
      ports {
        protocol = "TCP"
        port     = "8080"
      }
    }

    egress {
      # Allow DNS resolution
      to {}
      ports {
        protocol = "UDP"
        port     = "53"
      }
    }

    egress {
      # Allow HTTPS for external APIs (Hume AI)
      to {}
      ports {
        protocol = "TCP"
        port     = "443"
      }
    }
  }
}
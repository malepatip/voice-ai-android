variable "kubeconfig_path" {
  description = "Path to the kubeconfig file for Hetzner cluster"
  type        = string
  default     = "~/.kube/config"
}

variable "kube_context" {
  description = "Kubernetes context to use"
  type        = string
  default     = "default"
}

variable "namespace" {
  description = "Kubernetes namespace for voice AI application"
  type        = string
  default     = "voice-ai"
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

# Resource limits for cost optimization
variable "cpu_quota" {
  description = "Total CPU quota for namespace"
  type        = string
  default     = "2000m"  # 2 CPU cores
}

variable "memory_quota" {
  description = "Total memory quota for namespace"
  type        = string
  default     = "4Gi"    # 4GB RAM
}

variable "cpu_limit" {
  description = "Maximum CPU limit for namespace"
  type        = string
  default     = "3000m"  # 3 CPU cores max
}

variable "memory_limit" {
  description = "Maximum memory limit for namespace"
  type        = string
  default     = "6Gi"    # 6GB RAM max
}

variable "storage_size" {
  description = "Storage size for audio cache"
  type        = string
  default     = "10Gi"
}

variable "storage_class" {
  description = "Storage class for PVCs"
  type        = string
  default     = "hcloud-volumes"  # Hetzner storage class
}

# Secrets
variable "hume_api_key" {
  description = "Hume AI API key"
  type        = string
  sensitive   = true
}

variable "hume_secret_key" {
  description = "Hume AI secret key"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT secret for authentication"
  type        = string
  sensitive   = true
  default     = ""
}

variable "database_url" {
  description = "Database connection URL"
  type        = string
  sensitive   = true
  default     = ""
}

# ARM-specific configurations
variable "arm_node_selector" {
  description = "Node selector for ARM nodes"
  type        = map(string)
  default = {
    "kubernetes.io/arch" = "arm64"
  }
}

variable "preferred_node_zones" {
  description = "Preferred availability zones for ARM nodes"
  type        = list(string)
  default     = ["eu-central"]  # Adjust for Hetzner regions
}
# Terraform variables for Hetzner ARM deployment

# Kubernetes configuration
kubeconfig_path = "~/.kube/config"
kube_context    = "hetzner-k3s-default"  # Your Hetzner cluster context name
namespace       = "voice-ai"
environment     = "dev"

# Resource limits (adjust based on your Hetzner VPS specs)
cpu_quota    = "2000m"  # 2 CPU cores
memory_quota = "4Gi"    # 4GB RAM
cpu_limit    = "3000m"  # 3 CPU cores max burst
memory_limit = "6Gi"    # 6GB RAM max burst

# Storage configuration
storage_size  = "10Gi"
storage_class = "local-path"  # K3s default storage class

# Secrets
hume_api_key = "A45cWnfPtgNq7tUBwMJcqYriDpxBYeyNSd39dEAcU2vs5puF"
hume_secret_key = "cmxqGQzeohX4qG6aXk15t4TsIak4FLbvMEe3euzSezlmF2m92u9FKLv2kPa6Dtxq"
jwt_secret   = "bfbc3a49c4851308bea24546fde8c78dbea581e0ff54a3e3b3f6bc397727cc84"  # Generate a random secret for JWT using command `openssl rand -hex 32`
database_url = ""  # Optional - leave empty if not using external database

# ARM-specific settings
arm_node_selector = {
  "kubernetes.io/arch" = "arm64"
}

preferred_node_zones = ["eu-central"]
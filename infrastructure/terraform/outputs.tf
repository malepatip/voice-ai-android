output "namespace" {
  description = "The created namespace for voice AI application"
  value       = kubernetes_namespace.voice_ai.metadata[0].name
}

output "resource_quota_name" {
  description = "Name of the resource quota"
  value       = kubernetes_resource_quota.voice_ai_quota.metadata[0].name
}

output "secrets_name" {
  description = "Name of the secrets"
  value       = kubernetes_secret.voice_ai_secrets.metadata[0].name
}

output "config_map_name" {
  description = "Name of the config map"
  value       = kubernetes_config_map.voice_ai_config.metadata[0].name
}

output "audio_cache_pvc_name" {
  description = "Name of the audio cache PVC"
  value       = kubernetes_persistent_volume_claim.audio_cache.metadata[0].name
}

output "cluster_resource_limits" {
  description = "Configured resource limits for the namespace"
  value = {
    cpu_quota    = var.cpu_quota
    memory_quota = var.memory_quota
    cpu_limit    = var.cpu_limit
    memory_limit = var.memory_limit
  }
}
# 🔒 Security Guidelines

## ⚠️ IMPORTANT: This is a PUBLIC repository

### 🚫 Never Commit These Files:
- `infrastructure/terraform/terraform.tfvars` (contains API keys)
- `infrastructure/terraform/terraform.tfstate*` (contains sensitive state)
- `.env` files with secrets
- Kubeconfig files
- Any files containing API keys, passwords, or tokens

### ✅ Safe to Commit:
- `*.example` files (templates without real secrets)
- `*.template` files
- Documentation
- Code without hardcoded secrets

## 🔑 Setting Up Secrets Locally

### 1. Terraform Variables
```bash
# Copy the secure example
cp infrastructure/terraform/terraform.tfvars.secure-example infrastructure/terraform/terraform.tfvars

# Edit with your real values
vim infrastructure/terraform/terraform.tfvars
```

### 2. Environment Variables
```bash
# Create local environment file
cat > .env.local << EOF
GITHUB_TOKEN=your_github_token_here
HUME_API_KEY=your_hume_api_key
HUME_SECRET_KEY=your_hume_secret_key
EOF

# Load environment variables
source .env.local
```

### 3. Kubernetes Secrets
```bash
# Create secrets directly in cluster (never commit)
kubectl create secret generic voice-ai-secrets \
  --from-literal=hume-api-key="your-api-key" \
  --from-literal=hume-secret-key="your-secret-key" \
  --from-literal=jwt-secret="your-jwt-secret" \
  --namespace=voice-ai
```

## 🛡️ GitHub Security Best Practices

### Repository Settings:
1. **Branch Protection**: Require reviews for main branch
2. **Security Alerts**: Enable Dependabot alerts
3. **Secret Scanning**: GitHub automatically scans for secrets

### Environment Variables in GitHub Actions:
Use GitHub Secrets for CI/CD:
- Settings → Secrets and variables → Actions
- Add secrets like `HUME_API_KEY`, `DOCKER_TOKEN`, etc.

## 🔍 Checking for Leaked Secrets

Before committing:
```bash
# Check for potential secrets
git diff --cached | grep -i "api.*key\|secret\|password\|token"

# Scan with git-secrets (if installed)
git secrets --scan
```

## 🚨 If You Accidentally Commit Secrets

1. **Immediately revoke/rotate** all exposed credentials
2. **Remove from history**:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch path/to/secret/file" \
     --prune-empty --tag-name-filter cat -- --all
   ```
3. **Force push** (⚠️ destructive):
   ```bash
   git push origin --force --all
   ```

## 📋 Security Checklist

Before pushing to GitHub:
- [ ] No API keys in any files
- [ ] `.gitignore` includes all sensitive patterns
- [ ] `terraform.tfvars` is not tracked
- [ ] No `.env` files with real secrets
- [ ] All examples use placeholder values
- [ ] Terraform state files are excluded

## 🔗 Secure Development Workflow

1. **Clone repository**
2. **Copy template files**:
   ```bash
   cp infrastructure/terraform/terraform.tfvars.secure-example infrastructure/terraform/terraform.tfvars
   ```
3. **Add real credentials locally** (never commit)
4. **Test deployment**
5. **Commit only safe files**

## 📞 Security Contact

If you discover a security vulnerability, please:
1. **Do NOT** create a public issue
2. **Email**: Create a private security advisory on GitHub
3. **Include**: Detailed description and steps to reproduce
# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability, please email security@example.com or open a private security advisory on GitHub.

**Please do not open public issues for security vulnerabilities.**

## Security Best Practices

### Environment Variables

**NEVER commit sensitive credentials to version control!**

All sensitive configuration should be stored as environment variables:

#### Required Environment Variables

```bash
# Database credentials
export DATABASE_PASSWORD="your_secure_password"

# JWT secret (256-bit minimum)
export JWT_SECRET="your_secure_jwt_secret_key"
```

#### Generate Secure JWT Secret

```bash
# Using OpenSSL (recommended)
openssl rand -base64 32

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

# Or using Python
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

### Production Deployment Checklist

- [ ] All environment variables configured
- [ ] Database password rotated
- [ ] JWT secret is strong (256-bit minimum)
- [ ] HTTPS/TLS enabled
- [ ] Rate limiting configured
- [ ] Security headers enabled
- [ ] Audit logging active
- [ ] Database backups configured
- [ ] Secrets stored in secure vault (AWS Secrets Manager, Azure Key Vault, etc.)

### Credential Rotation

**If credentials have been exposed:**

1. **Immediately rotate all affected credentials**
   - Change database password
   - Generate new JWT secret
   - Revoke all existing JWT tokens

2. **Update environment variables**
   ```bash
   export DATABASE_PASSWORD="new_secure_password"
   export JWT_SECRET="new_secure_jwt_secret"
   ```

3. **Restart application**
   ```bash
   mvn spring-boot:run
   # or
   docker-compose restart backend
   ```

4. **Notify users** (if customer data was potentially exposed)

### GitGuardian Alert Response

If GitGuardian detects exposed credentials:

1. **Rotate credentials immediately**
2. **Remove from git history** (see below)
3. **Force push to remove from GitHub**
4. **Verify removal** at GitHub

### Remove Secrets from Git History

⚠️ **WARNING**: This rewrites git history. Coordinate with your team first.

#### Option 1: Using BFG Repo Cleaner (Recommended)

```bash
# Download BFG
brew install bfg  # macOS
# or download from https://rtyley.github.io/bfg-repo-cleaner/

# Remove file containing secrets
bfg --delete-files application.properties

# Clean up
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push origin --force --all
```

#### Option 2: Using git filter-branch

```bash
# Remove specific file from history
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch src/main/resources/application.properties" \
  --prune-empty --tag-name-filter cat -- --all

# Clean up
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push origin --force --all
git push origin --force --tags
```

#### Option 3: Delete and recreate repository (Safest)

```bash
# Backup your code locally
# Delete the GitHub repository
# Create a new repository
# Push clean code without sensitive data
```

### Secrets Management Solutions

For production environments, use dedicated secrets management:

#### AWS
```bash
# AWS Secrets Manager
aws secretsmanager get-secret-value --secret-id prod/database/password

# AWS Systems Manager Parameter Store
aws ssm get-parameter --name /prod/database/password --with-decryption
```

#### Azure
```bash
# Azure Key Vault
az keyvault secret show --vault-name myKeyVault --name DatabasePassword
```

#### HashiCorp Vault
```bash
# Vault
vault kv get secret/database/password
```

#### Docker Secrets
```yaml
# docker-compose.yml
services:
  backend:
    secrets:
      - db_password
      - jwt_secret

secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

### .gitignore Configuration

Ensure these patterns are in `.gitignore`:

```
# Environment variables
.env
.env.local
.env.*.local

# Secrets
secrets/
*.pem
*.key
*.crt

# Configuration with credentials
application-local.properties
application-secret.properties

# IDE
.idea/
*.iml
.vscode/

# Build
target/
*.log
```

### Security Headers

The application includes the following security headers:

```
Content-Security-Policy: default-src 'self'
Strict-Transport-Security: max-age=31536000
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
```

### Rate Limiting

- **Default**: 100 requests per minute per IP
- **Configurable** via application properties
- **DDoS Protection** with exponential backoff

### Audit Logging

All security events are logged:
- Login attempts (success/failure)
- Account lockouts
- Password changes
- Role modifications
- API access with IP and user agent

### Database Security

- ✅ Passwords hashed with BCrypt (cost factor 10)
- ✅ Prepared statements (SQL injection protection)
- ✅ Connection pooling with HikariCP
- ✅ Unique constraints on sensitive fields

### Dependency Security

Keep dependencies updated:

```bash
# Check for vulnerabilities
mvn dependency-check:check

# Update dependencies
mvn versions:use-latest-releases
```

## Security Contacts

- **Email**: security@example.com
- **GitHub Security Advisories**: https://github.com/Srikanthnexushub/test1-crud-api/security/advisories

## Acknowledgments

We appreciate responsible disclosure of security vulnerabilities.

---

**Last Updated**: February 2026

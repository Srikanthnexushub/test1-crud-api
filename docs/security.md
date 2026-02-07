# Security Architecture Document

**Version**: 1.0
**Last Updated**: 2026-02-07
**Classification**: Internal
**Owner**: Security Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Threat Model](#2-threat-model)
3. [Authentication Architecture](#3-authentication-architecture)
4. [Authorization Model](#4-authorization-model)
5. [Data Protection](#5-data-protection)
6. [API Security](#6-api-security)
7. [Infrastructure Security](#7-infrastructure-security)
8. [Security Controls Matrix](#8-security-controls-matrix)
9. [Incident Response](#9-incident-response)
10. [Compliance](#10-compliance)
11. [Security Testing](#11-security-testing)
12. [Security Checklist](#12-security-checklist)

---

## 1. Executive Summary

### 1.1 Security Posture

The CRUD API implements defense-in-depth security with multiple layers of protection:

| Layer | Controls |
|-------|----------|
| **Network** | HTTPS/TLS, CORS, Rate Limiting |
| **Application** | JWT Authentication, RBAC, Input Validation |
| **Data** | BCrypt Password Hashing, Encrypted Secrets |
| **Audit** | Comprehensive Logging, Trace IDs |

### 1.2 Key Security Features

- **Authentication**: JWT with short-lived tokens + refresh token rotation
- **Multi-Factor**: TOTP-based 2FA with backup codes
- **Account Protection**: Automatic lockout after failed attempts
- **Email Verification**: Blocking verification before login
- **Rate Limiting**: 100 requests/minute per IP
- **Security Headers**: HSTS, CSP, X-Frame-Options, etc.

### 1.3 Security Contacts

| Role | Contact |
|------|---------|
| Security Lead | security@example.com |
| Incident Response | security-incident@example.com |
| Bug Bounty | security-reports@example.com |

---

## 2. Threat Model

### 2.1 STRIDE Analysis

| Threat | Category | Mitigation |
|--------|----------|------------|
| **Spoofing** | Identity | JWT authentication, 2FA, email verification |
| **Tampering** | Data Integrity | Input validation, parameterized queries, HTTPS |
| **Repudiation** | Audit | Comprehensive audit logging with trace IDs |
| **Information Disclosure** | Confidentiality | Encrypted passwords, secure headers, no stack traces |
| **Denial of Service** | Availability | Rate limiting, connection pooling, input size limits |
| **Elevation of Privilege** | Authorization | RBAC, principle of least privilege |

### 2.2 Attack Surface

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         ATTACK SURFACE MAP                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  EXTERNAL ATTACK VECTORS                                                │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                                                                 │   │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │   │
│  │  │ Credential   │    │   Injection  │    │    Session   │      │   │
│  │  │   Attacks    │    │   Attacks    │    │   Hijacking  │      │   │
│  │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘      │   │
│  │         │                   │                   │              │   │
│  │         ▼                   ▼                   ▼              │   │
│  │  • Brute force       • SQL injection     • Token theft        │   │
│  │  • Credential        • XSS               • Session fixation   │   │
│  │    stuffing          • Command injection • CSRF               │   │
│  │  • Password spray    • LDAP injection    • Cookie theft       │   │
│  │                                                                │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    MITIGATIONS APPLIED                          │   │
│  │                                                                 │   │
│  │  Credential:         Injection:           Session:             │   │
│  │  ✓ Account lockout   ✓ Parameterized SQL  ✓ JWT (no cookies)   │   │
│  │  ✓ 2FA required      ✓ Input validation   ✓ Short token TTL    │   │
│  │  ✓ Rate limiting     ✓ CSP headers        ✓ Refresh rotation   │   │
│  │  ✓ BCrypt hashing    ✓ Output encoding    ✓ HTTPS only         │   │
│  │  ✓ Email verify      ✓ @NoHtml validator  ✓ Secure headers     │   │
│  │                                                                 │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Trust Boundaries

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           TRUST BOUNDARIES                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  UNTRUSTED ──────────────────────────────────────────────────────────   │
│       │                                                                 │
│       │  ┌──────────────────┐                                          │
│       └─▶│   External       │  • All input is untrusted                │
│          │   Clients        │  • Must validate everything               │
│          └────────┬─────────┘                                          │
│                   │                                                     │
│  ═══════════════════════════════════════════════════════════════════   │
│  BOUNDARY 1: Network Edge (TLS termination, rate limiting)             │
│  ═══════════════════════════════════════════════════════════════════   │
│                   │                                                     │
│                   ▼                                                     │
│          ┌──────────────────┐                                          │
│          │  Security Filter │  • JWT validation                        │
│          │      Chain       │  • Authentication check                   │
│          └────────┬─────────┘                                          │
│                   │                                                     │
│  ═══════════════════════════════════════════════════════════════════   │
│  BOUNDARY 2: Authentication (JWT verified, user identity known)        │
│  ═══════════════════════════════════════════════════════════════════   │
│                   │                                                     │
│                   ▼                                                     │
│          ┌──────────────────┐                                          │
│          │   Application    │  • Role-based access control             │
│          │      Layer       │  • Business logic validation             │
│          └────────┬─────────┘                                          │
│                   │                                                     │
│  ═══════════════════════════════════════════════════════════════════   │
│  BOUNDARY 3: Authorization (RBAC enforced)                             │
│  ═══════════════════════════════════════════════════════════════════   │
│                   │                                                     │
│                   ▼                                                     │
│          ┌──────────────────┐                                          │
│          │    Database      │  • Parameterized queries only            │
│          │                  │  • Connection pooling                    │
│          └──────────────────┘                                          │
│                                                                         │
│  TRUSTED ────────────────────────────────────────────────────────────   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.4 Risk Assessment

| Risk | Likelihood | Impact | Severity | Mitigation Status |
|------|------------|--------|----------|-------------------|
| Credential Stuffing | High | High | Critical | Mitigated (2FA, lockout) |
| SQL Injection | Medium | Critical | High | Mitigated (JPA/parameterized) |
| XSS | Medium | Medium | Medium | Mitigated (CSP, validation) |
| Session Hijacking | Low | High | Medium | Mitigated (short JWT, HTTPS) |
| DDoS | Medium | High | High | Partial (rate limiting) |
| Data Breach | Low | Critical | High | Mitigated (encryption, access control) |

---

## 3. Authentication Architecture

### 3.1 JWT Token Design

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         JWT TOKEN LIFECYCLE                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  LOGIN REQUEST                                                          │
│       │                                                                 │
│       ▼                                                                 │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐            │
│  │   Validate   │────▶│  Check 2FA   │────▶│   Generate   │            │
│  │ Credentials  │     │   Status     │     │   Tokens     │            │
│  └──────────────┘     └──────────────┘     └──────────────┘            │
│                              │                     │                    │
│                              ▼                     ▼                    │
│                       2FA Enabled?          ┌──────────────┐            │
│                        │      │             │ Access Token │            │
│                       Yes    No             │  (15 min)    │            │
│                        │      │             └──────────────┘            │
│                        ▼      │                    +                    │
│                 ┌────────────┐│             ┌──────────────┐            │
│                 │  Partial   ││             │Refresh Token │            │
│                 │   Token    │└────────────▶│  (7 days)    │            │
│                 │  (5 min)   │              └──────────────┘            │
│                 └────────────┘                                          │
│                        │                                                │
│                        ▼                                                │
│                 ┌────────────┐                                          │
│                 │ Verify 2FA │                                          │
│                 │    Code    │                                          │
│                 └────────────┘                                          │
│                        │                                                │
│                        ▼                                                │
│                 ┌──────────────┐                                        │
│                 │ Full Tokens  │                                        │
│                 └──────────────┘                                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Token Specifications

| Token Type | Expiry | Storage | Purpose |
|------------|--------|---------|---------|
| Access Token | 15 minutes | Client memory | API authentication |
| Refresh Token | 7 days | Database + client | Token renewal |
| Partial Token | 5 minutes | Client memory | 2FA verification |
| Verification Token | 24 hours | Database | Email verification |
| Password Reset Token | 1 hour | Database | Password recovery |

### 3.3 JWT Claims

**Access Token Claims**:
```json
{
  "sub": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1707300000,
  "exp": 1707300900,
  "jti": "unique-token-id"
}
```

**Partial Token Claims** (2FA required):
```json
{
  "sub": "user@example.com",
  "type": "2FA_REQUIRED",
  "iat": 1707300000,
  "exp": 1707300300
}
```

### 3.4 Password Security

| Setting | Value | Rationale |
|---------|-------|-----------|
| Algorithm | BCrypt | Adaptive, GPU-resistant |
| Work Factor | 10 | Balance of security and performance |
| Min Length | 6 characters | Basic complexity |
| Max Length | 100 characters | Prevent DoS |

### 3.5 Account Lockout

| Setting | Value |
|---------|-------|
| Max Failed Attempts | 5 |
| Lock Duration | 15 minutes |
| Counter Reset | On successful login |
| Lockout Scope | Per user account |

### 3.6 Two-Factor Authentication

| Setting | Value |
|---------|-------|
| Algorithm | TOTP (RFC 6238) |
| Hash | SHA-1 |
| Time Step | 30 seconds |
| Code Length | 6 digits |
| Backup Codes | 10 codes x 8 characters |
| Compatible Apps | Google Authenticator, Authy, Microsoft Authenticator |

---

## 4. Authorization Model

### 4.1 Role-Based Access Control (RBAC)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         ROLE HIERARCHY                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│                        ┌──────────────┐                                 │
│                        │  ROLE_ADMIN  │                                 │
│                        │              │                                 │
│                        │ • All access │                                 │
│                        │ • User mgmt  │                                 │
│                        │ • Role assign│                                 │
│                        └──────┬───────┘                                 │
│                               │                                         │
│                               ▼                                         │
│                       ┌──────────────┐                                  │
│                       │ ROLE_MANAGER │                                  │
│                       │              │                                  │
│                       │ • Read users │                                  │
│                       │ • Reports    │                                  │
│                       └──────┬───────┘                                  │
│                              │                                          │
│                              ▼                                          │
│                       ┌──────────────┐                                  │
│                       │  ROLE_USER   │                                  │
│                       │              │                                  │
│                       │ • Own profile│                                  │
│                       │ • Basic ops  │                                  │
│                       └──────────────┘                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Permission Matrix

| Endpoint | ROLE_USER | ROLE_MANAGER | ROLE_ADMIN |
|----------|-----------|--------------|------------|
| POST /register | Public | Public | Public |
| POST /login | Public | Public | Public |
| GET /users | - | Read | Full |
| POST /users | - | - | Full |
| GET /users/{id} | Own only | All | All |
| PUT /users/{id} | Own only | - | All |
| DELETE /users/{id} | - | - | Full |
| 2FA endpoints | Own | Own | Own |

### 4.3 Resource Ownership

Users can only access their own resources unless they have elevated privileges:

```java
// Ownership check pattern
if (!currentUser.getId().equals(resourceOwnerId) &&
    !currentUser.hasRole("ROLE_ADMIN")) {
    throw new AccessDeniedException("Cannot access another user's resource");
}
```

---

## 5. Data Protection

### 5.1 Data Classification

| Classification | Examples | Protection |
|----------------|----------|------------|
| **Public** | API documentation | None required |
| **Internal** | User email | Access control |
| **Confidential** | Passwords, tokens | Encryption, hashing |
| **Restricted** | 2FA secrets, backup codes | Encryption at rest |

### 5.2 Encryption Standards

| Data Type | At Rest | In Transit |
|-----------|---------|------------|
| Passwords | BCrypt (irreversible) | TLS 1.3 |
| JWT Tokens | N/A (short-lived) | TLS 1.3 |
| 2FA Secrets | Database encryption | TLS 1.3 |
| Refresh Tokens | Database (UUID) | TLS 1.3 |

### 5.3 Sensitive Data Handling

**Never Log**:
- Passwords (plain or hashed)
- JWT tokens
- 2FA secrets
- Backup codes
- Session identifiers

**Safe to Log**:
- User email (audit purposes)
- IP address
- User agent
- Request path
- Response status
- Trace IDs

### 5.4 Data Retention

| Data Type | Retention | Deletion |
|-----------|-----------|----------|
| User accounts | Until deletion requested | Hard delete + audit |
| Audit logs | 90 days | Automatic purge |
| Refresh tokens | 7 days | Automatic expiry |
| Verification tokens | 24 hours | Automatic expiry |
| Failed login records | 15 minutes | Automatic reset |

---

## 6. API Security

### 6.1 Security Headers

| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Security-Policy` | `default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'` | XSS prevention |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | Force HTTPS |
| `X-Frame-Options` | `DENY` | Clickjacking prevention |
| `X-Content-Type-Options` | `nosniff` | MIME sniffing prevention |
| `X-XSS-Protection` | `1; mode=block` | Legacy XSS filter |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Referrer control |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=()` | Feature restriction |
| `Cache-Control` | `no-cache, no-store, must-revalidate` | Prevent caching |

### 6.2 Input Validation

| Validation | Implementation |
|------------|----------------|
| Email format | `@Email` annotation |
| Password length | `@Size(min=6, max=100)` |
| Required fields | `@NotBlank` |
| HTML injection | `@NoHtml` custom validator |
| 2FA code format | `@Pattern(regexp="^[0-9]{6}$")` |
| Role values | `@Pattern` with enum values |

### 6.3 Rate Limiting

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      RATE LIMITING ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Request ──▶ Extract IP ──▶ Check Bucket ──▶ Allow/Deny               │
│                   │              │                                      │
│                   ▼              ▼                                      │
│            X-Forwarded-For   Token Bucket                               │
│            or Remote Addr    (Bucket4j)                                 │
│                                  │                                      │
│                                  ▼                                      │
│                        ┌──────────────────┐                            │
│                        │ 100 tokens/min   │                            │
│                        │ per IP address   │                            │
│                        └──────────────────┘                            │
│                                  │                                      │
│                    ┌─────────────┴─────────────┐                       │
│                    ▼                           ▼                        │
│              Token Available            No Token                        │
│                    │                           │                        │
│                    ▼                           ▼                        │
│              Allow Request              429 Too Many                    │
│                                         Requests                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.4 CORS Configuration

```yaml
Allowed Origins:
  - http://localhost:3000
  - http://localhost:8080
  - https://yourdomain.com

Allowed Methods:
  - GET
  - POST
  - PUT
  - DELETE
  - OPTIONS
  - PATCH

Allowed Headers:
  - Authorization
  - Content-Type
  - X-2FA-Email

Exposed Headers:
  - Authorization

Allow Credentials: false
Max Age: 3600 seconds
```

---

## 7. Infrastructure Security

### 7.1 Container Security

| Control | Implementation |
|---------|----------------|
| Base Image | OpenJDK 17 (Alpine) |
| Non-root User | Application runs as non-root |
| Read-only FS | Where possible |
| Resource Limits | CPU/memory limits in Docker |
| Health Checks | Liveness/readiness probes |

### 7.2 Network Security

| Control | Implementation |
|---------|----------------|
| TLS | 1.2+ required, 1.3 preferred |
| Internal Network | Docker bridge network |
| Port Exposure | Only 8080 (app), 5434 (db dev) |
| Database Access | Internal network only |

### 7.3 Secret Management

| Secret | Storage | Rotation |
|--------|---------|----------|
| JWT Secret | Environment variable | On deployment |
| DB Password | Environment variable | Quarterly |
| SMTP Credentials | Environment variable | As needed |
| 2FA Secrets | Database (encrypted) | User-initiated |

---

## 8. Security Controls Matrix

### 8.1 OWASP Top 10 (2021) Mapping

| OWASP Risk | Control | Status |
|------------|---------|--------|
| **A01: Broken Access Control** | RBAC, JWT validation, ownership checks | ✅ Implemented |
| **A02: Cryptographic Failures** | BCrypt, TLS 1.3, secure headers | ✅ Implemented |
| **A03: Injection** | Parameterized queries (JPA), input validation | ✅ Implemented |
| **A04: Insecure Design** | Threat modeling, security reviews | ✅ Implemented |
| **A05: Security Misconfiguration** | Security headers, minimal exposure | ✅ Implemented |
| **A06: Vulnerable Components** | Dependency scanning (Dependabot) | ⚠️ Recommended |
| **A07: Auth Failures** | 2FA, account lockout, secure tokens | ✅ Implemented |
| **A08: Data Integrity Failures** | Input validation, signed JWTs | ✅ Implemented |
| **A09: Logging Failures** | Audit logging, trace IDs | ✅ Implemented |
| **A10: SSRF** | No external URL fetching | ✅ N/A |

### 8.2 CIS Controls Mapping

| CIS Control | Implementation |
|-------------|----------------|
| 1. Inventory | Docker images tracked |
| 4. Secure Configuration | Security headers, minimal exposure |
| 5. Account Management | RBAC, account lifecycle |
| 6. Access Control | JWT, role-based permissions |
| 8. Audit Logging | Comprehensive audit trail |
| 10. Malware Defenses | Container scanning |
| 13. Network Monitoring | Request logging |
| 14. Security Training | Documentation |

---

## 9. Incident Response

### 9.1 Security Event Categories

| Category | Severity | Example | Response Time |
|----------|----------|---------|---------------|
| **Critical** | P1 | Data breach, system compromise | < 1 hour |
| **High** | P2 | Credential stuffing attack | < 4 hours |
| **Medium** | P3 | Multiple failed logins | < 24 hours |
| **Low** | P4 | Single failed login | Monitor |

### 9.2 Incident Response Procedures

#### 9.2.1 Account Compromise

1. Immediately lock affected account
2. Invalidate all refresh tokens
3. Force password reset
4. Review audit logs
5. Notify user
6. Document incident

#### 9.2.2 Brute Force Attack

1. Verify rate limiting is active
2. Check for distributed attack
3. Consider IP blocking
4. Review affected accounts
5. Enable additional monitoring

#### 9.2.3 Data Breach

1. Isolate affected systems
2. Preserve evidence
3. Assess scope
4. Notify stakeholders
5. Engage legal/compliance
6. User notification (if required)
7. Post-incident review

### 9.3 Audit Log Queries

```sql
-- Failed login attempts in last hour
SELECT username, COUNT(*) as attempts, MAX(timestamp) as last_attempt
FROM audit_logs
WHERE action = 'USER_LOGIN' AND status = 'FAILURE'
AND timestamp > NOW() - INTERVAL '1 hour'
GROUP BY username
ORDER BY attempts DESC;

-- Account lockouts
SELECT * FROM audit_logs
WHERE action = 'ACCOUNT_LOCKED'
AND timestamp > NOW() - INTERVAL '24 hours';

-- Suspicious activity by IP
SELECT ip_address, COUNT(*) as requests
FROM audit_logs
WHERE timestamp > NOW() - INTERVAL '1 hour'
GROUP BY ip_address
HAVING COUNT(*) > 50;
```

---

## 10. Compliance

### 10.1 Regulatory Considerations

| Regulation | Applicability | Key Requirements |
|------------|---------------|------------------|
| **GDPR** | EU users | Data protection, consent, right to deletion |
| **CCPA** | California users | Privacy rights, data access |
| **SOC 2** | B2B SaaS | Security, availability, confidentiality |
| **PCI DSS** | Payment data | N/A (no payment processing) |

### 10.2 Audit Trail Requirements

The system maintains audit logs for:
- All authentication events (success/failure)
- Account modifications
- Role changes
- Password resets
- 2FA changes
- Administrative actions

Log retention: 90 days minimum

### 10.3 Data Subject Rights

| Right | Implementation |
|-------|----------------|
| Access | GET /users/{id} (own data) |
| Rectification | PUT /users/{id} |
| Erasure | DELETE /users/{id} (admin) |
| Portability | Export endpoint (future) |

---

## 11. Security Testing

### 11.1 Testing Requirements

| Test Type | Frequency | Tools |
|-----------|-----------|-------|
| SAST | Every commit | SonarQube |
| DAST | Weekly | OWASP ZAP |
| Dependency Scan | Daily | Dependabot, Snyk |
| Penetration Test | Annually | Third party |
| Security Review | Per release | Internal |

### 11.2 Security Test Cases

| Category | Test Case | Expected Result |
|----------|-----------|-----------------|
| Authentication | Login with invalid credentials | 401 Unauthorized |
| Authentication | Login after lockout | 423 Locked |
| Authorization | Access other user's data | 403 Forbidden |
| Injection | SQL injection in email | Input rejected |
| Injection | XSS in input fields | Sanitized/rejected |
| Rate Limiting | 101 requests/minute | 429 Too Many Requests |
| Tokens | Expired JWT | 401 Unauthorized |
| Tokens | Tampered JWT | 401 Unauthorized |

### 11.3 Vulnerability Disclosure

**Responsible Disclosure Policy**:
1. Report to security@example.com
2. 90-day disclosure timeline
3. No legal action for good-faith research
4. Credit in security acknowledgments

---

## 12. Security Checklist

### 12.1 Pre-Deployment Checklist

- [ ] All secrets in environment variables (not in code)
- [ ] JWT secret is minimum 256 bits
- [ ] HTTPS enforced
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] CORS properly configured
- [ ] Database credentials rotated
- [ ] Dependency scan clean
- [ ] No debug endpoints exposed
- [ ] Error messages don't leak internals

### 12.2 Ongoing Security Tasks

| Task | Frequency |
|------|-----------|
| Review audit logs | Daily |
| Check failed logins | Daily |
| Update dependencies | Weekly |
| Review access logs | Weekly |
| Rotate secrets | Quarterly |
| Security training | Annually |
| Penetration test | Annually |
| Disaster recovery test | Annually |

### 12.3 Security Improvement Roadmap

| Priority | Enhancement | Status |
|----------|-------------|--------|
| P1 | Add CAPTCHA for registration | Planned |
| P1 | Implement session revocation | Planned |
| P2 | Add WebAuthn/FIDO2 support | Future |
| P2 | Implement device fingerprinting | Future |
| P3 | Add anomaly detection | Future |
| P3 | Implement security questions | Future |

---

*This document should be reviewed and updated quarterly or after any significant security incident.*

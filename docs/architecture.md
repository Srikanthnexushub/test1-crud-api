# CRUD API - System Architecture Document

**Version**: 1.0
**Last Updated**: 2026-02-07
**Status**: Living Document
**Owner**: Engineering Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [System Overview](#2-system-overview)
3. [Architecture Diagram](#3-architecture-diagram)
4. [Layer Architecture](#4-layer-architecture)
5. [Component Catalog](#5-component-catalog)
6. [Security Architecture](#6-security-architecture)
7. [Data Architecture](#7-data-architecture)
8. [Infrastructure](#8-infrastructure)
9. [API Design](#9-api-design)
10. [Non-Functional Requirements](#10-non-functional-requirements)
11. [Key Architecture Decisions](#11-key-architecture-decisions)
12. [Appendices](#12-appendices)

---

## 1. Executive Summary

### 1.1 Purpose

This document describes the technical architecture of the CRUD API system - a secure, scalable REST API for user management with enterprise-grade authentication features.

### 1.2 Scope

| In Scope | Out of Scope |
|----------|--------------|
| User authentication & authorization | Frontend applications |
| Email verification workflow | Third-party integrations |
| Two-factor authentication (TOTP) | Payment processing |
| Password management | Analytics/reporting |
| Audit logging | Mobile applications |

### 1.3 Key Capabilities

- **User Management**: Registration, authentication, CRUD operations
- **Security**: JWT tokens, 2FA, account locking, rate limiting
- **Compliance**: Audit trail, security headers, input validation
- **Operations**: Health checks, metrics, containerized deployment

---

## 2. System Overview

### 2.1 Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Runtime** | Java | 17 (LTS) | Application runtime |
| **Framework** | Spring Boot | 3.2.0 | Application framework |
| **Database** | PostgreSQL | 15 | Primary data store |
| **ORM** | Hibernate/JPA | 6.2+ | Object-relational mapping |
| **Authentication** | JJWT | 0.12.3 | JWT token handling |
| **Rate Limiting** | Bucket4j | 8.7.0 | API rate limiting |
| **Email** | Spring Mail | 3.2.0 | SMTP email delivery |
| **Templates** | Thymeleaf | 3.1+ | Email HTML templates |
| **2FA** | dev.samstevens.totp | 1.7.1 | TOTP implementation |
| **API Docs** | SpringDoc OpenAPI | 2.3.0 | API documentation |
| **Container** | Docker | 3.8 | Containerization |
| **Build** | Maven | 3.8+ | Build automation |

### 2.2 Repository Structure

```
test1-crud-api/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── config/           # Spring configuration
│   │   │   │   └── properties/   # Type-safe config properties
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── security/         # Security filters & utilities
│   │   │   ├── service/          # Business logic
│   │   │   └── validation/       # Custom validators
│   │   └── resources/
│   │       ├── templates/email/  # Email templates
│   │       └── application.properties
│   └── test/                     # Test suites
├── docs/                         # Documentation
├── docker-compose.yml            # Container orchestration
├── pom.xml                       # Maven configuration
└── CLAUDE.md                     # AI assistant context
```

---

## 3. Architecture Diagram

### 3.1 High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                     │
│  │   Web    │  │  Mobile  │  │   CLI    │  │  Other   │                     │
│  │   App    │  │   App    │  │  Tools   │  │ Services │                     │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘                     │
└───────┼─────────────┼─────────────┼─────────────┼───────────────────────────┘
        │             │             │             │
        └─────────────┴──────┬──────┴─────────────┘
                             │ HTTPS (TLS 1.3)
                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY / LOAD BALANCER                          │
│                    (Future: nginx/Kong/AWS ALB)                              │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT APPLICATION                              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        SECURITY FILTER CHAIN                          │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                   │  │
│  │  │  Security   │─▶│    Rate     │─▶│    JWT      │                   │  │
│  │  │  Headers    │  │  Limiting   │  │   Filter    │                   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│                                    ▼                                         │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                      PRESENTATION LAYER                               │  │
│  │  ┌─────────────────┐           ┌─────────────────┐                   │  │
│  │  │ UserController  │           │TwoFactorController│                  │  │
│  │  │  /api/v1/users  │           │  /api/v1/auth/2fa │                  │  │
│  │  └────────┬────────┘           └────────┬────────┘                   │  │
│  └───────────┼─────────────────────────────┼────────────────────────────┘  │
│              │                             │                                 │
│              ▼                             ▼                                 │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        SERVICE LAYER                                  │  │
│  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐             │  │
│  │  │   User    │ │  TwoFactor │ │   Email   │ │Verification│            │  │
│  │  │  Service  │ │  Service   │ │  Service  │ │  Service   │            │  │
│  │  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └─────┬─────┘             │  │
│  │        │             │             │             │                    │  │
│  │  ┌─────┴─────┐ ┌─────┴─────┐       │       ┌─────┴─────┐             │  │
│  │  │ RefreshTkn│ │ AuditLog  │       │       │CustomUser │             │  │
│  │  │  Service  │ │  Service  │       │       │DetailsSvc │             │  │
│  │  └───────────┘ └───────────┘       │       └───────────┘             │  │
│  └────────────────────────────────────┼─────────────────────────────────┘  │
│                                       │                                     │
│              ┌────────────────────────┼────────────────────────┐            │
│              ▼                        ▼                        ▼            │
│  ┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐   │
│  │  REPOSITORY LAYER │    │   EXTERNAL SMTP   │    │  TOTP GENERATOR   │   │
│  │   (Spring Data)   │    │    (Async)        │    │   (ZXing QR)      │   │
│  └─────────┬─────────┘    └───────────────────┘    └───────────────────┘   │
│            │                                                                 │
└────────────┼─────────────────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           POSTGRESQL DATABASE                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  users   │ │  roles   │ │ refresh_ │ │verificat_│ │ audit_   │          │
│  │          │ │          │ │  tokens  │ │  tokens  │ │  logs    │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────────────┐                                                       │
│  │ two_factor_      │                                                       │
│  │ backup_codes     │                                                       │
│  └──────────────────┘                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Request Flow Diagram

```
Client Request
      │
      ▼
┌─────────────────┐
│ SecurityHeaders │──▶ Add HSTS, CSP, X-Frame-Options, etc.
│     Filter      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐
│  RateLimiting   │──▶  │  429 Too Many   │ (if limit exceeded)
│     Filter      │     │    Requests     │
└────────┬────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐
│      JWT        │──▶  │ 401 Unauthorized│ (if token invalid)
│     Filter      │     │                 │
└────────┬────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐
│   Controller    │──▶ Validate input, delegate to service
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Service      │──▶ Business logic, orchestration
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │──▶ Data access
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   PostgreSQL    │
└─────────────────┘
```

---

## 4. Layer Architecture

### 4.1 Presentation Layer

**Responsibility**: HTTP request/response handling, input validation, response formatting

| Component | File | Endpoints |
|-----------|------|-----------|
| `UserController` | `controller/UserController.java` | `/api/v1/users/*` |
| `TwoFactorController` | `controller/TwoFactorController.java` | `/api/v1/auth/2fa/*` |

**Design Patterns**:
- RESTful resource naming
- Standardized response wrapper (`ApiResponse<T>`)
- Bean Validation (JSR-380) for input validation
- OpenAPI annotations for documentation

### 4.2 Service Layer

**Responsibility**: Business logic, transaction management, orchestration

| Service | Responsibility | Key Methods |
|---------|---------------|-------------|
| `UserService` | User lifecycle, auth | `register()`, `login()`, `refreshToken()` |
| `TwoFactorService` | TOTP management | `generateSetupData()`, `verifyCode()` |
| `EmailService` | Email delivery | `sendVerificationEmail()`, `sendPasswordResetEmail()` |
| `VerificationTokenService` | Token lifecycle | `createToken()`, `validateToken()` |
| `RefreshTokenService` | JWT refresh | `createRefreshToken()`, `verifyExpiration()` |
| `AuditLogService` | Security audit | `logSuccess()`, `logFailure()` |

**Design Patterns**:
- Constructor injection (immutability)
- `@Transactional` boundaries at service layer
- `@Async` for non-blocking operations

### 4.3 Repository Layer

**Responsibility**: Data access abstraction

| Repository | Entity | Key Queries |
|------------|--------|-------------|
| `UserRepository` | `UserEntity` | `findByEmail()` |
| `RoleRepository` | `Role` | `findByName()` |
| `RefreshTokenRepository` | `RefreshToken` | `findByToken()`, `deleteByUser()` |
| `VerificationTokenRepository` | `VerificationToken` | `findByToken()`, `findByUserAndType()` |
| `TwoFactorBackupCodeRepository` | `TwoFactorBackupCode` | `findByUserAndCode()` |
| `AuditLogRepository` | `AuditLog` | `findByUsernameAndTimestamp()` |

**Design Pattern**: Spring Data JPA with derived queries

### 4.4 Entity Layer

**Responsibility**: Domain model, persistence mapping

See [Section 7: Data Architecture](#7-data-architecture) for entity details.

---

## 5. Component Catalog

### 5.1 Security Components

| Component | Type | Location | Purpose |
|-----------|------|----------|---------|
| `SecurityConfig` | Config | `security/SecurityConfig.java` | Security filter chain configuration |
| `JwtAuthenticationFilter` | Filter | `security/JwtAuthenticationFilter.java` | JWT token validation |
| `RateLimitingFilter` | Filter | `security/RateLimitingFilter.java` | API rate limiting (100 req/min) |
| `SecurityHeadersFilter` | Filter | `security/SecurityHeadersFilter.java` | HTTP security headers |
| `JwtUtil` | Utility | `security/JwtUtil.java` | JWT generation/validation |
| `CustomUserDetailsService` | Service | `security/CustomUserDetailsService.java` | User authentication |
| `HttpRequestAuditInterceptor` | Interceptor | `security/HttpRequestAuditInterceptor.java` | Request auditing |

### 5.2 Configuration Components

| Component | Type | Location | Purpose |
|-----------|------|----------|---------|
| `JwtProperties` | Properties | `config/properties/JwtProperties.java` | JWT configuration |
| `SecurityProperties` | Properties | `config/properties/SecurityProperties.java` | Account locking config |
| `EmailProperties` | Properties | `config/properties/EmailProperties.java` | Email/SMTP config |
| `TotpProperties` | Properties | `config/properties/TotpProperties.java` | 2FA issuer config |
| `OpenApiConfig` | Config | `config/OpenApiConfig.java` | Swagger/OpenAPI setup |
| `AsyncConfig` | Config | `config/AsyncConfig.java` | Async executor config |
| `WebMvcConfig` | Config | `config/WebMvcConfig.java` | MVC interceptors |

### 5.3 Exception Handling

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| `ResourceNotFoundException` | 404 | Entity not found |
| `DuplicateResourceException` | 409 | Email already registered |
| `InvalidCredentialsException` | 401 | Wrong email/password |
| `AccountLockedException` | 423 | Too many failed logins |
| `EmailNotVerifiedException` | 403 | Email not verified |
| `InvalidVerificationTokenException` | 400 | Bad/expired token |
| `TokenExpiredException` | 400 | JWT expired |
| `TwoFactorRequiredException` | 403 | 2FA step required |
| `Invalid2FACodeException` | 401 | Wrong 2FA code |

**Handler**: `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)

---

## 6. Security Architecture

### 6.1 Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     AUTHENTICATION FLOW                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    REGISTRATION FLOW                              │  │
│  │                                                                   │  │
│  │  Register ──▶ Create User ──▶ Send Verification Email ──▶ Done   │  │
│  │                  │                                                │  │
│  │                  ▼                                                │  │
│  │           emailVerified=false                                     │  │
│  │           (Cannot login until verified)                           │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    LOGIN FLOW (Standard)                          │  │
│  │                                                                   │  │
│  │  Login ──▶ Validate Email ──▶ Check Verified ──▶ Check Password  │  │
│  │                                     │                    │        │  │
│  │                                     ▼                    ▼        │  │
│  │                              EmailNotVerified      Invalid ──▶ 401│  │
│  │                               Exception                           │  │
│  │                                                          │        │  │
│  │                                                          ▼        │  │
│  │                                              Check Account Locked │  │
│  │                                                          │        │  │
│  │                              ┌───────────────────────────┤        │  │
│  │                              ▼                           ▼        │  │
│  │                         Locked ──▶ 423         Return JWT Tokens  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    LOGIN FLOW (with 2FA)                          │  │
│  │                                                                   │  │
│  │  Standard Login ──▶ 2FA Enabled? ──▶ Return Partial Token        │  │
│  │                          │                    │                   │  │
│  │                          ▼                    ▼                   │  │
│  │                        No ──▶ Full Token    Client sends 2FA code│  │
│  │                                                   │               │  │
│  │                                                   ▼               │  │
│  │                                         Verify TOTP/Backup Code   │  │
│  │                                                   │               │  │
│  │                                    ┌──────────────┤               │  │
│  │                                    ▼              ▼               │  │
│  │                              Invalid ──▶ 401    Full JWT Token    │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 JWT Token Structure

**Access Token** (15 minute expiry):
```json
{
  "sub": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1707300000,
  "exp": 1707300900
}
```

**Partial Token** (2FA required, 5 minute expiry):
```json
{
  "sub": "user@example.com",
  "type": "2FA_REQUIRED",
  "iat": 1707300000,
  "exp": 1707300300
}
```

**Refresh Token**: Stored in database, 7-day expiry

### 6.3 Security Headers

| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Security-Policy` | `default-src 'self'...` | XSS prevention |
| `Strict-Transport-Security` | `max-age=31536000` | Force HTTPS |
| `X-Frame-Options` | `DENY` | Clickjacking prevention |
| `X-Content-Type-Options` | `nosniff` | MIME sniffing prevention |
| `X-XSS-Protection` | `1; mode=block` | Browser XSS filter |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Referrer control |
| `Permissions-Policy` | `geolocation=()...` | Feature restrictions |
| `Cache-Control` | `no-cache, no-store` | Prevent caching |

### 6.4 Rate Limiting

| Configuration | Value |
|---------------|-------|
| Algorithm | Token Bucket (Bucket4j) |
| Limit | 100 requests/minute |
| Scope | Per IP address |
| IP Detection | `X-Forwarded-For` → Remote Address |
| Response | `429 Too Many Requests` |

### 6.5 Account Protection

| Feature | Configuration |
|---------|---------------|
| Max Failed Attempts | 5 |
| Lock Duration | 15 minutes |
| Password Encoding | BCrypt (strength: 10) |
| Email Verification | Required (blocking) |

### 6.6 CORS Policy

```yaml
Allowed Origins:
  - http://localhost:*
  - https://yourdomain.com

Allowed Methods:
  - GET, POST, PUT, DELETE, OPTIONS, PATCH

Allowed Headers:
  - Authorization, Content-Type

Max Age: 3600 seconds
```

---

## 7. Data Architecture

### 7.1 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     ENTITY RELATIONSHIP DIAGRAM                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐       │
│  │   users     │         │ user_roles  │         │   roles     │       │
│  ├─────────────┤         ├─────────────┤         ├─────────────┤       │
│  │ id (PK)     │◀────────│ user_id(FK) │         │ id (PK)     │       │
│  │ email       │         │ role_id(FK) │────────▶│ name        │       │
│  │ password    │         └─────────────┘         └─────────────┘       │
│  │ email_      │              M:N                                       │
│  │   verified  │                                                        │
│  │ two_factor_ │                                                        │
│  │   enabled   │                                                        │
│  │ two_factor_ │                                                        │
│  │   secret    │                                                        │
│  │ account_    │                                                        │
│  │   locked    │                                                        │
│  │ failed_     │                                                        │
│  │   attempts  │                                                        │
│  │ lock_time   │                                                        │
│  │ created_at  │                                                        │
│  │ updated_at  │                                                        │
│  └──────┬──────┘                                                        │
│         │                                                               │
│         │ 1:N                                                           │
│         │                                                               │
│    ┌────┴────┬────────────────┬───────────────────┐                    │
│    ▼         ▼                ▼                   ▼                    │
│ ┌────────┐ ┌────────────┐ ┌─────────────────┐ ┌──────────────────┐    │
│ │refresh_│ │verification│ │two_factor_      │ │   audit_logs     │    │
│ │tokens  │ │_tokens     │ │backup_codes     │ │                  │    │
│ ├────────┤ ├────────────┤ ├─────────────────┤ ├──────────────────┤    │
│ │id (PK) │ │id (PK)     │ │id (PK)          │ │id (PK)           │    │
│ │user_id │ │user_id(FK) │ │user_id(FK)      │ │username          │    │
│ │token   │ │token       │ │code             │ │action            │    │
│ │expiry_ │ │token_type  │ │used             │ │details           │    │
│ │date    │ │expiry_date │ │used_at          │ │ip_address        │    │
│ └────────┘ │used        │ │created_at       │ │user_agent        │    │
│            │used_at     │ └─────────────────┘ │timestamp         │    │
│            │created_at  │                     │status            │    │
│            └────────────┘                     │error_message     │    │
│                                               │resource_id       │    │
│                                               │resource_type     │    │
│                                               └──────────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Table Specifications

#### `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PK | Auto-generated ID |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | User email (login identifier) |
| `password` | VARCHAR(255) | NOT NULL | BCrypt encoded password |
| `email_verified` | BOOLEAN | DEFAULT false | Verification status |
| `email_verified_at` | TIMESTAMP | NULL | When verified |
| `two_factor_enabled` | BOOLEAN | DEFAULT false | 2FA status |
| `two_factor_secret` | VARCHAR(255) | NULL | TOTP secret |
| `account_locked` | BOOLEAN | DEFAULT false | Lock status |
| `failed_login_attempts` | INTEGER | DEFAULT 0 | Failed attempt count |
| `lock_time` | TIMESTAMP | NULL | When locked |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update |

**Indexes**: `email` (unique)

#### `roles`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PK | Auto-generated ID |
| `name` | VARCHAR(50) | UNIQUE | Role name enum |

**Values**: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MANAGER`

#### `verification_tokens`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PK | Auto-generated ID |
| `user_id` | BIGINT | FK → users | Owner reference |
| `token` | VARCHAR(255) | NOT NULL | UUID token |
| `token_type` | VARCHAR(50) | NOT NULL | EMAIL_VERIFICATION / PASSWORD_RESET |
| `expiry_date` | TIMESTAMP | NOT NULL | Expiration time |
| `used` | BOOLEAN | DEFAULT false | Usage status |
| `used_at` | TIMESTAMP | NULL | When used |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |

**Indexes**: `token`, `user_id`, `token_type`

#### `audit_logs`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PK | Auto-generated ID |
| `username` | VARCHAR(255) | NOT NULL | User identifier |
| `action` | VARCHAR(100) | NOT NULL | Action type |
| `details` | TEXT | NULL | Additional context |
| `ip_address` | VARCHAR(45) | NULL | Client IP |
| `user_agent` | VARCHAR(500) | NULL | Client user-agent |
| `timestamp` | TIMESTAMP | NOT NULL | Event time |
| `status` | VARCHAR(20) | NOT NULL | SUCCESS / FAILURE |
| `error_message` | TEXT | NULL | Error details |
| `resource_id` | VARCHAR(255) | NULL | Affected resource ID |
| `resource_type` | VARCHAR(100) | NULL | Affected resource type |

**Indexes**: `username`, `action`, `timestamp`

### 7.3 Database Configuration

#### Local Development (Port 5433)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/Crud_db
spring.datasource.username=postgres
spring.datasource.password=root
```

#### Docker (Port 5434)

```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/cruddb
spring.datasource.username=postgres
spring.datasource.password=root
```

#### Connection Pool (HikariCP)

| Setting | Value |
|---------|-------|
| Maximum Pool Size | 10 |
| Minimum Idle | 5 |
| Connection Timeout | 30s |
| Idle Timeout | 10m |
| Max Lifetime | 30m |

---

## 8. Infrastructure

### 8.1 Docker Architecture

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:15-alpine
    container_name: crud_postgres_db
    ports:
      - "5434:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]

  app:
    build: .
    container_name: crud_spring_app
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

networks:
  crud_network:
    driver: bridge

volumes:
  postgres_data:
```

### 8.2 Deployment Commands

```bash
# Start all services
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Clean restart (with data reset)
docker-compose down -v && docker-compose up -d --build
```

### 8.3 Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MAIL_HOST` | Yes* | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | Yes* | `587` | SMTP port |
| `MAIL_USERNAME` | Yes* | - | SMTP username |
| `MAIL_PASSWORD` | Yes* | - | SMTP password |
| `MAIL_FROM` | Yes* | `noreply@example.com` | From address |
| `APP_BASE_URL` | Yes* | `http://localhost:8080` | App URL for email links |
| `TOTP_ISSUER` | No | `CrudAPI` | 2FA issuer name |
| `JWT_SECRET` | No | (configured) | JWT signing key (min 256-bit) |

*Required for email features

### 8.4 Health & Monitoring

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Application health status |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus scrape endpoint |
| `/actuator/info` | Application info |

---

## 9. API Design

### 9.1 API Versioning

**Strategy**: URI-based versioning
**Current Version**: `v1`
**Base Path**: `/api/v1`

### 9.2 Endpoint Catalog

#### User Management (`/api/v1/users`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | Public | Register new user |
| POST | `/login` | Public | Authenticate user |
| POST | `/refresh` | Public | Refresh access token |
| POST | `/verify-email` | Public | Verify email address |
| POST | `/resend-verification` | Public | Resend verification email |
| POST | `/forgot-password` | Public | Request password reset |
| POST | `/reset-password` | Public | Reset password with token |
| GET | `/me` | JWT | Get current user |
| PUT | `/me` | JWT | Update current user |
| GET | `/{id}` | JWT + ADMIN | Get user by ID |
| DELETE | `/{id}` | JWT + ADMIN | Delete user |

#### Two-Factor Auth (`/api/v1/auth/2fa`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/setup` | JWT | Get QR code for 2FA setup |
| POST | `/enable` | JWT | Enable 2FA with code |
| POST | `/disable` | JWT | Disable 2FA |
| POST | `/verify` | Partial Token | Verify 2FA during login |
| POST | `/backup-codes` | JWT | Regenerate backup codes |

### 9.3 Response Format

**Success Response**:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-02-07T10:30:00Z"
}
```

**Error Response**:
```json
{
  "timestamp": "2026-02-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users/register",
  "validationErrors": {
    "email": "must be a valid email address"
  },
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 9.4 API Documentation

| Resource | URL |
|----------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI Spec | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

---

## 10. Non-Functional Requirements

### 10.1 Performance

| Metric | Target | Current |
|--------|--------|---------|
| API Response Time (p95) | < 200ms | TBD |
| API Response Time (p99) | < 500ms | TBD |
| Concurrent Users | 1,000 | TBD |
| Requests/Second | 100+ | TBD |

### 10.2 Availability

| Metric | Target |
|--------|--------|
| Uptime SLA | 99.9% |
| Recovery Time Objective (RTO) | 1 hour |
| Recovery Point Objective (RPO) | 1 hour |

### 10.3 Security Requirements

| Requirement | Status |
|-------------|--------|
| OWASP Top 10 Compliance | Implemented |
| SQL Injection Prevention | Parameterized queries |
| XSS Prevention | CSP headers, input validation |
| CSRF Protection | Stateless JWT (no cookies) |
| Rate Limiting | 100 req/min per IP |
| Password Policy | BCrypt, strength 10 |
| Session Management | JWT (15min access, 7d refresh) |
| Audit Logging | All auth events logged |

### 10.4 Scalability Considerations

| Component | Scaling Strategy |
|-----------|------------------|
| Application | Horizontal (stateless) |
| Database | Vertical → Read replicas |
| Email | Queue-based (async) |
| Rate Limiting | Distributed cache (future) |

---

## 11. Key Architecture Decisions

### ADR-001: JWT for Authentication

**Status**: Accepted
**Context**: Need stateless authentication for horizontal scaling
**Decision**: Use JWT with short-lived access tokens (15 min) and refresh tokens (7 days)
**Consequences**:
- (+) Stateless, scalable
- (-) Token revocation requires additional infrastructure

### ADR-002: Email Verification (Blocking)

**Status**: Accepted
**Context**: Prevent account creation with invalid emails
**Decision**: Users cannot login until email is verified
**Consequences**:
- (+) Ensures valid email addresses
- (-) Requires working email infrastructure

### ADR-003: TOTP for 2FA

**Status**: Accepted
**Context**: Need second factor authentication
**Decision**: Use TOTP (RFC 6238) compatible with standard authenticator apps
**Consequences**:
- (+) No SMS costs, works offline
- (-) Requires user to have authenticator app

### ADR-004: Layered Architecture

**Status**: Accepted
**Context**: Need maintainable, testable codebase
**Decision**: Controller → Service → Repository → Entity layering
**Consequences**:
- (+) Clear separation of concerns
- (+) Easy to test each layer independently
- (-) Some boilerplate code

### ADR-005: PostgreSQL as Primary Database

**Status**: Accepted
**Context**: Need reliable, ACID-compliant database
**Decision**: PostgreSQL 15 with HikariCP connection pooling
**Consequences**:
- (+) Robust, feature-rich, excellent tooling
- (-) Requires managing database infrastructure

---

## 12. Appendices

### 12.1 Quick Start

```bash
# Clone repository
git clone <repository-url>
cd test1-crud-api

# Start with Docker
docker-compose up -d --build

# Or run locally (requires PostgreSQL on port 5433)
./mvnw spring-boot:run

# Access API
curl http://localhost:8080/actuator/health

# Access documentation
open http://localhost:8080/swagger-ui.html
```

### 12.2 Configuration Files Reference

| File | Purpose |
|------|---------|
| `application.properties` | Main configuration |
| `docker-compose.yml` | Container orchestration |
| `pom.xml` | Maven dependencies |
| `Dockerfile` | Container build instructions |

### 12.3 Contact

| Role | Contact |
|------|---------|
| Architecture Owner | Engineering Team |
| Security Contact | Security Team |
| Operations | DevOps Team |

---

*This document is maintained as a living document. Update it as the architecture evolves.*

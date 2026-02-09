# Product Requirements Document (PRD)
## Enterprise-Grade CRUD API with Security & Email Verification

**Version:** 2.0.0
**Last Updated:** 2026-02-09
**Status:** ✅ Production Ready
**Author:** Srikanth (@Srikanthnexushub)

---

## 📋 Executive Summary

A production-ready, Fortune 100 enterprise-grade RESTful CRUD API built with Spring Boot 3.2.0, featuring comprehensive security, email verification, 2FA authentication, and 100% test coverage.

### Key Metrics
- **279 Tests Passing** (100% pass rate)
- **60% Code Coverage** with JaCoCo
- **Port:** 3000 (Backend API)
- **Database:** PostgreSQL 15
- **Architecture:** 6-layer immutable architecture

---

## 🎯 Product Vision

Build a secure, scalable, and maintainable REST API that serves as a reference implementation for enterprise-grade applications, demonstrating best practices in:
- Security (OWASP compliance)
- Testing (comprehensive test pyramid)
- Email verification & multi-factor authentication
- DevOps (Docker, CI/CD ready)
- Observability (logging, monitoring, metrics)

---

## 👥 Target Users

1. **End Users**: Register, login, manage profiles
2. **Administrators**: Full user management capabilities
3. **Managers**: Limited administrative access
4. **Developers**: API consumers building integrations
5. **DevOps Engineers**: Deploy and monitor the system

---

## ✨ Core Features

### 1. User Authentication & Authorization

#### 1.1 User Registration
- **Endpoint:** `POST /api/v1/users/register`
- **Requirements:**
  - Valid email format (RFC 5322 compliant)
  - Password: 6-100 characters
  - Automatic `ROLE_USER` assignment
  - BCrypt password hashing (strength 10)
  - **Email verification required** before login
  - Verification email sent automatically
  - Verification token expires in 24 hours

#### 1.2 Email Verification
- **Endpoint:** `POST /api/v1/users/verify-email`
- **Requirements:**
  - Users must verify email before first login
  - Verification link sent via email
  - Token-based verification (UUID)
  - Resend verification option available
  - Clear error messages for expired tokens

#### 1.3 User Login
- **Endpoint:** `POST /api/v1/users/login`
- **Requirements:**
  - Email + password authentication
  - **Email verification check** (blocks unverified users)
  - JWT token generation (1 hour expiry)
  - Refresh token generation (7 days expiry)
  - Rate limiting: 100 requests/minute per IP
  - Account locking after 5 failed attempts (15 min lockout)
  - Optional 2FA verification if enabled

#### 1.4 Two-Factor Authentication (2FA)
- **Setup:** `POST /api/v1/auth/2fa/setup`
- **Enable:** `POST /api/v1/auth/2fa/enable`
- **Verify:** `POST /api/v1/auth/2fa/verify`
- **Disable:** `POST /api/v1/auth/2fa/disable`
- **Backup Codes:** `POST /api/v1/auth/2fa/backup-codes`
- **Requirements:**
  - TOTP-based (Google Authenticator compatible)
  - QR code generation for easy setup
  - 6-digit verification codes
  - 10 backup codes for account recovery
  - Time-synced (30-second window)

#### 1.5 Password Reset
- **Request Reset:** `POST /api/v1/users/forgot-password`
- **Reset Password:** `POST /api/v1/users/reset-password`
- **Requirements:**
  - Email-based password reset
  - Reset token expires in 1 hour
  - Secure token generation
  - Password validation on reset

#### 1.6 Token Refresh
- **Endpoint:** `POST /api/v1/users/refresh`
- **Requirements:**
  - Exchange refresh token for new access token
  - Validate refresh token not expired
  - Invalidate refresh token after use (rotation)

### 2. User Management (CRUD)

#### 2.1 Get All Users
- **Endpoint:** `GET /api/v1/users`
- **Access:** Admin only
- **Returns:** List of users (passwords excluded)

#### 2.2 Get User by ID
- **Endpoint:** `GET /api/v1/users/{id}`
- **Access:** Self or Admin
- **Returns:** User details (password excluded)

#### 2.3 Update User
- **Endpoint:** `PUT /api/v1/users/{id}`
- **Access:** Self or Admin
- **Supports:**
  - Email update
  - Password update (optional)
  - Role update (Admin only)

#### 2.4 Delete User
- **Endpoint:** `DELETE /api/v1/users/{id}`
- **Access:** Admin only
- **Behavior:** Soft delete preferred, hard delete implemented

### 3. Role-Based Access Control (RBAC)

#### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ROLE_USER** | Read own profile, Update own profile |
| **ROLE_MANAGER** | USER permissions + Read all users |
| **ROLE_ADMIN** | MANAGER permissions + Create users, Delete users, Update any user |

---

## 🔒 Security Requirements

### 1. Authentication Security
- ✅ JWT with RS256 or HS256 algorithm
- ✅ Token expiry: 15 minutes (access), 7 days (refresh)
- ✅ Secure token storage (HttpOnly cookies recommended for production)
- ✅ Token refresh mechanism
- ✅ Email verification before login
- ✅ 2FA support (TOTP)

### 2. Password Security
- ✅ BCrypt hashing (strength 10+)
- ✅ Minimum length: 6 characters
- ✅ Maximum length: 100 characters
- ✅ No plaintext storage
- ✅ Secure password reset flow

### 3. Rate Limiting
- ✅ 100 requests per minute per IP
- ✅ 429 Too Many Requests response
- ✅ Retry-After header included
- ✅ Configurable limits per endpoint

### 4. Account Protection
- ✅ Account locking after 5 failed login attempts
- ✅ 15-minute lockout duration
- ✅ Automatic unlock after duration
- ✅ Email notification on suspicious activity (future)

### 5. OWASP Security Headers
```
Content-Security-Policy: default-src 'self'
Strict-Transport-Security: max-age=31536000
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
```

### 6. CORS Configuration
- ✅ Allowed origins: `http://localhost:3000`, `http://localhost:5173`
- ✅ Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- ✅ Allowed headers: Authorization, Content-Type
- ✅ Credentials support: Enabled
- ✅ Max age: 3600 seconds

---

## 📧 Email Features

### 1. Email Verification
- **Trigger:** User registration
- **Template:** HTML email with verification link
- **Expiry:** 24 hours
- **Resend:** Available via dedicated endpoint
- **Blocking:** Users cannot login without verification

### 2. Password Reset
- **Trigger:** Forgot password request
- **Template:** HTML email with reset link
- **Expiry:** 1 hour
- **One-time use:** Token invalidated after use

### 3. Email Configuration
- **SMTP Provider:** Configurable (Gmail, Office365, etc.)
- **TLS/SSL:** Supported
- **Templates:** Thymeleaf-based HTML templates
- **Async Sending:** Non-blocking email delivery
- **Error Handling:** Graceful failure, user still created

### 4. Email Templates
- `verification-email.html` - Email verification
- `password-reset-email.html` - Password reset
- Modern, responsive design
- Branded with application logo/colors

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP,
    two_factor_secret VARCHAR(255),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    backup_codes TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);
```

### User_Roles (Many-to-Many)
```sql
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Verification Tokens Table
```sql
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILURE
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    error_message TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🎨 API Design

### Base URL
```
http://localhost:3000/api/v1
```

### Response Format

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "token": "eyJhbGci...", // if applicable
  "refreshToken": "uuid..." // if applicable
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "details": { ... } // optional
}
```

### Error Codes
- `INVALID_CREDENTIALS` - Wrong email or password
- `EMAIL_NOT_VERIFIED` - User must verify email first
- `ACCOUNT_LOCKED` - Too many failed login attempts
- `TOKEN_EXPIRED` - JWT or verification token expired
- `USER_NOT_FOUND` - User doesn't exist
- `EMAIL_ALREADY_EXISTS` - Duplicate email registration
- `UNAUTHORIZED` - Missing or invalid JWT
- `FORBIDDEN` - Insufficient permissions
- `VALIDATION_ERROR` - Input validation failed
- `2FA_REQUIRED` - 2FA code needed
- `INVALID_2FA_CODE` - 2FA verification failed

---

## 🧪 Testing Requirements

### Test Coverage Goals
- ✅ **Overall:** 60%+ code coverage
- ✅ **Services:** 70%+ coverage
- ✅ **Controllers:** 80%+ coverage
- ✅ **Security:** 90%+ coverage

### Test Pyramid
```
        /\
       /  \      47 E2E Tests (Playwright)
      /    \
     /------\    149 Integration Tests (@SpringBootTest)
    /        \
   /----------\  73 Unit Tests (Mockito + JUnit 5)
  /____________\
```

### Test Categories
1. **Unit Tests (73)**
   - Service layer logic
   - DTO validation
   - Utility functions
   - Security components

2. **Integration Tests (149)**
   - API endpoint testing
   - Database operations
   - Transaction boundaries
   - Exception handling
   - Email service integration

3. **E2E Tests (47)**
   - Complete user workflows
   - Registration → Verification → Login
   - Password reset flow
   - 2FA setup and verification
   - Admin user management

---

## 🚀 Deployment Requirements

### Environment Variables

**Required:**
```bash
DATABASE_URL=jdbc:postgresql://localhost:5434/cruddb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=<secure-password>
JWT_SECRET=<256-bit-secret>
```

**Optional (Email):**
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<email>
MAIL_PASSWORD=<app-password>
MAIL_FROM=noreply@example.com
APP_BASE_URL=http://localhost:3000
```

### Docker Support
- ✅ Multi-stage Dockerfile
- ✅ Docker Compose orchestration
- ✅ PostgreSQL service included
- ✅ Environment variable injection
- ✅ Volume persistence
- ✅ Health checks configured

### System Requirements
- **Java:** 17+
- **Maven:** 3.8+
- **PostgreSQL:** 15+
- **RAM:** 512MB minimum, 2GB recommended
- **CPU:** 1 core minimum, 2+ recommended
- **Storage:** 100MB application + database

---

## 📊 Monitoring & Observability

### 1. Health Checks
- **Endpoint:** `GET /actuator/health`
- **Includes:**
  - Database connectivity
  - Disk space
  - Application status
  - Email service (if configured)

### 2. Metrics
- **Endpoint:** `GET /actuator/metrics`
- **Tracks:**
  - Request count
  - Response times
  - Error rates
  - JVM metrics (memory, threads, GC)

### 3. Audit Logging
- **All actions logged:**
  - User registration
  - Login attempts (success/failure)
  - Password changes
  - User updates/deletes
  - 2FA operations
- **Log fields:**
  - Username
  - Action type
  - Timestamp
  - IP address
  - User agent
  - Status
  - Error details

### 4. Application Logs
- **Location:** `logs/application.log`, `logs/error.log`
- **Format:** Structured JSON logging
- **Levels:** INFO, WARN, ERROR
- **Rotation:** Daily, 30-day retention

---

## 🔄 Future Enhancements

### Phase 2 (Planned)
- [ ] OAuth2 social login (Google, GitHub)
- [ ] User profile pictures
- [ ] Email notifications for security events
- [ ] Password strength meter
- [ ] Session management dashboard
- [ ] API rate limiting per user
- [ ] Webhook support
- [ ] WebSocket notifications

### Phase 3 (Consideration)
- [ ] Multi-tenancy support
- [ ] Advanced RBAC with custom permissions
- [ ] Data export (GDPR compliance)
- [ ] Account deletion workflow
- [ ] Two-factor authentication via SMS
- [ ] Biometric authentication
- [ ] GraphQL API endpoint
- [ ] Microservices architecture

---

## 📏 Performance Requirements

### Response Times
- **Registration:** < 2000ms (includes email sending)
- **Login:** < 1000ms
- **Token Refresh:** < 500ms
- **Get User:** < 500ms
- **Update User:** < 1500ms
- **Delete User:** < 1000ms

### Scalability
- **Concurrent Users:** 100+ simultaneous requests
- **Database Connections:** Pool size 10 (configurable)
- **Rate Limit:** 100 requests/minute per IP
- **Session Management:** Stateless JWT (horizontally scalable)

---

## ✅ Acceptance Criteria

### User Stories

**Story 1: User Registration**
- ✅ User can register with email and password
- ✅ Receives verification email within 5 seconds
- ✅ Cannot login until email is verified
- ✅ Can resend verification email if not received

**Story 2: User Login**
- ✅ Verified user can login with valid credentials
- ✅ Receives JWT access token and refresh token
- ✅ Cannot login with unverified email
- ✅ Cannot login with wrong password (5 attempts max)
- ✅ Account locks after 5 failed attempts

**Story 3: Two-Factor Authentication**
- ✅ User can enable 2FA with QR code
- ✅ Must enter 6-digit code during login
- ✅ Can use backup codes if app unavailable
- ✅ Can disable 2FA when needed

**Story 4: Password Reset**
- ✅ User can request password reset email
- ✅ Receives reset link within 5 seconds
- ✅ Can set new password with valid token
- ✅ Old password no longer works after reset

**Story 5: Admin Management**
- ✅ Admin can view all users
- ✅ Admin can create new users
- ✅ Admin can update user roles
- ✅ Admin can delete users

---

## 📖 Documentation Requirements

- ✅ README.md - Project overview
- ✅ PRD.md - This document
- ✅ STARTUP_GUIDE.md - Quick start reference
- ✅ SECURITY.md - Security practices
- ✅ API_TEST_COVERAGE.md - Test documentation
- ✅ INTERFACE_LAYERS.md - Architecture guide
- ✅ Swagger/OpenAPI - Interactive API docs
- ✅ Inline code comments (Javadoc)
- ✅ Environment setup guides

---

## 🔐 Compliance & Standards

### Security Standards
- ✅ OWASP Top 10 compliance
- ✅ GDPR considerations (data export, deletion)
- ✅ Password security (NIST guidelines)
- ✅ JWT best practices (RFC 7519)
- ✅ Email verification (IETF standards)
- ✅ 2FA standards (RFC 6238 - TOTP)

### Code Quality Standards
- ✅ Java 17 language features
- ✅ Spring Boot best practices
- ✅ Clean code principles
- ✅ SOLID design patterns
- ✅ Immutable architecture (Java Records)
- ✅ Comprehensive error handling

---

## 📞 Support & Maintenance

### Support Channels
- **GitHub Issues:** Bug reports and feature requests
- **Email:** support@example.com
- **Documentation:** All .md files in repository

### Maintenance Schedule
- **Security Updates:** As needed (critical < 24h)
- **Dependency Updates:** Monthly
- **Feature Releases:** Quarterly
- **Bug Fixes:** Bi-weekly

---

## 📝 Change Log

### Version 2.0.0 (2026-02-09)
- ✅ Added email verification (blocking)
- ✅ Added TOTP-based 2FA
- ✅ Added password reset flow
- ✅ Changed port from 8080 to 3000
- ✅ Updated all documentation
- ✅ 100% test pass rate (279 tests)

### Version 1.0.0 (2026-02-06)
- ✅ Initial release
- ✅ User CRUD operations
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ Docker support
- ✅ 60% code coverage

---

**Document Status:** ✅ Complete
**Review Date:** 2026-02-09
**Next Review:** 2026-03-09

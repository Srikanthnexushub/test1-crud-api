# The 5 Steps

## Fortune 100 Engineering Standard for Building Enterprise-Grade Projects

**Used by:** Netflix, Google, Amazon, Tesla, SpaceX, Meta, Apple, Microsoft

---

# STEP 1: DESIGN

## Prompt

```
I want to build [describe your idea in 1-2 sentences].

Target users: [who will use this]
Core problem: [what problem it solves]
Tech stack: [language, framework, database]
Scale target: [requests/sec, users, data volume]
Compliance: [SOC2, GDPR, HIPAA, PCI-DSS, or none]

Create complete architecture documentation:

1. docs/architecture.md
   - System overview and context diagram
   - Component diagram (ASCII/Mermaid)
   - Data flow diagram
   - Layer architecture (controller → service → repository → entity)
   - All components with responsibilities
   - External dependencies and integrations
   - Scaling approach (horizontal/vertical)
   - Failure modes and recovery strategies
   - Technology choices with justification

2. docs/api-spec.yaml (OpenAPI 3.0)
   - All endpoints with methods
   - Request/response schemas with validation rules
   - Authentication method (JWT, OAuth2, API keys)
   - Rate limiting per endpoint
   - Error response format (RFC 7807)
   - Pagination strategy
   - Versioning strategy
   - Examples for each endpoint

3. docs/data-model.md
   - Entity relationship diagram
   - All tables with columns, types, constraints
   - Relationships and foreign keys
   - Indexes (B-tree, GIN, covering)
   - Partitioning strategy (if needed)
   - Data retention policy
   - Backup strategy

4. docs/security.md
   - Threat model (STRIDE analysis)
   - Attack surface mapping
   - Authentication design (MFA, session management)
   - Authorization design (RBAC/ABAC, permission matrix)
   - OWASP Top 10 mitigations
   - Data protection (encryption at rest/transit)
   - Security headers (CSP, HSTS, etc.)
   - Secrets management approach
   - Audit logging requirements
   - Incident response plan outline

5. docs/slo.md (Service Level Objectives)
   - Availability target (99.9%, 99.99%)
   - Latency targets (p50, p95, p99)
   - Error budget definition
   - Throughput requirements
   - Recovery time objective (RTO)
   - Recovery point objective (RPO)

6. docs/capacity-planning.md
   - Expected traffic patterns
   - Peak load estimates
   - Resource requirements (CPU, memory, storage)
   - Cost estimates (cloud infrastructure)
   - Growth projections (6mo, 1yr, 3yr)

7. docs/adr/ (Architecture Decision Records)
   - ADR-001: Technology stack selection
   - ADR-002: Database choice
   - ADR-003: Authentication approach
   - ADR-004: API design philosophy
   - ADR-005: Deployment strategy
   - Each ADR includes: context, decision, consequences, alternatives considered

8. docs/rfc.md (Technical Design Document)
   - Problem statement
   - Proposed solution
   - Detailed design
   - API contracts
   - Data migrations
   - Rollout plan
   - Risk assessment
   - Open questions

Start by asking me questions to clarify requirements.
```

## Parallel Agents (Claude Code will spawn these automatically)

```
Agent 1 → docs/architecture.md + docs/capacity-planning.md
Agent 2 → docs/api-spec.yaml
Agent 3 → docs/data-model.md
Agent 4 → docs/security.md + docs/slo.md
Agent 5 → docs/adr/* + docs/rfc.md
```

## Exit Criteria

```
□ docs/architecture.md exists with system diagrams
□ docs/api-spec.yaml is complete OpenAPI 3.0 (validated)
□ docs/data-model.md has ER diagram with all relationships
□ docs/security.md has STRIDE threat model
□ docs/slo.md defines availability/latency targets
□ docs/capacity-planning.md has resource estimates
□ docs/adr/ has decision records for all major choices
□ docs/rfc.md is complete and reviewed
□ Design review conducted (self or peer)
□ I can explain the entire design without looking at docs
```

## Fortune 100 Standard: Design Review Checklist

```
□ Is the system observable? (metrics, logs, traces)
□ Is the system testable? (unit, integration, e2e)
□ Is the system secure? (defense in depth)
□ Is the system scalable? (10x current load)
□ Is the system resilient? (graceful degradation)
□ Is the system operable? (runbooks, alerts)
□ Is the system maintainable? (clear boundaries)
□ Are failure modes documented?
□ Are rollback procedures defined?
□ Is the blast radius limited?
```

---

# STEP 2: FOUNDATION

## Prompt

```
Architecture docs are complete and reviewed.

Set up the project foundation with Fortune 100 standards:

1. Project Structure
   /src
     /main/java (or your language)
       /config      - configuration classes, feature flags
       /controller  - REST controllers (thin, validation only)
       /service     - business logic (unit testable)
       /repository  - data access (interface-based)
       /entity      - domain models (immutable where possible)
       /dto         - request/response objects (validated)
       /exception   - custom exceptions with error codes
       /security    - auth filters, JWT utilities
       /client      - external service clients
       /event       - event publishers/listeners
       /util        - pure utility functions
     /test
       /unit        - fast, isolated tests
       /integration - database, API tests
       /e2e         - full workflow tests
       /fixtures    - test data factories
   /docs           - all documentation
   /scripts        - automation scripts
   /infra          - infrastructure as code

2. Dependencies (with security in mind)
   - Web framework (latest stable)
   - Database driver + ORM
   - Security/JWT (with refresh token support)
   - Validation (Bean Validation / Joi / Zod)
   - Testing (JUnit5/Jest + Mockito/Jest mocks + Testcontainers)
   - API documentation (Swagger/OpenAPI)
   - Logging (SLF4J/Winston + JSON format)
   - Metrics (Micrometer/prom-client)
   - Tracing (OpenTelemetry)
   - HTTP client (with retry, circuit breaker)
   - Feature flags (Unleash/LaunchDarkly client)

3. docker-compose.yml (local development)
   - Application container (with hot reload)
   - Database container (PostgreSQL/MySQL)
   - Redis (caching, sessions)
   - LocalStack (AWS services mock) OR MinIO (S3)
   - Jaeger (distributed tracing)
   - Prometheus + Grafana (local metrics)
   - Health checks for all services
   - Named volumes for persistence
   - Network isolation

4. Makefile with commands:
   make setup       - install deps, create .env, start containers
   make run         - start application (development mode)
   make test        - run all tests with coverage
   make test-unit   - run unit tests only (fast)
   make test-int    - run integration tests
   make lint        - run linter and formatter
   make security    - run security scans
   make build       - build production artifact
   make docker      - build Docker image
   make clean       - clean build artifacts
   make db-migrate  - run database migrations
   make db-seed     - seed development data

5. CI Pipeline (.github/workflows/ci.yml)
   Triggers: push, pull_request
   Jobs (parallel where possible):

   Job: lint
     - Code formatting check
     - Linting (ESLint/Checkstyle)
     - Type checking

   Job: security
     - Dependency vulnerability scan (Snyk/Dependabot)
     - SAST scan (CodeQL/Semgrep)
     - Secret detection (GitLeaks)
     - License compliance check

   Job: test
     - Unit tests (parallel)
     - Coverage report (minimum 80%)
     - Upload coverage to Codecov/Coveralls

   Job: integration
     - Spin up test containers
     - Run integration tests
     - Run API contract tests

   Job: build
     - Build production artifact
     - Build Docker image
     - Push to registry (on main only)
     - Sign image (cosign)

6. CD Pipeline (.github/workflows/cd.yml)
   Triggers: push to main, manual dispatch

   Job: deploy-staging
     - Deploy to staging (automatic)
     - Run smoke tests
     - Run load tests (baseline)
     - Notify team on Slack

   Job: deploy-production
     - Requires: staging success + manual approval
     - Deploy canary (5%)
     - Automated canary analysis (15 min)
     - Progressive rollout (25% → 50% → 100%)
     - Automated rollback on error spike

7. Database Setup
   - Connection pooling (HikariCP/pg-pool)
   - Migration tool (Flyway/Liquibase/Prisma)
   - Initial migration with complete schema
   - Seed data for development
   - Test fixtures for integration tests
   - Read replica configuration (if needed)
   - Connection encryption (SSL)

8. Observability (from Day 1)
   - Structured logging (JSON format)
   - Correlation ID middleware
   - Metrics endpoint (/metrics)
   - Health endpoints:
     /health/live   - liveness (is process running?)
     /health/ready  - readiness (can accept traffic?)
     /health/startup - startup probe
   - Distributed tracing (OpenTelemetry)
   - Error tracking integration (Sentry)

9. Configuration Management
   - application.properties/yaml with profiles
   - .env.example with ALL variables documented
   - Separate configs: dev, test, staging, prod
   - Feature flags configuration
   - Secrets placeholder (never commit real secrets)

10. README.md
    - Project description and purpose
    - Architecture overview (link to docs)
    - Prerequisites (versions specified)
    - Quick start (< 5 commands)
    - Development workflow
    - Testing guide
    - Deployment guide
    - Contributing guide
    - License

11. Infrastructure as Code (optional but recommended)
    /infra
      /terraform (or /pulumi)
        - VPC/Network configuration
        - Database provisioning
        - Container orchestration (ECS/K8s)
        - Load balancer
        - CDN configuration
        - Secrets management
        - Monitoring setup

Verify: Fresh git clone → `make setup && make run` → working app in under 10 minutes.
```

## Parallel Agents

```
Agent 1 → Project structure + dependencies + Makefile
Agent 2 → docker-compose.yml + database setup
Agent 3 → CI pipeline (.github/workflows/ci.yml)
Agent 4 → CD pipeline (.github/workflows/cd.yml)
Agent 5 → Configuration + README + observability setup
Agent 6 → Infrastructure as Code (if requested)
```

## Exit Criteria

```
□ Project structure matches design exactly
□ All dependencies installed with locked versions
□ docker-compose up -d starts all services
□ make setup completes without errors
□ make run starts the application
□ make test runs and passes (even if just smoke test)
□ make lint passes
□ make security finds no critical issues
□ CI pipeline triggers on push
□ CI pipeline passes (all jobs green)
□ CD pipeline configured with staging + production
□ Database migrations run successfully
□ Health endpoints return 200
□ Metrics endpoint returns Prometheus format
□ Logs are structured JSON
□ README has complete setup instructions
□ New developer can set up in < 10 minutes
□ .env.example has all variables documented
```

## Fortune 100 Standard: Foundation Checklist

```
□ Can I deploy this to production right now? (even if empty)
□ Can I rollback a deployment in < 5 minutes?
□ Can I see what the application is doing? (observability)
□ Can I run tests in CI without external dependencies?
□ Are all secrets external to the codebase?
□ Is the Docker image minimal and secure?
□ Are dependencies pinned to exact versions?
□ Is there a security scan in CI?
```

---

# STEP 3: CODE

## Prompt for First Feature (Authentication - Usually First)

```
Foundation is complete. CI is passing.

Build Feature: User Authentication

Requirements:
- User registration with email validation
- Login with JWT (access token: 15min, refresh token: 7 days)
- Password hashing with BCrypt (cost factor 12)
- Email verification (required before login)
- Password reset via email
- Account lockout after 5 failed attempts
- Refresh token rotation
- Logout (invalidate refresh token)

Security Requirements:
- Rate limiting: 5 requests/minute for auth endpoints
- No user enumeration (same response for exists/not exists)
- Secure password requirements (8+ chars, complexity)
- Audit log all auth events

API Endpoints (from api-spec.yaml):
- POST /api/v1/auth/register - create account
- POST /api/v1/auth/login - get tokens
- POST /api/v1/auth/refresh - refresh access token
- POST /api/v1/auth/logout - invalidate refresh token
- POST /api/v1/auth/verify-email - verify email with token
- POST /api/v1/auth/forgot-password - request reset
- POST /api/v1/auth/reset-password - reset with token
- GET  /api/v1/auth/me - get current user

Build in this order:
1. Entity: User, RefreshToken, EmailVerificationToken
2. Repository: UserRepository, RefreshTokenRepository
3. Service: AuthService, EmailService, TokenService
4. Controller: AuthController
5. Security: JwtFilter, SecurityConfig
6. Tests:
   - Unit tests for AuthService (all methods)
   - Integration tests for all endpoints
   - Security tests (injection, bypass attempts)

Definition of Done:
- All unit tests passing (>90% coverage for auth module)
- All integration tests passing
- Manual test with curl/Postman documented
- API matches api-spec.yaml exactly
- Security review checklist completed
- No hardcoded secrets
- CI passes

Commit when complete with message: "feat(auth): Add user authentication with JWT"
```

## Prompt for Additional Features

```
[Previous Feature] complete and committed.

Build Feature: [Feature Name]

Requirements:
- [Requirement 1 - specific and measurable]
- [Requirement 2 - specific and measurable]
- [Requirement 3 - specific and measurable]

API Endpoints:
- [METHOD] [PATH] - [description]
- [METHOD] [PATH] - [description]

Edge Cases:
- [Edge case 1] → [Expected behavior]
- [Edge case 2] → [Expected behavior]

Build in order: Entity → Repository → Service (with tests) → Controller (with tests)

Definition of Done:
- Unit test coverage > 80% for this feature
- Integration tests for all endpoints
- Error cases tested
- Documentation updated
- CI passes

Commit with: "feat([scope]): [description]"
```

## Prompt for 2FA Feature (Fortune 100 Standard)

```
Authentication complete.

Build Feature: Two-Factor Authentication (2FA)

Requirements:
- TOTP-based (Google Authenticator compatible)
- QR code generation for setup
- 10 backup codes (8 chars, alphanumeric)
- 2FA optional per user (user enables/disables)
- Remember device option (30 days)
- Rate limiting on 2FA verification

API Endpoints:
- POST /api/v1/auth/2fa/setup - get QR code and secret
- POST /api/v1/auth/2fa/enable - enable with verification code
- POST /api/v1/auth/2fa/disable - disable (requires password)
- POST /api/v1/auth/2fa/verify - verify during login
- POST /api/v1/auth/2fa/backup-codes - regenerate backup codes

Modified Login Flow:
1. User submits email/password
2. If valid and 2FA enabled → return { requires2FA: true, tempToken }
3. User submits tempToken + TOTP code
4. If valid → return access + refresh tokens

Build with tests. Commit with: "feat(auth): Add TOTP-based 2FA"
```

## Feature Development Workflow

```
For EACH feature:

1. UNDERSTAND
   - Read the requirements carefully
   - Check api-spec.yaml for exact contract
   - Identify edge cases
   - Ask questions if unclear

2. TEST FIRST (TDD)
   - Write failing unit tests for service layer
   - Write failing integration tests for API
   - Tests define the expected behavior

3. IMPLEMENT
   - Entity/Model layer
   - Repository layer
   - Service layer (make unit tests pass)
   - Controller layer (make integration tests pass)

4. VERIFY
   - All tests pass
   - Coverage meets threshold
   - Manual testing with curl
   - Code review checklist

5. COMMIT
   - Conventional commit message
   - Reference issue if applicable
   - CI must pass

6. NEXT
   - Move to next feature
   - Or fix issues found
```

## Exit Criteria for All Features

```
□ All features from scope are implemented
□ Each feature has unit tests (>80% coverage)
□ Each feature has integration tests
□ Each endpoint has positive and negative tests
□ Test coverage > 80% overall
□ All tests pass locally
□ All tests pass in CI
□ API documentation matches implementation exactly
□ No known bugs or TODO items in critical paths
□ Code review completed for each feature
□ Security review for auth/payment/PII features
□ Performance acceptable (< 200ms p95 for most endpoints)
```

## Fortune 100 Standard: Code Quality Checklist

```
□ No code duplication (DRY)
□ Functions/methods are small (< 20 lines ideal)
□ Clear naming (self-documenting)
□ Error handling is consistent
□ Logging is appropriate (not too much, not too little)
□ No secrets in code
□ Input validation on all endpoints
□ Output encoding where needed
□ SQL queries parameterized (no injection possible)
□ Dependencies are minimal and justified
□ No commented-out code
□ No console.log/print statements (use proper logging)
```

---

# STEP 4: HARDEN

## Prompt

```
All features complete. Tests passing. CI green.

Harden the project to Fortune 100 production standards:

1. Security Hardening (CRITICAL)

   OWASP Top 10 Audit:
   □ A01 Broken Access Control - authorization on every endpoint
   □ A02 Cryptographic Failures - encryption at rest/transit
   □ A03 Injection - parameterized queries, input validation
   □ A04 Insecure Design - threat model review
   □ A05 Security Misconfiguration - headers, defaults
   □ A06 Vulnerable Components - dependency scan
   □ A07 Auth Failures - session management, MFA
   □ A08 Data Integrity - CSRF, signature verification
   □ A09 Logging Failures - security event logging
   □ A10 SSRF - URL validation, allowlists

   Security Headers:
   - Strict-Transport-Security (HSTS)
   - Content-Security-Policy (CSP)
   - X-Content-Type-Options: nosniff
   - X-Frame-Options: DENY
   - X-XSS-Protection: 1; mode=block
   - Referrer-Policy: strict-origin-when-cross-origin
   - Permissions-Policy

   Rate Limiting:
   - Global: 100 req/min per IP
   - Auth endpoints: 5 req/min per IP
   - Sensitive operations: 10 req/min per user
   - Implement with Redis/memory store

   Dependency Security:
   - Run: npm audit / mvn dependency-check / safety
   - Fix all critical and high vulnerabilities
   - Document accepted risks for medium/low
   - Set up automated alerts (Dependabot/Snyk)

2. Penetration Testing

   Automated Scans:
   - DAST scan (OWASP ZAP/Burp Suite)
   - SQL injection testing
   - XSS testing
   - Authentication bypass attempts
   - Authorization bypass attempts

   Manual Testing:
   - Business logic flaws
   - Race conditions
   - IDOR (Insecure Direct Object References)
   - Privilege escalation

   Document all findings in: docs/security-assessment.md

3. Performance Optimization

   Profiling:
   - Profile all endpoints
   - Identify slow queries (> 100ms)
   - Identify memory leaks
   - Identify CPU bottlenecks

   Database:
   - Add missing indexes (check EXPLAIN plans)
   - Optimize N+1 queries
   - Add connection pooling (if not already)
   - Consider read replicas for heavy reads

   Caching:
   - Add cache for frequently accessed data
   - Cache invalidation strategy
   - Cache headers for static content
   - Redis/Memcached for session/data caching

   Target Metrics:
   - p50 < 50ms
   - p95 < 200ms
   - p99 < 500ms

4. Load Testing

   Create load test suite (k6 recommended):

   load-tests/
     smoke.js     - 1 user, verify works
     load.js      - expected traffic (100 users, 10 min)
     stress.js    - find breaking point (ramp to 1000 users)
     spike.js     - sudden traffic spike
     soak.js      - sustained load (8 hours)

   Document Results:
   - Maximum RPS before degradation
   - Error rate at load
   - Latency percentiles at load
   - Resource utilization (CPU, memory)
   - Bottleneck identification

   Save to: docs/load-test-results.md

5. Chaos Engineering

   Failure Injection:
   - Database connection failure
   - External service timeout
   - Memory pressure
   - CPU pressure
   - Network latency injection
   - Pod/instance termination

   Verify:
   - Graceful degradation
   - Circuit breakers activate
   - Retry logic works
   - Alerts fire correctly
   - Recovery is automatic

   Document in: docs/chaos-test-results.md

6. Error Handling

   Standards:
   - RFC 7807 Problem Details format
   - Consistent error codes (documented)
   - No stack traces in production
   - Meaningful messages for clients
   - Detailed logs for debugging

   Example Response:
   {
     "type": "https://api.example.com/errors/validation",
     "title": "Validation Error",
     "status": 400,
     "detail": "Email format is invalid",
     "instance": "/api/v1/users/register",
     "traceId": "abc-123-xyz"
   }

7. Logging & Audit

   Structured Logging:
   - JSON format
   - Timestamp (ISO 8601, UTC)
   - Log level (ERROR, WARN, INFO, DEBUG)
   - Correlation ID (trace ID)
   - Request ID
   - User ID (if authenticated)
   - Service name
   - Message
   - Additional context

   Security Events (must log):
   - Authentication success/failure
   - Authorization failure
   - Password changes
   - 2FA enable/disable
   - Account lockout
   - Admin actions
   - Data access (PII/sensitive)
   - Configuration changes

   Never Log:
   - Passwords (even hashed)
   - Full credit card numbers
   - SSN or equivalent
   - API keys or secrets
   - Session tokens

8. Documentation

   docs/runbook.md:
   - Service overview
   - Architecture diagram
   - Dependencies (internal/external)
   - Deployment procedure
   - Rollback procedure (with commands)
   - Scaling procedure
   - Common issues and solutions
   - Debug procedures
   - Log locations and queries
   - Metrics and dashboards
   - Alert runbooks (for each alert)
   - Emergency contacts
   - Escalation procedures
   - Backup and restore procedures

   docs/incident-response.md:
   - Severity definitions (SEV1-4)
   - Response times by severity
   - Communication templates
   - Post-mortem template
   - Blameless culture guidelines

Start with security hardening. Use parallel agents for independent tasks.
```

## Parallel Agents

```
Agent 1 → OWASP Top 10 audit + Security headers + Dependency scan
Agent 2 → Performance profiling + Database optimization + Caching
Agent 3 → Load test creation + Load test execution
Agent 4 → Error handling standardization + Logging improvements
Agent 5 → Runbook + Incident response documentation
Agent 6 → Chaos engineering tests (after load tests complete)
```

## Exit Criteria

```
□ OWASP Top 10 audit complete (documented)
□ Security headers configured
□ Rate limiting implemented
□ Dependency scan clean (no critical/high)
□ Penetration test complete (no critical findings)
□ Performance profiled and optimized
□ p95 latency < 200ms for all endpoints
□ Load testing complete with documented results
□ Maximum capacity known and documented
□ Chaos tests passed (graceful degradation verified)
□ Error responses follow RFC 7807
□ Structured logging implemented
□ Security events logged
□ Runbook complete and reviewed
□ Incident response plan documented
□ All documentation updated
□ Ready for production traffic at scale
```

## Fortune 100 Standard: Production Readiness Review

```
□ Can we handle 10x current expected traffic?
□ Do we know our breaking point?
□ Do we have runbooks for all alerts?
□ Can on-call engineer debug issues without waking others?
□ Is there a clear escalation path?
□ Are rollback procedures tested?
□ Is data backed up with tested restore?
□ Are all secrets rotatable without downtime?
□ Is the system observable (metrics, logs, traces)?
□ Have we tested failure scenarios?
□ Is documentation accurate and up-to-date?
□ Has security signed off?
```

---

# STEP 5: SHIP

## Prompt

```
Hardening complete. Production Readiness Review passed.

Deploy to production with Fortune 100 standards:

1. Pre-Deployment Checklist

   Code Readiness:
   □ All tests passing (unit, integration, e2e)
   □ Code coverage > 80%
   □ No critical/high security vulnerabilities
   □ Load testing passed
   □ Chaos testing passed
   □ Documentation complete

   Infrastructure Readiness:
   □ Production environment provisioned
   □ Database provisioned and configured
   □ Secrets configured in vault/secrets manager
   □ SSL/TLS certificates valid (auto-renewal)
   □ Domain/DNS configured
   □ CDN configured (static assets)
   □ WAF rules configured
   □ DDoS protection enabled

   Operational Readiness:
   □ Monitoring dashboards created
   □ Alerts configured and tested
   □ On-call rotation scheduled
   □ Runbooks reviewed by on-call team
   □ Rollback procedure documented and tested
   □ Communication channels ready (Slack, PagerDuty)

2. Staging Deployment

   Deploy:
   - Deploy to staging environment
   - Use exact same process as production

   Verify:
   - Run full test suite against staging
   - Run smoke tests
   - Run load test (50% of expected prod traffic)
   - Manual verification of key user journeys
   - Check metrics and logs
   - Verify alerts fire correctly (test alert)

   Bake Time:
   - Staging stable for minimum 24 hours
   - No errors in logs
   - Metrics within expected ranges

   Sign-off:
   - Engineering sign-off
   - QA sign-off (if applicable)
   - Security sign-off (for first deploy or major changes)

3. Production Deployment (Progressive Rollout)

   Phase 1: Canary (5%)
   - Deploy to canary instances
   - Route 5% of traffic to canary
   - Monitor for 15 minutes:
     □ Error rate < 0.1%
     □ Latency p95 < baseline + 10%
     □ No new error types in logs
     □ No alerts firing
   - Automated rollback if metrics degrade

   Phase 2: Small (25%)
   - Increase to 25% of traffic
   - Monitor for 15 minutes
   - Same success criteria
   - Manual checkpoint (on-call confirms)

   Phase 3: Half (50%)
   - Increase to 50% of traffic
   - Monitor for 30 minutes
   - Same success criteria
   - Check resource utilization

   Phase 4: Full (100%)
   - Roll out to all instances
   - Monitor for 2 hours
   - Verify all functionality
   - Declare deployment complete

   Rollback Triggers (automatic):
   - Error rate > 1%
   - Latency p95 > 2x baseline
   - Any SEV1/SEV2 alert
   - Health checks failing

4. Monitoring Setup (If Not Already Done)

   Dashboards:
   - Service overview (golden signals)
   - Request rate, error rate, latency (RED)
   - Resource utilization (CPU, memory, disk, network)
   - Business metrics (signups, transactions, etc.)
   - Dependency health

   Alerts (with runbook links):

   SEV1 (Page immediately):
   - Service down (all instances)
   - Error rate > 10%
   - Database unreachable
   - Data corruption detected

   SEV2 (Page during business hours):
   - Error rate > 1%
   - Latency p95 > 500ms
   - Single instance down
   - Disk > 90%

   SEV3 (Ticket):
   - Error rate > 0.1%
   - Latency p95 > 200ms
   - Memory > 80%
   - Certificate expiring in 30 days

   SEV4 (Review weekly):
   - Dependency deprecation warnings
   - Non-critical test failures
   - Documentation outdated

5. Post-Deployment

   Immediate (first 2 hours):
   - Monitor all dashboards actively
   - Watch error logs
   - Be ready for immediate rollback
   - Respond to any alerts

   First 24 hours:
   - Monitor metrics trends
   - Check for slow degradation
   - Gather initial user feedback
   - Document any issues

   First Week:
   - Review error rates daily
   - Analyze performance trends
   - Address any non-critical issues
   - Update runbooks based on learnings

6. Launch Communication

   Before Launch:
   - Notify stakeholders of deployment window
   - Status page prepared (if applicable)
   - Support team briefed

   During Launch:
   - Real-time updates to team channel
   - Update status page if public

   After Launch:
   - Announce successful deployment
   - Share key metrics
   - Thank the team

7. Post-Mortem (if any issues)

   Template:
   - Incident summary
   - Timeline of events
   - Root cause analysis (5 whys)
   - Impact assessment
   - What went well
   - What could be improved
   - Action items with owners and deadlines

   Principles:
   - Blameless culture
   - Focus on systems, not people
   - Share learnings widely
   - Follow up on action items

Start with pre-deployment checklist.
```

## Parallel Agents (Where Applicable)

```
Agent 1 → Pre-deployment checklist verification
Agent 2 → Staging deployment + testing
Agent 3 → Monitoring dashboard creation
Agent 4 → Alert configuration
Agent 5 → Documentation final review
```

## Exit Criteria

```
□ Pre-deployment checklist complete
□ Staging deployed and stable for 24 hours
□ All sign-offs obtained
□ Production infrastructure ready
□ Monitoring dashboards live
□ Alerts configured and tested
□ On-call rotation active
□ Canary deployment successful
□ Progressive rollout complete (100%)
□ No critical issues in first 2 hours
□ No degradation in first 24 hours
□ Team notified of successful launch
□ Runbook updated with any learnings
□ Post-mortem conducted (if any issues)
```

## Fortune 100 Standard: Ship Checklist

```
□ Would I be comfortable going on vacation after this deploy?
□ Can the on-call engineer handle any issue without me?
□ Is the rollback tested and documented?
□ Do we have visibility into what's happening?
□ Is there a clear owner for post-deploy monitoring?
□ Have we communicated to all stakeholders?
□ Is the system better than it was before?
```

---

# ONGOING OPERATIONS

After Step 5, enter operational mode:

```
DAILY:
□ Check dashboards (5 min)
□ Review overnight alerts
□ Check error log trends
□ Acknowledge/resolve any tickets

WEEKLY:
□ Review metrics trends
□ Review error rates and types
□ Check dependency updates (Dependabot)
□ Address tech debt items
□ Team sync on operational issues

MONTHLY:
□ Security patch review and apply
□ Dependency updates (minor versions)
□ Performance review
□ Cost review
□ Capacity planning update
□ Run load tests
□ Rotate credentials/secrets

QUARTERLY:
□ Major dependency updates
□ Disaster recovery drill
□ Chaos engineering exercises
□ Architecture review
□ Security audit
□ Documentation review
□ Runbook updates

ANNUALLY:
□ Major version upgrades
□ Architecture redesign (if needed)
□ Full security penetration test
□ Compliance audit
□ Team training/certification
□ Tool and process retrospective
```

---

# PARALLEL EXECUTION GUIDE

## When to Parallelize

```
SAFE TO PARALLELIZE (no dependencies):
✓ Multiple documentation files
✓ Different services/features
✓ Different types of tests
✓ Different environments
✓ Independent CI jobs

MUST BE SEQUENTIAL (dependencies):
✗ Step 1 → 2 → 3 → 4 → 5 (overall flow)
✗ Entity → Repository → Service → Controller
✗ Database schema → Migrations → Seed data
✗ Infrastructure → Deploy → Test
```

## Parallel Execution Examples

```
Example: Step 1 (Design) - 5 agents in parallel
┌─────────────────────────────────────────────────────────────┐
│ You: "Create architecture documentation"                    │
│                                                             │
│ Claude Code spawns:                                         │
│ ├── Agent 1 ─→ docs/architecture.md     ─┐                 │
│ ├── Agent 2 ─→ docs/api-spec.yaml       ─┼─→ All complete  │
│ ├── Agent 3 ─→ docs/data-model.md       ─┤   in parallel   │
│ ├── Agent 4 ─→ docs/security.md         ─┤                 │
│ └── Agent 5 ─→ docs/adr/*               ─┘                 │
└─────────────────────────────────────────────────────────────┘

Example: Step 4 (Harden) - 4 agents in parallel
┌─────────────────────────────────────────────────────────────┐
│ You: "Harden the project"                                   │
│                                                             │
│ Claude Code spawns:                                         │
│ ├── Agent 1 ─→ Security audit           ─┐                 │
│ ├── Agent 2 ─→ Performance optimization ─┼─→ All complete  │
│ ├── Agent 3 ─→ Load testing             ─┤   in parallel   │
│ └── Agent 4 ─→ Documentation            ─┘                 │
└─────────────────────────────────────────────────────────────┘
```

---

# ANTI-PATTERNS TO AVOID

```
❌ MEGA-PROMPT
   "Build the entire system"
   → Always break into steps

❌ SKIPPING STEPS
   "Let's skip testing and deploy"
   → Never skip, pay later with interest

❌ NO VERIFICATION
   "Build features 1-10"
   → Verify each before proceeding

❌ CONTEXT AMNESIA
   "Add the login endpoint" (no context)
   → Always provide current state and patterns

❌ HARDCODED EVERYTHING
   Secrets, URLs, configs in code
   → Externalize all configuration

❌ TESTING IN PRODUCTION
   "We'll find bugs when users report them"
   → Test before production, always

❌ HERO DEPLOYS
   Friday 5pm deployments
   → Deploy early in week, with time to monitor

❌ BLAME CULTURE
   "Who wrote this code?"
   → Focus on systems, not people

❌ DOCUMENTATION LATER
   "We'll document when it's done"
   → Document as you build

❌ IGNORING ALERTS
   Alert fatigue from too many
   → Only alert on actionable issues
```

---

# QUICK REFERENCE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    THE 5 STEPS - QUICK REFERENCE                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  STEP 1: DESIGN                                                     │
│    Input:  Idea + requirements                                      │
│    Output: architecture.md, api-spec.yaml, data-model.md,          │
│            security.md, slo.md, adr/, rfc.md                       │
│    Verify: Design review checklist passed                          │
│                                                                     │
│  STEP 2: FOUNDATION                                                 │
│    Input:  Design docs                                              │
│    Output: Project structure, CI/CD, docker-compose, observability │
│    Verify: make setup && make run works in < 10 min                │
│                                                                     │
│  STEP 3: CODE                                                       │
│    Input:  Foundation + feature specs                               │
│    Output: Working features with tests (>80% coverage)             │
│    Verify: All tests pass, CI green                                │
│                                                                     │
│  STEP 4: HARDEN                                                     │
│    Input:  Complete features                                        │
│    Output: Security audit, load tests, chaos tests, runbooks       │
│    Verify: Production readiness review passed                      │
│                                                                     │
│  STEP 5: SHIP                                                       │
│    Input:  Hardened project                                         │
│    Output: Production system with monitoring and alerts            │
│    Verify: Stable for 24 hours, on-call can operate               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

# RULES

1. **Complete each step fully before next step**
2. **Verify ALL exit criteria before proceeding**
3. **Commit after each completed component**
4. **Never skip steps or cut corners**
5. **If something is broken, fix it before proceeding**
6. **Document as you build, not after**
7. **Security is not optional**
8. **Tests are not optional**
9. **Monitoring is not optional**
10. **If in doubt, ask (don't assume)**

---

# METRICS THAT MATTER

```
Fortune 100 Engineering Standards:

RELIABILITY
├── Availability: 99.9% minimum (99.99% for critical)
├── MTTR: < 1 hour (mean time to recover)
├── MTBF: > 30 days (mean time between failures)
└── Error Budget: < 0.1% errors

PERFORMANCE
├── p50 Latency: < 50ms
├── p95 Latency: < 200ms
├── p99 Latency: < 500ms
└── Throughput: Handle 10x expected load

QUALITY
├── Test Coverage: > 80%
├── Critical Bugs: 0 in production
├── Security Vulns: 0 critical/high
└── Tech Debt: < 10% of sprint capacity

VELOCITY
├── Deploy Frequency: Daily (or more)
├── Lead Time: < 1 day (commit to production)
├── Change Failure Rate: < 5%
└── Recovery Time: < 1 hour
```

---

*This is how Netflix, Google, Amazon, Tesla, and SpaceX build software. Follow the process exactly.*

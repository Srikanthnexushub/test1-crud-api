# The 5 Steps

## Fortune 100 Engineering Standard for Building Enterprise-Grade Projects

**Used by:** Netflix, Google, Amazon, Tesla, SpaceX, Meta, Apple, Microsoft, Stripe, Uber

---

# HIDDEN SECRETS OF ELITE ENGINEERING

Before we begin, here are the secrets that separate Fortune 100 from everyone else:

```
SECRET 1: PRE-MORTEM ANALYSIS
  → Before building, imagine the project FAILED completely
  → Write down every reason WHY it failed
  → Design to prevent each failure mode
  → Netflix does this for every major feature

SECRET 2: ERROR BUDGETS (Not Just Uptime)
  → 99.9% uptime = 8.76 hours downtime/year = your ERROR BUDGET
  → Spend it wisely: deploy faster when budget is healthy
  → Freeze deployments when budget is burned
  → Google invented this, everyone copies it

SECRET 3: DARK LAUNCHING
  → Deploy code to production but don't expose to users
  → Test with real traffic, real data, real scale
  → Flip feature flag when ready
  → Facebook/Meta deploys 1000+ times/day this way

SECRET 4: GAME DAYS
  → Scheduled chaos: intentionally break production
  → Teams practice incident response
  → Find weaknesses before real outages
  → Amazon does this before every Prime Day

SECRET 5: BLAST RADIUS LIMITATION
  → Every change affects minimum possible users
  → Cell-based architecture
  → Regional isolation
  → If something breaks, only 1% of users affected

SECRET 6: THE THREE QUESTIONS
  → Before any deploy, answer:
     1. How do I know it's working? (observability)
     2. How do I know it's broken? (alerting)
     3. How do I fix it fast? (rollback)
  → If you can't answer all 3, don't deploy

SECRET 7: TRUNK-BASED DEVELOPMENT
  → No long-lived branches (max 1-2 days)
  → Feature flags, not feature branches
  → Everyone commits to main
  → Merge conflicts become impossible

SECRET 8: BREAK GLASS PROCEDURES
  → Documented ways to bypass normal processes
  → For emergencies only
  → Audited and reviewed after use
  → Every Fortune 100 has these

SECRET 9: SYNTHETIC MONITORING
  → Fake users constantly testing production
  → Detect outages before real users
  → Test critical paths every minute
  → You find problems, not customers

SECRET 10: BLAMELESS CULTURE
  → Incidents are system failures, not people failures
  → "What" happened, not "who" did it
  → Share learnings widely
  → Psychological safety = faster recovery
```

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
   - Cell-based architecture (if applicable)
   - Bulkhead patterns for isolation

2. docs/api-spec.yaml (OpenAPI 3.0)
   - All endpoints with methods
   - Request/response schemas with validation rules
   - Authentication method (JWT, OAuth2, API keys)
   - Rate limiting per endpoint
   - Error response format (RFC 7807)
   - Pagination strategy (cursor-based for scale)
   - Versioning strategy (URL vs header)
   - Deprecation policy
   - Examples for each endpoint
   - Idempotency keys for mutations

3. docs/data-model.md
   - Entity relationship diagram
   - All tables with columns, types, constraints
   - Relationships and foreign keys
   - Indexes (B-tree, GIN, covering, partial)
   - Partitioning strategy (range, hash, list)
   - Sharding strategy (if needed)
   - Data retention policy
   - Soft delete vs hard delete strategy
   - Audit columns (created_at, updated_at, deleted_at, created_by)
   - Schema evolution strategy (expand-contract pattern)
   - Backup strategy with tested restore

4. docs/security.md
   - Threat model (STRIDE analysis for each component)
   - Attack surface mapping
   - Authentication design (MFA, session management, token rotation)
   - Authorization design (RBAC/ABAC, permission matrix)
   - OWASP Top 10 mitigations (specific to your app)
   - Data classification (public, internal, confidential, restricted)
   - Encryption at rest (AES-256) and transit (TLS 1.3)
   - Key management strategy
   - Secrets rotation policy (90 days)
   - Security headers (CSP, HSTS, etc.)
   - Audit logging requirements
   - Incident response plan
   - Data breach notification procedures

5. docs/slo.md (Service Level Objectives)
   - Availability target (99.9%, 99.95%, 99.99%)
   - Latency targets (p50, p95, p99 per endpoint)
   - Error rate targets (< 0.1%)
   - Error budget calculation
   - Error budget policy (what happens when burned)
   - Throughput requirements
   - Recovery time objective (RTO)
   - Recovery point objective (RPO)
   - Dependency SLOs (what you depend on)
   - SLI definitions (how to measure)

6. docs/capacity-planning.md
   - Expected traffic patterns (daily, weekly, seasonal)
   - Peak load estimates (Black Friday, launches)
   - Resource requirements per 1000 RPS
   - Cost estimates (cloud infrastructure)
   - Growth projections (6mo, 1yr, 3yr)
   - Capacity testing targets (10x current)
   - Auto-scaling configuration
   - Reserved vs spot instance strategy

7. docs/pre-mortem.md (SECRET WEAPON)
   - Imagine the project failed completely
   - List every reason it could have failed
   - Category: Technical, Operational, Business, People
   - Mitigation strategy for each failure mode
   - Risk score (likelihood × impact)
   - Owner for each risk
   - Review schedule (monthly)

8. docs/adr/ (Architecture Decision Records)
   - ADR-001: Technology stack selection
   - ADR-002: Database choice (SQL vs NoSQL, which vendor)
   - ADR-003: Authentication approach (JWT vs session)
   - ADR-004: API design philosophy (REST vs GraphQL)
   - ADR-005: Deployment strategy (K8s vs ECS vs Lambda)
   - ADR-006: Observability stack
   - ADR-007: Feature flag system
   - ADR-008: Caching strategy
   - Each ADR: context, decision, consequences, alternatives

9. docs/rfc.md (Technical Design Document)
   - Problem statement
   - Proposed solution
   - Detailed design with diagrams
   - API contracts
   - Data migrations (expand-contract)
   - Rollout plan (percentage-based)
   - Rollback plan (specific commands)
   - Risk assessment
   - Dependencies on other teams
   - Timeline with milestones
   - Success metrics
   - Open questions
   - Reviewer sign-offs

10. docs/feature-flags.md
    - Flag taxonomy:
      - Release flags (temporary, ship features)
      - Ops flags (permanent, circuit breakers)
      - Experiment flags (A/B testing)
      - Permission flags (entitlements)
    - Naming convention
    - Lifecycle (creation → testing → rollout → cleanup)
    - Stale flag policy (remove after 30 days)
    - Testing with flags

Start by asking me questions to clarify requirements.
```

## Hidden Secret: The Pre-Mortem Exercise

```
Before designing, do this exercise:

1. Imagine it's 6 months from now
2. The project has FAILED spectacularly
3. Write a post-mortem for this fictional failure

Ask yourself:
- What technical decisions caused the failure?
- What operational gaps caused the failure?
- What did we not anticipate?
- What dependencies failed us?
- What security incident occurred?
- What performance issue killed us?

Now design to prevent EVERY failure you imagined.

This is how Amazon, Google, and Netflix avoid disasters.
```

## Parallel Agents

```
Agent 1 → docs/architecture.md + docs/capacity-planning.md
Agent 2 → docs/api-spec.yaml
Agent 3 → docs/data-model.md
Agent 4 → docs/security.md + docs/slo.md
Agent 5 → docs/adr/* + docs/rfc.md
Agent 6 → docs/pre-mortem.md + docs/feature-flags.md
```

## Exit Criteria

```
□ docs/architecture.md exists with system diagrams
□ docs/api-spec.yaml is complete OpenAPI 3.0 (validated)
□ docs/data-model.md has ER diagram with all relationships
□ docs/security.md has STRIDE threat model per component
□ docs/slo.md defines availability/latency with error budgets
□ docs/capacity-planning.md has 10x capacity estimates
□ docs/pre-mortem.md has failure modes and mitigations
□ docs/adr/ has decision records for all major choices
□ docs/rfc.md is complete with sign-offs
□ docs/feature-flags.md has flag strategy
□ Design review conducted
□ I can explain the entire design without looking at docs
□ I know what will break first under load
□ I know how to detect every failure mode
□ I know how to recover from every failure mode
```

## Fortune 100 Standard: Design Review Checklist

```
OBSERVABILITY:
□ Can I see every request flowing through the system?
□ Can I trace errors to root cause in < 5 minutes?
□ Are there dashboards for every SLI?
□ Are there runbooks for every alert?

TESTABILITY:
□ Can every component be tested in isolation?
□ Can I test with production-like data?
□ Can I test failure scenarios?
□ Is there a contract test for every integration?

SECURITY:
□ Is there defense in depth (multiple layers)?
□ Is principle of least privilege applied?
□ Are all secrets external and rotatable?
□ Is there an audit trail for sensitive operations?

SCALABILITY:
□ Can this handle 10x current expected load?
□ Are there no single points of failure?
□ Can components scale independently?
□ Is the database designed for growth?

RESILIENCE:
□ What happens when each dependency fails?
□ Are there circuit breakers?
□ Are there timeouts on every external call?
□ Is there graceful degradation?

OPERABILITY:
□ Can I deploy with zero downtime?
□ Can I rollback in < 5 minutes?
□ Are there health checks for load balancers?
□ Is there a runbook for common issues?

MAINTAINABILITY:
□ Are boundaries between components clear?
□ Is the code organized for team autonomy?
□ Are there clear ownership boundaries?
□ Is there documentation for complex logic?

BLAST RADIUS:
□ Can a bug affect all users?
□ Is there cell-based isolation?
□ Are feature flags used for new code?
□ Is the blast radius documented?
```

---

# STEP 2: FOUNDATION

## Prompt

```
Architecture docs are complete and reviewed.

Set up the project foundation with Fortune 100 standards:

1. Project Structure (The Golden Path)
   /src
     /main/java (or your language)
       /config         - configuration classes, feature flags
       /controller     - REST controllers (thin, validation only)
       /service        - business logic (unit testable)
       /repository     - data access (interface-based)
       /entity         - domain models (immutable where possible)
       /dto            - request/response objects (validated)
       /exception      - custom exceptions with error codes
       /security       - auth filters, JWT utilities
       /client         - external service clients (with circuit breakers)
       /event          - event publishers/listeners (async)
       /job            - scheduled jobs, background tasks
       /util           - pure utility functions (stateless)
       /middleware     - request/response interceptors
       /metrics        - custom metrics collectors
     /test
       /unit           - fast, isolated, no I/O
       /integration    - database, external services
       /contract       - API contract tests (Pact)
       /e2e            - full workflow tests
       /fixtures       - test data factories
       /chaos          - failure injection tests
     /docs             - all documentation
     /scripts          - automation scripts
     /infra            - infrastructure as code
     /load-tests       - k6/Gatling scripts

2. Dependencies (Security-First)
   Core:
   - Web framework (latest stable, patch auto-update)
   - Database driver + ORM (prepared statements only)
   - Validation (strict input validation)
   - JSON (safe deserialization, no arbitrary types)

   Security:
   - JWT (with refresh token rotation)
   - Password hashing (BCrypt/Argon2, cost 12+)
   - Rate limiting (Redis-backed)
   - CORS (strict origin validation)

   Observability:
   - Logging (SLF4J/Winston, JSON format)
   - Metrics (Micrometer/prom-client)
   - Tracing (OpenTelemetry)
   - Error tracking (Sentry)

   Resilience:
   - Circuit breaker (Resilience4j/opossum)
   - Retry with exponential backoff
   - Timeout on all external calls
   - Bulkhead pattern

   Testing:
   - Unit (JUnit5/Jest)
   - Mocking (Mockito/Jest mocks)
   - Containers (Testcontainers)
   - Contract (Pact)
   - Load (k6)

   Feature Flags:
   - Unleash/LaunchDarkly/Flagsmith client

3. docker-compose.yml (Production Parity)
   Application:
   - App container (hot reload in dev)
   - Same base image as production
   - Same JVM flags as production

   Data:
   - PostgreSQL (same version as prod)
   - Redis (caching, sessions, rate limiting)
   - Elasticsearch (if needed for search)

   Observability:
   - Jaeger (distributed tracing)
   - Prometheus (metrics collection)
   - Grafana (dashboards)
   - Loki (log aggregation)

   AWS/Cloud Mocks:
   - LocalStack (S3, SQS, SNS, SES)
   - MinIO (S3 alternative)

   Configuration:
   - Health checks (all services)
   - Named volumes (persistence)
   - Network isolation
   - Resource limits (match production ratios)

4. Makefile (Developer Experience)
   Setup:
   - make setup      - full environment setup
   - make install    - dependencies only
   - make dev-env    - start containers only

   Development:
   - make run        - start app (dev mode)
   - make run-prod   - start app (prod mode locally)
   - make watch      - run with hot reload

   Testing:
   - make test       - all tests
   - make test-unit  - unit only (< 30 seconds)
   - make test-int   - integration only
   - make test-e2e   - end-to-end
   - make test-load  - load tests
   - make test-chaos - chaos tests
   - make coverage   - with coverage report

   Quality:
   - make lint       - linting
   - make format     - auto-format code
   - make security   - security scans
   - make audit      - dependency audit

   Database:
   - make db-up      - start database
   - make db-migrate - run migrations
   - make db-rollback- rollback last migration
   - make db-seed    - seed data
   - make db-reset   - drop + migrate + seed

   Build:
   - make build      - production build
   - make docker     - build docker image
   - make publish    - push to registry

   Deploy:
   - make deploy-staging  - deploy to staging
   - make deploy-prod     - deploy to production (with confirmation)
   - make rollback        - rollback last deployment

5. CI Pipeline (.github/workflows/ci.yml)

   Triggers: push, pull_request

   Job: quality-gate (runs first, fast fail)
     - Formatting check (fail fast)
     - Linting (fail fast)
     - Type checking (fail fast)
     Duration: < 1 minute

   Job: security (parallel)
     - Dependency vulnerability scan (Snyk/Dependabot)
     - SAST scan (CodeQL/Semgrep)
     - Secret detection (GitLeaks/TruffleHog)
     - License compliance (FOSSA)
     - Container scan (Trivy)
     Duration: < 3 minutes

   Job: unit-tests (parallel)
     - Run unit tests (parallelized)
     - Coverage report
     - Coverage gate (80% minimum)
     - Mutation testing (optional, nightly)
     Duration: < 2 minutes

   Job: integration-tests (parallel)
     - Spin up test containers
     - Run integration tests
     - Run contract tests
     Duration: < 5 minutes

   Job: build (after tests pass)
     - Build production artifact
     - Build Docker image
     - Sign image (cosign/notation)
     - SBOM generation
     - Push to registry (on main only)
     Duration: < 3 minutes

   Job: deploy-preview (on PR)
     - Deploy to ephemeral environment
     - Run smoke tests
     - Add preview URL to PR comment
     - Auto-cleanup after merge

6. CD Pipeline (.github/workflows/cd.yml)

   Trigger: push to main

   Job: deploy-staging (automatic)
     - Deploy to staging
     - Run smoke tests
     - Run synthetic tests
     - Run load test baseline
     - Notify team on Slack

   Job: deploy-canary (after staging)
     - Deploy to 5% of production
     - Automated canary analysis (15 min):
       □ Compare error rates (new vs old)
       □ Compare latencies (p50, p95, p99)
       □ Compare resource usage
     - Auto-rollback if degradation > 10%
     - Promote or rollback based on analysis

   Job: deploy-production (after canary success)
     - Requires: canary success
     - Progressive rollout:
       - 25% (monitor 10 min)
       - 50% (monitor 10 min)
       - 100%
     - Feature flag activation (if applicable)
     - Notify team on completion

   Job: post-deploy
     - Tag release
     - Update changelog
     - Notify stakeholders
     - Update status page

7. Database Setup (Zero-Downtime Migrations)

   Connection:
   - Connection pooling (HikariCP/pgBouncer)
   - Max pool: 10-20 per instance
   - Connection timeout: 5s
   - Idle timeout: 10 min
   - SSL required

   Migrations:
   - Flyway/Liquibase/Prisma
   - Expand-contract pattern:
     Phase 1: Add new (column, table)
     Phase 2: Dual write (old + new)
     Phase 3: Backfill data
     Phase 4: Switch reads to new
     Phase 5: Remove old
   - Never rename columns directly
   - Never drop columns in same deploy
   - Test migrations on production copy

   Safety:
   - Migration lock timeout: 10s
   - Statement timeout: 30s (prevent long locks)
   - Always reversible migrations
   - Migration testing in CI

8. Observability (The Three Pillars + Events)

   Metrics (/metrics):
   - RED method: Rate, Errors, Duration
   - USE method: Utilization, Saturation, Errors
   - Business metrics (signups, transactions)
   - Custom metrics (feature flag usage)
   - Histogram buckets: 5ms, 10ms, 25ms, 50ms, 100ms, 250ms, 500ms, 1s, 2.5s, 5s, 10s

   Logs (structured JSON):
   {
     "timestamp": "2024-01-15T10:30:00.000Z",
     "level": "INFO",
     "message": "User login successful",
     "service": "auth-service",
     "version": "1.2.3",
     "traceId": "abc123",
     "spanId": "def456",
     "userId": "user_123",
     "requestId": "req_789",
     "duration_ms": 45,
     "extra": { ... }
   }

   Traces (OpenTelemetry):
   - Auto-instrumentation for frameworks
   - Custom spans for business operations
   - Sampling: 100% errors, 10% success (adjust based on volume)
   - Context propagation (W3C Trace Context)

   Events (Audit Log):
   - Authentication events
   - Authorization failures
   - Data changes (CRUD operations on sensitive data)
   - Admin actions
   - Configuration changes

   Health Endpoints:
   - /health/live    - liveness (process running)
   - /health/ready   - readiness (can serve traffic)
   - /health/startup - startup complete
   - /health/deep    - deep health (checks dependencies)

9. Configuration (12-Factor)

   Hierarchy:
   1. Default config (in code)
   2. Environment-specific file (dev, staging, prod)
   3. Environment variables (overrides)
   4. Feature flags (runtime overrides)

   Files:
   - application.yaml (defaults)
   - application-dev.yaml
   - application-staging.yaml
   - application-prod.yaml
   - .env.example (ALL variables documented)

   Secrets:
   - NEVER in code or config files
   - Use: AWS Secrets Manager / HashiCorp Vault / GCP Secret Manager
   - Rotate every 90 days
   - Different secrets per environment

   Feature Flags:
   - Use for all new features
   - Percentage rollouts
   - User targeting
   - Kill switch for every external call

10. README.md (5-Minute Setup)

    # Project Name

    One paragraph description.

    ## Quick Start
    ```bash
    git clone <repo>
    cd <repo>
    make setup  # This does everything
    # App running at http://localhost:8080
    ```

    ## Architecture
    [Link to /docs/architecture.md]

    ## Development
    - `make run` - start development server
    - `make test` - run all tests
    - `make lint` - check code quality

    ## Deployment
    - Merge to main → auto-deploy to staging
    - Manual approval → deploy to production

    ## Documentation
    - [Architecture](/docs/architecture.md)
    - [API Spec](/docs/api-spec.yaml)
    - [Runbook](/docs/runbook.md)

    ## Contributing
    [Link to CONTRIBUTING.md]

11. Infrastructure as Code

    /infra
      /terraform
        /modules
          /vpc          - network configuration
          /database     - RDS/Aurora
          /cache        - ElastiCache
          /compute      - ECS/EKS/Lambda
          /cdn          - CloudFront
          /dns          - Route53
          /secrets      - Secrets Manager
          /monitoring   - CloudWatch/Datadog
        /environments
          /dev
          /staging
          /prod
        main.tf
        variables.tf
        outputs.tf

    Principles:
    - All infrastructure in code
    - Environment parity (same modules, different vars)
    - State in S3 with locking (DynamoDB)
    - Plan before apply
    - No manual changes ever

Verify: git clone → make setup → working app in < 5 minutes.
```

## Hidden Secret: Developer Experience Metrics

```
Track these metrics for your developer setup:

TIME TO FIRST COMMIT
- Clone → running app → first commit
- Target: < 15 minutes
- Netflix: < 10 minutes

TIME TO TEST
- Code change → tests complete
- Target: < 2 minutes (unit), < 5 minutes (all)
- Google: < 5 minutes total

FEEDBACK LOOP
- How fast does developer know if code is broken?
- Target: < 30 seconds for linting
- Target: < 2 minutes for unit tests

ENVIRONMENT PARITY
- How similar is local to production?
- Target: Identical containers, configs, data
- Stripe: 100% parity

These metrics determine engineering velocity.
Slow setup = slow team = slow company.
```

## Exit Criteria

```
□ Project structure matches design exactly
□ All dependencies installed with locked versions
□ docker-compose up -d starts all services
□ make setup completes in < 5 minutes
□ make run starts the application
□ make test runs and passes
□ make lint passes
□ make security finds no critical issues
□ CI pipeline triggers on push and passes
□ CD pipeline configured with progressive rollout
□ Database migrations work (up and down)
□ Health endpoints return 200
□ Metrics endpoint returns Prometheus format
□ Logs are structured JSON with trace IDs
□ Feature flag system configured
□ Preview environments work on PRs
□ README allows 5-minute setup
□ .env.example has all variables documented
□ New developer productive in < 1 hour
```

---

# STEP 3: CODE

## Prompt for First Feature (Authentication)

```
Foundation is complete. CI is passing.

Build Feature: User Authentication

Requirements:
- User registration with email validation
- Login with JWT (access: 15min, refresh: 7 days)
- Password hashing (BCrypt cost 12 / Argon2id)
- Email verification (required before login)
- Password reset via email (token expires in 1 hour)
- Account lockout after 5 failed attempts (15 min cooldown)
- Refresh token rotation (old token invalidated on use)
- Logout (invalidate all sessions option)
- Session management (view active sessions, revoke)

Security Requirements:
- Rate limiting: 5 requests/minute for auth endpoints
- No user enumeration (same response time for exists/not exists)
- Password requirements: 12+ chars, complexity, not in breach database
- Timing-safe comparisons
- Audit log all auth events
- Suspicious activity detection (new device, new location)

API Endpoints:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/logout
- POST /api/v1/auth/logout-all
- POST /api/v1/auth/refresh
- POST /api/v1/auth/verify-email
- POST /api/v1/auth/resend-verification
- POST /api/v1/auth/forgot-password
- POST /api/v1/auth/reset-password
- POST /api/v1/auth/change-password
- GET  /api/v1/auth/me
- GET  /api/v1/auth/sessions
- DELETE /api/v1/auth/sessions/{id}

Build Order:
1. Entity: User, RefreshToken, EmailVerificationToken, PasswordResetToken, LoginAttempt
2. Repository: All repositories with custom queries
3. Service: AuthService, EmailService, TokenService, SessionService
4. Controller: AuthController
5. Security: JwtFilter, RateLimitFilter, SecurityConfig
6. Events: AuthEventPublisher (for audit log)
7. Tests: Unit (90%+ coverage), Integration (all endpoints), Security

Definition of Done:
- All tests passing
- Rate limiting verified
- Timing attack prevention verified
- Audit logs verified
- API matches spec exactly
- Security review completed
- CI passes

Commit: "feat(auth): Add secure user authentication"
```

## Prompt for 2FA (Two-Factor Authentication)

```
Authentication complete.

Build Feature: Two-Factor Authentication (2FA)

Requirements:
- TOTP-based (RFC 6238, Google Authenticator compatible)
- QR code generation (with issuer name, account name)
- 10 backup codes (8 chars, alphanumeric, one-time use)
- 2FA optional per user (enable/disable)
- Remember device (30 days, fingerprint-based)
- Rate limiting (5 attempts per 15 min)
- Recovery flow (backup codes, admin reset)

Security:
- Secret encrypted at rest
- Backup codes hashed (BCrypt)
- Window of 1 for TOTP (30 seconds before/after)
- Device fingerprint secure hash

API Endpoints:
- POST /api/v1/auth/2fa/setup      - get QR code
- POST /api/v1/auth/2fa/enable     - verify and enable
- POST /api/v1/auth/2fa/disable    - requires password
- POST /api/v1/auth/2fa/verify     - during login
- POST /api/v1/auth/2fa/backup-codes/regenerate
- POST /api/v1/auth/2fa/backup-codes/verify
- POST /api/v1/auth/2fa/remember-device
- GET  /api/v1/auth/2fa/remembered-devices
- DELETE /api/v1/auth/2fa/remembered-devices/{id}

Modified Login Flow:
1. POST /login with email/password
2. If valid + 2FA enabled:
   Return: { requires2FA: true, tempToken: "...", methods: ["totp", "backup"] }
3. POST /2fa/verify with tempToken + code
4. Return: { accessToken, refreshToken }

Commit: "feat(auth): Add TOTP-based 2FA with backup codes"
```

## Prompt for RBAC (Role-Based Access Control)

```
2FA complete.

Build Feature: Role-Based Access Control (RBAC)

Requirements:
- Roles: SUPER_ADMIN, ADMIN, MANAGER, USER
- Permissions: resource:action format (user:read, user:write, etc.)
- Role hierarchy (SUPER_ADMIN > ADMIN > MANAGER > USER)
- Custom permissions per user (additions/removals)
- Permission caching (Redis, 5 min TTL)
- Audit log permission checks

Permission Matrix:
SUPER_ADMIN: *:* (all permissions)
ADMIN: user:*, role:read, audit:read
MANAGER: user:read, user:list, report:*
USER: user:read (self only), profile:*

API Endpoints:
- GET  /api/v1/admin/roles
- POST /api/v1/admin/roles
- PUT  /api/v1/admin/roles/{id}
- DELETE /api/v1/admin/roles/{id}
- GET  /api/v1/admin/permissions
- PUT  /api/v1/admin/users/{id}/role
- PUT  /api/v1/admin/users/{id}/permissions

Annotations/Decorators:
@RequirePermission("user:read")
@RequireRole("ADMIN")
@RequireOwnership // user can only access own resources

Commit: "feat(auth): Add RBAC with permission matrix"
```

## Feature Development Workflow (The Secret Sauce)

```
For EACH feature, follow this EXACT workflow:

1. UNDERSTAND (30 min)
   □ Read requirements completely
   □ Check api-spec.yaml for contracts
   □ Identify edge cases
   □ Identify failure modes
   □ Ask questions if ANYTHING is unclear
   □ Update api-spec.yaml if needed

2. DESIGN (15 min)
   □ Sketch data model changes
   □ Sketch service interfaces
   □ Identify what can break
   □ Identify what to log
   □ Identify what to measure

3. TEST FIRST - TDD (varies)
   □ Write failing unit tests for happy path
   □ Write failing unit tests for error cases
   □ Write failing unit tests for edge cases
   □ Write failing integration tests
   □ Tests define the contract

4. IMPLEMENT (varies)
   □ Entity/Model layer
   □ Repository layer
   □ Service layer → unit tests pass
   □ Controller layer → integration tests pass
   □ Add metrics (counters, histograms)
   □ Add logging (info for happy path, warn/error for issues)
   □ Add feature flag (if new feature)

5. HARDEN (30 min)
   □ Add input validation
   □ Add rate limiting (if user-facing)
   □ Add authorization checks
   □ Add audit logging (if sensitive)
   □ Review for OWASP Top 10

6. DOCUMENT (15 min)
   □ Update API spec if changed
   □ Add inline comments for complex logic
   □ Update README if needed
   □ Add runbook entry if new failure mode

7. VERIFY (30 min)
   □ All tests pass
   □ Coverage meets threshold
   □ Manual test with curl/Postman
   □ Check logs are correct
   □ Check metrics are emitting
   □ Feature flag works (on/off)
   □ CI passes

8. COMMIT
   □ Conventional commit message
   □ Reference issue/ticket
   □ Small, focused commit
   □ CI must pass

Time per feature: 2-8 hours depending on complexity
```

## Hidden Secret: Contract Testing

```
Contract tests prevent integration failures.

WHAT: Tests that verify API contracts between services
WHY: Catch breaking changes before deployment
HOW: Pact, Spring Cloud Contract, or similar

Example Flow:
1. Consumer defines expected contract
2. Provider verifies it can fulfill contract
3. Contract stored in broker
4. CI checks contracts before merge

Without contracts:
- Service A changes response
- Service B breaks in production
- 3am pages, angry customers

With contracts:
- Service A tries to change response
- CI fails: "Breaking change detected"
- Developer fixes before merge
- Everyone sleeps peacefully
```

## Hidden Secret: Mutation Testing

```
Mutation testing finds weak tests.

WHAT: Automatically modify your code, check if tests fail
WHY: Tests that don't fail on bugs are useless
HOW: PITest (Java), Stryker (JS), mutmut (Python)

Example:
Original code: if (age >= 18) { allow() }
Mutation 1:    if (age > 18) { allow() }  // Changed >= to >
Mutation 2:    if (age >= 17) { allow() } // Changed 18 to 17

If your tests don't catch these mutations, they're weak.

Target: 70%+ mutation score
Netflix: Runs mutation testing nightly
```

## Exit Criteria for All Features

```
□ All features from scope implemented
□ Each feature follows TDD workflow
□ Unit test coverage > 80% overall
□ Unit test coverage > 90% for auth/security
□ Integration tests for every endpoint
□ Negative tests for every endpoint
□ Contract tests for external integrations
□ Feature flags for every new feature
□ Metrics for every feature
□ Logging for every feature
□ No known bugs
□ No TODO in critical paths
□ Security review for sensitive features
□ Performance acceptable (p95 < 200ms)
□ CI passes
```

---

# STEP 4: HARDEN

## Prompt

```
All features complete. Tests passing. CI green.

Harden to Fortune 100 production standards:

1. SECURITY HARDENING (Week 1)

   OWASP Top 10 Audit:
   □ A01: Broken Access Control
     - Authorization check on EVERY endpoint
     - Object-level authorization (user can only access own data)
     - Disable directory listing
     - Deny by default
   □ A02: Cryptographic Failures
     - TLS 1.3 only
     - AES-256 for data at rest
     - No sensitive data in URLs
     - Secure random number generation
   □ A03: Injection
     - Parameterized queries EVERYWHERE
     - ORM usage (no raw SQL)
     - Input validation with allowlists
     - Output encoding
   □ A04: Insecure Design
     - Threat model review
     - Rate limiting
     - Resource limits
   □ A05: Security Misconfiguration
     - Remove default accounts
     - Remove unnecessary features
     - Error messages don't leak info
     - Security headers configured
   □ A06: Vulnerable Components
     - Dependency scan (zero critical/high)
     - SBOM generated
     - Auto-update for patches
   □ A07: Auth Failures
     - MFA available
     - Password policy enforced
     - Session management secure
     - Credential stuffing protection
   □ A08: Software/Data Integrity
     - Code signing
     - Dependency verification
     - CI/CD pipeline security
   □ A09: Logging Failures
     - Security events logged
     - Logs tamper-proof
     - Alerting on anomalies
   □ A10: SSRF
     - URL validation
     - Allowlist for external calls
     - No user-controlled URLs in server requests

   Security Headers:
   Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
   Content-Security-Policy: default-src 'self'; script-src 'self'
   X-Content-Type-Options: nosniff
   X-Frame-Options: DENY
   X-XSS-Protection: 0  # Disabled, use CSP instead
   Referrer-Policy: strict-origin-when-cross-origin
   Permissions-Policy: camera=(), microphone=(), geolocation=()
   Cache-Control: no-store (for sensitive data)

   Rate Limiting (Redis-backed):
   - Global: 1000 req/min per IP
   - Auth: 5 req/min per IP
   - API: 100 req/min per user
   - Admin: 30 req/min per user
   - Response: 429 with Retry-After header

   Dependency Security:
   □ Run Snyk/Dependabot
   □ Fix all critical/high
   □ Document accepted medium/low
   □ Set up automated scanning
   □ Generate SBOM

2. PENETRATION TESTING (Week 1)

   Automated:
   □ OWASP ZAP scan
   □ Burp Suite scan
   □ SQLMap for injection
   □ Nikto for web server
   □ Nuclei for known vulns

   Manual Testing:
   □ Authentication bypass attempts
   □ Authorization bypass (IDOR)
   □ Business logic flaws
   □ Race conditions
   □ File upload attacks (if applicable)
   □ API abuse
   □ Session management
   □ Information disclosure

   Document: docs/security-assessment.md

3. PERFORMANCE OPTIMIZATION (Week 1)

   Profiling:
   □ Profile all endpoints
   □ Identify slow queries (EXPLAIN ANALYZE)
   □ Identify N+1 queries
   □ Identify memory leaks
   □ Identify CPU bottlenecks

   Database:
   □ Add missing indexes
   □ Optimize slow queries
   □ Add covering indexes for common queries
   □ Add partial indexes for filtered queries
   □ Connection pooling tuned
   □ Query result caching (Redis)

   Application:
   □ Add caching (Redis)
   □ Cache invalidation strategy
   □ Compression enabled (gzip/brotli)
   □ HTTP/2 enabled
   □ Connection keep-alive
   □ Async processing for heavy tasks

   Targets:
   - p50 < 50ms
   - p95 < 200ms
   - p99 < 500ms
   - Max memory: 512MB per instance
   - Max CPU: 50% at expected load

4. LOAD TESTING (Week 2)

   k6 Test Suite:
   /load-tests
     /scripts
       smoke.js        - 1 VU, 30s, verify works
       average-load.js - 50 VU, 10m, normal traffic
       stress.js       - ramp to 200 VU, find limits
       spike.js        - 0→200→0 VU, sudden traffic
       soak.js         - 50 VU, 4h, find memory leaks
       breakpoint.js   - ramp until failure

   Metrics to Capture:
   - Requests per second (RPS)
   - Error rate (%)
   - Latency (p50, p95, p99)
   - CPU usage (%)
   - Memory usage (MB)
   - Database connections
   - Response time distribution

   Acceptance Criteria:
   □ 100 RPS with p95 < 200ms
   □ Error rate < 0.1% at expected load
   □ No errors at 2x expected load
   □ Graceful degradation at 5x load
   □ Recovery after spike

   Document: docs/load-test-results.md

5. CHAOS ENGINEERING (Week 2)

   Failure Injection:
   □ Database down (kill connection)
   □ Database slow (add latency)
   □ Redis down
   □ External API timeout
   □ External API error (500)
   □ Memory pressure (fill heap)
   □ CPU pressure (busy loop)
   □ Network partition
   □ DNS failure
   □ Instance termination

   Verify:
   □ Circuit breakers activate
   □ Graceful degradation
   □ No cascading failures
   □ Proper error responses
   □ Alerts fire
   □ Logs capture failure
   □ Automatic recovery

   Game Day (once before production):
   □ Schedule 2-hour window
   □ Inject failures in staging
   □ Team responds as if real
   □ Document learnings
   □ Fix gaps found

   Document: docs/chaos-test-results.md

6. ERROR HANDLING (Week 2)

   RFC 7807 Format:
   {
     "type": "https://api.example.com/errors/validation",
     "title": "Validation Error",
     "status": 400,
     "detail": "The email field is not a valid email address",
     "instance": "/api/v1/users/register",
     "traceId": "abc-123",
     "errors": [
       {
         "field": "email",
         "code": "INVALID_FORMAT",
         "message": "Must be a valid email"
       }
     ]
   }

   Error Codes:
   - Documented in api-spec.yaml
   - Consistent across all endpoints
   - Machine-readable (UPPER_SNAKE_CASE)
   - Include trace ID for debugging

   Principles:
   □ No stack traces to clients (ever)
   □ Log full details server-side
   □ Client gets actionable message
   □ Same response time for success/error (prevent timing attacks)

7. LOGGING & AUDIT (Week 2)

   Structured Logging:
   {
     "timestamp": "2024-01-15T10:30:00.000Z",
     "level": "INFO",
     "message": "User login successful",
     "logger": "AuthService",
     "service": "crud-api",
     "version": "1.2.3",
     "environment": "production",
     "traceId": "abc123",
     "spanId": "def456",
     "requestId": "req_789",
     "userId": "user_123",
     "action": "LOGIN",
     "duration_ms": 145,
     "ip": "masked",
     "userAgent": "Mozilla/5.0...",
     "success": true
   }

   Security Audit Events (MUST log):
   - AUTH_LOGIN_SUCCESS
   - AUTH_LOGIN_FAILURE
   - AUTH_LOGOUT
   - AUTH_PASSWORD_CHANGE
   - AUTH_PASSWORD_RESET
   - AUTH_2FA_ENABLE
   - AUTH_2FA_DISABLE
   - AUTH_SESSION_CREATE
   - AUTH_SESSION_REVOKE
   - AUTHZ_ACCESS_DENIED
   - ADMIN_USER_CREATE
   - ADMIN_USER_UPDATE
   - ADMIN_USER_DELETE
   - ADMIN_ROLE_CHANGE
   - DATA_EXPORT
   - DATA_DELETE
   - CONFIG_CHANGE

   NEVER Log:
   □ Passwords (plain or hashed)
   □ Credit card numbers
   □ SSN/National ID
   □ API keys/secrets
   □ Session tokens
   □ PII unless required and encrypted

   Log Retention:
   - Security events: 1 year
   - Application logs: 30 days
   - Debug logs: 7 days

8. DOCUMENTATION (Week 2)

   docs/runbook.md:
   - Service overview
   - Architecture diagram
   - Dependencies
   - Common operations:
     - Deploy new version
     - Rollback
     - Scale up/down
     - Restart service
     - Clear cache
     - Database maintenance
   - Troubleshooting guide:
     - High latency
     - High error rate
     - Memory issues
     - Database issues
   - Alert runbooks (for each alert)
   - Emergency contacts
   - Escalation matrix

   docs/incident-response.md:
   - Severity definitions:
     SEV1: Service down, data loss (page immediately)
     SEV2: Major degradation (page during hours)
     SEV3: Minor issue (next business day)
     SEV4: Cosmetic (sprint backlog)
   - Response procedures
   - Communication templates
   - Post-mortem template
   - Blameless culture guidelines
   - Stakeholder notification matrix

Start with security hardening. Run parallel agents for independent tasks.
```

## Hidden Secret: Error Budget Policy

```
Error budget is how Google runs reliable systems.

WHAT:
- 99.9% availability = 0.1% error budget = 8.76 hours/year
- Track actual errors against budget
- Make decisions based on remaining budget

POLICY EXAMPLE:
If error budget > 50%:
  - Deploy freely
  - Run experiments
  - Take risks

If error budget 20-50%:
  - Require extra review
  - Limit risky changes
  - Focus on stability

If error budget < 20%:
  - Freeze non-critical deployments
  - Focus on reliability
  - All hands on stability

If error budget = 0%:
  - Complete deployment freeze
  - Only emergency fixes
  - Incident review required

This is how you balance velocity and reliability.
```

## Hidden Secret: Synthetic Monitoring

```
Don't wait for users to report outages.

WHAT: Fake users constantly testing production
HOW: Datadog Synthetics, Pingdom, custom scripts

TESTS:
Every 1 minute:
- Health check endpoint
- Login flow
- Critical transaction
- Key API endpoints

Every 5 minutes:
- Full user journey
- All integrations
- Performance baseline

ALERTS:
- 2 consecutive failures → page on-call
- Latency > 2x baseline → warning
- Any region failing → page on-call

You detect outages in 1-2 minutes, not 30.
```

## Exit Criteria

```
□ OWASP Top 10 audit complete, all mitigated
□ Security scan clean (0 critical/high)
□ Penetration test complete, no critical findings
□ All endpoints profiled
□ p95 < 200ms for all endpoints
□ Load test: 100 RPS at < 0.1% error rate
□ Stress test: breaking point documented
□ Chaos tests passed, graceful degradation verified
□ Error responses follow RFC 7807
□ All security events logged
□ Runbook complete and reviewed by on-call
□ Incident response documented
□ Error budget policy defined
□ Synthetic monitoring configured
□ Ready for production traffic
```

---

# STEP 5: SHIP

## Prompt

```
Hardening complete. Production Readiness Review passed.

Deploy to production with Fortune 100 standards:

1. PRE-DEPLOYMENT CHECKLIST

   Code Readiness:
   □ All tests passing (unit, integration, e2e)
   □ Test coverage > 80%
   □ No critical/high security vulnerabilities
   □ Load testing passed
   □ Chaos testing passed
   □ Performance targets met
   □ Documentation complete and reviewed
   □ Feature flags in place

   Infrastructure Readiness:
   □ Production environment provisioned
   □ Database provisioned (correct size/IOPS)
   □ Read replicas configured (if needed)
   □ Redis/cache configured
   □ Secrets in vault (not in code/config)
   □ SSL/TLS certificates valid
   □ Domain/DNS configured
   □ CDN configured (static assets)
   □ WAF rules configured
   □ DDoS protection enabled
   □ Auto-scaling configured
   □ Backup configured and tested

   Operational Readiness:
   □ Monitoring dashboards created
   □ Alerts configured and tested
   □ On-call rotation scheduled
   □ PagerDuty/Opsgenie configured
   □ Runbooks reviewed by on-call team
   □ Rollback procedure tested
   □ Communication channels ready
   □ Status page prepared

   Stakeholder Readiness:
   □ Product sign-off
   □ Security sign-off
   □ Operations sign-off
   □ Launch date communicated
   □ Support team briefed

2. STAGING DEPLOYMENT

   Deploy:
   □ Deploy exact production build to staging
   □ Use identical process as production

   Verify (automated):
   □ Health checks passing
   □ Smoke tests passing
   □ Integration tests passing
   □ Synthetic tests passing

   Verify (manual):
   □ Key user journeys work
   □ No visual regressions
   □ Performance acceptable
   □ Logs look correct
   □ Metrics are emitting

   Load Test:
   □ Run load test at 50% expected prod traffic
   □ Verify no errors
   □ Verify latency targets met

   Bake Time:
   □ Staging stable for 24 hours minimum
   □ No errors in logs
   □ No alerts firing
   □ Metrics within expected ranges

   Sign-offs:
   □ Engineering: Tests pass, metrics good
   □ QA: Manual testing complete
   □ Security: No new vulnerabilities
   □ Product: Acceptance criteria met

3. PRODUCTION DEPLOYMENT (The Netflix Way)

   Phase 0: Dark Launch (if applicable)
   - Deploy with feature flag OFF
   - Verify deployment successful
   - No user impact
   - Monitor for 15 minutes

   Phase 1: Canary (1-5%)
   - Enable feature flag for 5% of users
   - OR route 5% traffic to new version
   - Monitor for 15 minutes:
     □ Error rate < baseline + 0.1%
     □ Latency p95 < baseline + 10%
     □ No new error types
     □ No alerts firing
   - Automated rollback if:
     □ Error rate > baseline + 1%
     □ Latency > baseline + 50%
     □ Any SEV1 alert

   Phase 2: Small Ring (10-25%)
   - Increase to 25%
   - Monitor for 15 minutes
   - Same success criteria
   - On-call confirms metrics

   Phase 3: Large Ring (50%)
   - Increase to 50%
   - Monitor for 30 minutes
   - Same success criteria
   - Check resource utilization
   - Verify auto-scaling works

   Phase 4: Full (100%)
   - Roll out to all traffic
   - Monitor for 2 hours actively
   - Monitor for 24 hours passively
   - Declare deployment complete

   Rollback Triggers (automated):
   - Error rate > 5% (immediate)
   - Error rate > 1% for 5 minutes
   - Latency p95 > 2x baseline for 5 minutes
   - Any SEV1 alert
   - Health checks failing > 30 seconds
   - Deployment timeout (30 minutes)

   Rollback Procedure:
   1. Automated detection triggers
   2. Route traffic to old version (instant)
   3. Page on-call with details
   4. Investigate in low-traffic window
   5. Never rollback database migrations (use expand-contract)

4. MONITORING & ALERTING

   Dashboards:
   - Service Overview (the golden signals):
     □ Request rate (RPS)
     □ Error rate (%)
     □ Latency (p50, p95, p99)
     □ Saturation (CPU, memory)
   - Business Metrics:
     □ Active users
     □ Signups
     □ Key transactions
   - Dependencies:
     □ Database response time
     □ External API status
     □ Cache hit rate
   - Infrastructure:
     □ Instance count
     □ CPU per instance
     □ Memory per instance
     □ Disk usage

   Alert Hierarchy:

   SEV1 - PAGE IMMEDIATELY (24/7):
   - Service completely down
   - Error rate > 10%
   - Data corruption detected
   - Security incident
   - Database unreachable
   → Response: 5 minutes
   → Resolution target: 30 minutes

   SEV2 - PAGE DURING HOURS:
   - Error rate > 1%
   - Latency p95 > 500ms
   - Partial service degradation
   - Single instance down
   - Disk > 90%
   → Response: 15 minutes
   → Resolution target: 4 hours

   SEV3 - TICKET (Next Business Day):
   - Error rate > 0.1%
   - Latency p95 > 200ms
   - Non-critical feature degraded
   - Memory > 80%
   - Certificate expires in 30 days
   → Resolution target: 1 week

   SEV4 - REVIEW WEEKLY:
   - Warning thresholds
   - Dependency deprecations
   - Tech debt indicators
   → Resolution target: Sprint

   Alert Rules:
   - Every alert has a runbook link
   - Every alert is actionable
   - No alert should fire more than once per on-call
   - Alert on symptoms, not causes
   - Use multi-window burn rate alerts (SRE book)

5. POST-DEPLOYMENT

   First 2 Hours (active monitoring):
   □ Watch all dashboards
   □ Check error logs every 15 min
   □ Respond to any alerts
   □ Be ready for instant rollback
   □ Stay in "war room" if team

   First 24 Hours:
   □ Monitor metrics trends
   □ Check for slow degradation
   □ Review error logs
   □ Gather user feedback
   □ Document any issues

   First Week:
   □ Daily metrics review
   □ Address non-critical issues
   □ Remove feature flags (if appropriate)
   □ Update runbooks with learnings
   □ Performance baseline update

   Post-Launch Cleanup:
   □ Remove old code paths
   □ Clean up temporary flags
   □ Archive old dashboards
   □ Update documentation
   □ Close launch tickets

6. COMMUNICATION

   Before Launch:
   - Stakeholder email: "Deploying X on [date/time]"
   - Team Slack: "Deployment starting"
   - Status page: "Scheduled maintenance" (if applicable)

   During Launch:
   - Team Slack updates every phase
   - Status page updates (if user-facing change)
   - Escalation if issues found

   After Success:
   - Team Slack: "Deployment complete, all metrics green"
   - Stakeholder email: "X is now live, metrics attached"
   - Celebration (if warranted)

   If Issues:
   - Status page: Incident declared
   - Stakeholder notification
   - Communication lead assigned
   - Updates every 30 minutes (or per severity)

7. POST-MORTEM (Blameless)

   Trigger: Any SEV1/SEV2 or rollback

   Timeline: Within 48 hours of resolution

   Template:
   # Incident Post-Mortem: [Title]
   Date: [Date]
   Duration: [X hours Y minutes]
   Severity: [SEV1/SEV2]
   Author: [Name]
   Reviewers: [Names]

   ## Summary
   [2-3 sentences describing what happened]

   ## Impact
   - Users affected: [number]
   - Revenue impact: [$X]
   - Duration of impact: [X minutes]

   ## Timeline
   - HH:MM - First alert fired
   - HH:MM - On-call paged
   - HH:MM - Investigation started
   - HH:MM - Root cause identified
   - HH:MM - Mitigation applied
   - HH:MM - Service recovered
   - HH:MM - Incident closed

   ## Root Cause
   [Detailed explanation of the actual root cause]

   ## Contributing Factors
   - [Factor 1]
   - [Factor 2]
   - [Factor 3]

   ## What Went Well
   - [Thing 1]
   - [Thing 2]

   ## What Could Be Improved
   - [Thing 1]
   - [Thing 2]

   ## Action Items
   | Action | Owner | Due Date | Priority |
   |--------|-------|----------|----------|
   | [Action 1] | [Name] | [Date] | P0 |
   | [Action 2] | [Name] | [Date] | P1 |

   ## Lessons Learned
   [What the team learned]

   Principles:
   - BLAMELESS: Focus on systems, not people
   - TRANSPARENT: Share widely
   - ACTIONABLE: Every finding has an owner
   - FOLLOW-UP: Track action items to completion
```

## Hidden Secret: Feature Flag Rollout

```
The fastest rollback is a feature flag toggle.

INSTANT ROLLBACK:
- Code is deployed but behind flag
- Flag is OFF for all users
- Turn ON for 1% → 10% → 50% → 100%
- Issue found? Turn OFF immediately (milliseconds)
- No deployment needed

TARGETING:
- Internal users first (dogfooding)
- Beta users second
- 1% random users
- Ramp by country/region
- Ramp by customer tier

SAFETY:
- Kill switch for every external dependency
- Circuit breaker as flag
- Instant disable without deploy

This is how Facebook ships 1000+ times per day.
```

## Hidden Secret: The Launch Checklist

```
Elite teams use checklists. Period.

PRE-LAUNCH (1 week before):
□ Feature complete and tested
□ Load testing passed
□ Security review passed
□ Documentation complete
□ Runbook written
□ Alerts configured
□ On-call briefed
□ Rollback tested
□ Stakeholders informed

LAUNCH DAY:
□ Check all systems green
□ Verify rollback works
□ Deploy to staging
□ Wait for bake time
□ Begin production rollout
□ Monitor every phase
□ Celebrate when done

POST-LAUNCH (1 week after):
□ Review metrics
□ Address feedback
□ Clean up flags
□ Write retrospective
□ Thank the team

Use the checklist. Every time. No exceptions.
```

## Exit Criteria

```
□ Pre-deployment checklist complete
□ Staging stable for 24 hours
□ All sign-offs obtained
□ Canary deployment successful
□ Progressive rollout complete (100%)
□ No alerts during rollout
□ No rollback needed
□ Active monitoring for 2 hours complete
□ Passive monitoring for 24 hours complete
□ No degradation detected
□ Team notified of successful launch
□ Runbook updated with learnings
□ Post-mortem conducted (if any issues)
□ Feature flags cleaned up
□ Documentation finalized
□ Stakeholders informed
```

---

# ONGOING OPERATIONS

## The Daily/Weekly/Monthly Cadence

```
DAILY (10 minutes):
□ Check dashboard (2 min)
  - Any alerts last 24h?
  - Error rate trend?
  - Latency trend?
□ Check error logs (3 min)
  - New error types?
  - Error volume normal?
□ Check tickets (5 min)
  - Any new issues?
  - Any escalations?

WEEKLY (1 hour):
□ Metrics deep dive (20 min)
  - Week-over-week trends
  - SLO compliance
  - Error budget status
□ Dependency review (15 min)
  - Check Dependabot alerts
  - Review pending updates
  - Check CVE alerts
□ On-call handoff (15 min)
  - Review last week's incidents
  - Share learnings
  - Update runbook if needed
□ Tech debt review (10 min)
  - Prioritize debt items
  - Schedule fixes

MONTHLY (4 hours):
□ SLO review (1 hour)
  - Are targets appropriate?
  - Error budget status
  - Adjust if needed
□ Security patches (1 hour)
  - Apply pending patches
  - Test after patching
□ Performance review (1 hour)
  - Compare to baseline
  - Identify regressions
  - Optimize if needed
□ Capacity review (1 hour)
  - Growth projections
  - Scaling plans
  - Cost optimization
□ Load testing
  - Run full suite
  - Compare to baseline
□ Secret rotation
  - Rotate any due secrets
  - Update where needed

QUARTERLY (1-2 days):
□ Disaster recovery drill
  - Simulate region failure
  - Test failover
  - Document results
□ Chaos engineering
  - Game day
  - Team practice
□ Architecture review
  - Is design still appropriate?
  - Plan improvements
□ Security audit
  - Penetration test
  - Compliance check
□ Major dependency updates
  - Framework upgrades
  - Database upgrades
□ Documentation review
  - Is it still accurate?
  - Update where needed
□ Runbook review
  - Are procedures current?
  - Run through scenarios

ANNUALLY:
□ Major version upgrades
□ Architecture redesign (if needed)
□ Full security penetration test
□ Compliance audit (SOC2, etc.)
□ Team training/certification
□ Tool and process retrospective
□ Disaster recovery full test
□ Business continuity review
```

## Error Budget Policy

```
GREEN (Budget > 50%):
- Deploy freely
- Run experiments
- Innovate

YELLOW (Budget 20-50%):
- Extra review for risky changes
- No experiments
- Focus on stability

RED (Budget < 20%):
- Only critical fixes deployed
- Reliability focus
- All hands on stability

EXHAUSTED (Budget = 0%):
- Complete freeze
- Emergency fixes only
- Incident review required
- Leadership escalation
```

---

# PARALLEL EXECUTION GUIDE

## Safe to Parallelize

```
STEP 1 (Design) - 6 agents:
├── Agent 1 → architecture.md + capacity-planning.md
├── Agent 2 → api-spec.yaml
├── Agent 3 → data-model.md
├── Agent 4 → security.md + slo.md
├── Agent 5 → adr/* + rfc.md
└── Agent 6 → pre-mortem.md + feature-flags.md

STEP 2 (Foundation) - 6 agents:
├── Agent 1 → Project structure + dependencies
├── Agent 2 → docker-compose.yml + database
├── Agent 3 → CI pipeline
├── Agent 4 → CD pipeline
├── Agent 5 → Configuration + README
└── Agent 6 → Infrastructure as Code

STEP 3 (Code) - Parallel features:
├── Agent 1 → Auth feature
├── Agent 2 → 2FA feature (after auth)
├── Agent 3 → Core feature A
├── Agent 4 → Core feature B
└── ... (independent features in parallel)

STEP 4 (Harden) - 6 agents:
├── Agent 1 → Security hardening
├── Agent 2 → Performance optimization
├── Agent 3 → Load testing
├── Agent 4 → Chaos engineering
├── Agent 5 → Error handling + logging
└── Agent 6 → Documentation

STEP 5 (Ship) - Sequential mostly:
├── Staging deployment (sequential)
├── Production deployment (sequential)
├── Agent 1 → Dashboard creation (parallel)
├── Agent 2 → Alert configuration (parallel)
└── Agent 3 → Documentation finalization (parallel)
```

## Must Be Sequential

```
OVERALL FLOW:
Step 1 → Step 2 → Step 3 → Step 4 → Step 5

WITHIN FEATURES:
Entity → Repository → Service → Controller

DATABASE:
Schema → Migrations → Seed → Test data

DEPLOYMENT:
Staging → Canary → Progressive → Full

TESTING:
Unit → Integration → E2E → Load → Chaos
```

---

# ANTI-PATTERNS (What NOT to Do)

```
❌ THE BIG BANG
"Let's build everything then test at the end"
→ Iterate. Test continuously. Ship incrementally.

❌ THE COWBOY
"I'll just push directly to production"
→ Always use pull requests. Always use CI. Always use staging.

❌ THE HERO
"Only I understand this code"
→ Documentation. Pair programming. Knowledge sharing.

❌ THE HOARDER
"I'll keep this branch alive for 3 months"
→ Trunk-based development. Feature flags. Ship small.

❌ THE OPTIMIST
"Testing in production is fine"
→ Test before production. Synthetic monitoring. Canary deployments.

❌ THE OSTRICH
"That alert always fires, ignore it"
→ Fix or remove. Every alert must be actionable.

❌ THE BLAMER
"Who broke production?"
→ Blameless culture. Focus on systems. Learn together.

❌ THE PERFECTIONIST
"We need 100% coverage before shipping"
→ 80% is fine. Ship and iterate. Perfect is the enemy of good.

❌ THE FIREFIGHTER
"We're always putting out fires"
→ Invest in reliability. Error budgets. Root cause fixes.

❌ THE MAGICIAN
"It works on my machine"
→ Container parity. Environment consistency. Infrastructure as code.
```

---

# THE SECRET SAUCE

## What Separates Good from Great

```
GOOD ENGINEERS:
- Write code that works
- Test their code
- Deploy carefully
- Fix bugs when found

GREAT ENGINEERS (Fortune 100):
- Write code that can't fail (or fails gracefully)
- Test failure modes, not just success
- Deploy so safely rollback is never needed
- Fix systems so bugs can't exist
- Design for the failure they haven't imagined yet
- Measure everything, guess nothing
- Automate everything, manually do nothing
- Document as they build, not after
- Share knowledge before being asked
- Treat production like their child
```

## The Three Questions (Before Any Change)

```
1. HOW DO I KNOW IT'S WORKING?
   - Metrics that show success
   - Logs that tell the story
   - Traces that show the flow
   - Alerts that detect issues

2. HOW DO I KNOW IT'S BROKEN?
   - Alerts that page me
   - Error rates that spike
   - Latencies that increase
   - Users that complain

3. HOW DO I FIX IT FAST?
   - Rollback in < 5 minutes
   - Feature flag off instantly
   - Runbook for every scenario
   - On-call that knows the system

If you can't answer all three, you're not ready to ship.
```

---

# METRICS THAT MATTER

```
THE DORA METRICS (Elite Performance):
┌─────────────────────────────────────┬───────────────┐
│ Metric                              │ Elite Target  │
├─────────────────────────────────────┼───────────────┤
│ Deployment Frequency                │ On-demand     │
│ Lead Time for Changes               │ < 1 day       │
│ Time to Restore Service (MTTR)      │ < 1 hour      │
│ Change Failure Rate                 │ < 5%          │
└─────────────────────────────────────┴───────────────┘

RELIABILITY METRICS:
┌─────────────────────────────────────┬───────────────┐
│ Metric                              │ Target        │
├─────────────────────────────────────┼───────────────┤
│ Availability                        │ 99.9%+        │
│ Error Rate                          │ < 0.1%        │
│ p50 Latency                         │ < 50ms        │
│ p95 Latency                         │ < 200ms       │
│ p99 Latency                         │ < 500ms       │
│ MTBF (Mean Time Between Failures)   │ > 30 days     │
│ MTTR (Mean Time To Recovery)        │ < 1 hour      │
└─────────────────────────────────────┴───────────────┘

QUALITY METRICS:
┌─────────────────────────────────────┬───────────────┐
│ Metric                              │ Target        │
├─────────────────────────────────────┼───────────────┤
│ Test Coverage                       │ > 80%         │
│ Mutation Score                      │ > 70%         │
│ Critical Bugs in Production         │ 0             │
│ Security Vulnerabilities (Crit/High)│ 0             │
│ Tech Debt Ratio                     │ < 5%          │
└─────────────────────────────────────┴───────────────┘
```

---

# QUICK REFERENCE

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         THE 5 STEPS SUMMARY                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  STEP 1: DESIGN                                                         │
│    → Pre-mortem: How could this fail?                                  │
│    → SLOs: What are our targets?                                       │
│    → Threat model: What could attack us?                               │
│    → Output: 10+ documentation files                                   │
│                                                                         │
│  STEP 2: FOUNDATION                                                     │
│    → Golden path: 5-minute setup                                       │
│    → CI/CD: Automated everything                                       │
│    → Observability: From day 1                                         │
│    → Output: Production-ready infrastructure                           │
│                                                                         │
│  STEP 3: CODE                                                           │
│    → TDD: Tests first, code second                                     │
│    → Feature flags: Everything toggleable                              │
│    → Contract tests: Never break integrations                          │
│    → Output: Working features with 80%+ coverage                       │
│                                                                         │
│  STEP 4: HARDEN                                                         │
│    → Security: OWASP Top 10 + pen test                                 │
│    → Performance: Profile everything                                   │
│    → Chaos: Break it before production                                 │
│    → Output: Production-ready system                                   │
│                                                                         │
│  STEP 5: SHIP                                                           │
│    → Progressive: 1% → 10% → 50% → 100%                               │
│    → Instant rollback: Feature flags + automation                     │
│    → Blameless: Learn from everything                                  │
│    → Output: Live, monitored, operated                                 │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│  THE THREE QUESTIONS (Ask Before Every Change):                         │
│    1. How do I know it's working?                                       │
│    2. How do I know it's broken?                                        │
│    3. How do I fix it fast?                                            │
└─────────────────────────────────────────────────────────────────────────┘
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
11. **Pre-mortem everything**
12. **Measure everything**
13. **Automate everything**
14. **Blame systems, not people**
15. **Ship small, ship often, ship safely**

---

*This is how Netflix, Google, Amazon, Tesla, SpaceX, Stripe, and Uber build software.*

*The hidden secrets are now yours. Use them wisely.*

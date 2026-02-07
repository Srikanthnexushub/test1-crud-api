# The 5 Steps

## Complete Specification for Building Enterprise-Grade Projects

---

# STEP 1: DESIGN

## Prompt

```
I want to build [describe your idea in 1-2 sentences].

Target users: [who will use this]
Core problem: [what problem it solves]
Tech stack: [language, framework, database]

Create complete architecture documentation:

1. docs/architecture.md
   - System overview
   - Component diagram (ASCII)
   - Data flow diagram
   - Layer architecture (controller → service → repository → entity)
   - All components with responsibilities
   - External dependencies
   - Scaling approach

2. docs/api-spec.yaml (OpenAPI 3.0)
   - All endpoints with methods
   - Request/response schemas
   - Authentication method
   - Error response format
   - Examples for each endpoint

3. docs/data-model.md
   - Entity relationship diagram
   - All tables with columns and types
   - Relationships and foreign keys
   - Indexes needed

4. docs/security.md
   - Authentication design
   - Authorization design (roles, permissions)
   - OWASP Top 10 considerations
   - Data protection approach
   - Security headers

5. docs/adr/ (Architecture Decision Records)
   - ADR for each technology choice
   - ADR for each major design pattern
   - Include: context, decision, consequences

Start by asking me questions to clarify requirements.
```

## Exit Criteria

```
□ docs/architecture.md exists with diagrams
□ docs/api-spec.yaml is complete OpenAPI 3.0
□ docs/data-model.md exists with ER diagram
□ docs/security.md covers all security aspects
□ docs/adr/ has decision records for each tech choice
□ I can explain the entire design without looking at docs
```

---

# STEP 2: FOUNDATION

## Prompt

```
Architecture docs are complete.

Set up the project foundation:

1. Project Structure
   /src
     /main/java (or your language)
       /config      - configuration classes
       /controller  - REST controllers
       /service     - business logic
       /repository  - data access
       /entity      - domain models
       /dto         - request/response objects
       /exception   - custom exceptions
       /security    - auth filters, utilities
   /test           - mirror of main structure
   /docs           - documentation

2. Dependencies
   - Web framework
   - Database driver + ORM
   - Security/JWT
   - Validation
   - Testing (unit + integration)
   - API documentation (Swagger/OpenAPI)
   - Logging

3. docker-compose.yml
   - Application container
   - Database container
   - Any other services (Redis, etc.)
   - Health checks
   - Volumes for persistence

4. Makefile with commands:
   - make setup    (install dependencies, create .env)
   - make run      (start application)
   - make test     (run all tests)
   - make build    (build for production)

5. CI Pipeline (.github/workflows/ci.yml)
   - Trigger: push and pull request
   - Build application
   - Run unit tests
   - Run integration tests
   - Check code coverage (minimum 80%)
   - Security scan (dependency check)
   - Lint check

6. CD Pipeline (.github/workflows/cd.yml)
   - Trigger: merge to main
   - Build Docker image
   - Push to registry
   - Deploy to staging (automatic)
   - Deploy to production (manual approval)

7. Database
   - Connection configuration
   - Migration tool (Flyway/Liquibase/Alembic)
   - Initial migration with schema from data model
   - Seed data for development

8. Configuration
   - application.properties/yaml (or equivalent)
   - .env.example with all required variables
   - Separate configs for dev/staging/prod

9. README.md
   - Project description
   - Prerequisites
   - Setup instructions
   - How to run
   - How to test
   - How to deploy

Verify: Fresh clone → working app in under 15 minutes.
```

## Exit Criteria

```
□ Project structure matches design
□ All dependencies installed
□ docker-compose up starts everything
□ make setup && make run works
□ make test runs and passes
□ CI pipeline runs on push
□ CD pipeline configured
□ Database migrations work
□ README has complete setup instructions
□ New developer can set up in < 15 minutes
```

---

# STEP 3: CODE

## Prompt for First Feature

```
Foundation is complete. CI is passing.

Build Feature: [Feature Name]

Requirements:
- [Requirement 1 - be specific]
- [Requirement 2 - be specific]
- [Requirement 3 - be specific]

Acceptance Criteria:
- [What must be true when complete]
- [What must be true when complete]

API Endpoints (from api-spec.yaml):
- [METHOD] [PATH] - [description]
- [METHOD] [PATH] - [description]

Build in this order:
1. Entity/Model
2. Repository
3. Service (with unit tests)
4. Controller (with integration tests)
5. Update API documentation if needed

Verify:
- All tests pass
- Manual test with curl/Postman
- CI passes

Commit when complete.
```

## Prompt for Next Features

```
[Previous Feature] complete and committed.

Build Feature: [Next Feature Name]

Requirements:
- [Requirement 1]
- [Requirement 2]

Acceptance Criteria:
- [What must be true]

API Endpoints:
- [METHOD] [PATH] - [description]

Build with tests. Verify. Commit.
```

## Repeat Until

```
All features from architecture doc are complete.
```

## Exit Criteria

```
□ All features from scope are implemented
□ Each feature has unit tests
□ Each feature has integration tests
□ Test coverage > 80%
□ All tests pass
□ CI passes
□ API documentation matches implementation
□ No known bugs
```

---

# STEP 4: HARDEN

## Prompt

```
All features complete. Tests passing. CI green.

Harden the project:

1. Security Review
   - OWASP Top 10 audit
   - Input validation on ALL endpoints
   - SQL injection prevention verified
   - XSS prevention verified
   - Authentication cannot be bypassed
   - Authorization on every endpoint
   - Rate limiting configured
   - Security headers configured
   - Secrets not in code
   - Dependency vulnerability scan (fix critical/high)

2. Performance
   - Identify slow endpoints
   - Optimize database queries
   - Add missing indexes
   - Add caching where needed
   - Connection pooling configured

3. Load Testing
   - Create load test scripts (k6 or Gatling)
   - Smoke test (verify works)
   - Load test (expected traffic)
   - Stress test (find breaking point)
   - Document results and limits

4. Error Handling
   - Consistent error response format
   - No stack traces in production responses
   - All errors logged with context
   - Meaningful error messages

5. Logging
   - Structured logging (JSON)
   - Correlation IDs on requests
   - Security events logged
   - No sensitive data in logs
   - Appropriate log levels

6. Testing
   - Unit test coverage > 80%
   - Integration tests for all endpoints
   - E2E tests for critical paths
   - All tests passing

7. Documentation
   - docs/runbook.md
     - How to deploy
     - How to rollback
     - How to debug common issues
     - How to access logs
     - How to restore from backup
     - Emergency contacts

Start with security review.
```

## Exit Criteria

```
□ Security review complete, no critical issues
□ Dependency scan clean
□ Load testing complete, limits documented
□ Performance optimized
□ Error handling consistent
□ Logging complete
□ Test coverage > 80%
□ Runbook written
□ Ready for production traffic
```

---

# STEP 5: SHIP

## Prompt

```
Hardening complete. Ready for production.

Deploy the project:

1. Staging Deployment
   - Deploy to staging environment
   - Run all tests against staging
   - Run smoke tests
   - Manual verification of key features
   - Load test staging
   - Fix any issues found
   - Staging stable for 24 hours

2. Production Preparation
   - Production infrastructure ready
   - Database provisioned
   - Secrets configured (not in code)
   - SSL/TLS certificates
   - Domain/DNS configured
   - CDN configured (if needed)
   - Backup configured
   - Rollback procedure tested

3. Monitoring Setup
   - Health check endpoint
   - Metrics endpoint (Prometheus)
   - Dashboard (Grafana or equivalent)
   - Alerts configured:
     - Error rate > 1%
     - Response time p95 > 500ms
     - Service down
     - Disk/CPU/Memory thresholds
   - On-call rotation set

4. Production Deployment
   - Deploy canary (5-10% traffic)
   - Monitor for 15 minutes
   - Check error rate, latency, logs
   - If OK: increase to 50%
   - Monitor for 15 minutes
   - If OK: increase to 100%
   - Monitor for 2 hours

5. Post-Launch
   - Verify all functionality
   - Check all alerts working
   - Document any issues
   - Update runbook if needed
   - Team notified of launch

Start with staging deployment.
```

## Exit Criteria

```
□ Staging deployed and stable
□ Production infrastructure ready
□ Monitoring and alerting active
□ Production deployed successfully
□ Canary verified before full rollout
□ No critical issues after launch
□ Team can operate the system
```

---

# ONGOING

After Step 5, continue with:

```
Daily:
- Check dashboards
- Review alerts
- Check error logs

Weekly:
- Review metrics trends
- Update dependencies
- Address tech debt

Monthly:
- Security patches
- Performance review
- Load testing

Quarterly:
- Disaster recovery drill
- Architecture review
- Major updates
```

---

# QUICK REFERENCE

```
STEP 1: DESIGN
  Input:  Idea + requirements
  Output: architecture.md, api-spec.yaml, data-model.md, security.md, adr/
  Verify: Can explain entire design

STEP 2: FOUNDATION
  Input:  Design docs
  Output: Project structure, CI/CD, database, docker-compose
  Verify: Fresh clone works in 15 min

STEP 3: CODE
  Input:  Foundation + feature requirements
  Output: Working features with tests
  Verify: All tests pass, CI green

STEP 4: HARDEN
  Input:  Complete features
  Output: Security, performance, load tests, runbook
  Verify: Ready for production traffic

STEP 5: SHIP
  Input:  Hardened project
  Output: Running production system with monitoring
  Verify: System stable, alerts working
```

---

# RULES

1. **Complete each step fully before next step**
2. **Verify exit criteria before proceeding**
3. **Commit after each completed piece**
4. **Never skip steps**
5. **If something is broken, fix before proceeding**

# The Enterprise Engineering Playbook

## How Fortune 100 Companies Build Software

**From Zero to Production at Scale**

---

> "The best part is no part. The best process is no process. It weighs nothing, costs nothing, can't go wrong."
> — Elon Musk

> "We don't have a QA team. You build it, you run it."
> — Werner Vogels, CTO Amazon

> "Context, not control."
> — Netflix Culture

---

## Table of Contents

1. [Mindset & Principles](#phase-0-mindset--principles)
2. [Phase 1: Discovery & Validation](#phase-1-discovery--validation-weeks-1-2)
3. [Phase 2: Technical Design](#phase-2-technical-design-weeks-2-3)
4. [Phase 3: Foundation](#phase-3-foundation-weeks-3-4)
5. [Phase 4: Core Implementation](#phase-4-core-implementation-weeks-4-8)
6. [Phase 5: Hardening](#phase-5-hardening-weeks-8-10)
7. [Phase 6: Observability](#phase-6-observability-weeks-10-11)
8. [Phase 7: Deployment & Release](#phase-7-deployment--release-weeks-11-12)
9. [Phase 8: Operations & Growth](#phase-8-operations--growth-ongoing)
10. [Checklists](#checklists)
11. [Anti-Patterns to Avoid](#anti-patterns-to-avoid)

---

## Phase 0: Mindset & Principles

### The Netflix/Google/Stripe Engineering Mindset

Before writing a single line of code, internalize these principles:

#### 1. First Principles Thinking (SpaceX)

```
Don't ask: "How do others do authentication?"
Ask: "What is authentication fundamentally trying to solve?"

Break down to atomic truths:
1. We need to verify identity
2. We need to maintain session state
3. We need to authorize actions

Now rebuild from these truths.
```

#### 2. Question Every Requirement (Tesla)

```
For every feature/requirement ask:
1. Why do we need this?
2. What happens if we don't build it?
3. Can we solve this with existing tools?
4. What's the simplest possible solution?

Delete before you optimize.
Simplify before you automate.
```

#### 3. You Build It, You Run It (Amazon)

```
No separate ops team. Engineers:
- Write the code
- Deploy the code
- Monitor the code
- Get paged at 3 AM when it breaks
- Fix it and write postmortem

This creates accountability and quality.
```

#### 4. Bias for Action (Amazon)

```
Two-way door decisions: Move fast, easy to reverse
- API parameter names
- Internal implementation
- UI layouts

One-way door decisions: Move carefully, hard to reverse
- Public API contracts
- Database schema fundamentals
- Core architecture patterns

Most decisions are two-way doors. Don't overthink them.
```

#### 5. Write Things Down (Google)

```
If it's not written, it doesn't exist.
- Design docs before code
- Runbooks before production
- Postmortems after incidents
- ADRs for decisions

Future you and your team will thank you.
```

---

## Phase 1: Discovery & Validation (Weeks 1-2)

### 1.1 Problem Definition

**Before writing any code, answer these questions in writing:**

```markdown
# Problem Statement

## What problem are we solving?
[One sentence. If you can't explain it simply, you don't understand it.]

## Who has this problem?
[Specific user persona, not "everyone"]

## How do they solve it today?
[Current alternatives, workarounds, competitors]

## Why is now the right time?
[What changed? Why didn't this exist before?]

## What does success look like?
[Measurable outcomes, not outputs]

## What are we NOT building?
[Explicit scope boundaries - this is critical]
```

### 1.2 Validate Before Building

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    VALIDATION HIERARCHY                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  CHEAPEST ─────────────────────────────────────────────── EXPENSIVE    │
│                                                                         │
│  Talk to users    Prototype    MVP    Full Build    Scale             │
│       │               │          │         │          │                │
│       ▼               ▼          ▼         ▼          ▼                │
│   10 conversations  Figma     2 weeks   2 months   Ongoing            │
│   $0, 2 days        $0, 1wk   $$$       $$$$       $$$$$              │
│                                                                         │
│   STOP if you can't validate at each stage                             │
└─────────────────────────────────────────────────────────────────────────┘
```

**Validation Checklist:**
- [ ] Talked to 10+ potential users
- [ ] Identified 3+ people who would pay/use immediately
- [ ] Documented specific pain points with quotes
- [ ] Validated pricing/business model (if applicable)
- [ ] Identified existing solutions and why they're insufficient

### 1.3 Success Metrics (Define Before Building)

```markdown
# Success Metrics

## North Star Metric
[One metric that matters most. e.g., "Monthly Active Users"]

## Input Metrics (Leading Indicators)
1. [e.g., "Registration completion rate"]
2. [e.g., "Time to first value"]
3. [e.g., "API response time p95"]

## Output Metrics (Lagging Indicators)
1. [e.g., "Monthly recurring revenue"]
2. [e.g., "Net Promoter Score"]
3. [e.g., "Customer retention rate"]

## Guardrail Metrics (Don't Break These)
1. [e.g., "Error rate < 0.1%"]
2. [e.g., "Uptime > 99.9%"]
3. [e.g., "Security incidents = 0"]
```

---

## Phase 2: Technical Design (Weeks 2-3)

### 2.1 Write the RFC (Request for Comments)

**Every significant project at Google, Netflix, Uber starts with an RFC.**

```markdown
# RFC: [Project Name]

## Metadata
- Author: [Name]
- Reviewers: [Names]
- Status: Draft | In Review | Approved | Implemented | Deprecated
- Created: [Date]
- Last Updated: [Date]

## Summary
[2-3 sentences. What are we building and why?]

## Motivation
[Why is this important? What problem does it solve?]

## Detailed Design

### Architecture Overview
[Diagram + explanation]

### API Design
[Key endpoints, contracts]

### Data Model
[Schema, relationships]

### Key Algorithms
[Non-trivial logic explained]

## Alternatives Considered
[What else did you consider? Why not?]

## Security Considerations
[Threat model, mitigations]

## Privacy Considerations
[Data handling, compliance]

## Operational Considerations
[How will this be deployed, monitored, maintained?]

## Rollout Plan
[How will this be released? Feature flags? Canary?]

## Open Questions
[What's still unclear?]

## Appendix
[Additional details, references]
```

### 2.2 Architecture Review Checklist

Before approval, the design must address:

```
□ Scalability
  - What's the expected load?
  - How does it scale horizontally?
  - What are the bottlenecks?

□ Reliability
  - What happens when [component] fails?
  - What's the blast radius?
  - How do we recover?

□ Security
  - What's the threat model?
  - How is authentication/authorization handled?
  - What data is sensitive?

□ Observability
  - How do we know it's working?
  - What metrics/logs/traces?
  - How do we debug issues?

□ Cost
  - What are the infrastructure costs?
  - How does cost scale with usage?
  - Any expensive operations?

□ Maintainability
  - Can another engineer understand this?
  - What's the testing strategy?
  - How do we deploy changes?
```

### 2.3 Technology Selection Framework

```
For each technology choice, document:

┌─────────────────────────────────────────────────────────────────────────┐
│ TECHNOLOGY DECISION: [e.g., "Database Selection"]                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ Requirements:                                                           │
│ - [e.g., "ACID compliance for transactions"]                           │
│ - [e.g., "Handle 10K writes/second"]                                   │
│ - [e.g., "Team has experience with it"]                                │
│                                                                         │
│ Options Evaluated:                                                      │
│ ┌─────────────┬───────────┬───────────┬───────────┐                   │
│ │ Criteria    │ PostgreSQL│ MongoDB   │ DynamoDB  │                   │
│ ├─────────────┼───────────┼───────────┼───────────┤                   │
│ │ ACID        │ ✅        │ ⚠️ (v4+)  │ ⚠️ (per-item)│                │
│ │ Scale       │ ⚠️        │ ✅        │ ✅         │                   │
│ │ Team exp.   │ ✅        │ ❌        │ ❌         │                   │
│ │ Cost        │ $$        │ $$        │ $$$       │                   │
│ └─────────────┴───────────┴───────────┴───────────┘                   │
│                                                                         │
│ Decision: PostgreSQL                                                    │
│ Rationale: ACID required, team expertise, scale is acceptable          │
│            for current projections. Revisit at 100K users.             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Phase 3: Foundation (Weeks 3-4)

### 3.1 Repository Setup

```bash
# Standard project structure
project/
├── .github/
│   ├── workflows/           # CI/CD pipelines
│   │   ├── ci.yml          # Build, test, lint
│   │   ├── cd.yml          # Deploy
│   │   └── security.yml    # Security scanning
│   ├── CODEOWNERS          # Who reviews what
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── ISSUE_TEMPLATE/
├── docs/
│   ├── architecture.md
│   ├── api-spec.yaml
│   ├── runbook.md
│   └── adr/                # Architecture Decision Records
├── src/
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── infrastructure/
│   ├── terraform/          # Or Pulumi
│   └── kubernetes/         # Or docker-compose
├── scripts/
│   ├── setup.sh
│   ├── build.sh
│   └── deploy.sh
├── .env.example
├── docker-compose.yml
├── Dockerfile
├── Makefile               # Common commands
├── README.md
├── CONTRIBUTING.md
├── CHANGELOG.md
└── CLAUDE.md              # AI assistant context
```

### 3.2 Development Environment

**One-command setup is non-negotiable:**

```bash
# New engineer joins, runs one command:
make setup

# This should:
# 1. Install dependencies
# 2. Set up local database
# 3. Create .env from template
# 4. Run migrations
# 5. Seed test data
# 6. Verify everything works
```

**Makefile example:**
```makefile
.PHONY: setup build test run deploy

setup:
	@echo "Setting up development environment..."
	cp .env.example .env
	docker-compose up -d postgres redis
	./scripts/wait-for-it.sh localhost:5432
	./mvnw flyway:migrate
	./scripts/seed-data.sh
	@echo "✅ Setup complete. Run 'make run' to start."

build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

test-integration:
	./mvnw verify -Pintegration

run:
	./mvnw spring-boot:run

docker-build:
	docker build -t myapp:latest .

deploy-staging:
	./scripts/deploy.sh staging

deploy-production:
	./scripts/deploy.sh production
```

### 3.3 CI/CD Pipeline (Set Up Immediately)

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build
        run: ./mvnw clean package -DskipTests

      - name: Unit Tests
        run: ./mvnw test

      - name: Integration Tests
        run: ./mvnw verify -Pintegration

  code-quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: SonarQube Scan
        run: ./mvnw sonar:sonar

      - name: Check Coverage
        run: |
          COVERAGE=$(cat target/site/jacoco/index.html | grep -oP 'Total.*?([0-9]+)%' | grep -oP '[0-9]+')
          if [ "$COVERAGE" -lt 80 ]; then
            echo "Coverage is ${COVERAGE}%, required 80%"
            exit 1
          fi

  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Dependency Scan
        uses: snyk/actions/maven@master
        with:
          command: test

      - name: Container Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'myapp:latest'
          severity: 'CRITICAL,HIGH'
```

### 3.4 Branch Strategy

```
main (production)
  │
  ├── develop (staging)
  │     │
  │     ├── feature/AUTH-123-add-oauth
  │     ├── feature/AUTH-124-fix-token-refresh
  │     └── feature/AUTH-125-add-mfa
  │
  └── hotfix/critical-security-fix
```

**Rules:**
- `main` is always deployable
- All changes via PR
- Require 1+ approval
- All tests must pass
- No direct commits to main

---

## Phase 4: Core Implementation (Weeks 4-8)

### 4.1 Development Workflow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DEVELOPMENT WORKFLOW                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Pick task from backlog                                              │
│     └── Must have clear acceptance criteria                            │
│                                                                         │
│  2. Create feature branch                                               │
│     └── git checkout -b feature/JIRA-123-description                   │
│                                                                         │
│  3. Write failing test first (TDD)                                      │
│     └── Red → Green → Refactor                                         │
│                                                                         │
│  4. Implement smallest possible solution                                │
│     └── YAGNI: You Aren't Gonna Need It                                │
│                                                                         │
│  5. Self-review before PR                                               │
│     └── git diff main...HEAD                                           │
│                                                                         │
│  6. Create PR with context                                              │
│     └── What, Why, How, Testing, Screenshots                           │
│                                                                         │
│  7. Address review feedback                                             │
│     └── Discuss, don't defend                                          │
│                                                                         │
│  8. Merge and deploy                                                    │
│     └── Feature flags for incomplete features                          │
│                                                                         │
│  9. Monitor after deploy                                                │
│     └── Watch metrics for 30 min after deploy                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Code Review Standards

**Every PR must:**
```
□ Have clear title and description
□ Link to ticket/issue
□ Include tests for new functionality
□ Not decrease code coverage
□ Pass all CI checks
□ Have no security vulnerabilities
□ Follow coding standards
□ Be reviewable in < 30 minutes (small PRs!)
```

**Reviewer responsibilities:**
```
□ Review within 24 hours
□ Be constructive, not critical
□ Distinguish "must fix" from "nit"
□ Approve when good enough, not perfect
□ Don't block on style preferences
```

### 4.3 Testing Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TESTING PYRAMID                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│                         ┌───────┐                                       │
│                        /  E2E   \           Slow, Expensive             │
│                       /  Tests   \          Few (5-10)                  │
│                      /─────────────\                                    │
│                     /  Integration  \       Medium Speed                │
│                    /     Tests       \      Moderate (50-100)           │
│                   /───────────────────\                                 │
│                  /     Unit Tests      \    Fast, Cheap                 │
│                 /                       \   Many (hundreds)             │
│                └─────────────────────────┘                              │
│                                                                         │
│  COVERAGE REQUIREMENTS:                                                 │
│  - Unit tests: 80%+ line coverage                                       │
│  - Integration tests: All API endpoints                                 │
│  - E2E tests: Critical user journeys                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**What to test:**
```
Unit Tests:
- Business logic
- Edge cases
- Error handling
- Validation rules

Integration Tests:
- API endpoints
- Database operations
- External service calls (mocked)
- Authentication/authorization

E2E Tests:
- User registration → login → action → logout
- Payment flow
- Critical business workflows
```

### 4.4 Feature Flags

**Deploy != Release. Use feature flags.**

```java
// Feature flag implementation
if (featureFlags.isEnabled("new-checkout-flow", user)) {
    return newCheckoutFlow();
} else {
    return legacyCheckoutFlow();
}
```

**Benefits:**
- Deploy to production without releasing
- Gradual rollout (1% → 10% → 50% → 100%)
- Instant rollback without deploy
- A/B testing
- Kill switch for broken features

**Tools:** LaunchDarkly, Split.io, Unleash, or simple config

---

## Phase 5: Hardening (Weeks 8-10)

### 5.1 Security Hardening Checklist

```
OWASP TOP 10 VERIFICATION:

□ A01:2021 – Broken Access Control
  - All endpoints have authorization checks
  - RBAC properly implemented
  - No IDOR vulnerabilities

□ A02:2021 – Cryptographic Failures
  - TLS 1.2+ enforced
  - Passwords hashed with bcrypt/argon2
  - No sensitive data in logs

□ A03:2021 – Injection
  - All queries parameterized
  - Input validation on all endpoints
  - Output encoding

□ A04:2021 – Insecure Design
  - Threat modeling completed
  - Security requirements defined
  - Abuse cases considered

□ A05:2021 – Security Misconfiguration
  - Security headers configured
  - Default credentials changed
  - Unnecessary features disabled

□ A06:2021 – Vulnerable Components
  - Dependency scanning enabled
  - No known vulnerabilities
  - Update process defined

□ A07:2021 – Auth Failures
  - Strong password policy
  - Rate limiting on auth endpoints
  - MFA available

□ A08:2021 – Data Integrity Failures
  - Signed JWTs
  - Integrity checks on critical data
  - CI/CD pipeline secured

□ A09:2021 – Logging Failures
  - Security events logged
  - Logs don't contain secrets
  - Log retention policy

□ A10:2021 – SSRF
  - URL validation
  - Allowlist for external calls
  - Network segmentation
```

### 5.2 Performance Optimization

```
OPTIMIZATION ORDER (do in this sequence):

1. MEASURE FIRST
   - Profile before optimizing
   - Identify actual bottlenecks
   - Set performance budgets

2. DATABASE
   - Add missing indexes
   - Optimize slow queries
   - Consider caching
   - Connection pooling

3. APPLICATION
   - Remove N+1 queries
   - Async where possible
   - Optimize hot paths

4. INFRASTRUCTURE
   - Right-size instances
   - CDN for static assets
   - Load balancing

5. CACHING
   - Cache expensive operations
   - Set appropriate TTLs
   - Invalidation strategy
```

### 5.3 Load Testing

```
RUN THESE BEFORE PRODUCTION:

1. Smoke Test
   - Verify system works
   - 1-2 users, 1 minute
   - All tests should pass

2. Load Test
   - Expected production load
   - 100 users, 15 minutes
   - Verify SLOs met

3. Stress Test
   - Find breaking point
   - Ramp to 500+ users
   - Document limits

4. Spike Test
   - Sudden traffic surge
   - 10 → 500 → 10 users
   - Verify recovery

5. Soak Test
   - Long-term stability
   - 50 users, 2+ hours
   - Check for memory leaks
```

---

## Phase 6: Observability (Weeks 10-11)

### 6.1 The Three Pillars

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY PILLARS                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  LOGS ─────────────────────────────────────────────────────────────    │
│  What happened?                                                         │
│  - Structured JSON format                                               │
│  - Correlation IDs for tracing                                          │
│  - Appropriate log levels                                               │
│  - No sensitive data                                                    │
│  Tools: ELK Stack, Loki, CloudWatch                                     │
│                                                                         │
│  METRICS ──────────────────────────────────────────────────────────    │
│  How is the system performing?                                          │
│  - RED: Rate, Errors, Duration                                          │
│  - USE: Utilization, Saturation, Errors                                 │
│  - Business metrics                                                     │
│  Tools: Prometheus + Grafana, Datadog, New Relic                        │
│                                                                         │
│  TRACES ───────────────────────────────────────────────────────────    │
│  How do requests flow through the system?                               │
│  - Distributed tracing                                                  │
│  - Service dependencies                                                 │
│  - Latency breakdown                                                    │
│  Tools: Jaeger, Zipkin, AWS X-Ray                                       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Key Metrics to Track

```
SYSTEM METRICS (USE):
- CPU utilization
- Memory utilization
- Disk I/O
- Network I/O

APPLICATION METRICS (RED):
- Request rate (requests/second)
- Error rate (% of failures)
- Duration (latency percentiles)

BUSINESS METRICS:
- Signups per hour
- Active users
- Transactions completed
- Revenue (if applicable)

SLIs (Service Level Indicators):
- Availability: % of successful requests
- Latency: p50, p95, p99 response times
- Throughput: requests/second
- Error rate: % of errors
```

### 6.3 Alerting Strategy

```
ALERT HIERARCHY:

CRITICAL (Page immediately):
- Service is down
- Error rate > 10%
- Data integrity issue
- Security incident

WARNING (Page during business hours):
- Error rate > 1%
- Latency p99 > 2s
- Disk usage > 80%
- Certificate expiring in 7 days

INFO (Review daily):
- Deployment completed
- Unusual traffic patterns
- Dependency updates available
```

**Alerting Rules:**
```
□ Every alert must be actionable
□ Include runbook link in alert
□ No alert should fire more than once/day normally
□ Review and tune alerts monthly
□ On-call rotation with clear escalation
```

### 6.4 Dashboards

**Every service needs:**
```
1. Overview Dashboard
   - Health status (up/down)
   - Request rate
   - Error rate
   - Latency

2. API Dashboard
   - Requests by endpoint
   - Latency by endpoint
   - Errors by endpoint
   - Top errors

3. Infrastructure Dashboard
   - CPU/Memory/Disk
   - Database connections
   - Cache hit rate
   - Queue depth

4. Business Dashboard
   - Active users
   - Signups
   - Key actions completed
   - Funnel metrics
```

---

## Phase 7: Deployment & Release (Weeks 11-12)

### 7.1 Deployment Pipeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT PIPELINE                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Code Push                                                              │
│      │                                                                  │
│      ▼                                                                  │
│  ┌─────────────┐                                                       │
│  │   Build     │ ─── Compile, unit tests, lint                        │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │   Test      │ ─── Integration tests, security scan                 │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │  Staging    │ ─── Deploy to staging, smoke tests                   │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │  Approval   │ ─── Manual gate (for now)                            │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │  Canary     │ ─── 5% of traffic, monitor                           │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │  Rollout    │ ─── 25% → 50% → 100%                                 │
│  └──────┬──────┘                                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────┐                                                       │
│  │  Monitor    │ ─── Watch metrics, ready to rollback                 │
│  └─────────────┘                                                       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Release Checklist

```
PRE-RELEASE:
□ All tests passing
□ Security scan clean
□ Performance benchmarks met
□ Documentation updated
□ Runbook updated
□ Rollback plan documented
□ Stakeholders notified
□ On-call engineer assigned

DURING RELEASE:
□ Deploy to canary
□ Monitor for 15 minutes
□ Check error rates
□ Check latency
□ Gradually increase traffic
□ Continue monitoring

POST-RELEASE:
□ Verify all functionality
□ Check business metrics
□ Update status page
□ Announce completion
□ Update changelog
□ Schedule retrospective if issues
```

### 7.3 Rollback Strategy

```
WHEN TO ROLLBACK:
- Error rate > 5%
- Latency p99 > 3x baseline
- Customer-facing bug
- Security vulnerability
- Data corruption

HOW TO ROLLBACK:
1. Disable feature flag (fastest)
2. Revert traffic to previous version (fast)
3. Redeploy previous version (medium)
4. Database rollback (last resort, slow)

PRACTICE ROLLBACKS:
- Run rollback drills monthly
- Document rollback time
- Automate where possible
```

---

## Phase 8: Operations & Growth (Ongoing)

### 8.1 On-Call & Incident Response

```
INCIDENT SEVERITY:

SEV-1 (Critical):
- Service completely down
- Data breach
- Response: 5 min acknowledge, all hands

SEV-2 (High):
- Major feature broken
- Significant performance degradation
- Response: 15 min acknowledge, primary on-call

SEV-3 (Medium):
- Minor feature broken
- Workaround available
- Response: 1 hour acknowledge, next business day fix

SEV-4 (Low):
- Cosmetic issue
- Edge case bug
- Response: Next sprint
```

### 8.2 Incident Response Process

```
1. DETECT
   - Alert fires or user reports
   - Acknowledge in Slack/PagerDuty

2. TRIAGE
   - Assess severity
   - Assign incident commander

3. MITIGATE
   - Focus on restoring service
   - Don't worry about root cause yet
   - Communicate status

4. RESOLVE
   - Confirm service restored
   - Verify no ongoing issues

5. POSTMORTEM
   - Blameless retrospective
   - Document timeline
   - Identify root cause
   - Define action items
```

### 8.3 Postmortem Template

```markdown
# Incident Postmortem: [Title]

## Metadata
- Date: [When it happened]
- Duration: [How long]
- Severity: [SEV-1/2/3/4]
- Author: [Who wrote this]
- Attendees: [Who was involved]

## Summary
[2-3 sentences describing what happened]

## Impact
- [X] users affected
- [Y] minutes of downtime
- [Z] revenue impact (if applicable)

## Timeline (all times in UTC)
- HH:MM - Alert fires
- HH:MM - On-call acknowledges
- HH:MM - Issue identified
- HH:MM - Mitigation applied
- HH:MM - Service restored

## Root Cause
[What actually caused the issue]

## What Went Well
- [Things that worked]

## What Went Wrong
- [Things that didn't work]

## Action Items
| Action | Owner | Due Date | Status |
|--------|-------|----------|--------|
| [Action 1] | [Name] | [Date] | [Done/In Progress] |

## Lessons Learned
[What we'll do differently]
```

### 8.4 Continuous Improvement

```
WEEKLY:
- Review metrics dashboard
- Address any alerts that fired
- Update documentation if needed

MONTHLY:
- Dependency updates
- Security patches
- Performance review
- Cost review

QUARTERLY:
- Architecture review
- Tech debt assessment
- Load testing
- Disaster recovery drill
- Team retrospective

ANNUALLY:
- Major version upgrades
- Infrastructure review
- Compliance audit
- Penetration testing
```

---

## Checklists

### Project Start Checklist

```
□ Problem clearly defined
□ Users validated
□ Success metrics defined
□ RFC written and approved
□ Repository set up
□ CI/CD pipeline working
□ Development environment one-command setup
□ First test written
```

### Pre-Production Checklist

```
□ All tests passing (unit, integration, e2e)
□ Code coverage > 80%
□ Security scan clean
□ Load testing completed
□ Documentation complete
□ Runbook written
□ Monitoring/alerting configured
□ Backup/recovery tested
□ Rollback plan documented
□ Stakeholders trained
```

### Production Readiness Review

```
□ SLOs defined and measurable
□ Error budget established
□ On-call rotation set up
□ Incident response process documented
□ Postmortem process established
□ Capacity planning done
□ Disaster recovery plan tested
□ Compliance requirements met
```

---

## Anti-Patterns to Avoid

### 1. Big Bang Releases
```
❌ Build for 6 months, then release
✅ Release early, release often, iterate
```

### 2. Hero Culture
```
❌ One person who knows everything
✅ Documentation, knowledge sharing, bus factor > 1
```

### 3. Manual Processes
```
❌ "SSH into server and restart"
✅ Automate everything repeatable
```

### 4. Premature Optimization
```
❌ "We might need to handle 1M users"
✅ Build for current scale, design for future
```

### 5. Ignoring Technical Debt
```
❌ "We'll fix it later"
✅ Dedicate 20% of each sprint to debt
```

### 6. Skipping Documentation
```
❌ "The code is self-documenting"
✅ Document why, not what
```

### 7. Not Monitoring
```
❌ "Users will tell us if it's broken"
✅ Know before your users
```

### 8. Fear of Deleting Code
```
❌ "We might need this later"
✅ Version control exists; delete freely
```

---

## Summary: The 12-Week Plan

| Week | Phase | Key Deliverables |
|------|-------|------------------|
| 1-2 | Discovery | Problem statement, validation, metrics |
| 2-3 | Design | RFC, architecture, tech decisions |
| 3-4 | Foundation | Repo, CI/CD, dev environment |
| 4-8 | Build | Core features, tests, feature flags |
| 8-10 | Harden | Security, performance, load testing |
| 10-11 | Observe | Logging, metrics, alerting, dashboards |
| 11-12 | Release | Deployment pipeline, canary, rollout |
| Ongoing | Operate | Incidents, postmortems, improvements |

---

## Final Words

> "Simplicity is prerequisite for reliability."
> — Edje Dijkstra

The difference between amateur and professional engineering isn't complexity—it's discipline. The practices in this playbook aren't optional extras; they're the foundation that allows Netflix to deploy thousands of times per day and SpaceX to land rockets.

Start with the basics:
1. Write it down before building
2. Test before committing
3. Monitor after deploying
4. Learn from failures

Everything else builds on these fundamentals.

---

*This playbook should be treated as a living document. Update it as you learn.*

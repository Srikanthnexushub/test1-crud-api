# The 5 Steps

## How to Build Any Project with Claude Code

---

## STEP 1: DESIGN

```
I want to build [your idea].

Create the architecture documentation:
1. docs/architecture.md - system design with diagrams
2. docs/api-spec.yaml - OpenAPI specification
3. docs/security.md - security design
4. docs/adr/ - technology decisions

Start by asking me questions to understand what I need.
```

---

## STEP 2: FOUNDATION

```
Architecture docs are complete.

Now set up the project foundation:
1. Project structure with dependencies
2. docker-compose.yml for local development
3. CI pipeline (.github/workflows/ci.yml)
4. CD pipeline (.github/workflows/cd.yml)
5. Database with migration tool

I should be able to run one command to start everything.
```

---

## STEP 3: CODE

```
Foundation is complete.

Now build the features. Start with [Feature 1 name].

Requirements:
- [requirement 1]
- [requirement 2]
- [requirement 3]

Build it with tests. Commit when done.
```

**Repeat for each feature:**

```
Feature [N] done.

Next: [Feature N+1 name].

Requirements:
- [requirement 1]
- [requirement 2]

Build it with tests. Commit when done.
```

---

## STEP 4: HARDEN

```
All features are complete.

Now harden the project:
1. Security review (OWASP Top 10)
2. Load testing scripts
3. Performance optimization
4. Add missing tests (coverage > 80%)
5. Complete error handling
6. Create runbook (docs/runbook.md)

Start with security review.
```

---

## STEP 5: SHIP

```
Hardening complete.

Now deploy:
1. Deploy to staging
2. Run tests against staging
3. Deploy to production
4. Set up monitoring and alerts
5. Verify everything works

Start with staging deployment.
```

---

## Summary

```
Step 1: DESIGN     → Architecture, API spec, security, decisions
Step 2: FOUNDATION → Project setup, CI/CD, database
Step 3: CODE       → Build features one by one with tests
Step 4: HARDEN     → Security, performance, testing, runbook
Step 5: SHIP       → Staging, production, monitoring
```

---

## Rules

1. Complete each step before moving to next
2. Verify everything works before proceeding
3. Commit after each completed piece
4. Never skip steps

# The Master Framework

## Beyond Fortune 100: The Complete System for Software Success

---

> "Perfection is achieved not when there is nothing more to add, but when there is nothing left to take away."
> — Antoine de Saint-Exupéry

---

## What This Is

This is not a checklist. This is a **thinking system**.

Checklists tell you WHAT to do. This framework tells you HOW to think.

The difference between good engineers and great engineers is not knowledge—it's judgment. This framework develops that judgment.

---

# PART 1: NEW PROJECTS

## The 7 Gates

Every new project must pass through 7 gates. You cannot proceed to the next gate until the current gate is passed with 100% confidence.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         THE 7 GATES                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  GATE 1: CLARITY ──────────────────────────────────────────────────    │
│          "Do I understand the problem completely?"                      │
│                                                                         │
│  GATE 2: VALIDATION ───────────────────────────────────────────────    │
│          "Is this worth building?"                                      │
│                                                                         │
│  GATE 3: DESIGN ───────────────────────────────────────────────────    │
│          "Do I know exactly how to build it?"                          │
│                                                                         │
│  GATE 4: FOUNDATION ───────────────────────────────────────────────    │
│          "Can I build it correctly from the start?"                    │
│                                                                         │
│  GATE 5: EXECUTION ────────────────────────────────────────────────    │
│          "Is each piece working before moving on?"                     │
│                                                                         │
│  GATE 6: HARDENING ────────────────────────────────────────────────    │
│          "Is it ready for real users?"                                 │
│                                                                         │
│  GATE 7: OPERATION ────────────────────────────────────────────────    │
│          "Can I keep it running reliably?"                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## GATE 1: CLARITY

**The Question:** "Do I understand the problem completely?"

### The Test

Answer these questions in writing. If you cannot answer clearly, you are not ready.

```
┌─────────────────────────────────────────────────────────────────────────┐
│ CLARITY TEST                                                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ 1. THE PROBLEM                                                          │
│    Complete this sentence in exactly ONE line:                          │
│    "[User type] struggles to [action] because [reason]."               │
│                                                                         │
│    Example: "Developers struggle to deploy code quickly because        │
│    their CI/CD pipelines are complex and slow."                        │
│                                                                         │
│    Your answer: ________________________________________________       │
│                                                                         │
│ 2. THE SOLUTION                                                         │
│    Complete this sentence in exactly ONE line:                          │
│    "[Product] helps [user type] to [action] by [method]."              │
│                                                                         │
│    Example: "FastDeploy helps developers deploy code in seconds        │
│    by providing one-click deployment pipelines."                        │
│                                                                         │
│    Your answer: ________________________________________________       │
│                                                                         │
│ 3. THE SUCCESS                                                          │
│    Complete this sentence:                                              │
│    "I will know this succeeded when [measurable outcome]."             │
│                                                                         │
│    Example: "I will know this succeeded when 100 developers            │
│    deploy at least once per day with < 5 min deploy time."             │
│                                                                         │
│    Your answer: ________________________________________________       │
│                                                                         │
│ 4. THE SCOPE                                                            │
│    List exactly 3-5 things this WILL do in V1:                         │
│    1. ____________________________________________________________     │
│    2. ____________________________________________________________     │
│    3. ____________________________________________________________     │
│                                                                         │
│    List exactly 3-5 things this will NOT do (ever or in V1):           │
│    1. ____________________________________________________________     │
│    2. ____________________________________________________________     │
│    3. ____________________________________________________________     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Working with Claude Code at Gate 1

```
YOU: "I want to build [idea]. Before we start, help me achieve clarity.
     Ask me questions until we both understand the problem completely.
     Challenge my assumptions. Be critical."

CLAUDE: [Asks probing questions]

YOU: [Answer honestly, including "I don't know"]

CLAUDE: [Summarizes understanding, identifies gaps]

REPEAT until you can fill out the Clarity Test with confidence.
```

### Gate 1 Exit Criteria

```
□ I can explain the problem in one sentence
□ I can explain the solution in one sentence
□ I have a measurable definition of success
□ I know exactly what is in scope
□ I know exactly what is out of scope
□ I have written this down in docs/clarity.md
```

**If any checkbox is unclear: DO NOT PROCEED.**

---

## GATE 2: VALIDATION

**The Question:** "Is this worth building?"

### The Test

```
┌─────────────────────────────────────────────────────────────────────────┐
│ VALIDATION TEST                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ EVIDENCE REQUIRED (check all that apply):                               │
│                                                                         │
│ □ I have talked to 5+ potential users                                  │
│   Names: ________________________________________________________      │
│                                                                         │
│ □ At least 3 said they would use this immediately                      │
│   Quotes: _______________________________________________________      │
│                                                                         │
│ □ I understand why existing solutions don't work for them              │
│   Reasons: ______________________________________________________      │
│                                                                         │
│ □ I have validated the technical approach is feasible                  │
│   How: __________________________________________________________      │
│                                                                         │
│ □ I have estimated effort vs. value                                    │
│   Effort: ____ weeks  |  Value: ____________________________           │
│                                                                         │
│ FOR PERSONAL/LEARNING PROJECTS:                                         │
│                                                                         │
│ □ I have a clear learning goal                                         │
│   Goal: _________________________________________________________      │
│                                                                         │
│ □ I have time blocked to complete this                                 │
│   Timeline: _____________________________________________________      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### The Kill Criteria

**STOP the project if:**

```
- No one wants it except you
- You can't explain why existing solutions aren't enough
- The effort vastly exceeds the value
- You're building to avoid making a harder decision
- You're building because the technology is "cool"
```

### Working with Claude Code at Gate 2

```
YOU: "Here's my clarity document. Now help me validate:
     1. What questions should I ask potential users?
     2. What existing solutions should I evaluate?
     3. What are the technical risks I should prototype?"

CLAUDE: [Provides validation plan]

YOU: [Go do the validation - talk to users, test competitors]

YOU: "Here's what I learned: [findings]. Should I proceed?"

CLAUDE: [Gives honest assessment based on evidence]
```

### Gate 2 Exit Criteria

```
□ I have real evidence (not assumptions) that this is worth building
□ I have documented the evidence in docs/validation.md
□ I have made a conscious GO/NO-GO decision
□ If GO: I commit to seeing this through
```

**If evidence is weak: DO NOT PROCEED. Validate more or kill the project.**

---

## GATE 3: DESIGN

**The Question:** "Do I know exactly how to build it?"

### The Test

You must be able to answer YES to all of these:

```
┌─────────────────────────────────────────────────────────────────────────┐
│ DESIGN TEST                                                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ARCHITECTURE                                                            │
│ □ I can draw the system on a whiteboard and explain it                 │
│ □ I know every component and why it exists                             │
│ □ I know how data flows through the system                             │
│ □ I know what happens when each component fails                        │
│                                                                         │
│ DATA                                                                    │
│ □ I can draw the data model and explain every relationship             │
│ □ I know how the data will grow over time                              │
│ □ I know what queries will be expensive                                │
│                                                                         │
│ API                                                                     │
│ □ I have defined every endpoint                                        │
│ □ I have defined request/response formats                              │
│ □ I have defined error formats                                         │
│ □ I have written the OpenAPI spec                                      │
│                                                                         │
│ SECURITY                                                                │
│ □ I know how authentication works                                      │
│ □ I know how authorization works                                       │
│ □ I have identified the main threats                                   │
│ □ I have mitigations for each threat                                   │
│                                                                         │
│ TECHNOLOGY                                                              │
│ □ I have chosen every technology (language, framework, database)       │
│ □ I can justify each choice with specific reasons                      │
│ □ I have documented alternatives I rejected and why                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Working with Claude Code at Gate 3

```
SESSION 1: Architecture
YOU: "Help me design the architecture. Here's my clarity doc and validation.
     I need:
     1. System architecture diagram
     2. Component breakdown
     3. Data flow diagram

     Start with questions to understand my constraints."

CLAUDE: [Asks about scale, team size, timeline, existing systems]

YOU: [Provides context]

CLAUDE: [Proposes architecture]

YOU: "What are the weaknesses of this design?"

CLAUDE: [Identifies trade-offs]

YOU: [Decide if acceptable]

─────────────────────────────────────────────────────────────────

SESSION 2: Data Model
YOU: "Now let's design the data model. Based on the architecture,
     what entities do we need? Walk me through each one."

CLAUDE: [Proposes entities with fields]

YOU: "Draw the ER diagram and explain the relationships."

CLAUDE: [Creates diagram]

YOU: "What queries will be slow? What indexes do we need?"

CLAUDE: [Analyzes performance]

─────────────────────────────────────────────────────────────────

SESSION 3: API Design
YOU: "Now let's design the API. For each feature in scope,
     what endpoints do we need? Use REST conventions."

CLAUDE: [Proposes endpoints]

YOU: "Write the OpenAPI spec for these."

CLAUDE: [Creates api-spec.yaml]

─────────────────────────────────────────────────────────────────

SESSION 4: Security Design
YOU: "Now let's design security. Walk me through:
     1. Authentication approach
     2. Authorization approach
     3. Main threats (use STRIDE)
     4. Mitigations for each"

CLAUDE: [Creates security design]

─────────────────────────────────────────────────────────────────

SESSION 5: Technology Decisions
YOU: "Now let's finalize technology choices. For each decision,
     give me 2-3 options with pros/cons, then recommend one."

CLAUDE: [For each: database, framework, etc.]

YOU: [Makes decisions, documents in ADRs]

─────────────────────────────────────────────────────────────────

SESSION 6: Design Review
YOU: "Review the complete design:
     1. What could go wrong?
     2. What's the weakest part?
     3. What would you change if we had more time?
     4. What am I missing?"

CLAUDE: [Critical review]

YOU: [Address concerns or accept trade-offs]
```

### Gate 3 Exit Criteria

```
□ docs/architecture.md complete with diagrams
□ docs/data-model.md complete with ER diagram
□ docs/api-spec.yaml complete
□ docs/security.md complete
□ docs/adr/*.md for each technology decision
□ I can explain the entire design without looking at docs
□ I have reviewed with someone else (or Claude critically)
```

**If you can't explain any part of the design: DO NOT PROCEED.**

---

## GATE 4: FOUNDATION

**The Question:** "Can I build it correctly from the start?"

### The Test

```
┌─────────────────────────────────────────────────────────────────────────┐
│ FOUNDATION TEST                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ REPOSITORY                                                              │
│ □ Git repository created                                               │
│ □ Branch protection on main                                            │
│ □ README explains how to set up                                        │
│ □ CONTRIBUTING explains how to contribute                              │
│                                                                         │
│ DEVELOPMENT ENVIRONMENT                                                 │
│ □ One command sets up everything                                       │
│ □ Runs locally without cloud dependencies                              │
│ □ Seed data available for testing                                      │
│ □ Fresh clone to working app < 15 minutes                              │
│                                                                         │
│ CI PIPELINE                                                             │
│ □ Builds on every push                                                 │
│ □ Runs unit tests                                                      │
│ □ Runs linting                                                         │
│ □ Blocks merge if any fail                                             │
│                                                                         │
│ CD PIPELINE                                                             │
│ □ Auto-deploys to staging on main merge                                │
│ □ Manual approval for production                                       │
│ □ Rollback procedure documented                                        │
│                                                                         │
│ DATABASE                                                                │
│ □ Database runs locally                                                │
│ □ Migration tool configured                                            │
│ □ Initial schema migrated                                              │
│ □ Can reset database easily                                            │
│                                                                         │
│ TESTING                                                                 │
│ □ Test framework configured                                            │
│ □ Sample test passes                                                   │
│ □ Coverage reporting works                                             │
│                                                                         │
│ VERIFICATION                                                            │
│ □ Teammate can clone and run (or you on fresh machine)                 │
│ □ CI passes                                                            │
│ □ Can deploy to staging                                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Working with Claude Code at Gate 4

```
SESSION 1: Project Creation
YOU: "Create the project foundation based on our design docs.
     Tech stack: [from ADRs]

     Start with:
     1. Project structure
     2. Dependencies
     3. Configuration files
     4. Dockerfile
     5. docker-compose.yml"

CLAUDE: [Creates foundation]

YOU: "Does it run? Let me try: make setup && make run"

[Test it actually works]

─────────────────────────────────────────────────────────────────

SESSION 2: Database Setup
YOU: "Set up the database layer:
     1. Configure connection
     2. Set up Flyway/Liquibase
     3. Create initial migration from data model
     4. Create seed data script"

CLAUDE: [Creates database setup]

YOU: "Let me run migrations: make db-migrate"

[Test migrations work]

─────────────────────────────────────────────────────────────────

SESSION 3: CI Pipeline
YOU: "Create GitHub Actions CI pipeline:
     1. Build
     2. Unit tests
     3. Linting
     4. Coverage (80% gate)"

CLAUDE: [Creates .github/workflows/ci.yml]

YOU: "Push and verify CI passes."

[Actually push and verify]

─────────────────────────────────────────────────────────────────

SESSION 4: CD Pipeline
YOU: "Create CD pipeline:
     1. Deploy to staging on main merge
     2. Manual approval for production
     3. Smoke tests after deploy"

CLAUDE: [Creates .github/workflows/cd.yml]

─────────────────────────────────────────────────────────────────

SESSION 5: Verification
YOU: "Let's verify the foundation:
     1. Fresh clone and setup
     2. Run tests
     3. Deploy to staging

     Walk me through each step."

[Actually do each step]
```

### Gate 4 Exit Criteria

```
□ Fresh clone → working app in < 15 minutes
□ CI pipeline passes
□ Can deploy to staging
□ Database migrations work
□ Tests run and pass
□ Committed and pushed to main
```

**If foundation is shaky: FIX IT BEFORE PROCEEDING.**

---

## GATE 5: EXECUTION

**The Question:** "Is each piece working before moving on?"

### The Principle

```
BUILD VERTICALLY, NOT HORIZONTALLY

❌ WRONG (Horizontal):
   Build all entities → Build all repositories → Build all services → Build all controllers
   (If first layer is wrong, everything is wrong)

✅ RIGHT (Vertical):
   Build Feature 1 (entity → repo → service → controller → test → merge)
   Build Feature 2 (entity → repo → service → controller → test → merge)
   (Each feature is complete and verified before next)
```

### The Execution Loop

```
FOR EACH FEATURE:

┌─────────────────────────────────────────────────────────────────────────┐
│                        THE EXECUTION LOOP                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│     ┌─────────┐                                                        │
│     │ 1. SPEC │ ─── Write exact requirements for this feature          │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │ 2. TEST │ ─── Write failing test first                          │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │ 3. BUILD│ ─── Implement minimum to pass test                     │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │4. VERIFY│ ─── Run tests, manual testing                          │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │5. REVIEW│ ─── Self-review or peer review                         │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │6. COMMIT│ ─── Commit with clear message                          │
│     └────┬────┘                                                        │
│          │                                                              │
│          ▼                                                              │
│     ┌─────────┐                                                        │
│     │ 7. NEXT │ ─── Move to next feature                               │
│     └─────────┘                                                        │
│                                                                         │
│  DO NOT proceed to step N+1 until step N is 100% complete.             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Working with Claude Code at Gate 5

```
FOR EACH FEATURE:

YOU: "Feature: [Name]

     Requirements:
     - [Req 1]
     - [Req 2]
     - [Req 3]

     Let's build this vertically.

     Step 1: Write the test first."

CLAUDE: [Writes failing test]

YOU: "Run the test. Confirm it fails."

[Test fails as expected]

YOU: "Now implement to make the test pass. Minimum code only."

CLAUDE: [Implements]

YOU: "Run the test."

[Test passes]

YOU: "Review this code:
     1. Any bugs?
     2. Any security issues?
     3. Any edge cases?
     4. Any improvements?"

CLAUDE: [Reviews]

YOU: [Address any issues]

YOU: "Commit this feature."

CLAUDE: [Creates commit]

YOU: "Next feature: [Name]..."

[REPEAT]
```

### Gate 5 Exit Criteria

```
FOR EACH FEATURE:
□ Requirements documented
□ Tests written and passing
□ Implementation complete
□ Code reviewed
□ Committed to main
□ CI passes

FOR ALL FEATURES:
□ All MVP features complete
□ Test coverage > 80%
□ No known bugs
□ Integration tests pass
□ API documentation matches implementation
```

**If any feature is incomplete: COMPLETE IT BEFORE NEXT FEATURE.**

---

## GATE 6: HARDENING

**The Question:** "Is it ready for real users?"

### The Hardening Checklist

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      HARDENING CHECKLIST                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ SECURITY                                                                │
│ □ OWASP Top 10 reviewed and addressed                                  │
│ □ Input validation on ALL endpoints                                    │
│ □ SQL injection impossible (parameterized queries)                     │
│ □ XSS impossible (output encoding, CSP)                                │
│ □ Authentication cannot be bypassed                                    │
│ □ Authorization checked on every request                               │
│ □ Secrets not in code                                                  │
│ □ Dependencies scanned, no critical vulns                              │
│ □ Rate limiting in place                                               │
│ □ Security headers configured                                          │
│                                                                         │
│ PERFORMANCE                                                             │
│ □ Response times measured                                              │
│ □ Slow queries identified and fixed                                    │
│ □ Indexes added where needed                                           │
│ □ N+1 queries eliminated                                               │
│ □ Caching added where beneficial                                       │
│ □ Load tested at expected traffic                                      │
│ □ Load tested at 2-3x expected traffic                                 │
│ □ Breaking point identified                                            │
│                                                                         │
│ RELIABILITY                                                             │
│ □ All errors handled gracefully                                        │
│ □ No stack traces leaked to users                                      │
│ □ Retry logic for transient failures                                   │
│ □ Timeouts configured                                                  │
│ □ Circuit breakers for external services                               │
│ □ Graceful degradation planned                                         │
│                                                                         │
│ OBSERVABILITY                                                           │
│ □ Structured logging                                                   │
│ □ Correlation IDs on all requests                                      │
│ □ Key metrics exposed                                                  │
│ □ Health endpoints work                                                │
│ □ Errors logged with context                                           │
│ □ Dashboards created                                                   │
│ □ Alerts configured                                                    │
│                                                                         │
│ OPERATIONS                                                              │
│ □ Runbook written                                                      │
│ □ Common issues documented                                             │
│ □ Rollback procedure documented and tested                             │
│ □ Backup procedure documented                                          │
│ □ Recovery procedure documented and tested                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Working with Claude Code at Gate 6

```
SESSION 1: Security Review
YOU: "Review the entire codebase for security issues.
     Go through OWASP Top 10 one by one.
     Be thorough. Find problems."

CLAUDE: [Reviews, finds issues]

YOU: "Fix each issue."

CLAUDE: [Fixes]

YOU: "Verify fixes."

─────────────────────────────────────────────────────────────────

SESSION 2: Performance
YOU: "Let's optimize performance:
     1. Run load tests
     2. Identify bottlenecks
     3. Fix them
     4. Verify improvement"

CLAUDE: [Runs tests, identifies issues]

YOU: "Fix the top 3 issues."

CLAUDE: [Fixes]

─────────────────────────────────────────────────────────────────

SESSION 3: Observability
YOU: "Set up observability:
     1. Structured logging with correlation IDs
     2. Prometheus metrics
     3. Grafana dashboards
     4. Alert rules"

CLAUDE: [Implements each]

YOU: "Verify I can see a request flow through the system."

─────────────────────────────────────────────────────────────────

SESSION 4: Operations Prep
YOU: "Create the runbook:
     1. How to deploy
     2. How to rollback
     3. How to debug common issues
     4. How to restore from backup"

CLAUDE: [Creates runbook]
```

### Gate 6 Exit Criteria

```
□ Security checklist 100% complete
□ Performance checklist 100% complete
□ Reliability checklist 100% complete
□ Observability checklist 100% complete
□ Operations checklist 100% complete
□ Runbook tested (actually followed it)
□ Load testing passed
□ Security scan clean
```

**If any item is incomplete: FIX IT BEFORE DEPLOYING.**

---

## GATE 7: OPERATION

**The Question:** "Can I keep it running reliably?"

### Pre-Launch Verification

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     PRE-LAUNCH CHECKLIST                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ STAGING VERIFICATION                                                    │
│ □ Deployed to staging                                                  │
│ □ All tests pass against staging                                       │
│ □ Load tests pass against staging                                      │
│ □ Manual testing complete                                              │
│ □ Staging stable for 24+ hours                                         │
│                                                                         │
│ PRODUCTION READY                                                        │
│ □ Production infrastructure provisioned                                │
│ □ Database provisioned and migrated                                    │
│ □ Secrets configured                                                   │
│ □ SSL/TLS configured                                                   │
│ □ DNS configured                                                       │
│ □ CDN configured (if needed)                                           │
│ □ Monitoring connected                                                 │
│ □ Alerting configured                                                  │
│ □ On-call rotation set                                                 │
│                                                                         │
│ LAUNCH PREPARATION                                                      │
│ □ Rollback tested                                                      │
│ □ Team trained on operations                                           │
│ □ Support prepared (if applicable)                                     │
│ □ Communication plan (if applicable)                                   │
│ □ Launch time chosen (not Friday 5 PM)                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Launch Procedure

```
THE LAUNCH:

1. DEPLOY CANARY (5% traffic)
   □ Deploy to production (canary)
   □ Monitor for 15 minutes
   □ Check: Error rate? Latency? Logs?
   □ GO/NO-GO decision

2. GRADUAL ROLLOUT
   □ Increase to 25%
   □ Monitor for 15 minutes
   □ Increase to 50%
   □ Monitor for 15 minutes
   □ Increase to 100%

3. POST-LAUNCH MONITORING
   □ Watch for 2 hours
   □ Check all dashboards
   □ Verify key user journeys
   □ Celebrate (briefly)

4. NEXT 24 HOURS
   □ Monitor closely
   □ Address any issues immediately
   □ Gather initial feedback
```

### Ongoing Operations

```
DAILY:
□ Check dashboards (5 min)
□ Review any alerts
□ Check error logs

WEEKLY:
□ Review metrics trends
□ Check dependency updates
□ Address any tech debt

MONTHLY:
□ Security patches
□ Performance review
□ Cost review
□ Update dependencies

QUARTERLY:
□ Load testing
□ Disaster recovery drill
□ Architecture review
□ Team retrospective

ANNUALLY:
□ Major version upgrades
□ Penetration testing
□ Compliance audit
□ Full infrastructure review
```

---

# PART 2: EXISTING PROJECTS

## The Recovery Framework

For projects already in progress, use this framework to get back on track.

### Step 1: Assessment

```
YOU: "I have an existing project. Help me assess its current state.
     Let's go through each area systematically."

─────────────────────────────────────────────────────────────────

ASSESSMENT CHECKLIST:

CLARITY
□ Is the problem clearly documented?
□ Is the scope clearly defined?
□ Are success metrics defined?
→ If NO to any: Create docs/clarity.md

DESIGN
□ Is there architecture documentation?
□ Is there a data model diagram?
□ Is there an API specification?
□ Is there security documentation?
→ If NO to any: Create the missing docs

FOUNDATION
□ Can a new person set up in < 15 minutes?
□ Is there CI running?
□ Is there CD running?
□ Are tests running?
→ If NO to any: Fix foundation first

CODE QUALITY
□ Is test coverage > 80%?
□ Are there security vulnerabilities?
□ Are there known bugs?
□ Is there significant tech debt?
→ Document all issues

OPERATIONS
□ Is there monitoring?
□ Is there alerting?
□ Is there a runbook?
□ Can you deploy with confidence?
→ If NO to any: Add to priority list
```

### Step 2: Prioritization

```
PRIORITY MATRIX:

                    LOW EFFORT    HIGH EFFORT
                   ┌─────────────┬─────────────┐
    HIGH IMPACT    │ DO FIRST    │ PLAN THESE  │
                   │             │             │
                   ├─────────────┼─────────────┤
    LOW IMPACT     │ DO LATER    │ DON'T DO    │
                   │             │             │
                   └─────────────┴─────────────┘

LIST ALL GAPS, THEN CATEGORIZE:

DO FIRST (High Impact, Low Effort):
1. ________________________________
2. ________________________________
3. ________________________________

PLAN THESE (High Impact, High Effort):
1. ________________________________
2. ________________________________
3. ________________________________

DO LATER (Low Impact, Low Effort):
1. ________________________________
2. ________________________________

DON'T DO (Low Impact, High Effort):
1. ________________________________
```

### Step 3: Execution

```
FOR EACH ITEM IN "DO FIRST":

YOU: "I need to add [missing thing].
     Current state: [describe]
     Goal state: [describe]
     Constraint: Don't break existing functionality.

     What's the minimal change to achieve this?"

CLAUDE: [Proposes approach]

YOU: [Implement, test, verify]

YOU: "Commit this improvement."

[REPEAT until "DO FIRST" is empty]

THEN: Plan "PLAN THESE" items into sprints
```

### Step 4: Prevention

```
AFTER RECOVERY, PREVENT REGRESSION:

□ Add missing item to CI (so it's enforced)
□ Add to PR checklist
□ Document in CONTRIBUTING.md
□ Add to project checklist for next project

EXAMPLE:
- "Test coverage dropped" → Add coverage gate to CI
- "Docs got outdated" → Add docs check to PR template
- "Security vuln introduced" → Add security scan to CI
```

---

# PART 3: WORKING WITH CLAUDE CODE

## The Communication Protocol

### Starting a Session

```
TEMPLATE:

"Project: [name]
Current state: [brief summary or "check MEMORY.md"]
Today's goal: [specific outcome]

Let's start with: [first step]"
```

### Requesting Work

```
TEMPLATE:

"Task: [name]

Requirements:
- [Req 1]
- [Req 2]
- [Req 3]

Constraints:
- [Constraint 1]
- [Constraint 2]

Expected output:
- [Output 1]
- [Output 2]

Start with: [first component]"
```

### Verification

```
AFTER EVERY SIGNIFICANT CHANGE:

YOU: "Let me verify this works."

[Actually test it]

YOU: "Tests pass. / Tests fail because [reason]."

[If fail: debug together]
[If pass: commit and continue]
```

### Ending a Session

```
TEMPLATE:

"Let's wrap up:
1. Summarize what we accomplished
2. Update MEMORY.md
3. List next steps
4. Commit changes"
```

---

## The Quality Protocol

### Before Every Commit

```
□ Tests pass
□ I understand all the code
□ I manually tested the change
□ There are no obvious bugs
□ There are no security issues
□ Code follows project patterns
```

### Before Every Merge to Main

```
□ All commit checks pass
□ PR reviewed (by person or carefully by Claude)
□ No merge conflicts
□ CI passes
□ Changes documented (if needed)
```

### Before Every Deploy

```
□ All tests pass (unit, integration, e2e)
□ Security scan clean
□ Performance acceptable
□ Rollback plan ready
□ Monitoring ready
□ Team aware
```

---

# THE MASTER CHECKLIST

## For Reference: All Gates in One View

```
GATE 1: CLARITY
□ Problem statement (one sentence)
□ Solution statement (one sentence)
□ Success definition (measurable)
□ Scope (what's in, what's out)
→ Output: docs/clarity.md

GATE 2: VALIDATION
□ User evidence
□ Feasibility verified
□ Effort vs. value assessed
□ GO decision made
→ Output: docs/validation.md

GATE 3: DESIGN
□ Architecture (docs/architecture.md)
□ Data model (docs/data-model.md)
□ API spec (docs/api-spec.yaml)
□ Security design (docs/security.md)
□ Tech decisions (docs/adr/*.md)
→ Output: Complete design docs

GATE 4: FOUNDATION
□ Repository set up
□ Dev environment works
□ CI pipeline works
□ CD pipeline works
□ Database works
□ Testing works
→ Output: One-command setup, CI/CD working

GATE 5: EXECUTION
□ Each feature: spec → test → build → verify → commit
□ All MVP features complete
□ Test coverage > 80%
□ API docs match implementation
→ Output: Working software

GATE 6: HARDENING
□ Security hardened
□ Performance optimized
□ Reliability ensured
□ Observability added
□ Operations documented
→ Output: Production-ready software

GATE 7: OPERATION
□ Staging verified
□ Production deployed
□ Monitoring active
□ Operations ongoing
→ Output: Running production system
```

---

# FINAL WORDS

## The Truth About Success

The difference between projects that succeed and projects that fail is not:
- Intelligence
- Experience
- Technology choices
- Team size
- Budget

**It is discipline.**

Following this framework is not exciting. It's not innovative. It's not "move fast and break things."

It's disciplined. It's methodical. It's boring.

And it works.

Every time.

---

## The Commitment

Before starting any project, make this commitment:

```
I COMMIT TO:

□ Not skipping steps because I'm eager to code
□ Not proceeding when something is unclear
□ Not deploying when something is broken
□ Documenting decisions, not just making them
□ Testing before committing
□ Verifying before moving on
□ Asking "is this ready?" honestly

SIGNATURE: _____________________
DATE: _________________________
```

---

*The best engineers are not the fastest coders. They are the most disciplined thinkers.*

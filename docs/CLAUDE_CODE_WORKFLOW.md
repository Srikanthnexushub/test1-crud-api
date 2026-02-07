# How to Work with Claude Code Like a Senior Engineer

## The Secret: It's Not About Prompts, It's About Process

Most people fail because they treat AI like a magic box. Senior engineers succeed because they treat it like a **junior developer who is incredibly fast but needs clear direction**.

---

## The 5 Principles

### 1. YOU Are the Architect, Claude is the Builder

```
❌ WRONG: "Build me an authentication system"
   (Too vague, Claude will make assumptions you don't want)

✅ RIGHT: "I need JWT authentication with these specs:
   - Access token: 15 min expiry
   - Refresh token: 7 days, stored in DB
   - Endpoints: /login, /register, /refresh
   - Use BCrypt for passwords
   - Return format: { token, refreshToken, user }

   Start with the entity and repository layer."
```

**You decide WHAT and WHY. Claude figures out HOW.**

### 2. Work in Small, Verified Steps

```
❌ WRONG: "Build the entire user management system"
   (Too big, hard to verify, errors compound)

✅ RIGHT:
   Step 1: "Create the User entity with these fields: ..."
   [Verify it works]

   Step 2: "Add the UserRepository with findByEmail"
   [Verify it works]

   Step 3: "Create UserService with register method"
   [Verify it works]

   Step 4: "Add the REST controller"
   [Verify it works]
```

**Each step should be testable in under 5 minutes.**

### 3. Always Provide Context

```
❌ WRONG: "Add 2FA"
   (What kind? Where? How does it fit with existing code?)

✅ RIGHT: "I need to add TOTP-based 2FA. Here's the current state:
   - We have JWT auth working (UserController, UserService)
   - Users are stored in PostgreSQL (UserEntity)
   - We're using Spring Boot 3.2 + Java 17

   The flow should be:
   1. User enables 2FA → get QR code
   2. User scans with Google Authenticator
   3. On next login, if 2FA enabled → require code

   Start with the TwoFactorService."
```

**Claude doesn't remember previous projects. Always set the scene.**

### 4. Review Before Moving On

```
❌ WRONG: "Build A, then B, then C, then D"
   (If A is wrong, B, C, D are all wrong)

✅ RIGHT:
   "Build A"
   [You review the code]
   [You test it]
   [You ask questions if unclear]
   "Looks good, now build B"
```

**Never let Claude run for 10 steps without verification.**

### 5. Use Claude's Strengths

```
CLAUDE IS GREAT AT:
✅ Writing boilerplate code fast
✅ Implementing well-defined specs
✅ Finding bugs in code
✅ Explaining complex code
✅ Generating tests
✅ Writing documentation
✅ Refactoring existing code
✅ Security review

CLAUDE NEEDS YOUR HELP WITH:
⚠️ Business decisions
⚠️ Architecture trade-offs
⚠️ What to prioritize
⚠️ User experience decisions
⚠️ What NOT to build
⚠️ Context about your specific situation
```

---

## The Workflow: Session by Session

### Session 1: Project Setup

```
YOU: "I'm starting a new project. Here's what I need:
     - [Brief description]
     - [Tech stack I want]
     - [Key features]

     Help me create:
     1. Project structure
     2. Initial configuration
     3. Development environment setup"

CLAUDE: [Creates foundation]

YOU: [Verify everything works]
     "Run the app and confirm it starts"

YOU: "Commit this as initial setup"
```

### Session 2: Core Feature 1

```
YOU: "Now let's build [Feature 1]. Here are the requirements:
     - [Requirement 1]
     - [Requirement 2]
     - [Requirement 3]

     Start with the data model."

CLAUDE: [Creates entities]

YOU: [Review] "Why did you use @ManyToMany here instead of @OneToMany?"

CLAUDE: [Explains reasoning]

YOU: "Makes sense. Now add the service layer."

CLAUDE: [Creates service]

YOU: [Test it] "Write a test for the register method"

CLAUDE: [Writes test]

YOU: "Run the tests"

CLAUDE: [Runs tests, shows results]

YOU: "All passing. Now add the controller."

[Continue until feature complete]

YOU: "Commit this feature"
```

### Session 3: Continue Building

```
YOU: "Summarize where we left off"

CLAUDE: [Reads MEMORY.md, summarizes state]

YOU: "Good. Next feature: [Feature 2]..."
```

---

## The Communication Patterns

### Pattern 1: Spec-First Request

```
"Implement [feature] with these exact specifications:

Input:
- [field]: [type] - [validation]
- [field]: [type] - [validation]

Output:
- [field]: [type]
- [field]: [type]

Behavior:
- When [condition], then [action]
- When [condition], then [action]

Error cases:
- If [condition], return [error]
- If [condition], return [error]"
```

### Pattern 2: Explore-Then-Implement

```
"Before implementing, I need to understand:
1. How does [existing code] handle [thing]?
2. What patterns are we using for [thing]?
3. Are there any constraints I should know about?

Explore the codebase and summarize, then we'll implement."
```

### Pattern 3: Fix With Context

```
"I'm seeing this error: [error message]

Here's what I was trying to do: [action]
Here's what I expected: [expected]
Here's what happened: [actual]

Find the root cause and fix it."
```

### Pattern 4: Review Request

```
"Review this code/approach for:
- Security issues
- Performance problems
- Edge cases I might have missed
- Better alternatives

Be critical. I want to find problems now, not in production."
```

### Pattern 5: Incremental Enhancement

```
"Current state: [what exists now]
Desired state: [what I want]
Constraint: Don't break [existing functionality]

Make the minimal changes needed."
```

---

## The Session Structure

### Opening a Session

```
YOU: "Let's continue working on [project].
     Summarize the current state and what we were working on."

CLAUDE: [Reads MEMORY.md and recent files]
        "Last session we completed X. Current state:
         - Feature A: Complete
         - Feature B: In progress (controller done, tests pending)
         - Feature C: Not started

         We left off at [specific point]."

YOU: "Good. Let's finish Feature B tests."
```

### During a Session

```
EVERY 15-30 MINUTES:
- Verify what was built works
- Run tests
- Commit working code
- Update any documentation

DON'T:
- Let Claude write for 2 hours without checking
- Skip testing "to save time"
- Forget to commit working checkpoints
```

### Closing a Session

```
YOU: "Let's wrap up. Please:
     1. Summarize what we accomplished
     2. Update MEMORY.md with current state
     3. List any pending items for next session
     4. Commit all changes"

CLAUDE: [Does all of this]

YOU: [Verify commit, push if ready]
```

---

## Real Examples from This Project

### Example 1: Starting the 2FA Feature

**What I said:**
```
"I need to add TOTP-based 2FA. Requirements:
- Use dev.samstevens.totp library
- Generate QR codes for authenticator apps
- 10 backup codes, 8 chars each
- New endpoints under /api/v1/auth/2fa

Start with the TwoFactorService."
```

**Why it worked:**
- Specified the library (no ambiguity)
- Defined the scope (QR codes, backup codes)
- Set the endpoint structure
- Told Claude where to start (service layer)

### Example 2: Documentation Request

**What I said:**
```
"Create /docs/architecture.md for this project.
Make it enterprise-grade - the kind Netflix would write.
Include:
- System diagrams (ASCII)
- Layer descriptions
- Component catalog
- All the details from exploring the codebase"
```

**Why it worked:**
- Specified the output (architecture.md)
- Set the quality bar (enterprise-grade, Netflix)
- Listed what to include
- Asked Claude to explore first (gather context)

### Example 3: Debugging

**What I said:**
```
"The login endpoint returns 401 even with correct credentials.
I verified:
- User exists in database
- Password is correct (tested with BCrypt online)
- Email is verified

Find why authentication is failing."
```

**Why it worked:**
- Described the symptom clearly
- Listed what I already verified
- Asked for root cause, not just a fix

---

## The Mistakes to Avoid

### Mistake 1: The Mega-Prompt

```
❌ "Build me a complete e-commerce system with user auth,
    product catalog, shopping cart, checkout, payments,
    order management, admin panel, and reporting."

✅ "Let's build an e-commerce system. First, let's set up
    user authentication. Here are the requirements..."
```

### Mistake 2: No Verification

```
❌ "Build A, B, C, D, E" [never checking if any of them work]

✅ "Build A" → Test → "Build B" → Test → ...
```

### Mistake 3: Assuming Context

```
❌ "Add the login endpoint"
   [Claude doesn't know your patterns, existing code structure]

✅ "Add the login endpoint. Follow the pattern in UserController.
    Use the existing UserService.authenticate method."
```

### Mistake 4: Being Too Vague

```
❌ "Make it better"
❌ "Fix the security issues"
❌ "Improve performance"

✅ "The /users endpoint is slow (2s response). Profile it and
    identify the bottleneck."
✅ "Review the auth flow for OWASP Top 10 vulnerabilities."
```

### Mistake 5: Not Using Claude's Full Capabilities

```
❌ Only asking Claude to write code

✅ Also use Claude to:
   - Review your code
   - Explain code you don't understand
   - Generate test cases
   - Write documentation
   - Explore codebases
   - Debug issues
   - Refactor for readability
```

---

## The Power Moves

### Power Move 1: Parallel Exploration

```
"I need to understand this codebase. Explore in parallel:
1. How authentication works
2. The database schema
3. The API structure
4. The testing approach

Give me a summary of each."
```

(Claude launches multiple exploration agents)

### Power Move 2: Plan Mode

```
"I want to add [complex feature]. Enter plan mode:
1. Explore the codebase to understand current patterns
2. Design the approach
3. Present the plan for my approval
4. Only implement after I approve"
```

### Power Move 3: Checkpoint Commits

```
"After each working component:
1. Run tests
2. If passing, commit with descriptive message
3. Then continue to next component

This way we have rollback points."
```

### Power Move 4: Memory Management

```
At start of project:
"Create /docs/MEMORY.md to track:
- Current state
- Key decisions made
- Pending items
- Session notes

Update this at the end of each session."
```

### Power Move 5: Quality Gates

```
"Before we call this feature done:
1. Run all tests
2. Check code coverage
3. Review for security issues
4. Update documentation
5. Create commit

Don't proceed until all gates pass."
```

---

## The Template for Any New Project

### Step 1: Project Kickoff
```
"I'm building [project name].

Purpose: [one sentence]
Tech stack: [list]
Key features: [list]

Create the project foundation:
1. Project structure
2. Dependencies
3. Configuration
4. Development setup (docker-compose)
5. README with setup instructions

Make it work with one command: `make setup && make run`"
```

### Step 2: Each Feature
```
"Next feature: [name]

Requirements:
- [req 1]
- [req 2]
- [req 3]

Acceptance criteria:
- [criterion 1]
- [criterion 2]

Start with [specific layer/component]."
```

### Step 3: Hardening
```
"Core features are complete. Now harden:
1. Security review (OWASP Top 10)
2. Add input validation
3. Add rate limiting
4. Add proper error handling
5. Add logging

Go through each systematically."
```

### Step 4: Documentation
```
"Create production-ready documentation:
1. Architecture doc
2. API specification
3. Runbook for operations
4. ADRs for key decisions

Make it enterprise-grade."
```

### Step 5: Testing
```
"Add comprehensive tests:
1. Unit tests for services (80% coverage)
2. Integration tests for APIs
3. Load tests (k6 scripts)

Run tests and show me the coverage report."
```

### Step 6: Deployment
```
"Prepare for deployment:
1. Dockerfile optimized for production
2. CI/CD pipeline (GitHub Actions)
3. Environment configuration
4. Health checks
5. Monitoring setup"
```

---

## Summary: The Real Secret

The engineers at Netflix, Google, and SpaceX who are most effective with AI tools do these things:

1. **They think first, then ask** - They know what they want before asking
2. **They work incrementally** - Small steps, verified constantly
3. **They provide context** - Never assume Claude remembers or knows
4. **They stay in control** - Claude proposes, they approve
5. **They verify everything** - Trust but verify
6. **They document as they go** - Memory files, commits, notes
7. **They use the right tool for the job** - Claude for implementation, their brain for decisions

**The secret is not a magic prompt. The secret is discipline and process.**

---

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CLAUDE CODE QUICK REFERENCE                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  SESSION START:                                                         │
│  "Summarize where we left off from MEMORY.md"                          │
│                                                                         │
│  NEW FEATURE:                                                           │
│  "[Feature name]. Requirements: [list]. Start with [component]."       │
│                                                                         │
│  DEBUGGING:                                                             │
│  "Error: [message]. Expected: [X]. Actual: [Y]. Find root cause."      │
│                                                                         │
│  REVIEW:                                                                │
│  "Review [code/approach] for [security/performance/issues]."           │
│                                                                         │
│  EXPLORATION:                                                           │
│  "Explore the codebase. How does [X] work? What patterns used?"        │
│                                                                         │
│  SESSION END:                                                           │
│  "Summarize progress. Update MEMORY.md. Commit changes."               │
│                                                                         │
│  ALWAYS:                                                                │
│  ✓ Verify each step works                                              │
│  ✓ Commit working checkpoints                                          │
│  ✓ Provide context                                                     │
│  ✓ Review before approving                                             │
│  ✓ Ask "why" when unsure                                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

*The best prompt is a clear thought. The best workflow is verified steps.*

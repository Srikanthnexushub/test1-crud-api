# ADR-002: Blocking Email Verification

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team, Security Team
**Technical Story**: Ensure valid email addresses for user accounts

## Context

We need to ensure that users registering for accounts provide valid, accessible email addresses. This is critical for:
- Account recovery (password reset)
- Security notifications
- Preventing abuse (spam registrations)
- Communication with users

## Decision Drivers

* Need to verify email ownership before granting access
* Password reset functionality requires verified email
* Prevent fake account creation at scale
* Compliance with communication consent requirements
* User experience considerations

## Considered Options

1. **Blocking verification** (cannot login until verified)
2. **Non-blocking verification** (can login, limited features until verified)
3. **Delayed verification** (verify within X days or account suspended)
4. **No verification** (trust user-provided email)

## Decision Outcome

Chosen option: **Blocking verification**, because it ensures 100% of active accounts have verified email ownership, which is critical for security features like password reset.

### Implementation Details

```
Verification Flow:
1. User registers with email/password
2. Account created with emailVerified=false
3. Verification email sent with unique token (24-hour expiry)
4. User clicks link, token validated
5. Account marked emailVerified=true
6. User can now login

Token Specification:
- Format: UUID v4
- Expiry: 24 hours
- Single use: Marked as used after verification
- Resend: Generates new token, invalidates old
```

### Positive Consequences

* **Guaranteed valid emails**: All active users have verified email
* **Secure password reset**: Can trust email for account recovery
* **Reduced spam**: Higher barrier prevents mass fake accounts
* **Communication reliable**: Can reach all users via email
* **Simple logic**: Binary state (verified/not verified)

### Negative Consequences

* **Friction in registration**: Extra step before first use
* **Email deliverability issues**: Users may not receive verification email
* **Abandoned registrations**: Some users won't complete verification
* **Support overhead**: "Didn't receive email" support tickets

## Pros and Cons of the Options

### Blocking Verification (Chosen)

* Good, because guarantees all active accounts have valid email
* Good, because password reset can trust email ownership
* Good, because prevents mass fake account creation
* Good, because simple binary state model
* Bad, because adds friction to registration
* Bad, because email deliverability can block users

### Non-blocking Verification

* Good, because users can start using app immediately
* Good, because lower registration friction
* Bad, because password reset unreliable for unverified users
* Bad, because complex state management (partial features)
* Bad, because users may never verify

### Delayed Verification

* Good, because balances UX and verification
* Good, because grace period reduces friction
* Bad, because complex expiry logic
* Bad, because users may forget and lose access
* Bad, because creates support burden for expired accounts

### No Verification

* Good, because zero registration friction
* Good, because simplest implementation
* Bad, because password reset impossible for typo emails
* Bad, because enables spam/abuse at scale
* Bad, because unreliable user communication

## Mitigation Strategies

To address negative consequences:

1. **Email deliverability**:
   - Use reputable SMTP provider
   - Implement SPF, DKIM, DMARC
   - Clear "check spam folder" guidance

2. **Friction reduction**:
   - Clear, immediate feedback on registration
   - Resend verification option
   - Deep link support for mobile

3. **Support reduction**:
   - Self-service resend verification
   - Clear error messages
   - FAQ for common issues

## Links

* Related: [ADR-001 JWT Authentication](001-jwt-authentication.md) - Login requires verified email
* Related: Password Reset feature depends on this verification

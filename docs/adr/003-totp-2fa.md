# ADR-003: TOTP for Two-Factor Authentication

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team, Security Team
**Technical Story**: Add second factor authentication for enhanced security

## Context

Single-factor authentication (password only) is vulnerable to:
- Credential stuffing attacks
- Phishing
- Password database breaches
- Keyloggers

We need a second factor to protect high-value accounts and provide users with enhanced security options.

## Decision Drivers

* User demand for enhanced security
* Protection against password compromise
* Industry best practice for sensitive applications
* Cost considerations (SMS is expensive)
* User experience (must be usable)
* Offline support requirements

## Considered Options

1. **TOTP (Time-based One-Time Password)**
2. **SMS OTP**
3. **Email OTP**
4. **WebAuthn/FIDO2 (hardware keys)**
5. **Push notifications**

## Decision Outcome

Chosen option: **TOTP (Time-based One-Time Password)**, because it provides strong security, works offline, has no per-use cost, and integrates with popular authenticator apps users may already have.

### Implementation Details

```
TOTP Configuration (RFC 6238):
- Algorithm: SHA-1 (compatibility)
- Time step: 30 seconds
- Code length: 6 digits
- Time window: Current ± 1 step (90 seconds total)
- Secret length: 160 bits (base32 encoded)

Backup Codes:
- Count: 10 codes
- Length: 8 characters each
- Character set: Alphanumeric uppercase
- Single use: Marked used after redemption

Compatible Apps:
- Google Authenticator
- Microsoft Authenticator
- Authy
- 1Password
- Any RFC 6238 compliant app
```

### Positive Consequences

* **No recurring cost**: Unlike SMS, no per-message fees
* **Works offline**: No network required for code generation
* **User familiarity**: Many users already have authenticator apps
* **Strong security**: Time-based codes resist replay attacks
* **Standards-based**: RFC 6238 ensures interoperability
* **Backup codes**: Recovery option if device lost

### Negative Consequences

* **App requirement**: Users must install authenticator app
* **Device dependency**: Losing device requires backup codes
* **Time sync issues**: Server/device clock drift can cause failures
* **Setup complexity**: QR code scanning may confuse some users
* **No remote revocation**: Cannot invalidate codes remotely

## Pros and Cons of the Options

### TOTP (Chosen)

* Good, because no per-use cost
* Good, because works offline
* Good, because widely supported apps
* Good, because open standard (RFC 6238)
* Good, because phishing-resistant (sort of - codes are short-lived)
* Bad, because requires app installation
* Bad, because device-dependent

### SMS OTP

* Good, because no app installation needed
* Good, because users understand SMS
* Bad, because per-message cost ($0.01-0.05 each)
* Bad, because vulnerable to SIM swapping
* Bad, because delivery delays possible
* Bad, because not phishing-resistant

### Email OTP

* Good, because no additional app needed
* Good, because works across devices
* Bad, because email can be compromised alongside password
* Bad, because email delivery can be slow
* Bad, because not truly a "second factor" if email is primary

### WebAuthn/FIDO2

* Good, because strongest security (phishing-proof)
* Good, because excellent UX with platform authenticators
* Good, because no shared secrets
* Bad, because requires compatible browser/device
* Bad, because hardware keys have cost
* Bad, because complex implementation

### Push Notifications

* Good, because excellent UX (one-tap approve)
* Good, because includes context (location, device)
* Bad, because requires dedicated mobile app
* Bad, because requires network connectivity
* Bad, because infrastructure cost for push service

## Security Considerations

1. **Secret storage**: TOTP secrets encrypted at rest in database
2. **Backup codes**: Hashed, single-use, can be regenerated
3. **Enrollment security**: 2FA setup requires valid session
4. **Disabling 2FA**: Requires current 2FA code
5. **Brute force**: Rate limiting on 2FA verification endpoint

## Future Considerations

- Add WebAuthn as additional/alternative second factor
- Add push-based authentication for mobile app users
- Consider passwordless authentication with FIDO2

## Links

* [RFC 6238 - TOTP](https://tools.ietf.org/html/rfc6238)
* [RFC 4226 - HOTP](https://tools.ietf.org/html/rfc4226) (base algorithm)
* Related: [ADR-001 JWT Authentication](001-jwt-authentication.md) - 2FA extends JWT flow

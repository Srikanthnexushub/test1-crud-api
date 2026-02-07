# ADR-001: JWT for Authentication

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team
**Technical Story**: Implement stateless authentication for API

## Context

We need an authentication mechanism for our REST API that:
- Supports horizontal scaling without shared session state
- Works across multiple clients (web, mobile, CLI)
- Provides secure, tamper-proof authentication tokens
- Allows for role-based access control claims

## Decision Drivers

* Need for stateless authentication (no server-side session storage)
* Horizontal scaling requirements
* Cross-platform client support
* Performance (avoid database lookups on every request)
* Industry standard adoption

## Considered Options

1. **JWT (JSON Web Tokens)**
2. **Session-based authentication with Redis**
3. **OAuth 2.0 with opaque tokens**
4. **API Keys**

## Decision Outcome

Chosen option: **JWT (JSON Web Tokens)**, because it provides stateless authentication that scales horizontally without requiring shared infrastructure, while embedding user claims directly in the token.

### Implementation Details

```
Token Configuration:
- Algorithm: HS256 (HMAC-SHA256)
- Access Token Expiry: 15 minutes
- Refresh Token Expiry: 7 days
- Secret Key: Minimum 256 bits

Access Token Claims:
{
  "sub": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": <issued-at>,
  "exp": <expiry>
}
```

### Positive Consequences

* **Stateless**: No server-side session storage required
* **Scalable**: Any instance can validate tokens without shared state
* **Self-contained**: User info embedded in token reduces database lookups
* **Standard**: Wide library support, well-understood security properties
* **Cross-platform**: Works with any HTTP client

### Negative Consequences

* **Token size**: Larger than session IDs (~500 bytes vs ~32 bytes)
* **Revocation complexity**: Cannot instantly revoke tokens (mitigated by short expiry)
* **Secret management**: Must secure signing key across all instances
* **Clock sync**: Token validation depends on accurate server time

## Pros and Cons of the Options

### JWT (JSON Web Tokens)

* Good, because stateless and horizontally scalable
* Good, because self-contained with embedded claims
* Good, because industry standard with mature libraries
* Good, because works across web, mobile, CLI
* Bad, because cannot be instantly revoked
* Bad, because larger payload than session cookies

### Session-based with Redis

* Good, because instant revocation possible
* Good, because smaller cookie size
* Bad, because requires shared Redis infrastructure
* Bad, because adds operational complexity
* Bad, because single point of failure

### OAuth 2.0 with Opaque Tokens

* Good, because instant revocation via token introspection
* Good, because well-suited for third-party integrations
* Bad, because requires token validation endpoint (database lookup)
* Bad, because more complex to implement
* Bad, because overkill for first-party authentication

### API Keys

* Good, because simple to implement
* Good, because no expiry management
* Bad, because static (if compromised, must be rotated)
* Bad, because not suitable for end-user authentication
* Bad, because typically used for service-to-service auth

## Security Considerations

1. **Short-lived access tokens**: 15-minute expiry limits exposure window
2. **Refresh token rotation**: Refresh tokens stored in database, enabling revocation
3. **HTTPS required**: Tokens only transmitted over encrypted connections
4. **Secure storage**: Clients must store tokens securely (not localStorage for web)

## Links

* [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
* [OWASP JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
* Related: [ADR-003 TOTP 2FA](003-totp-2fa.md) - Adds second factor to JWT-based auth

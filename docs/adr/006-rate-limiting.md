# ADR-006: Token Bucket Rate Limiting

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team, Security Team
**Technical Story**: Protect API from abuse and denial of service

## Context

Our API needs protection against:
- Brute force attacks on authentication endpoints
- API abuse (excessive requests)
- Resource exhaustion (DoS)
- Scraping and automation abuse

## Decision Drivers

* Protection against brute force attacks
* Fair resource allocation across clients
* Developer experience (reasonable limits)
* Implementation simplicity
* Performance overhead
* Operational visibility

## Considered Options

1. **Token Bucket** (Bucket4j)
2. **Fixed Window Counter**
3. **Sliding Window Log**
4. **Leaky Bucket**

## Decision Outcome

Chosen option: **Token Bucket using Bucket4j**, because it provides smooth rate limiting with burst tolerance, has excellent Java integration, and low memory overhead.

### Implementation Details

```java
Configuration:
- Limit: 100 requests per minute per IP
- Bucket capacity: 100 tokens
- Refill rate: 100 tokens per 60 seconds
- Refill type: Greedy (gradual)

IP Detection Priority:
1. X-Forwarded-For header (behind proxy)
2. Remote address (direct connection)

Response on limit exceeded:
- HTTP Status: 429 Too Many Requests
- Body: {"error": "Rate limit exceeded. Try again later."}
- Header: Retry-After: <seconds>
```

### Algorithm Visualization

```
Token Bucket Algorithm:

  ┌─────────────────────────────────────┐
  │         Bucket (capacity: 100)      │
  │  ┌─────────────────────────────┐    │
  │  │ ● ● ● ● ● ● ● ● ● ● ● ● ●  │    │ ◀── Tokens refill
  │  │ ● ● ● ● ● ● ● ● ● ● ● ● ●  │    │     at steady rate
  │  │ ● ● ● ● ● ● ● ● ● ● ● ● ●  │    │     (100/minute)
  │  └─────────────────────────────┘    │
  └─────────────────┬───────────────────┘
                    │
                    ▼
           Request arrives
                    │
           ┌───────┴───────┐
           ▼               ▼
      Has token?      No token
           │               │
           ▼               ▼
      Allow request   429 Too Many
      (remove token)    Requests
```

### Positive Consequences

* **Burst tolerance**: Can handle short traffic spikes
* **Smooth limiting**: Gradual token refill prevents cliff edges
* **Low overhead**: O(1) time and space per bucket
* **Predictable**: Easy to understand and communicate limits
* **Flexible**: Can adjust limits per endpoint if needed

### Negative Consequences

* **Per-instance state**: Each app instance has separate counters
* **IP-based limitations**: Shared IPs (NAT) may hit limits unfairly
* **No distributed coordination**: Inconsistent limits in horizontal scaling
* **Memory usage**: One bucket per unique IP

## Pros and Cons of the Options

### Token Bucket (Chosen)

* Good, because allows controlled bursts
* Good, because smooth rate limiting
* Good, because low computational overhead
* Good, because Bucket4j has excellent Spring integration
* Bad, because per-instance state (not distributed)
* Bad, because memory grows with unique IPs

### Fixed Window Counter

* Good, because simple implementation
* Good, because low memory usage
* Bad, because boundary burst problem (2x limit at window edge)
* Bad, because abrupt limit resets

### Sliding Window Log

* Good, because accurate rate limiting
* Good, because no boundary issues
* Bad, because high memory usage (stores all timestamps)
* Bad, because more complex to implement

### Leaky Bucket

* Good, because smooth, consistent output rate
* Good, because prevents any bursting
* Bad, because may be too strict for interactive APIs
* Bad, because queuing adds complexity

## Implementation Code

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1))))
            .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        String ip = getClientIP(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
        }
    }
}
```

## Future Improvements

1. **Distributed rate limiting**: Use Redis for shared state across instances
2. **Endpoint-specific limits**: Stricter limits on auth endpoints
3. **Authenticated user limits**: Higher limits for logged-in users
4. **Dynamic limits**: Adjust based on server load
5. **API key limits**: Different tiers for different clients

## Links

* [Bucket4j Documentation](https://bucket4j.com/)
* [Rate Limiting Algorithms Explained](https://blog.cloudflare.com/counting-things-a-lot-of-different-things/)
* Related: [ADR-001 JWT Authentication](001-jwt-authentication.md) - Rate limiting protects auth endpoints

# ADR-007: Comprehensive Audit Logging

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team, Security Team
**Technical Story**: Implement security audit trail for compliance

## Context

We need audit logging for:
- Security incident investigation
- Compliance requirements (SOC 2, GDPR)
- User activity tracking
- Debugging authentication issues
- Anomaly detection foundation

## Decision Drivers

* Security incident response capability
* Compliance requirements
* Debugging and troubleshooting
* Performance impact considerations
* Storage and retention costs
* Query capabilities

## Considered Options

1. **Database-backed audit log** (PostgreSQL table)
2. **File-based logging** (structured log files)
3. **External audit service** (AWS CloudTrail, Datadog)
4. **Event sourcing** (full event store)

## Decision Outcome

Chosen option: **Database-backed audit log**, because it provides queryable storage, transactional consistency with user operations, and simple implementation without external dependencies.

### Implementation Details

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL,  -- SUCCESS, FAILURE
    error_message TEXT,
    resource_id VARCHAR(255),
    resource_type VARCHAR(100)
);

CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
```

### Logged Events

| Action | Trigger | Details Captured |
|--------|---------|------------------|
| USER_REGISTER | Registration | Email, IP, user agent |
| USER_LOGIN | Login attempt | Email, success/failure, IP |
| USER_LOGOUT | Token invalidation | Email, IP |
| PASSWORD_CHANGE | Password updated | User ID |
| PASSWORD_RESET_REQUEST | Reset requested | Email, IP |
| PASSWORD_RESET_COMPLETE | Reset completed | User ID |
| EMAIL_VERIFIED | Email verification | User ID, token |
| 2FA_ENABLED | 2FA setup complete | User ID |
| 2FA_DISABLED | 2FA removed | User ID |
| ACCOUNT_LOCKED | Failed attempts exceeded | Email, IP |
| ACCOUNT_UNLOCKED | Lock expired/admin | User ID |

### Log Entry Structure

```json
{
  "id": 12345,
  "username": "user@example.com",
  "action": "USER_LOGIN",
  "details": "Login successful",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2026-02-07T10:30:00Z",
  "status": "SUCCESS",
  "errorMessage": null,
  "resourceId": "123",
  "resourceType": "USER"
}
```

### Positive Consequences

* **Queryable**: SQL queries for investigation
* **Transactional**: Consistent with user operations
* **Simple**: No external dependencies
* **Indexed**: Fast lookups by user, action, time
* **Compliance ready**: Supports audit requirements

### Negative Consequences

* **Storage growth**: Audit logs grow indefinitely
* **Write overhead**: Additional INSERT per operation
* **Same database**: Shares resources with application data
* **Retention management**: Need cleanup jobs

## Pros and Cons of the Options

### Database Audit Log (Chosen)

* Good, because queryable with SQL
* Good, because transactionally consistent
* Good, because no external dependencies
* Good, because simple to implement
* Bad, because storage grows with activity
* Bad, because shares database resources

### File-based Logging

* Good, because simple append-only writes
* Good, because easy to ship to log aggregators
* Good, because low database impact
* Bad, because harder to query
* Bad, because requires log aggregation for search
* Bad, because not transactional with DB operations

### External Audit Service

* Good, because managed retention and compliance
* Good, because built-in alerting and analysis
* Good, because offloads storage
* Bad, because external dependency
* Bad, because additional cost
* Bad, because vendor lock-in

### Event Sourcing

* Good, because complete history
* Good, because replay capability
* Good, because natural audit trail
* Bad, because complex to implement
* Bad, because storage intensive
* Bad, because overkill for audit-only needs

## Retention Policy

```
Retention: 90 days

Cleanup Job (scheduled daily):
DELETE FROM audit_logs
WHERE timestamp < NOW() - INTERVAL '90 days';

Archive (before deletion):
pg_dump -t audit_logs --where="timestamp < '...'"> archive.sql
```

## Security Considerations

1. **Sensitive data**: Never log passwords or tokens
2. **Access control**: Audit logs readable only by admins
3. **Immutability**: No UPDATE/DELETE in application code
4. **Integrity**: Consider hash chain for tamper detection
5. **Encryption**: Consider column encryption for PII

## Query Examples

```sql
-- Failed logins in last hour
SELECT username, ip_address, COUNT(*), MAX(timestamp)
FROM audit_logs
WHERE action = 'USER_LOGIN' AND status = 'FAILURE'
AND timestamp > NOW() - INTERVAL '1 hour'
GROUP BY username, ip_address
ORDER BY COUNT(*) DESC;

-- Account activity for specific user
SELECT action, status, timestamp, ip_address
FROM audit_logs
WHERE username = 'user@example.com'
ORDER BY timestamp DESC
LIMIT 50;

-- Security events summary
SELECT action, status, COUNT(*)
FROM audit_logs
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY action, status
ORDER BY COUNT(*) DESC;
```

## Future Improvements

1. **Partitioning**: Partition by month for faster queries
2. **Alerting**: Real-time alerts for suspicious patterns
3. **Analytics**: Dashboard for security metrics
4. **Archival**: Move old logs to cold storage
5. **SIEM integration**: Export to security tools

## Links

* [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)
* [SOC 2 Audit Requirements](https://www.aicpa.org/soc)
* Related: [ADR-001 JWT Authentication](001-jwt-authentication.md) - Auth events logged

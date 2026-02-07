# ADR-005: PostgreSQL as Primary Database

**Status**: Accepted
**Date**: 2026-02-07
**Deciders**: Engineering Team
**Technical Story**: Select primary database for user data storage

## Context

We need a database that:
- Stores user accounts, credentials, and related data
- Provides ACID compliance for transaction integrity
- Scales with user growth
- Supports complex queries for future reporting
- Has strong ecosystem and tooling

## Decision Drivers

* ACID compliance for transactional data
* Scalability (vertical and horizontal read replicas)
* Operational familiarity
* Ecosystem maturity
* Cost effectiveness
* Cloud provider support

## Considered Options

1. **PostgreSQL**
2. **MySQL/MariaDB**
3. **MongoDB**
4. **Amazon DynamoDB**

## Decision Outcome

Chosen option: **PostgreSQL**, because it provides excellent ACID compliance, rich feature set, strong ecosystem, and aligns with team expertise.

### Implementation Details

```
Configuration:
- Version: PostgreSQL 15
- Image: postgres:15-alpine (Docker)
- Connection Pool: HikariCP
  - Max Pool Size: 10
  - Min Idle: 5
  - Connection Timeout: 30s
  - Idle Timeout: 10m
  - Max Lifetime: 30m

Dual Environment Setup:
- Local Development: Port 5433 (Crud_db)
- Docker/Production: Port 5434 (cruddb)

JPA Configuration:
- Hibernate DDL: update (auto-create tables)
- Open-in-view: disabled
- Dialect: PostgreSQL 15
```

### Schema Overview

```sql
Tables:
├── users              # User accounts
├── roles              # Role definitions
├── user_roles         # User-role mappings (M:N)
├── refresh_tokens     # JWT refresh tokens
├── verification_tokens # Email/password tokens
├── two_factor_backup_codes # 2FA backup codes
└── audit_logs         # Security event log

Key Indexes:
├── users(email) UNIQUE
├── verification_tokens(token)
├── audit_logs(username, timestamp)
└── audit_logs(action, timestamp)
```

### Positive Consequences

* **ACID compliance**: Strong transactional guarantees
* **Rich SQL**: Complex queries, CTEs, window functions
* **Extensible**: JSON support, full-text search, extensions
* **Mature ecosystem**: Excellent tooling, monitoring, backup
* **Cloud support**: Available on AWS RDS, GCP Cloud SQL, Azure
* **Team expertise**: Team familiar with PostgreSQL

### Negative Consequences

* **Operational overhead**: Requires database management
* **Vertical scaling limits**: Eventually need read replicas
* **Schema migrations**: DDL changes require coordination
* **Cost at scale**: Can be expensive for very large datasets

## Pros and Cons of the Options

### PostgreSQL (Chosen)

* Good, because ACID compliant with strong consistency
* Good, because rich feature set (JSON, FTS, extensions)
* Good, because excellent tooling and ecosystem
* Good, because team has expertise
* Good, because cloud-agnostic with managed options
* Bad, because requires operational management
* Bad, because schema changes need migrations

### MySQL/MariaDB

* Good, because widely adopted
* Good, because simple replication setup
* Good, because lower resource usage
* Bad, because fewer advanced features than PostgreSQL
* Bad, because historical consistency issues (improved in MySQL 8)
* Bad, because less robust JSON support

### MongoDB

* Good, because flexible schema
* Good, because horizontal scaling built-in
* Good, because good for document-oriented data
* Bad, because not ACID compliant (until v4.0 transactions)
* Bad, because not suited for relational data (user-roles)
* Bad, because different query paradigm

### Amazon DynamoDB

* Good, because fully managed, infinite scale
* Good, because low latency at any scale
* Good, because pay-per-request option
* Bad, because vendor lock-in
* Bad, because limited query patterns
* Bad, because complex for relational data
* Bad, because expensive for scan-heavy workloads

## Operational Considerations

### Backup Strategy

```bash
# Daily backup
pg_dump -U postgres cruddb > backup_$(date +%Y%m%d).sql

# Point-in-time recovery (production)
wal_level = replica
archive_mode = on
```

### Monitoring

- Connection pool metrics via HikariCP
- Query performance via pg_stat_statements
- Table sizes and bloat monitoring
- Replication lag (if read replicas)

### Scaling Path

1. **Vertical**: Increase CPU/RAM (up to limits)
2. **Read replicas**: Offload read queries
3. **Connection pooling**: PgBouncer for high connection counts
4. **Partitioning**: Partition large tables (audit_logs)

## Links

* [PostgreSQL Documentation](https://www.postgresql.org/docs/)
* [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
* [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

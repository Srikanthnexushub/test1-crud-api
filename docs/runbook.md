# Operations Runbook

**Version**: 1.0
**Last Updated**: 2026-02-07
**On-Call Rotation**: ops-team@example.com

---

## Table of Contents

1. [Quick Reference](#1-quick-reference)
2. [System Overview](#2-system-overview)
3. [Startup & Shutdown](#3-startup--shutdown)
4. [Health Checks](#4-health-checks)
5. [Common Operations](#5-common-operations)
6. [Troubleshooting](#6-troubleshooting)
7. [Incident Response](#7-incident-response)
8. [Backup & Recovery](#8-backup--recovery)
9. [Scaling](#9-scaling)
10. [Monitoring & Alerts](#10-monitoring--alerts)

---

## 1. Quick Reference

### 1.1 Service URLs

| Environment | URL | Health Check |
|-------------|-----|--------------|
| Local | http://localhost:8080 | http://localhost:8080/actuator/health |
| Docker | http://localhost:8080 | http://localhost:8080/actuator/health |
| Production | https://api.example.com | https://api.example.com/actuator/health |

### 1.2 Quick Commands

```bash
# Start all services
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

# Restart application
docker-compose restart app

# Stop all services
docker-compose down

# Full reset (including data)
docker-compose down -v && docker-compose up -d --build
```

### 1.3 Key Ports

| Service | Port | Purpose |
|---------|------|---------|
| Application | 8080 | REST API |
| PostgreSQL (Docker) | 5434 | Database (external) |
| PostgreSQL (Local) | 5433 | Database (development) |
| Actuator | 8080 | Metrics & health |

### 1.4 Emergency Contacts

| Role | Contact | Escalation Time |
|------|---------|-----------------|
| On-Call Engineer | ops@example.com | Immediate |
| Tech Lead | techlead@example.com | 15 min |
| Security | security@example.com | For security incidents |

---

## 2. System Overview

### 2.1 Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        PRODUCTION ARCHITECTURE                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐     ┌──────────────────┐     ┌──────────────────┐        │
│  │  Client  │────▶│  Load Balancer   │────▶│  Spring Boot     │        │
│  │          │     │  (nginx/ALB)     │     │  Application     │        │
│  └──────────┘     └──────────────────┘     └────────┬─────────┘        │
│                                                      │                  │
│                                                      ▼                  │
│                                            ┌──────────────────┐        │
│                                            │   PostgreSQL     │        │
│                                            │   Database       │        │
│                                            └──────────────────┘        │
│                                                      │                  │
│                                                      ▼                  │
│                                            ┌──────────────────┐        │
│                                            │   SMTP Server    │        │
│                                            │   (Email)        │        │
│                                            └──────────────────┘        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Container Inventory

| Container | Image | Restart Policy |
|-----------|-------|----------------|
| crud_spring_app | custom (Dockerfile) | unless-stopped |
| crud_postgres_db | postgres:15-alpine | unless-stopped |

### 2.3 Resource Requirements

| Service | CPU | Memory | Storage |
|---------|-----|--------|---------|
| Application | 0.5-2 cores | 512MB-2GB | 100MB (logs) |
| PostgreSQL | 0.5-1 core | 256MB-1GB | 10GB+ (data) |

---

## 3. Startup & Shutdown

### 3.1 Normal Startup

```bash
# Navigate to project directory
cd /Users/user/IdeaProjects/test1-crud-api

# Start all services (detached)
docker-compose up -d --build

# Verify services are running
docker-compose ps

# Check application logs
docker-compose logs -f app

# Wait for health check
curl http://localhost:8080/actuator/health
```

**Expected startup time**: 30-60 seconds

### 3.2 Startup Verification

```bash
# 1. Check container status
docker-compose ps
# Expected: Both containers "Up" and healthy

# 2. Check database connectivity
docker exec crud_postgres_db pg_isready -U postgres
# Expected: "accepting connections"

# 3. Check application health
curl -s http://localhost:8080/actuator/health | jq
# Expected: {"status":"UP"}

# 4. Test API endpoint
curl -s http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}' | jq
# Expected: Success or duplicate error (if already exists)
```

### 3.3 Graceful Shutdown

```bash
# Stop application first (allows connection draining)
docker-compose stop app

# Wait 10 seconds for connections to drain
sleep 10

# Stop database
docker-compose stop postgres

# Or stop all at once (less graceful)
docker-compose down
```

### 3.4 Emergency Shutdown

```bash
# Force stop all containers immediately
docker-compose kill

# Remove containers
docker-compose down
```

---

## 4. Health Checks

### 4.1 Health Endpoints

| Endpoint | Purpose | Expected Response |
|----------|---------|-------------------|
| `/actuator/health` | Overall health | `{"status":"UP"}` |
| `/actuator/health/liveness` | Kubernetes liveness | `{"status":"UP"}` |
| `/actuator/health/readiness` | Kubernetes readiness | `{"status":"UP"}` |
| `/actuator/info` | Application info | Build details |

### 4.2 Health Check Script

```bash
#!/bin/bash
# health-check.sh

APP_URL=${1:-http://localhost:8080}

echo "Checking application health..."

# Check health endpoint
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $APP_URL/actuator/health)

if [ "$HEALTH" == "200" ]; then
    echo "✅ Application is healthy"
    exit 0
else
    echo "❌ Application is unhealthy (HTTP $HEALTH)"
    exit 1
fi
```

### 4.3 Database Health Check

```bash
# Check PostgreSQL is accepting connections
docker exec crud_postgres_db pg_isready -U postgres -d cruddb

# Check connection count
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname = 'cruddb';"

# Check table sizes
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT relname, pg_size_pretty(pg_total_relation_size(relid))
   FROM pg_catalog.pg_statio_user_tables
   ORDER BY pg_total_relation_size(relid) DESC;"
```

---

## 5. Common Operations

### 5.1 View Logs

```bash
# Application logs (live)
docker-compose logs -f app

# Application logs (last 100 lines)
docker-compose logs --tail=100 app

# Database logs
docker-compose logs -f postgres

# All logs
docker-compose logs -f

# Search logs for errors
docker-compose logs app 2>&1 | grep -i error

# Logs with timestamp
docker-compose logs -t app
```

### 5.2 Database Operations

```bash
# Connect to database
docker exec -it crud_postgres_db psql -U postgres -d cruddb

# Run SQL query
docker exec crud_postgres_db psql -U postgres -d cruddb -c "SELECT COUNT(*) FROM users;"

# Export database
docker exec crud_postgres_db pg_dump -U postgres cruddb > backup.sql

# Import database
cat backup.sql | docker exec -i crud_postgres_db psql -U postgres -d cruddb
```

### 5.3 User Management

```sql
-- List all users
SELECT id, email, email_verified, two_factor_enabled, account_locked, created_at
FROM users ORDER BY created_at DESC LIMIT 20;

-- Unlock a user account
UPDATE users SET account_locked = false, failed_login_attempts = 0
WHERE email = 'user@example.com';

-- Force email verification
UPDATE users SET email_verified = true, email_verified_at = NOW()
WHERE email = 'user@example.com';

-- Disable 2FA for user
UPDATE users SET two_factor_enabled = false, two_factor_secret = NULL
WHERE email = 'user@example.com';

-- Delete all sessions for user
DELETE FROM refresh_tokens WHERE user_id = (SELECT id FROM users WHERE email = 'user@example.com');
```

### 5.4 Token Management

```sql
-- View active refresh tokens
SELECT u.email, rt.token, rt.expiry_date
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.id
WHERE rt.expiry_date > NOW();

-- Invalidate all tokens (force re-login)
DELETE FROM refresh_tokens;

-- Clean expired tokens
DELETE FROM refresh_tokens WHERE expiry_date < NOW();

-- Clean expired verification tokens
DELETE FROM verification_tokens WHERE expiry_date < NOW();
```

### 5.5 Container Operations

```bash
# Restart application only
docker-compose restart app

# Rebuild and restart application
docker-compose up -d --build app

# Shell into application container
docker exec -it crud_spring_app /bin/sh

# View container resource usage
docker stats crud_spring_app crud_postgres_db

# View container processes
docker top crud_spring_app
```

---

## 6. Troubleshooting

### 6.1 Application Won't Start

**Symptoms**: Container exits immediately or health check fails

**Diagnostic Steps**:
```bash
# Check container status
docker-compose ps

# Check exit code
docker inspect crud_spring_app --format='{{.State.ExitCode}}'

# Check logs for errors
docker-compose logs app | tail -50

# Common issues:
# - Database not ready: Wait for postgres health check
# - Port already in use: Check with `lsof -i :8080`
# - Missing environment variables: Check docker-compose.yml
```

**Resolution**:
```bash
# If database not ready, restart with dependencies
docker-compose down
docker-compose up -d postgres
sleep 10
docker-compose up -d app

# If port conflict
lsof -i :8080
kill -9 <PID>
docker-compose up -d
```

### 6.2 Database Connection Issues

**Symptoms**: "Connection refused" or timeout errors

**Diagnostic Steps**:
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Test connection from app container
docker exec crud_spring_app nc -zv postgres 5432

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

**Resolution**:
```bash
# Restart database
docker-compose restart postgres

# If data corruption, restore from backup
docker-compose down -v
docker-compose up -d postgres
cat backup.sql | docker exec -i crud_postgres_db psql -U postgres -d cruddb
docker-compose up -d app
```

### 6.3 High Memory Usage

**Symptoms**: OOM errors, slow response times

**Diagnostic Steps**:
```bash
# Check container memory
docker stats --no-stream

# Check JVM memory (if endpoint exposed)
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check for memory leaks
docker exec crud_spring_app jcmd 1 GC.heap_info
```

**Resolution**:
```bash
# Increase memory limits in docker-compose.yml
# deploy:
#   resources:
#     limits:
#       memory: 2G

# Restart with new limits
docker-compose down
docker-compose up -d
```

### 6.4 Slow API Responses

**Symptoms**: Response times > 500ms

**Diagnostic Steps**:
```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check database slow queries
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT query, calls, mean_time
   FROM pg_stat_statements
   ORDER BY mean_time DESC LIMIT 10;"

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

**Resolution**:
```sql
-- Add missing indexes
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Analyze tables
ANALYZE users;
ANALYZE audit_logs;
```

### 6.5 Authentication Issues

**Symptoms**: 401/403 errors, login failures

**Diagnostic Steps**:
```bash
# Check audit logs
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT * FROM audit_logs WHERE action = 'USER_LOGIN' ORDER BY timestamp DESC LIMIT 10;"

# Check user status
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT email, email_verified, account_locked, failed_login_attempts FROM users WHERE email = 'user@example.com';"
```

**Resolution**:
```sql
-- Unlock user
UPDATE users SET account_locked = false, failed_login_attempts = 0 WHERE email = 'user@example.com';

-- Reset password (generate new BCrypt hash)
UPDATE users SET password = '$2a$10$...' WHERE email = 'user@example.com';
```

### 6.6 Email Not Sending

**Symptoms**: Verification/reset emails not received

**Diagnostic Steps**:
```bash
# Check application logs for email errors
docker-compose logs app | grep -i mail

# Verify SMTP configuration
docker exec crud_spring_app env | grep MAIL

# Check async task executor
curl http://localhost:8080/actuator/metrics/executor.pool.size
```

**Resolution**:
```bash
# Verify SMTP credentials
# Update docker-compose.yml with correct values:
# MAIL_HOST: smtp.gmail.com
# MAIL_PORT: 587
# MAIL_USERNAME: your-email@gmail.com
# MAIL_PASSWORD: your-app-password

docker-compose up -d app
```

---

## 7. Incident Response

### 7.1 Incident Severity Levels

| Level | Description | Response Time | Examples |
|-------|-------------|---------------|----------|
| **P1 - Critical** | Service down | < 15 min | Complete outage, data breach |
| **P2 - High** | Major degradation | < 1 hour | Auth failures, DB issues |
| **P3 - Medium** | Minor degradation | < 4 hours | Slow responses, partial feature failure |
| **P4 - Low** | Cosmetic/minor | < 24 hours | UI issues, minor bugs |

### 7.2 Incident Playbook

#### P1 - Service Down

```bash
# 1. Acknowledge incident
echo "Incident acknowledged at $(date)" >> /tmp/incident.log

# 2. Check all services
docker-compose ps
curl -s http://localhost:8080/actuator/health

# 3. Check logs for errors
docker-compose logs --tail=200 app | grep -i "error\|exception"

# 4. Attempt restart
docker-compose restart app

# 5. If restart fails, full recreate
docker-compose down
docker-compose up -d --build

# 6. If still failing, check database
docker-compose logs postgres
docker exec crud_postgres_db pg_isready

# 7. If database corrupted, restore from backup
# (See Backup & Recovery section)
```

#### P2 - Authentication Failures

```bash
# 1. Check rate limiting
curl http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/v1/users/login

# 2. Check for brute force attacks
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT ip_address, COUNT(*) FROM audit_logs
   WHERE action = 'USER_LOGIN' AND status = 'FAILURE'
   AND timestamp > NOW() - INTERVAL '1 hour'
   GROUP BY ip_address ORDER BY COUNT(*) DESC LIMIT 10;"

# 3. Check locked accounts
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT email, lock_time FROM users WHERE account_locked = true;"

# 4. If attack detected, consider IP blocking at firewall level
```

### 7.3 Post-Incident Review

After any P1/P2 incident:
1. Document timeline in incident report
2. Identify root cause
3. Create action items to prevent recurrence
4. Update runbook with new knowledge
5. Share learnings with team

---

## 8. Backup & Recovery

### 8.1 Backup Procedures

#### Daily Database Backup

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/cruddb_$DATE.sql.gz"

# Create backup
docker exec crud_postgres_db pg_dump -U postgres cruddb | gzip > $BACKUP_FILE

# Verify backup
if [ -s "$BACKUP_FILE" ]; then
    echo "Backup successful: $BACKUP_FILE"

    # Keep only last 7 days of backups
    find $BACKUP_DIR -name "cruddb_*.sql.gz" -mtime +7 -delete
else
    echo "Backup failed!"
    exit 1
fi
```

### 8.2 Recovery Procedures

#### Full Database Recovery

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: ./restore.sh <backup_file>"
    exit 1
fi

# Stop application
docker-compose stop app

# Drop and recreate database
docker exec crud_postgres_db psql -U postgres -c "DROP DATABASE IF EXISTS cruddb;"
docker exec crud_postgres_db psql -U postgres -c "CREATE DATABASE cruddb;"

# Restore backup
gunzip -c $BACKUP_FILE | docker exec -i crud_postgres_db psql -U postgres -d cruddb

# Restart application
docker-compose start app

# Verify
curl http://localhost:8080/actuator/health
```

### 8.3 Point-in-Time Recovery

For production, enable WAL archiving:

```bash
# In postgresql.conf
wal_level = replica
archive_mode = on
archive_command = 'cp %p /backups/wal/%f'
```

---

## 9. Scaling

### 9.1 Vertical Scaling

```yaml
# docker-compose.yml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### 9.2 Horizontal Scaling

```bash
# Scale to 3 instances (requires load balancer)
docker-compose up -d --scale app=3

# With nginx load balancer
# nginx.conf:
# upstream api {
#     server app1:8080;
#     server app2:8080;
#     server app3:8080;
# }
```

### 9.3 Database Scaling

For read-heavy workloads:
1. Set up PostgreSQL streaming replication
2. Direct read queries to replica
3. Keep writes on primary

---

## 10. Monitoring & Alerts

### 10.1 Key Metrics

| Metric | Warning | Critical | Action |
|--------|---------|----------|--------|
| CPU Usage | > 70% | > 90% | Scale up |
| Memory Usage | > 70% | > 90% | Scale up / investigate leak |
| Response Time (p95) | > 500ms | > 2s | Investigate slow queries |
| Error Rate | > 1% | > 5% | Investigate errors |
| DB Connections | > 80% pool | > 95% pool | Increase pool size |
| Disk Usage | > 70% | > 90% | Clean up / expand |

### 10.2 Prometheus Metrics

```bash
# Access Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Key metrics to monitor:
# - http_server_requests_seconds_count
# - http_server_requests_seconds_sum
# - jvm_memory_used_bytes
# - hikaricp_connections_active
# - process_cpu_usage
```

### 10.3 Log Monitoring

```bash
# Monitor for errors in real-time
docker-compose logs -f app 2>&1 | grep -E "ERROR|WARN|Exception"

# Count errors per minute
docker-compose logs app 2>&1 | grep ERROR | \
  awk '{print $1}' | uniq -c | tail -10
```

### 10.4 Alert Configuration (Example for Prometheus Alertmanager)

```yaml
groups:
  - name: crud-api
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: High error rate detected

      - alert: SlowResponses
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: API response time degraded
```

---

## Appendix: Useful Commands

```bash
# Check all container resource usage
docker stats --no-stream

# View network connections
docker exec crud_spring_app netstat -an | grep ESTABLISHED

# Check disk usage
docker system df

# Clean up unused resources
docker system prune -f

# Export container logs to file
docker-compose logs > logs_$(date +%Y%m%d).txt

# Check environment variables
docker exec crud_spring_app env | sort

# Generate thread dump
docker exec crud_spring_app jstack 1 > thread_dump.txt

# Generate heap dump
docker exec crud_spring_app jmap -dump:format=b,file=/tmp/heap.hprof 1
docker cp crud_spring_app:/tmp/heap.hprof ./heap.hprof
```

---

*This runbook should be reviewed and updated after any significant operational changes or incidents.*

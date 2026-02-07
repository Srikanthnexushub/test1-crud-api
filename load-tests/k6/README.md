# k6 Load Tests for CRUD API

## Quick Start

### Prerequisites

1. Install k6:
   ```bash
   # macOS
   brew install k6

   # Linux
   sudo apt-get install k6

   # Docker
   docker pull grafana/k6
   ```

2. Start the application:
   ```bash
   cd ../..  # Go to project root
   docker-compose up -d --build
   ```

3. Create test user:
   ```bash
   # Register test user
   curl -X POST http://localhost:8080/api/v1/users/register \
     -H "Content-Type: application/json" \
     -d '{"email":"loadtest@example.com","password":"LoadTest123!"}'

   # Verify email in database
   docker exec crud_postgres_db psql -U postgres -d cruddb -c \
     "UPDATE users SET email_verified = true WHERE email = 'loadtest@example.com';"
   ```

### Running Tests

```bash
# Make run script executable
chmod +x run.sh

# Run smoke test (quick validation)
./run.sh smoke local

# Run load test (normal load)
./run.sh load local

# Run stress test (find breaking point)
./run.sh stress local

# Run spike test (sudden traffic surge)
./run.sh spike local

# Run soak test (long-duration stability)
./run.sh soak local
```

### Direct k6 Commands

```bash
# Run with default settings
k6 run scenarios/smoke.js

# Run with custom VUs and duration
k6 run --vus 50 --duration 5m scenarios/load.js

# Run against different environment
k6 run --env TARGET_ENV=staging scenarios/load.js

# Run with output to file
k6 run --out json=results.json scenarios/load.js
```

## Test Scenarios

| Scenario | Duration | Max VUs | Purpose |
|----------|----------|---------|---------|
| `smoke.js` | 1 min | 1 | Quick validation |
| `load.js` | 16 min | 100 | Normal load testing |
| `stress.js` | 23 min | 500 | Find breaking point |
| `spike.js` | 7 min | 500 | Sudden traffic surge |
| `soak.js` | 2+ hours | 50 | Long-term stability |

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `TARGET_ENV` | `local` | Target environment (local, docker, staging, production) |
| `TEST_EMAIL` | `loadtest@example.com` | Test user email |
| `TEST_PASSWORD` | `LoadTest123!` | Test user password |

### Thresholds (SLOs)

Default thresholds in `config/thresholds.js`:
- p50 response time < 100ms
- p95 response time < 200ms
- p99 response time < 500ms
- Error rate < 1%

## Results

Results are saved to the `results/` directory:
- `{scenario}_{env}_{timestamp}.json` - Full test data
- `{scenario}_{env}_{timestamp}_summary.json` - Summary statistics

## Troubleshooting

### Test user cannot login
1. Ensure application is running: `curl http://localhost:8080/actuator/health`
2. Verify test user exists and email is verified
3. Check rate limiting isn't blocking requests

### High error rate
1. Check application logs: `docker-compose logs -f app`
2. Monitor database connections
3. Verify database is healthy

### Connection refused
1. Check if application is running
2. Verify correct port (8080)
3. Check Docker network if using containers

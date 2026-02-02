# 🚀 Docker Quick Start

## Files Created

1. **Dockerfile** - Instructions to build your Spring Boot app image
2. **docker-compose.yml** - Orchestrates Spring Boot + PostgreSQL
3. **.dockerignore** - Excludes unnecessary files from Docker build
4. **DOCKER_GUIDE.md** - Complete detailed guide (read this for deep understanding)

---

## Quick Commands

### Start Everything (First Time)
```bash
# Build images and start containers
docker-compose up -d --build

# Wait 30 seconds for startup, then check:
docker-compose logs -f app
```

### Start Everything (Subsequent Times)
```bash
docker-compose up -d
```

### Stop Everything (Keep Data)
```bash
docker-compose down
```

### Stop Everything (Delete Data)
```bash
docker-compose down -v
```

### View Logs
```bash
# All logs
docker-compose logs

# Only Spring Boot
docker-compose logs app

# Follow in real-time
docker-compose logs -f app
```

### Check Status
```bash
docker-compose ps
```

### Rebuild After Code Changes
```bash
docker-compose up -d --build
```

### Access Database
```bash
docker exec -it crud_postgres_db psql -U postgres -d cruddb
```

---

## Testing Your Dockerized App

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"docker@example.com","password":"dockerpass123"}'

# 2. Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"docker@example.com","password":"dockerpass123"}'

# 3. Check database
docker exec -it crud_postgres_db psql -U postgres -d cruddb -c "SELECT * FROM users;"
```

---

## Architecture

```
┌─────────────────────────────────────┐
│   Your Machine (localhost)          │
│                                      │
│  ┌────────────┐      ┌────────────┐ │
│  │ Port 8080  │      │ Port 5432  │ │
│  └──────┬─────┘      └──────┬─────┘ │
│         │                   │        │
└─────────┼───────────────────┼────────┘
          │                   │
    ┌─────▼─────────────────▼──────┐
    │    crud_network (Bridge)     │
    │                               │
    │  ┌──────────────────────┐   │
    │  │  crud_spring_app     │   │
    │  │  (Spring Boot)       │   │
    │  │  Port: 8080          │   │
    │  └──────────┬───────────┘   │
    │             │                │
    │             │ Connects to    │
    │             ▼                │
    │  ┌──────────────────────┐   │
    │  │  crud_postgres_db    │   │
    │  │  (PostgreSQL 15)     │   │
    │  │  Port: 5432          │   │
    │  │  Data: postgres_data │   │
    │  └──────────────────────┘   │
    └───────────────────────────────┘
```

---

## What Each File Does

### Dockerfile
- **Stage 1**: Compiles your code with Maven
- **Stage 2**: Creates minimal runtime image with just Java + JAR
- **Result**: ~300MB image instead of ~700MB

### docker-compose.yml
- **postgres service**: PostgreSQL database container
- **app service**: Your Spring Boot application
- **crud_network**: Connects both containers
- **postgres_data**: Persists database data

### .dockerignore
- Excludes unnecessary files (target/, .idea/, etc.)
- Makes builds faster and images smaller

---

## Common Issues

### Port Already in Use
```bash
# Stop local PostgreSQL
brew services stop postgresql  # Mac
sudo systemctl stop postgresql # Linux

# Or use different ports in docker-compose.yml
```

### Container Won't Start
```bash
# Check logs
docker-compose logs app

# Rebuild
docker-compose down
docker-compose up --build -d
```

### Database Connection Failed
```bash
# Wait for postgres to be healthy
docker-compose ps

# Check postgres logs
docker-compose logs postgres
```

---

## File Structure
```
test1/
├── Dockerfile              # Build instructions
├── docker-compose.yml      # Multi-container setup
├── .dockerignore          # Ignore patterns
├── DOCKER_GUIDE.md        # Detailed guide
├── DOCKER_QUICKSTART.md   # This file
├── pom.xml
└── src/
```

---

## Benefits of This Setup

✅ **One-Command Setup**: `docker-compose up -d`
✅ **Consistent Environment**: Works same everywhere
✅ **No Manual Installation**: Java, Maven, PostgreSQL included
✅ **Data Persistence**: Database survives restarts
✅ **Easy Cleanup**: `docker-compose down`
✅ **Production-Ready**: Same setup for dev/prod

---

## Next: Manual Walkthrough

Open **DOCKER_GUIDE.md** for:
- Line-by-line explanation of every file
- Manual step-by-step process
- Detailed troubleshooting
- Docker concepts explained

---

**Ready to try?** Run: `docker-compose up -d --build`

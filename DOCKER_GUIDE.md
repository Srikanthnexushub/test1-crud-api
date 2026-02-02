# 🐳 Complete Docker Guide - Manual Walkthrough

## Table of Contents
1. [What is Docker?](#what-is-docker)
2. [Key Docker Concepts](#key-docker-concepts)
3. [Understanding the Files](#understanding-the-files)
4. [Step-by-Step Manual Process](#step-by-step-manual-process)
5. [Docker Commands Explained](#docker-commands-explained)
6. [Troubleshooting](#troubleshooting)

---

## What is Docker?

### The Problem Without Docker

**Scenario**: You want to share your CRUD app with a teammate.

**Steps they need:**
1. Install Java 17 (exact version matters)
2. Install Maven
3. Install PostgreSQL 15
4. Create database named "cruddb"
5. Set username/password
6. Clone your code
7. Update application.properties with their database settings
8. Run `mvn spring-boot:run`

**Problems:**
- ❌ Takes 1-2 hours to set up
- ❌ "Works on my machine" issues
- ❌ Version mismatches cause bugs
- ❌ Different OS = different setup steps

### The Solution With Docker

**Steps with Docker:**
1. Install Docker
2. Clone your code
3. Run `docker-compose up`

**Benefits:**
- ✅ Works in 5 minutes
- ✅ Identical environment everywhere
- ✅ No version conflicts
- ✅ Same commands on Mac, Windows, Linux

---

## Key Docker Concepts

### 1. **Docker Image**
- **What**: A blueprint/template for your application
- **Analogy**: Like a class in Java
- **Contains**: Your code + Java + dependencies + OS
- **Example**: `postgres:15-alpine` (PostgreSQL image from Docker Hub)

### 2. **Docker Container**
- **What**: A running instance of an image
- **Analogy**: Like an object instance of a class
- **Contains**: Your running application
- **Example**: When you run the image, it becomes a container

### 3. **Dockerfile**
- **What**: Instructions to build an image
- **Analogy**: Like a recipe
- **Contains**: Step-by-step commands (FROM, COPY, RUN, etc.)

### 4. **Docker Compose**
- **What**: Tool to run multiple containers together
- **Why**: Your app needs Spring Boot + PostgreSQL (2 containers)
- **File**: `docker-compose.yml`

### 5. **Docker Volume**
- **What**: Persistent storage for data
- **Why**: Without volumes, data is lost when container stops
- **Example**: PostgreSQL data stored in `postgres_data` volume

### 6. **Docker Network**
- **What**: Virtual network connecting containers
- **Why**: So Spring Boot can talk to PostgreSQL
- **Example**: `crud_network` connects app + database

---

## Understanding the Files

### 1. Dockerfile (Multi-Stage Build)

```dockerfile
# STAGE 1: Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Runtime Stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Why Multi-Stage?**
- **Stage 1 (Build)**: Compiles your code, creates JAR (uses Maven + JDK = large)
- **Stage 2 (Runtime)**: Only copies the JAR and runs it (uses JRE only = small)
- **Result**: Final image is ~300MB instead of ~700MB

**Step-by-Step Breakdown:**

| Step | What Happens | Why |
|------|-------------|-----|
| `FROM maven:3.9...` | Start with Maven + Java 17 | Need Maven to build |
| `WORKDIR /app` | Create /app folder in container | Organized workspace |
| `COPY pom.xml .` | Copy only pom.xml first | Docker caching trick |
| `RUN mvn dependency:go-offline` | Download all dependencies | Cached if pom.xml unchanged |
| `COPY src ./src` | Copy source code | Now copy code |
| `RUN mvn clean package` | Build JAR file | Creates target/*.jar |
| `FROM eclipse-temurin:17-jre` | Start fresh with just JRE | Smaller final image |
| `COPY --from=build ...` | Copy JAR from stage 1 | Get compiled JAR |
| `EXPOSE 8080` | Document port usage | Info only |
| `ENTRYPOINT [...]` | Run the JAR when container starts | Start Spring Boot |

**Docker Caching Trick:**
- Dockerfile layers are cached
- If `pom.xml` doesn't change, Docker reuses the dependency layer
- This makes rebuilds much faster (seconds instead of minutes)

### 2. docker-compose.yml

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: cruddb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data/pgdata
    networks:
      - crud_network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s

  app:
    build:
      context: .
      dockerfile: Dockerfile
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cruddb
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    ports:
      - "8080:8080"
    networks:
      - crud_network

networks:
  crud_network:
    driver: bridge

volumes:
  postgres_data:
    driver: local
```

**Key Concepts:**

| Concept | Explanation | Example |
|---------|------------|---------|
| **Services** | Containers to run | postgres, app |
| **depends_on** | Start order | app waits for postgres |
| **healthcheck** | Check if ready | pg_isready command |
| **environment** | Config variables | Database URL, username |
| **ports** | Expose to host | 8080:8080 = localhost:8080 |
| **volumes** | Persistent data | postgres_data stores DB |
| **networks** | Container communication | crud_network connects both |

**How Containers Communicate:**
```
Your Machine (localhost)
├── Port 8080 → Spring Boot Container
│   └── Connects to "postgres:5432" via crud_network
└── Port 5432 → PostgreSQL Container
    └── Stores data in postgres_data volume
```

---

## Step-by-Step Manual Process

### Prerequisites

**Install Docker:**
- **Mac**: Download Docker Desktop from docker.com
- **Windows**: Download Docker Desktop from docker.com
- **Linux**:
  ```bash
  curl -fsSL https://get.docker.com -o get-docker.sh
  sudo sh get-docker.sh
  ```

**Verify Installation:**
```bash
docker --version
# Should show: Docker version 24.x.x

docker-compose --version
# Should show: Docker Compose version v2.x.x
```

---

### Process 1: Build Docker Image Manually

**Step 1: Build the Image**

```bash
# Navigate to project directory
cd /Users/ainexusstudio/Documents/GitHub/test1

# Build the Docker image
# -t = tag (name) for the image
# crud-app:1.0 = image name:version
# . = build context (current directory)
docker build -t crud-app:1.0 .
```

**What Happens Internally:**
1. Docker reads Dockerfile
2. Downloads base images (maven:3.9, eclipse-temurin:17)
3. Copies pom.xml, downloads dependencies
4. Copies source code
5. Runs `mvn clean package`
6. Creates final image with JAR
7. Tags it as `crud-app:1.0`

**Expected Output:**
```
[+] Building 45.2s (14/14) FINISHED
 => [internal] load build definition from Dockerfile
 => [internal] load .dockerignore
 => [build 1/5] FROM docker.io/library/maven:3.9-eclipse-temurin-17
 => [build 2/5] WORKDIR /app
 => [build 3/5] COPY pom.xml .
 => [build 4/5] RUN mvn dependency:go-offline -B
 => [build 5/5] COPY src ./src
 => [build 6/6] RUN mvn clean package -DskipTests
 => [stage-1 1/3] FROM docker.io/library/eclipse-temurin:17-jre
 => [stage-1 2/3] WORKDIR /app
 => [stage-1 3/3] COPY --from=build /app/target/*.jar app.jar
 => exporting to image
 => => naming to docker.io/library/crud-app:1.0
```

**Step 2: Verify Image Created**

```bash
# List all Docker images
docker images

# You should see:
# REPOSITORY    TAG       IMAGE ID       CREATED         SIZE
# crud-app      1.0       abc123def456   2 minutes ago   320MB
```

**Step 3: Run PostgreSQL Container Manually**

```bash
# Run PostgreSQL container
# --name = container name
# -e = environment variable
# -p = port mapping
# -d = detached mode (run in background)
docker run --name postgres-db \
  -e POSTGRES_DB=cruddb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15-alpine

# Check if running
docker ps

# You should see postgres-db running
```

**Step 4: Run Spring Boot Container Manually**

```bash
# Run Spring Boot container
# --name = container name
# --link = connect to postgres-db container
# -e = environment variables for database connection
# -p = port mapping
# -d = detached mode
docker run --name spring-app \
  --link postgres-db:postgres \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cruddb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -p 8080:8080 \
  -d crud-app:1.0

# Check logs
docker logs -f spring-app

# You should see Spring Boot starting up
```

**Step 5: Test the Application**

```bash
# Test registration
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"docker@example.com","password":"dockerpass123"}'

# Expected: {"success":true,"message":"Registration successful"}
```

**Step 6: Clean Up**

```bash
# Stop containers
docker stop spring-app postgres-db

# Remove containers
docker rm spring-app postgres-db

# Remove image (optional)
docker rmi crud-app:1.0
```

---

### Process 2: Using Docker Compose (Recommended)

**Docker Compose automates all the manual steps above!**

**Step 1: Start Everything**

```bash
# Navigate to project directory
cd /Users/ainexusstudio/Documents/GitHub/test1

# Start all services (builds image, starts containers)
# -d = detached mode (run in background)
docker-compose up -d
```

**What Happens:**
1. Reads `docker-compose.yml`
2. Creates `crud_network` network
3. Creates `postgres_data` volume
4. Builds `app` image from Dockerfile
5. Starts `postgres` container
6. Waits for postgres healthcheck to pass
7. Starts `app` container
8. Connects both to `crud_network`

**Expected Output:**
```
[+] Running 4/4
 ✔ Network test1_crud_network      Created
 ✔ Volume "test1_postgres_data"    Created
 ✔ Container crud_postgres_db      Healthy
 ✔ Container crud_spring_app       Started
```

**Step 2: Check Status**

```bash
# View running containers
docker-compose ps

# You should see:
# NAME                 STATUS              PORTS
# crud_postgres_db     Up (healthy)        0.0.0.0:5432->5432/tcp
# crud_spring_app      Up                  0.0.0.0:8080->8080/tcp
```

**Step 3: View Logs**

```bash
# View all logs
docker-compose logs

# View only Spring Boot logs
docker-compose logs app

# Follow logs in real-time
docker-compose logs -f app
```

**Step 4: Test the Application**

```bash
# Test registration
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"docker@example.com","password":"dockerpass123"}'

# Test login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"docker@example.com","password":"dockerpass123"}'
```

**Step 5: Access Database Directly**

```bash
# Connect to PostgreSQL container
docker exec -it crud_postgres_db psql -U postgres -d cruddb

# Inside PostgreSQL shell:
\dt                           # List tables
SELECT * FROM users;          # View users
\q                            # Quit
```

**Step 6: Stop Everything**

```bash
# Stop all containers (data is preserved)
docker-compose stop

# Stop and remove containers (data still preserved in volume)
docker-compose down

# Stop, remove containers AND volumes (deletes all data)
docker-compose down -v
```

---

## Docker Commands Explained

### Essential Commands

| Command | Purpose | Example |
|---------|---------|---------|
| `docker build` | Build image from Dockerfile | `docker build -t myapp .` |
| `docker run` | Run a container | `docker run -p 8080:8080 myapp` |
| `docker ps` | List running containers | `docker ps` |
| `docker ps -a` | List all containers | `docker ps -a` |
| `docker images` | List images | `docker images` |
| `docker logs` | View container logs | `docker logs crud_spring_app` |
| `docker exec` | Run command in container | `docker exec -it app bash` |
| `docker stop` | Stop container | `docker stop crud_spring_app` |
| `docker rm` | Remove container | `docker rm crud_spring_app` |
| `docker rmi` | Remove image | `docker rmi crud-app:1.0` |

### Docker Compose Commands

| Command | Purpose | Example |
|---------|---------|---------|
| `docker-compose up` | Start all services | `docker-compose up -d` |
| `docker-compose down` | Stop and remove | `docker-compose down` |
| `docker-compose ps` | List services | `docker-compose ps` |
| `docker-compose logs` | View logs | `docker-compose logs -f` |
| `docker-compose build` | Rebuild images | `docker-compose build` |
| `docker-compose restart` | Restart services | `docker-compose restart app` |

### Useful Flags

| Flag | Meaning | Example |
|------|---------|---------|
| `-d` | Detached mode (background) | `docker-compose up -d` |
| `-f` | Follow logs (real-time) | `docker logs -f app` |
| `-it` | Interactive terminal | `docker exec -it app bash` |
| `-p` | Port mapping | `docker run -p 8080:8080` |
| `-e` | Environment variable | `docker run -e DB_URL=...` |
| `-v` | Volume mount | `docker run -v data:/data` |
| `--rm` | Remove after stop | `docker run --rm app` |

---

## Troubleshooting

### Problem 1: Port Already in Use

**Error:**
```
Error: bind: address already in use
```

**Cause:** PostgreSQL or Spring Boot already running on your machine

**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080

# Find what's using port 5432
lsof -i :5432

# Stop your local PostgreSQL
brew services stop postgresql  # Mac
sudo systemctl stop postgresql # Linux

# Or change ports in docker-compose.yml:
ports:
  - "8081:8080"  # Use 8081 on host instead
```

### Problem 2: Container Won't Start

**Check logs:**
```bash
docker-compose logs app

# Common issues:
# - Database not ready yet (should retry automatically)
# - Wrong database credentials
# - Missing environment variables
```

**Solution:**
```bash
# Restart with fresh build
docker-compose down
docker-compose up --build -d
```

### Problem 3: Database Data Lost

**Cause:** Ran `docker-compose down -v` (deletes volumes)

**Solution:**
```bash
# Never use -v flag unless you want to delete data
docker-compose down      # Good - keeps data
docker-compose down -v   # Bad - deletes data
```

### Problem 4: "No Space Left on Device"

**Cause:** Docker images/containers taking too much space

**Solution:**
```bash
# Remove unused containers
docker container prune

# Remove unused images
docker image prune

# Remove everything unused (careful!)
docker system prune -a

# Check disk usage
docker system df
```

### Problem 5: Can't Connect to Database

**Check:**
```bash
# 1. Is postgres healthy?
docker-compose ps
# Should show "Up (healthy)" for postgres

# 2. Check postgres logs
docker-compose logs postgres

# 3. Test connection manually
docker exec -it crud_postgres_db psql -U postgres -d cruddb
```

---

## Quick Reference

### Start Project
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f app
```

### Stop Project
```bash
docker-compose down
```

### Rebuild After Code Changes
```bash
docker-compose up -d --build
```

### Access Database
```bash
docker exec -it crud_postgres_db psql -U postgres -d cruddb
```

### Clean Everything
```bash
docker-compose down -v
docker system prune -a
```

---

## What You've Learned

✅ **Docker Images** - Blueprints for your application
✅ **Docker Containers** - Running instances of images
✅ **Dockerfile** - Instructions to build an image
✅ **Multi-stage Builds** - Create smaller, optimized images
✅ **Docker Compose** - Orchestrate multiple containers
✅ **Volumes** - Persist data across container restarts
✅ **Networks** - Connect containers together
✅ **Healthchecks** - Ensure services are ready
✅ **Environment Variables** - Configure containers

---

## Next Steps

1. **Production Deployment**: Deploy to AWS/Azure using Docker
2. **Docker Hub**: Push your image to Docker Hub for sharing
3. **Kubernetes**: Orchestrate containers at scale
4. **CI/CD**: Auto-build Docker images in GitHub Actions

---

**Your project is now fully Dockerized! 🎉**

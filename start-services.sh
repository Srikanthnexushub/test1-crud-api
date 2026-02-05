#!/bin/bash

# ============================================
# Start All Services Script
# ============================================
# This script starts the backend, frontend, and verifies database connection

set -e  # Exit on error

# Java Home (Homebrew OpenJDK 17)
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
export PATH="$JAVA_HOME/bin:$PATH"

echo "🚀 Starting All Services (Local Mode)..."
echo "=========================================="
echo ""
echo "ℹ️  This script starts services in LOCAL mode (PostgreSQL port 5433)"
echo ""
echo "   For Docker mode, use:"
echo "   $ docker-compose up -d"
echo ""
echo "   To sync Docker DB → Local DB:"
echo "   $ ./sync-db.sh"
echo ""
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# ============================================
# 1. Check PostgreSQL Database
# ============================================
echo ""
echo "📊 Checking PostgreSQL Database..."

if PGPASSWORD=P0st psql -h localhost -p 5433 -U postgres -d Crud_db -c "SELECT 1" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ PostgreSQL is running on port 5433${NC}"
else
    echo -e "${RED}❌ PostgreSQL is not running or connection failed${NC}"
    echo "Please start PostgreSQL first"
    exit 1
fi

# ============================================
# 2. Stop Any Existing Services
# ============================================
echo ""
echo "🛑 Stopping any existing services..."

pkill -f "java.*Crud_Operation" 2>/dev/null && echo "  - Stopped existing backend" || echo "  - No existing backend running"
pkill -f "node.*vite" 2>/dev/null && echo "  - Stopped existing frontend" || echo "  - No existing frontend running"

sleep 2

# ============================================
# 3. Start Backend (Spring Boot)
# ============================================
echo ""
echo "🔧 Starting Backend (Spring Boot)..."

# Check if JAR exists
if [ ! -f "target/Crud_Operation-1.0-SNAPSHOT.jar" ]; then
    echo -e "${YELLOW}⚠️  JAR file not found. Building backend...${NC}"
    mvn clean package -Dmaven.test.skip=true
fi

# Load environment variables and start backend
export $(cat .env | grep -v '^#' | xargs)
nohup java -jar target/Crud_Operation-1.0-SNAPSHOT.jar > logs/application.log 2>&1 &
BACKEND_PID=$!

echo "  - Backend started (PID: $BACKEND_PID)"
echo "  - Waiting for backend to be ready..."

# Wait for backend to start
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend is ready!${NC}"
        break
    fi

    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Backend failed to start within 30 seconds${NC}"
        echo "Check logs/application.log for errors"
        exit 1
    fi

    echo -n "."
    sleep 1
done

# ============================================
# 4. Start Frontend (React + Vite)
# ============================================
echo ""
echo "⚛️  Starting Frontend (React + Vite)..."

cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}⚠️  node_modules not found. Installing dependencies...${NC}"
    npm install
fi

# Start frontend in background
nohup npm run dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo "  - Frontend started (PID: $FRONTEND_PID)"
echo "  - Waiting for frontend to be ready..."

cd ..

# Wait for frontend to start
for i in {1..15}; do
    if lsof -i :3000 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend is ready!${NC}"
        break
    fi

    if [ $i -eq 15 ]; then
        echo -e "${YELLOW}⚠️  Frontend may take a moment to fully start${NC}"
    fi

    echo -n "."
    sleep 1
done

# ============================================
# 5. Display Service Status
# ============================================
echo ""
echo "=========================================="
echo -e "${GREEN}🎉 All Services Started Successfully!${NC}"
echo "=========================================="
echo ""
echo "📍 Service URLs:"
echo "   • Frontend:        http://localhost:3000"
echo "   • Backend API:     http://localhost:8080"
echo "   • API Health:      http://localhost:8080/actuator/health"
echo "   • Swagger UI:      http://localhost:8080/swagger-ui.html"
echo ""
echo "🗄️  Database (Local Mode):"
echo "   • PostgreSQL:      localhost:5433"
echo "   • Database Name:   Crud_db"
echo "   • Username:        postgres"
echo "   • Password:        P0st"
echo ""
echo "🐳 Docker Database (Not Started):"
echo "   • PostgreSQL:      localhost:5434"
echo "   • Database Name:   cruddb"
echo "   • Start:           docker-compose up -d"
echo "   • Sync:            ./sync-db.sh (Docker → Local)"
echo ""
echo "👤 Admin Credentials:"
echo "   • Email:           admin@example.com"
echo "   • Password:        Admin@123456"
echo ""
echo "📋 Process IDs:"
echo "   • Backend PID:     $BACKEND_PID"
echo "   • Frontend PID:    $FRONTEND_PID"
echo ""
echo "📝 Logs:"
echo "   • Backend:         tail -f logs/application.log"
echo "   • Frontend:        tail -f logs/frontend.log"
echo ""
echo "🔧 Management Commands:"
echo "   • Stop services:   ./stop-services.sh"
echo "   • Check status:    ./status-services.sh"
echo "   • Sync databases:  ./sync-db.sh"
echo ""
echo "=========================================="

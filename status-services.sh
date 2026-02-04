#!/bin/bash

# ============================================
# Check Service Status Script
# ============================================

echo "📊 Service Status Check"
echo "=========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check Local PostgreSQL
echo ""
echo "🗄️  Local PostgreSQL Database:"
if PGPASSWORD=P0st psql -h localhost -p 5433 -U postgres -d Crud_db -c "SELECT 1" > /dev/null 2>&1; then
    echo -e "   Status: ${GREEN}✅ Running${NC}"
    echo "   Port:   5433"
    echo "   DB:     Crud_db"
    USER_COUNT=$(PGPASSWORD=P0st psql -h localhost -p 5433 -U postgres -d Crud_db -t -c "SELECT COUNT(*) FROM users;" 2>/dev/null | tr -d ' ' || echo "N/A")
    echo "   Users:  $USER_COUNT"
else
    echo -e "   Status: ${RED}❌ Not Running${NC}"
fi

# Check Docker PostgreSQL
echo ""
echo "🐳 Docker PostgreSQL Database:"
if docker ps | grep crud_postgres_db > /dev/null 2>&1; then
    echo -e "   Status: ${GREEN}✅ Running (Container)${NC}"
    echo "   Port:   5434"
    echo "   DB:     cruddb"
    USER_COUNT=$(PGPASSWORD=postgres psql -h localhost -p 5434 -U postgres -d cruddb -t -c "SELECT COUNT(*) FROM users;" 2>/dev/null | tr -d ' ' || echo "N/A")
    echo "   Users:  $USER_COUNT"
elif PGPASSWORD=postgres psql -h localhost -p 5434 -U postgres -d cruddb -c "SELECT 1" > /dev/null 2>&1; then
    echo -e "   Status: ${GREEN}✅ Running (Port only)${NC}"
    echo "   Port:   5434"
    echo "   DB:     cruddb"
else
    echo -e "   Status: ${RED}❌ Not Running${NC}"
fi

# Check Backend
echo ""
echo "🔧 Backend (Spring Boot):"
if pgrep -f "java.*Crud_Operation" > /dev/null; then
    PID=$(pgrep -f "java.*Crud_Operation")
    echo -e "   Status: ${GREEN}✅ Running${NC}"
    echo "   PID:    $PID"
    echo "   URL:    http://localhost:8080"

    # Check health endpoint
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "   Health: ${GREEN}UP${NC}"
    else
        echo -e "   Health: ${RED}DOWN${NC}"
    fi
else
    echo -e "   Status: ${RED}❌ Not Running${NC}"
fi

# Check Frontend
echo ""
echo "⚛️  Frontend (React + Vite):"
if pgrep -f "node.*vite" > /dev/null; then
    PID=$(pgrep -f "node.*vite")
    echo -e "   Status: ${GREEN}✅ Running${NC}"
    echo "   PID:    $PID"

    # Check which port it's on
    if lsof -i :3000 | grep LISTEN > /dev/null 2>&1; then
        echo "   URL:    http://localhost:3000"
    elif lsof -i :5173 | grep LISTEN > /dev/null 2>&1; then
        echo "   URL:    http://localhost:5173"
    fi
else
    echo -e "   Status: ${RED}❌ Not Running${NC}"
fi

# Check ports
echo ""
echo "🔌 Port Status:"
echo "   8080 (Backend):   $(lsof -i :8080 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"
echo "   3000 (Frontend):  $(lsof -i :3000 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"
echo "   5433 (Local DB):  $(lsof -i :5433 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"
echo "   5434 (Docker DB): $(lsof -i :5434 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"

# Sync Script
echo ""
echo "🔄 Database Sync:"
if [ -f "sync-db.sh" ]; then
    echo -e "   Script: ${GREEN}✅ Available${NC}"
    echo "   Usage:  ./sync-db.sh (Docker → Local)"
else
    echo -e "   Script: ${RED}❌ Not Found${NC}"
fi

echo ""
echo "=========================================="

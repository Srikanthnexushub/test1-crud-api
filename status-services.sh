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

# Check PostgreSQL
echo ""
echo "🗄️  PostgreSQL Database:"
if PGPASSWORD=P0st psql -h localhost -p 5433 -U postgres -d Crud_db -c "SELECT 1" > /dev/null 2>&1; then
    echo -e "   Status: ${GREEN}✅ Running${NC}"
    echo "   Port:   5433"
    echo "   DB:     Crud_db"
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
echo "   8080:   $(lsof -i :8080 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"
echo "   3000:   $(lsof -i :3000 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"
echo "   5433:   $(lsof -i :5433 | grep LISTEN > /dev/null 2>&1 && echo -e "${GREEN}In Use${NC}" || echo -e "${RED}Free${NC}")"

echo ""
echo "=========================================="

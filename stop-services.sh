#!/bin/bash

# ============================================
# Stop All Services Script
# ============================================

echo "🛑 Stopping All Services..."
echo "=========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Stop backend
echo ""
echo "Stopping local services..."
if pkill -f "java.*Crud_Operation" 2>/dev/null; then
    echo -e "${GREEN}✅ Backend stopped${NC}"
else
    echo "  - No backend process running"
fi

# Stop frontend
if pkill -f "node.*vite" 2>/dev/null; then
    echo -e "${GREEN}✅ Frontend stopped${NC}"
else
    echo "  - No frontend process running"
fi

sleep 1

# Check Docker containers
echo ""
echo "Checking Docker containers..."
if docker ps | grep -E "crud_postgres_db|crud_spring_app" > /dev/null 2>&1; then
    echo -e "${YELLOW}ℹ️  Docker containers are still running${NC}"
    echo ""
    read -p "Do you want to stop Docker containers too? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down
        echo -e "${GREEN}✅ Docker containers stopped${NC}"
    else
        echo "  - Docker containers left running"
        echo "  - To stop manually: docker-compose down"
    fi
else
    echo "  - No Docker containers running"
fi

# Verify all stopped
echo ""
if pgrep -f "java.*Crud_Operation\|node.*vite" > /dev/null; then
    echo -e "${RED}⚠️  Some processes may still be running${NC}"
    echo "Run 'ps aux | grep -E \"java.*Crud_Operation|node.*vite\"' to check"
else
    echo "=========================================="
    echo -e "${GREEN}✅ All local services stopped${NC}"
    echo "=========================================="
    echo ""
    echo "💡 Available Commands:"
    echo "   • Start local:     ./start-services.sh"
    echo "   • Start Docker:    docker-compose up -d"
    echo "   • Check status:    ./status-services.sh"
    echo ""
fi

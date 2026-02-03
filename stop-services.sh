#!/bin/bash

# ============================================
# Stop All Services Script
# ============================================

echo "🛑 Stopping All Services..."
echo "=========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Stop backend
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

# Verify all stopped
if pgrep -f "java.*Crud_Operation\|node.*vite" > /dev/null; then
    echo -e "${RED}⚠️  Some processes may still be running${NC}"
    echo "Run 'ps aux | grep -E \"java.*Crud_Operation|node.*vite\"' to check"
else
    echo ""
    echo "=========================================="
    echo -e "${GREEN}✅ All services stopped successfully${NC}"
    echo "=========================================="
    echo ""
    echo "To start services again, run:"
    echo "  ./start-services.sh"
    echo ""
fi

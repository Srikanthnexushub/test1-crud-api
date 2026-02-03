#!/bin/bash

# Script to create admin user via API

echo "🔐 Creating Admin User..."
echo ""

# Admin credentials
ADMIN_EMAIL="admin@example.com"
ADMIN_PASSWORD="Admin@123456"

# Wait for API to be ready
echo "⏳ Waiting for API to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ API is ready!"
        break
    fi
    echo "   Attempt $i/30..."
    sleep 2
done

# Register admin user
echo ""
echo "📝 Registering admin user..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")

echo "$RESPONSE" | grep -q "success.*true" && echo "✅ Admin user created successfully!" || echo "⚠️  User might already exist or registration failed"

# Login to get token
echo ""
echo "🔑 Logging in as admin..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo ""
echo "================================================"
echo "🎉 ADMIN CREDENTIALS"
echo "================================================"
echo "Email:    $ADMIN_EMAIL"
echo "Password: $ADMIN_PASSWORD"
echo ""
echo "JWT Token: $TOKEN"
echo "================================================"
echo ""
echo "📋 Usage Examples:"
echo ""
echo "# Login:"
echo "curl -X POST http://localhost:8080/api/v1/users/login \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}'"
echo ""
echo "# Get user details:"
echo "curl -X GET http://localhost:8080/api/v1/users/1 \\"
echo "  -H 'Authorization: Bearer $TOKEN'"
echo ""
echo "================================================"

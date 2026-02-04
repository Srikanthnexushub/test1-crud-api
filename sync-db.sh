#!/bin/bash
# ============================================
# Database Sync Script
# Syncs Docker PostgreSQL → Local PostgreSQL
# ============================================

set -e  # Exit on error

echo "========================================"
echo "  Database Sync: Docker → Local"
echo "========================================"

# Configuration
DOCKER_PORT=5434
LOCAL_PORT=5433
DOCKER_DB=cruddb
LOCAL_DB=Crud_db
POSTGRES_USER=postgres
DOCKER_PASSWORD=postgres
LOCAL_PASSWORD=P0st
TEMP_FILE=/tmp/docker_db_sync_$(date +%s).sql

echo ""
echo "Step 1: Exporting from Docker (localhost:$DOCKER_PORT/$DOCKER_DB)..."
docker exec crud_postgres_db pg_dump -U $POSTGRES_USER -d $DOCKER_DB --clean --if-exists > $TEMP_FILE
LINES=$(wc -l < $TEMP_FILE | tr -d ' ')
echo "✓ Exported $LINES lines to $TEMP_FILE"

echo ""
echo "Step 2: Terminating active connections to local database..."
PGPASSWORD=$LOCAL_PASSWORD psql -h localhost -p $LOCAL_PORT -U $POSTGRES_USER -d postgres -c "
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = '$LOCAL_DB'
  AND pid <> pg_backend_pid();" > /dev/null 2>&1 || echo "No active connections to terminate"

echo ""
echo "Step 3: Dropping and recreating local database..."
PGPASSWORD=$LOCAL_PASSWORD psql -h localhost -p $LOCAL_PORT -U $POSTGRES_USER -d postgres -c "DROP DATABASE IF EXISTS \"$LOCAL_DB\";" > /dev/null
PGPASSWORD=$LOCAL_PASSWORD psql -h localhost -p $LOCAL_PORT -U $POSTGRES_USER -d postgres -c "CREATE DATABASE \"$LOCAL_DB\";" > /dev/null
echo "✓ Database recreated"

echo ""
echo "Step 4: Importing data to local database (localhost:$LOCAL_PORT/$LOCAL_DB)..."
PGPASSWORD=$LOCAL_PASSWORD psql -h localhost -p $LOCAL_PORT -U $POSTGRES_USER -d $LOCAL_DB -f $TEMP_FILE > /dev/null 2>&1
echo "✓ Data imported successfully"

echo ""
echo "Step 5: Verifying sync..."
DOCKER_COUNT=$(PGPASSWORD=$DOCKER_PASSWORD psql -h localhost -p $DOCKER_PORT -U $POSTGRES_USER -d $DOCKER_DB -t -c "SELECT COUNT(*) FROM users;" | tr -d ' ')
LOCAL_COUNT=$(PGPASSWORD=$LOCAL_PASSWORD psql -h localhost -p $LOCAL_PORT -U $POSTGRES_USER -d $LOCAL_DB -t -c "SELECT COUNT(*) FROM users;" | tr -d ' ')

echo "Docker database: $DOCKER_COUNT users"
echo "Local database:  $LOCAL_COUNT users"

if [ "$DOCKER_COUNT" == "$LOCAL_COUNT" ]; then
    echo "✓ Sync successful! Databases match."
else
    echo "✗ Warning: User counts don't match"
    exit 1
fi

echo ""
echo "Step 6: Cleaning up..."
rm -f $TEMP_FILE
echo "✓ Temporary file removed"

echo ""
echo "========================================"
echo "  ✓ Sync Complete!"
echo "========================================"
echo ""
echo "Both databases now have the same data:"
echo "  • Docker:  localhost:$DOCKER_PORT/$DOCKER_DB"
echo "  • Local:   localhost:$LOCAL_PORT/$LOCAL_DB"
echo ""

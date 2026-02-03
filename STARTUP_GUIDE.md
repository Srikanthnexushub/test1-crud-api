# Quick Startup Guide

## 🚀 Start All Services (One Command)

```bash
./start-services.sh
```

This will:
- ✅ Check PostgreSQL connection
- ✅ Stop any existing services
- ✅ Start backend (Spring Boot)
- ✅ Start frontend (React + Vite)
- ✅ Display all URLs and credentials

## 🛑 Stop All Services

```bash
./stop-services.sh
```

## 📊 Check Service Status

```bash
./status-services.sh
```

## 📋 Access URLs

After starting services:

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:3000 |
| **Backend API** | http://localhost:8080 |
| **API Health** | http://localhost:8080/actuator/health |
| **Swagger Docs** | http://localhost:8080/swagger-ui.html |

## 👤 Admin Login

- **Email:** admin@example.com
- **Password:** Admin@123456

## 📝 View Logs

```bash
# Backend logs
tail -f logs/application.log

# Frontend logs
tail -f /tmp/frontend.log
```

## 🔧 Manual Start (If Needed)

### Backend:
```bash
export $(cat .env | grep -v '^#' | xargs)
java -jar target/Crud_Operation-1.0-SNAPSHOT.jar
```

### Frontend:
```bash
cd frontend
npm run dev
```

## ⚠️ Prerequisites

- PostgreSQL running on port 5433
- Database: Crud_db
- User: postgres
- Password: P0st

## 🆘 Troubleshooting

### PostgreSQL not running?
```bash
# Check PostgreSQL status
pg_ctl status -D /Library/PostgreSQL/18/data

# Start PostgreSQL
pg_ctl start -D /Library/PostgreSQL/18/data
```

### Port already in use?
```bash
# Check what's using port 8080
lsof -i :8080

# Kill process on port 8080
kill $(lsof -t -i:8080)
```

### Services won't start?
```bash
# Check service status
./status-services.sh

# Stop all and try again
./stop-services.sh
./start-services.sh
```

## 📦 Rebuild Backend

```bash
mvn clean package -Dmaven.test.skip=true
```

## 📦 Reinstall Frontend Dependencies

```bash
cd frontend
rm -rf node_modules
npm install
```

---

**Need help?** Check the main README.md for detailed documentation.

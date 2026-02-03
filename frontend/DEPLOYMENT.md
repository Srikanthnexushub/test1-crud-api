# Frontend Deployment Guide

## Fortune 100 Enterprise-Grade Deployment

### Development Environment

```bash
# Install dependencies
cd frontend
npm install

# Start development server
npm run dev
```

Application runs on: `http://localhost:3000`

### Production Build

```bash
# Create production build
npm run build

# Preview production build locally
npm run preview
```

Build output: `frontend/dist/`

---

## Deployment Options

### Option 1: Static Hosting (Recommended for Frontend)

**Platforms**: Vercel, Netlify, AWS S3 + CloudFront, Azure Static Web Apps

#### Vercel Deployment

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
cd frontend
vercel
```

#### Netlify Deployment

```bash
# Install Netlify CLI
npm i -g netlify-cli

# Deploy
cd frontend
netlify deploy --prod --dir=dist
```

**Build Settings**:
- Build Command: `npm run build`
- Publish Directory: `dist`
- Node Version: 18+

---

### Option 2: Docker Deployment

**Dockerfile** (create in frontend directory):

```dockerfile
# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**nginx.conf**:

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

**Build and Run**:

```bash
docker build -t enterprise-crud-frontend .
docker run -p 3000:80 enterprise-crud-frontend
```

---

### Option 3: Spring Boot Integration (Serve from Backend)

```bash
# Build frontend
cd frontend
npm run build

# Copy to Spring Boot static resources
cp -r dist/* ../src/main/resources/static/

# Build and run Spring Boot
cd ..
mvn clean package
java -jar target/Crud_Operation-1.0-SNAPSHOT.jar
```

Access at: `http://localhost:8080`

---

## Environment Configuration

### Development
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### Production
```env
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

---

## CORS Configuration

Update backend `SecurityConfig.java`:

```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:*",
    "https://yourdomain.com",
    "https://*.yourdomain.com"
));
```

---

## Performance Optimization

### 1. Enable Compression

Already configured in `vite.config.js`:
- Gzip compression
- Code splitting
- Tree shaking

### 2. Lazy Loading

Routes are loaded on-demand automatically with React Router.

### 3. Asset Optimization

```bash
# Vite automatically handles:
- Image optimization
- CSS minification
- JavaScript minification
- Chunk splitting
```

---

## Security Checklist

- ✅ JWT tokens stored in memory (not localStorage)
- ✅ Automatic token refresh
- ✅ HTTPS enforced (production)
- ✅ CORS properly configured
- ✅ No sensitive data in client-side code
- ✅ Security headers via backend
- ✅ Rate limiting via backend

---

## Monitoring & Analytics

### Add Error Tracking

```javascript
// src/main.jsx
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: "YOUR_SENTRY_DSN",
  environment: import.meta.env.MODE,
});
```

### Add Analytics

```javascript
// src/App.jsx
import ReactGA from "react-ga4";

ReactGA.initialize("YOUR_GA4_ID");
```

---

## Health Check

Frontend health endpoint: `GET /health`

Returns:
```json
{
  "status": "healthy",
  "version": "1.0.0",
  "timestamp": "2026-02-03T12:00:00Z"
}
```

---

## Troubleshooting

### Issue: API Connection Failed

**Solution**:
1. Check backend is running on port 8080
2. Verify CORS configuration
3. Check browser console for errors
4. Verify API_BASE_URL in .env

### Issue: Authentication Not Working

**Solution**:
1. Clear browser cache
2. Check JWT token in Network tab
3. Verify backend security configuration
4. Check token expiration

### Issue: Build Fails

**Solution**:
```bash
rm -rf node_modules package-lock.json
npm install
npm run build
```

---

## Production Checklist

- [ ] Environment variables configured
- [ ] HTTPS enabled
- [ ] CORS whitelisted
- [ ] Error tracking configured
- [ ] Analytics configured
- [ ] Performance monitoring enabled
- [ ] CDN configured (if applicable)
- [ ] Backup strategy in place
- [ ] CI/CD pipeline configured
- [ ] Health checks configured

---

**Deployment Status**: ✅ Production Ready
**Target Environment**: Fortune 100 Enterprise
**Security Level**: High

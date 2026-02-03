# Enterprise CRUD Frontend

Fortune 100 enterprise-grade React frontend application with JWT authentication, RBAC, and comprehensive security features.

## Features

- ✅ JWT Authentication (Access + Refresh Tokens)
- ✅ Role-Based Access Control (RBAC)
- ✅ Secure API Communication
- ✅ Token Refresh Mechanism
- ✅ Protected Routes
- ✅ Responsive Design
- ✅ Error Handling & User Feedback
- ✅ Loading States
- ✅ Modern React with Hooks
- ✅ Vite for Fast Development

## Tech Stack

- **Framework**: React 18
- **Build Tool**: Vite
- **Routing**: React Router 6
- **HTTP Client**: Axios
- **Icons**: Lucide React
- **Styling**: CSS3 with CSS Variables

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend API running on http://localhost:8080

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

Application will be available at http://localhost:3000

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── src/
│   ├── components/          # React components
│   │   ├── Dashboard.jsx    # Main dashboard
│   │   ├── Login.jsx        # Login page
│   │   ├── Register.jsx     # Registration page
│   │   ├── UserList.jsx     # User management
│   │   └── ProtectedRoute.jsx
│   ├── context/
│   │   └── AuthContext.jsx  # Authentication state
│   ├── services/
│   │   └── api.js           # API service layer
│   ├── App.jsx              # Main app component
│   ├── main.jsx             # Entry point
│   └── index.css            # Global styles
├── index.html               # HTML entry point
├── vite.config.js           # Vite configuration
└── package.json             # Dependencies
```

## API Integration

The frontend connects to the backend API at:
- Base URL: `http://localhost:8080/api/v1`
- Authentication: JWT Bearer tokens
- Endpoints:
  - POST /users/register
  - POST /users/login
  - POST /users/refresh
  - GET /users/{id}
  - PUT /users/{id}
  - DELETE /users/{id} (Admin only)

## Security Features

1. **JWT Token Management**
   - Access tokens stored in memory (not localStorage)
   - Refresh tokens for token rotation
   - Automatic token refresh on expiry

2. **Protected Routes**
   - Unauthenticated users redirected to login
   - Role-based component rendering

3. **Secure API Calls**
   - Automatic token attachment
   - CORS handling
   - Error interceptors

## User Roles

- **USER**: Can view and edit own profile
- **ADMIN**: Can delete users
- **MANAGER**: Extended permissions

## Environment Variables

Create `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

Private - Enterprise Use Only

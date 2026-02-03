import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import UserManagement from './UserManagement';
import {
  LogOut,
  User,
  Shield,
  Activity,
  Clock,
  CheckCircle,
  Users
} from 'lucide-react';

const Dashboard = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleDisplay = () => {
    if (hasRole('ROLE_ADMIN')) return { name: 'Administrator', color: '#dc2626', icon: Shield };
    if (hasRole('ROLE_MANAGER')) return { name: 'Manager', color: '#ea580c', icon: Users };
    return { name: 'User', color: '#059669', icon: User };
  };

  const roleInfo = getRoleDisplay();
  const RoleIcon = roleInfo.icon;

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <div className="nav-content">
          <div className="nav-brand">
            <Activity size={28} />
            <h1>Enterprise CRUD</h1>
          </div>
          <button onClick={handleLogout} className="btn btn-secondary">
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </nav>

      <div className="dashboard-content">
        <div className="welcome-section">
          <div className="welcome-card">
            <div className="welcome-header">
              <div className="welcome-icon">
                <User size={48} />
              </div>
              <div>
                <h2>Welcome Back!</h2>
                <p className="user-email">{user?.email}</p>
              </div>
            </div>

            <div className="user-stats">
              <div className="stat-card">
                <RoleIcon size={24} style={{ color: roleInfo.color }} />
                <div>
                  <p className="stat-label">Role</p>
                  <p className="stat-value" style={{ color: roleInfo.color }}>
                    {roleInfo.name}
                  </p>
                </div>
              </div>

              <div className="stat-card">
                <CheckCircle size={24} style={{ color: '#059669' }} />
                <div>
                  <p className="stat-label">Status</p>
                  <p className="stat-value" style={{ color: '#059669' }}>Active</p>
                </div>
              </div>

              <div className="stat-card">
                <Clock size={24} style={{ color: '#3b82f6' }} />
                <div>
                  <p className="stat-label">Session</p>
                  <p className="stat-value" style={{ color: '#3b82f6' }}>Authenticated</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="features-grid">
          <div className="feature-card">
            <Shield size={32} className="feature-icon" />
            <h3>Secure Authentication</h3>
            <p>JWT-based authentication with automatic token refresh and BCrypt password encryption.</p>
            <div className="feature-badge">Active</div>
          </div>

          <div className="feature-card">
            <Activity size={32} className="feature-icon" />
            <h3>Audit Logging</h3>
            <p>Comprehensive audit trail tracking all user actions with IP address and timestamps.</p>
            <div className="feature-badge">Enabled</div>
          </div>

          <div className="feature-card">
            <Users size={32} className="feature-icon" />
            <h3>Role-Based Access</h3>
            <p>Fine-grained access control with multiple user roles and permissions.</p>
            <div className="feature-badge">{roleInfo.name}</div>
          </div>

          {hasRole('ROLE_ADMIN') && (
            <div className="feature-card feature-card-admin">
              <Shield size={32} className="feature-icon" />
              <h3>Admin Functions</h3>
              <p>Full system access including user management and deletion capabilities.</p>
              <div className="feature-badge feature-badge-admin">Admin Only</div>
            </div>
          )}
        </div>

        <div className="info-section">
          <div className="info-card">
            <h3>System Information</h3>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">API Version:</span>
                <span className="info-value">v1.0</span>
              </div>
              <div className="info-item">
                <span className="info-label">Security Level:</span>
                <span className="info-value">Fortune 100 Grade</span>
              </div>
              <div className="info-item">
                <span className="info-label">Encryption:</span>
                <span className="info-value">BCrypt + JWT</span>
              </div>
              <div className="info-item">
                <span className="info-label">Rate Limiting:</span>
                <span className="info-value">100 req/min</span>
              </div>
            </div>
          </div>
        </div>

        {/* Admin-only User Management */}
        {hasRole('ROLE_ADMIN') && <UserManagement />}
      </div>
    </div>
  );
};

export default Dashboard;

import { useState, useEffect } from 'react';
import { userAPI, authAPI } from '../services/api';
import { Users, Edit, Trash2, Plus, X, Save, AlertCircle, CheckCircle, Shield, User as UserIcon, Eye, EyeOff } from 'lucide-react';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [formData, setFormData] = useState({ email: '', password: '', role: 'ROLE_USER' });
  const [formErrors, setFormErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [showEditPassword, setShowEditPassword] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await userAPI.getAllUsers();
      setUsers(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const errors = {};

    if (!formData.email || !formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Invalid email format';
    }

    if (!showEditModal || formData.password) {
      if (!formData.password) {
        errors.password = 'Password is required';
      } else if (formData.password.length < 6) {
        errors.password = 'Password must be at least 6 characters';
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setError('');
      await userAPI.createUser(formData.email, formData.password, formData.role);
      setSuccess('User created successfully');
      setShowCreateModal(false);
      setFormData({ email: '', password: '', role: 'ROLE_USER' });
      await loadUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create user');
    }
  };

  const handleEditUser = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setError('');
      await userAPI.updateUser(
        selectedUser.id,
        formData.email,
        formData.password,
        formData.role
      );
      setSuccess('User updated successfully');
      setShowEditModal(false);
      setFormData({ email: '', password: '', role: 'ROLE_USER' });
      setSelectedUser(null);
      await loadUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update user');
    }
  };

  const handleDeleteUser = async () => {
    try {
      setError('');
      await userAPI.deleteUser(selectedUser.id);
      setSuccess('User deleted successfully');
      setShowDeleteModal(false);
      setSelectedUser(null);
      await loadUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete user');
    }
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    const userRole = user.roles && user.roles.length > 0 ? user.roles[0].name : 'ROLE_USER';
    setFormData({ email: user.email, password: '', role: userRole });
    setFormErrors({});
    setShowEditPassword(false);
    setShowEditModal(true);
  };

  const openDeleteModal = (user) => {
    setSelectedUser(user);
    setShowDeleteModal(true);
  };

  const openCreateModal = () => {
    setFormData({ email: '', password: '', role: 'ROLE_USER' });
    setFormErrors({});
    setShowPassword(false);
    setShowCreateModal(true);
  };

  const closeModals = () => {
    setShowCreateModal(false);
    setShowEditModal(false);
    setShowDeleteModal(false);
    setSelectedUser(null);
    setFormData({ email: '', password: '', role: 'ROLE_USER' });
    setFormErrors({});
    setShowPassword(false);
    setShowEditPassword(false);
  };

  const getRoleBadge = (roles) => {
    if (!roles || roles.length === 0) return null;
    const role = roles[0];

    const roleConfig = {
      ROLE_ADMIN: { label: 'Admin', color: '#dc2626', icon: Shield },
      ROLE_MANAGER: { label: 'Manager', color: '#ea580c', icon: Users },
      ROLE_USER: { label: 'User', color: '#059669', icon: UserIcon }
    };

    const config = roleConfig[role.name] || roleConfig.ROLE_USER;
    const Icon = config.icon;

    return (
      <span
        className="role-badge"
        style={{
          backgroundColor: `${config.color}15`,
          color: config.color,
          padding: '4px 12px',
          borderRadius: '12px',
          fontSize: '12px',
          fontWeight: '600',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px'
        }}
      >
        <Icon size={14} />
        {config.label}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="user-management">
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div className="spinner"></div>
          <p>Loading users...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="user-management">
      <div className="management-header">
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '12px', margin: 0 }}>
            <Users size={28} />
            User Management
          </h2>
          <p style={{ color: '#6b7280', margin: '8px 0 0 0' }}>
            Manage user accounts and permissions
          </p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary">
          <Plus size={18} />
          Create User
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={20} />
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle size={20} />
          {success}
        </div>
      )}

      <div className="table-container">
        <table className="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
                  No users found
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.email}</td>
                  <td>{getRoleBadge(user.roles)}</td>
                  <td>
                    <span
                      className="status-badge"
                      style={{
                        padding: '4px 12px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: '600',
                        backgroundColor: user.accountLocked ? '#fee' : '#efe',
                        color: user.accountLocked ? '#c00' : '#060'
                      }}
                    >
                      {user.accountLocked ? 'Locked' : 'Active'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        onClick={() => openEditModal(user)}
                        className="btn-icon btn-edit"
                        title="Edit user"
                      >
                        <Edit size={16} />
                      </button>
                      <button
                        onClick={() => openDeleteModal(user)}
                        className="btn-icon btn-delete"
                        title="Delete user"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={closeModals}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Create New User</h3>
              <button onClick={closeModals} className="btn-icon">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleCreateUser}>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className={formErrors.email ? 'error' : ''}
                  placeholder="user@example.com"
                />
                {formErrors.email && <span className="error-text">{formErrors.email}</span>}
              </div>
              <div className="form-group">
                <label>Password</label>
                <div className="password-input-wrapper">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    className={formErrors.password ? 'error' : ''}
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {formErrors.password && <span className="error-text">{formErrors.password}</span>}
              </div>
              <div className="form-group">
                <label>Role</label>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  className="role-select"
                >
                  <option value="ROLE_USER">User</option>
                  <option value="ROLE_MANAGER">Manager</option>
                  <option value="ROLE_ADMIN">Administrator</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" onClick={closeModals} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  <Save size={18} />
                  Create User
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      {showEditModal && selectedUser && (
        <div className="modal-overlay" onClick={closeModals}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Edit User</h3>
              <button onClick={closeModals} className="btn-icon">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleEditUser}>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className={formErrors.email ? 'error' : ''}
                  placeholder="user@example.com"
                />
                {formErrors.email && <span className="error-text">{formErrors.email}</span>}
              </div>
              <div className="form-group">
                <label>New Password (leave blank to keep current)</label>
                <div className="password-input-wrapper">
                  <input
                    type={showEditPassword ? 'text' : 'password'}
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    className={formErrors.password ? 'error' : ''}
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowEditPassword(!showEditPassword)}
                    aria-label={showEditPassword ? 'Hide password' : 'Show password'}
                  >
                    {showEditPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {formErrors.password && <span className="error-text">{formErrors.password}</span>}
              </div>
              <div className="form-group">
                <label>Role</label>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  className="role-select"
                >
                  <option value="ROLE_USER">User</option>
                  <option value="ROLE_MANAGER">Manager</option>
                  <option value="ROLE_ADMIN">Administrator</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" onClick={closeModals} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  <Save size={18} />
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && selectedUser && (
        <div className="modal-overlay" onClick={closeModals}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Delete User</h3>
              <button onClick={closeModals} className="btn-icon">
                <X size={20} />
              </button>
            </div>
            <div style={{ padding: '20px 0' }}>
              <p>Are you sure you want to delete this user?</p>
              <p style={{ marginTop: '8px', color: '#6b7280' }}>
                <strong>Email:</strong> {selectedUser.email}
              </p>
              <p style={{ marginTop: '16px', color: '#dc2626', fontSize: '14px' }}>
                This action cannot be undone.
              </p>
            </div>
            <div className="modal-actions">
              <button onClick={closeModals} className="btn btn-secondary">
                Cancel
              </button>
              <button onClick={handleDeleteUser} className="btn btn-danger">
                <Trash2 size={18} />
                Delete User
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;

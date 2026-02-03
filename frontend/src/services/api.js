import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token management in memory (more secure than localStorage)
let accessToken = null;
let refreshToken = null;

export const setTokens = (access, refresh) => {
  accessToken = access;
  refreshToken = refresh;
};

export const getAccessToken = () => accessToken;
export const getRefreshToken = () => refreshToken;

export const clearTokens = () => {
  accessToken = null;
  refreshToken = null;
};

// Request interceptor - attach token to every request
api.interceptors.request.use(
  (config) => {
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 and we haven't tried to refresh yet
    if (error.response?.status === 401 && !originalRequest._retry && refreshToken) {
      originalRequest._retry = true;

      try {
        // Try to refresh the token
        const response = await axios.post(`${API_BASE_URL}/users/refresh`, {
          refreshToken: refreshToken
        });

        const { token } = response.data;
        setTokens(token, refreshToken);

        // Retry the original request with new token
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// API methods
export const authAPI = {
  register: async (email, password) => {
    const response = await api.post('/users/register', { email, password });
    if (response.data.token && response.data.refreshToken) {
      setTokens(response.data.token, response.data.refreshToken);
    }
    return response.data;
  },

  login: async (email, password) => {
    const response = await api.post('/users/login', { email, password });
    if (response.data.token && response.data.refreshToken) {
      setTokens(response.data.token, response.data.refreshToken);
    }
    return response.data;
  },

  logout: () => {
    clearTokens();
  }
};

export const userAPI = {
  getUser: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },

  updateUser: async (id, email, password) => {
    const response = await api.put(`/users/${id}`, { email, password });
    return response.data;
  },

  deleteUser: async (id) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
  }
};

export default api;

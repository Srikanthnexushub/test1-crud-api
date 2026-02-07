import http from 'k6/http';
import { check } from 'k6';
import { getEnv } from '../config/environments.js';

const env = getEnv();
const BASE_URL = env.fullBaseUrl;

/**
 * Common HTTP headers
 */
const jsonHeaders = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

/**
 * Get auth headers with JWT token
 */
function authHeaders(token) {
  return {
    ...jsonHeaders,
    'Authorization': `Bearer ${token}`,
  };
}

/**
 * Register a new user
 */
export function register(email, password) {
  const payload = JSON.stringify({ email, password });
  const params = {
    headers: jsonHeaders,
    tags: { name: 'register' },
  };

  const res = http.post(`${BASE_URL}/users/register`, payload, params);

  check(res, {
    'register: status 200 or 409': (r) => r.status === 200 || r.status === 409,
  });

  return res;
}

/**
 * Login with email and password
 */
export function login(email, password) {
  const payload = JSON.stringify({ email, password });
  const params = {
    headers: jsonHeaders,
    tags: { name: 'login' },
  };

  const res = http.post(`${BASE_URL}/users/login`, payload, params);

  const success = check(res, {
    'login: status 200': (r) => r.status === 200,
    'login: has token': (r) => {
      try {
        const body = r.json();
        return body.token !== null && body.token !== undefined;
      } catch {
        return false;
      }
    },
  });

  return { response: res, success };
}

/**
 * Get user by ID
 */
export function getUser(userId, token) {
  const params = {
    headers: authHeaders(token),
    tags: { name: 'get_user' },
  };

  const res = http.get(`${BASE_URL}/users/${userId}`, params);

  check(res, {
    'get_user: status 200': (r) => r.status === 200,
  });

  return res;
}

/**
 * Get all users (admin only)
 */
export function getAllUsers(token) {
  const params = {
    headers: authHeaders(token),
    tags: { name: 'get_all_users' },
  };

  const res = http.get(`${BASE_URL}/users`, params);

  check(res, {
    'get_all_users: status 200 or 403': (r) => r.status === 200 || r.status === 403,
  });

  return res;
}

/**
 * Refresh access token
 */
export function refreshToken(refreshTkn) {
  const payload = JSON.stringify({ refreshToken: refreshTkn });
  const params = {
    headers: jsonHeaders,
    tags: { name: 'refresh' },
  };

  const res = http.post(`${BASE_URL}/users/refresh`, payload, params);

  check(res, {
    'refresh: status 200': (r) => r.status === 200,
    'refresh: has new token': (r) => {
      try {
        return r.json('token') !== null;
      } catch {
        return false;
      }
    },
  });

  return res;
}

/**
 * Request password reset
 */
export function forgotPassword(email) {
  const payload = JSON.stringify({ email });
  const params = {
    headers: jsonHeaders,
    tags: { name: 'forgot_password' },
  };

  const res = http.post(`${BASE_URL}/users/forgot-password`, payload, params);

  check(res, {
    'forgot_password: status 200': (r) => r.status === 200,
  });

  return res;
}

/**
 * Verify 2FA code
 */
export function verify2FA(email, code) {
  const payload = JSON.stringify({ code });
  const params = {
    headers: {
      ...jsonHeaders,
      'X-2FA-Email': email,
    },
    tags: { name: '2fa_verify' },
  };

  const res = http.post(`${BASE_URL}/auth/2fa/verify`, payload, params);

  return res;
}

/**
 * Setup 2FA
 */
export function setup2FA(token) {
  const params = {
    headers: authHeaders(token),
    tags: { name: '2fa_setup' },
  };

  const res = http.post(`${BASE_URL}/auth/2fa/setup`, null, params);

  check(res, {
    '2fa_setup: status 200': (r) => r.status === 200,
  });

  return res;
}

/**
 * Health check
 */
export function healthCheck() {
  const res = http.get(`${env.baseUrl}/actuator/health`, {
    tags: { name: 'health' },
  });

  check(res, {
    'health: status 200': (r) => r.status === 200,
    'health: status UP': (r) => {
      try {
        return r.json('status') === 'UP';
      } catch {
        return false;
      }
    },
  });

  return res;
}

/**
 * Environment configurations for load tests
 */
export const environments = {
  local: {
    baseUrl: 'http://localhost:8080',
    apiVersion: 'v1',
  },
  docker: {
    baseUrl: 'http://localhost:8080',
    apiVersion: 'v1',
  },
  staging: {
    baseUrl: 'https://staging-api.example.com',
    apiVersion: 'v1',
  },
  production: {
    baseUrl: 'https://api.example.com',
    apiVersion: 'v1',
  },
};

export function getEnv() {
  const envName = __ENV.TARGET_ENV || 'local';
  const env = environments[envName];

  if (!env) {
    throw new Error(`Unknown environment: ${envName}. Available: ${Object.keys(environments).join(', ')}`);
  }

  return {
    ...env,
    fullBaseUrl: `${env.baseUrl}/api/${env.apiVersion}`,
  };
}

// Test user credentials (must exist and be verified)
export const TEST_USER = {
  email: __ENV.TEST_EMAIL || 'loadtest@example.com',
  password: __ENV.TEST_PASSWORD || 'LoadTest123!',
};

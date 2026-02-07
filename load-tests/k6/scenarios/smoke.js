/**
 * Smoke Test
 *
 * Purpose: Verify system is working correctly
 * Duration: ~1 minute
 * VUs: 1-2
 *
 * Run: k6 run scenarios/smoke.js
 */
import { sleep } from 'k6';
import { login, getUser, healthCheck } from '../lib/api.js';
import { TEST_USER } from '../config/environments.js';
import { thresholds } from '../config/thresholds.js';

export const options = {
  vus: 1,
  duration: '1m',
  thresholds: thresholds,
};

export function setup() {
  // Verify system is up
  console.log('Checking system health...');
  const health = healthCheck();

  if (health.status !== 200) {
    throw new Error(`System health check failed: ${health.status}`);
  }

  console.log('System is healthy. Starting smoke test.');

  return {
    testUser: TEST_USER,
  };
}

export default function (data) {
  const { testUser } = data;

  // Step 1: Health check
  healthCheck();
  sleep(1);

  // Step 2: Login
  const { response: loginRes, success } = login(testUser.email, testUser.password);

  if (success && loginRes.status === 200) {
    const body = loginRes.json();
    const token = body.token;

    sleep(1);

    // Step 3: Get user profile
    getUser(1, token);
  }

  sleep(1);
}

export function teardown(data) {
  console.log('Smoke test completed.');
}

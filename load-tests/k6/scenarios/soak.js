/**
 * Soak Test
 *
 * Purpose: Detect memory leaks and long-term stability issues
 * Duration: 2+ hours
 * VUs: Constant 50
 *
 * Run: k6 run scenarios/soak.js
 */
import { sleep } from 'k6';
import { login, getUser, refreshToken, healthCheck } from '../lib/api.js';
import { TEST_USER } from '../config/environments.js';
import { thresholds } from '../config/thresholds.js';
import { thinkTime } from '../lib/data.js';

export const options = {
  stages: [
    { duration: '5m', target: 50 },     // Ramp up
    { duration: '2h', target: 50 },     // Soak for 2 hours
    { duration: '5m', target: 0 },      // Ramp down
  ],
  thresholds: thresholds,
};

// Track metrics over time
let iterationCount = 0;

export function setup() {
  console.log('Starting soak test...');
  console.log('This test runs for 2+ hours to detect memory leaks and degradation.');
  console.log(`Test user: ${TEST_USER.email}`);

  // Initial health check
  const health = healthCheck();
  console.log(`Initial health status: ${health.status}`);

  return {
    testUser: TEST_USER,
    startTime: new Date().toISOString(),
  };
}

export default function (data) {
  const { testUser } = data;
  iterationCount++;

  // Periodic health check (every 100 iterations)
  if (iterationCount % 100 === 0) {
    healthCheck();
  }

  // Full user session simulation
  const { response: loginRes, success } = login(testUser.email, testUser.password);

  if (!success) {
    sleep(thinkTime(5, 10));
    return;
  }

  const body = loginRes.json();
  const token = body.token;
  const refreshTkn = body.refreshToken;

  // Simulate a browsing session
  for (let i = 0; i < 3; i++) {
    sleep(thinkTime(5, 15));
    getUser(1, token);
  }

  // Refresh token mid-session
  if (refreshTkn) {
    sleep(thinkTime(2, 5));
    refreshToken(refreshTkn);
  }

  // More browsing
  for (let i = 0; i < 2; i++) {
    sleep(thinkTime(5, 15));
    getUser(1, token);
  }

  // Session end - longer pause before next session
  sleep(thinkTime(10, 30));
}

export function teardown(data) {
  console.log('Soak test completed.');
  console.log(`Start time: ${data.startTime}`);
  console.log(`End time: ${new Date().toISOString()}`);
  console.log(`Total iterations: ${iterationCount}`);

  // Final health check
  const health = healthCheck();
  console.log(`Final health status: ${health.status}`);

  console.log('\nCheck for:');
  console.log('- Memory growth over time');
  console.log('- Response time degradation');
  console.log('- Connection pool exhaustion');
  console.log('- Database connection leaks');
}

/**
 * Stress Test
 *
 * Purpose: Find system breaking point
 * Duration: ~23 minutes
 * VUs: Ramp to 500
 *
 * Run: k6 run scenarios/stress.js
 */
import { sleep } from 'k6';
import { login, getUser } from '../lib/api.js';
import { TEST_USER } from '../config/environments.js';
import { stressThresholds } from '../config/thresholds.js';

export const options = {
  stages: [
    { duration: '2m', target: 100 },   // Ramp to baseline
    { duration: '3m', target: 100 },   // Hold baseline
    { duration: '2m', target: 200 },   // Push higher
    { duration: '3m', target: 200 },   // Hold
    { duration: '2m', target: 300 },   // More stress
    { duration: '3m', target: 300 },   // Hold
    { duration: '2m', target: 500 },   // Peak stress
    { duration: '3m', target: 500 },   // Hold peak
    { duration: '3m', target: 0 },     // Recovery
  ],
  thresholds: stressThresholds,
};

export function setup() {
  console.log('Starting stress test...');
  console.log('WARNING: This test will push the system to its limits.');
  console.log(`Test user: ${TEST_USER.email}`);

  return { testUser: TEST_USER };
}

export default function (data) {
  const { testUser } = data;

  // Rapid fire login attempts
  const { response: loginRes, success } = login(testUser.email, testUser.password);

  if (success) {
    const token = loginRes.json('token');

    // Quick profile fetch
    sleep(0.3);
    getUser(1, token);
  }

  // Minimal delay between iterations
  sleep(0.5);
}

export function teardown(data) {
  console.log('Stress test completed.');
  console.log('Check results for degradation patterns.');
}

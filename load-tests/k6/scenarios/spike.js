/**
 * Spike Test
 *
 * Purpose: Test system behavior under sudden traffic spike
 * Duration: ~7 minutes
 * VUs: 10 -> 500 -> 10
 *
 * Run: k6 run scenarios/spike.js
 */
import { sleep } from 'k6';
import { login, healthCheck } from '../lib/api.js';
import { TEST_USER } from '../config/environments.js';
import { spikeThresholds } from '../config/thresholds.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },    // Normal load baseline
    { duration: '10s', target: 500 },  // SPIKE! Sudden traffic surge
    { duration: '2m', target: 500 },   // Stay at spike level
    { duration: '10s', target: 10 },   // Traffic drops suddenly
    { duration: '2m', target: 10 },    // Recovery period
    { duration: '30s', target: 0 },    // Wind down
  ],
  thresholds: spikeThresholds,
};

export function setup() {
  console.log('Starting spike test...');
  console.log('This simulates a sudden traffic surge (e.g., viral event, news mention)');
  console.log(`Test user: ${TEST_USER.email}`);

  // Verify system is healthy before spike
  const health = healthCheck();
  if (health.status !== 200) {
    throw new Error('System not healthy before spike test');
  }

  return { testUser: TEST_USER };
}

export default function (data) {
  const { testUser } = data;

  // Just login - simulating burst of users trying to access
  login(testUser.email, testUser.password);

  // Very short delay during spike
  sleep(0.1);
}

export function teardown(data) {
  console.log('Spike test completed.');
  console.log('Review how system behaved during and after the spike.');

  // Final health check
  const health = healthCheck();
  if (health.status === 200) {
    console.log('System recovered successfully.');
  } else {
    console.log('WARNING: System may not have fully recovered.');
  }
}

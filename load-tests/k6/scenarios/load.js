/**
 * Load Test
 *
 * Purpose: Validate system under expected load
 * Duration: ~16 minutes
 * VUs: Ramp to 100
 *
 * Run: k6 run scenarios/load.js
 */
import { sleep } from 'k6';
import { login, getUser, refreshToken } from '../lib/api.js';
import { TEST_USER } from '../config/environments.js';
import { thresholds } from '../config/thresholds.js';
import { thinkTime, weightedChoice } from '../lib/data.js';

export const options = {
  stages: [
    { duration: '2m', target: 50 },   // Ramp up to 50 users
    { duration: '5m', target: 50 },   // Stay at 50 users
    { duration: '2m', target: 100 },  // Ramp up to 100 users
    { duration: '5m', target: 100 },  // Stay at 100 users
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: thresholds,
};

// User journey distribution
const journeys = [
  { value: 'full_session', weight: 60 },
  { value: 'quick_login', weight: 25 },
  { value: 'token_refresh', weight: 15 },
];

export function setup() {
  console.log('Starting load test...');
  console.log(`Test user: ${TEST_USER.email}`);

  // Verify we can login
  const { success } = login(TEST_USER.email, TEST_USER.password);
  if (!success) {
    throw new Error('Cannot login with test user. Ensure user exists and email is verified.');
  }

  return { testUser: TEST_USER };
}

export default function (data) {
  const { testUser } = data;
  const journey = weightedChoice(journeys);

  switch (journey) {
    case 'full_session':
      fullSessionJourney(testUser);
      break;
    case 'quick_login':
      quickLoginJourney(testUser);
      break;
    case 'token_refresh':
      tokenRefreshJourney(testUser);
      break;
  }
}

/**
 * Full user session: login -> browse -> refresh -> browse
 */
function fullSessionJourney(testUser) {
  // Login
  const { response: loginRes, success } = login(testUser.email, testUser.password);

  if (!success) {
    sleep(thinkTime(1, 2));
    return;
  }

  const body = loginRes.json();
  const token = body.token;
  const refreshTkn = body.refreshToken;

  // Browse profile
  sleep(thinkTime(1, 3));
  getUser(1, token);

  // Read time
  sleep(thinkTime(2, 5));

  // Refresh token
  if (refreshTkn) {
    refreshToken(refreshTkn);
  }

  // More browsing
  sleep(thinkTime(1, 2));
  getUser(1, token);

  sleep(thinkTime(1, 2));
}

/**
 * Quick login: just authenticate and leave
 */
function quickLoginJourney(testUser) {
  login(testUser.email, testUser.password);
  sleep(thinkTime(1, 2));
}

/**
 * Token refresh: login and immediately refresh
 */
function tokenRefreshJourney(testUser) {
  const { response: loginRes, success } = login(testUser.email, testUser.password);

  if (!success) {
    sleep(thinkTime(1, 2));
    return;
  }

  const refreshTkn = loginRes.json('refreshToken');

  if (refreshTkn) {
    sleep(1);
    refreshToken(refreshTkn);
  }

  sleep(thinkTime(1, 2));
}

export function teardown(data) {
  console.log('Load test completed.');
}

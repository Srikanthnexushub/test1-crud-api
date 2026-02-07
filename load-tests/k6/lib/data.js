/**
 * Test data generators
 */

/**
 * Generate a random string of specified length
 */
export function randomString(length) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * Generate a unique email address
 */
export function generateEmail() {
  const timestamp = Date.now();
  const random = randomString(6);
  return `loadtest_${timestamp}_${random}@example.com`;
}

/**
 * Generate a secure password
 */
export function generatePassword() {
  return `Password${randomString(8)}!`;
}

/**
 * Generate a test user object
 */
export function generateUser() {
  return {
    email: generateEmail(),
    password: generatePassword(),
  };
}

/**
 * Generate multiple test users
 */
export function generateUsers(count) {
  const users = [];
  for (let i = 0; i < count; i++) {
    users.push(generateUser());
  }
  return users;
}

/**
 * Random think time between min and max seconds
 */
export function thinkTime(min = 1, max = 3) {
  return Math.random() * (max - min) + min;
}

/**
 * Pick a random item from an array
 */
export function randomChoice(array) {
  return array[Math.floor(Math.random() * array.length)];
}

/**
 * Weighted random choice
 * @param {Array<{value: any, weight: number}>} items
 */
export function weightedChoice(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let random = Math.random() * totalWeight;

  for (const item of items) {
    random -= item.weight;
    if (random <= 0) {
      return item.value;
    }
  }

  return items[items.length - 1].value;
}

/**
 * Performance thresholds (SLOs)
 */
export const thresholds = {
  // Overall response time thresholds
  http_req_duration: [
    'p(50)<100',   // 50% of requests under 100ms
    'p(95)<200',   // 95% under 200ms
    'p(99)<500',   // 99% under 500ms
  ],

  // Error rate threshold
  http_req_failed: ['rate<0.01'],  // <1% errors

  // Endpoint-specific thresholds
  'http_req_duration{name:login}': ['p(95)<200'],
  'http_req_duration{name:register}': ['p(95)<300'],
  'http_req_duration{name:get_user}': ['p(95)<100'],
  'http_req_duration{name:refresh}': ['p(95)<100'],
  'http_req_duration{name:2fa_verify}': ['p(95)<200'],
};

// Relaxed thresholds for stress tests
export const stressThresholds = {
  http_req_duration: ['p(95)<1000'],
  http_req_failed: ['rate<0.05'],
};

// Very relaxed thresholds for spike tests
export const spikeThresholds = {
  http_req_duration: ['p(95)<2000'],
  http_req_failed: ['rate<0.10'],
};

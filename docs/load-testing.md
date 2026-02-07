# Load Testing Guide

**Version**: 1.0
**Last Updated**: 2026-02-07
**Tools**: k6, Gatling

---

## Table of Contents

1. [Overview](#1-overview)
2. [Performance Targets](#2-performance-targets)
3. [Test Scenarios](#3-test-scenarios)
4. [k6 Setup & Tests](#4-k6-setup--tests)
5. [Gatling Setup & Tests](#5-gatling-setup--tests)
6. [Running Tests](#6-running-tests)
7. [Interpreting Results](#7-interpreting-results)
8. [CI/CD Integration](#8-cicd-integration)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Overview

### 1.1 Purpose

Load testing validates that the CRUD API meets performance requirements under expected and peak load conditions. This guide covers setup and execution for both k6 and Gatling.

### 1.2 When to Run Load Tests

| Trigger | Test Type | Duration |
|---------|-----------|----------|
| Before release | Full suite | 30-60 min |
| After infrastructure change | Baseline | 10 min |
| Performance investigation | Targeted | 5-15 min |
| Continuous (CI) | Smoke test | 2 min |

### 1.3 Tool Comparison

| Feature | k6 | Gatling |
|---------|-----|---------|
| Language | JavaScript | Scala/Java |
| Learning curve | Easy | Moderate |
| Resource usage | Low | Moderate |
| Cloud integration | k6 Cloud | Gatling Enterprise |
| Best for | API testing, CI/CD | Complex scenarios |

---

## 2. Performance Targets

### 2.1 Response Time SLOs

| Endpoint | p50 | p95 | p99 |
|----------|-----|-----|-----|
| POST /login | < 100ms | < 200ms | < 500ms |
| POST /register | < 150ms | < 300ms | < 600ms |
| GET /users/{id} | < 50ms | < 100ms | < 200ms |
| POST /refresh | < 50ms | < 100ms | < 200ms |
| POST /2fa/verify | < 100ms | < 200ms | < 500ms |

### 2.2 Throughput Targets

| Scenario | Target RPS | Max Error Rate |
|----------|------------|----------------|
| Normal load | 100 RPS | < 0.1% |
| Peak load | 500 RPS | < 1% |
| Stress test | 1000 RPS | < 5% |

### 2.3 Concurrency Targets

| Metric | Target |
|--------|--------|
| Concurrent users | 1,000 |
| Concurrent connections | 500 |
| Connection pool | 80% max utilization |

---

## 3. Test Scenarios

### 3.1 Scenario Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        LOAD TEST SCENARIOS                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. SMOKE TEST (2 min)                                                  │
│     • 1-5 VUs                                                           │
│     • Verify system works                                               │
│     • Run in CI pipeline                                                │
│                                                                         │
│  2. LOAD TEST (10-30 min)                                               │
│     • Ramp to 100 VUs                                                   │
│     • Sustain normal load                                               │
│     • Verify SLOs met                                                   │
│                                                                         │
│  3. STRESS TEST (15-30 min)                                             │
│     • Ramp to 500+ VUs                                                  │
│     • Find breaking point                                               │
│     • Observe degradation                                               │
│                                                                         │
│  4. SPIKE TEST (10 min)                                                 │
│     • Sudden traffic spike                                              │
│     • 10 → 500 → 10 VUs                                                 │
│     • Test auto-scaling                                                 │
│                                                                         │
│  5. SOAK TEST (2-4 hours)                                               │
│     • Constant moderate load                                            │
│     • Detect memory leaks                                               │
│     • Long-term stability                                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 User Journey Breakdown

| Journey | Weight | Steps |
|---------|--------|-------|
| New user registration | 10% | Register → Verify email → Login |
| Returning user login | 60% | Login → Browse → Logout |
| Login with 2FA | 15% | Login → 2FA verify → Browse |
| Password reset | 5% | Forgot password → Reset |
| Token refresh | 10% | Use refresh token |

---

## 4. k6 Setup & Tests

### 4.1 Installation

```bash
# macOS
brew install k6

# Linux (Debian/Ubuntu)
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
  --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | \
  sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6

# Docker
docker pull grafana/k6

# Verify installation
k6 version
```

### 4.2 Project Structure

```
/load-tests
├── k6/
│   ├── config/
│   │   ├── environments.js    # Environment configs
│   │   └── thresholds.js      # SLO thresholds
│   ├── scenarios/
│   │   ├── smoke.js           # Smoke test
│   │   ├── load.js            # Load test
│   │   ├── stress.js          # Stress test
│   │   ├── spike.js           # Spike test
│   │   └── soak.js            # Soak test
│   ├── lib/
│   │   ├── api.js             # API helper functions
│   │   ├── auth.js            # Auth utilities
│   │   └── data.js            # Test data generators
│   └── run.sh                 # Test runner script
└── results/                    # Test results output
```

### 4.3 Configuration Files

**config/environments.js**
```javascript
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
  return environments[envName];
}
```

**config/thresholds.js**
```javascript
export const thresholds = {
  // Response time thresholds
  http_req_duration: [
    'p(50)<100',   // 50% of requests under 100ms
    'p(95)<200',   // 95% under 200ms
    'p(99)<500',   // 99% under 500ms
  ],

  // Error rate threshold
  http_req_failed: ['rate<0.01'],  // <1% errors

  // Custom metrics
  'http_req_duration{name:login}': ['p(95)<200'],
  'http_req_duration{name:register}': ['p(95)<300'],
  'http_req_duration{name:get_user}': ['p(95)<100'],
};
```

### 4.4 Helper Library

**lib/api.js**
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { getEnv } from '../config/environments.js';

const env = getEnv();
const BASE_URL = `${env.baseUrl}/api/${env.apiVersion}`;

export function register(email, password) {
  const payload = JSON.stringify({ email, password });
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'register' },
  };

  const res = http.post(`${BASE_URL}/users/register`, payload, params);

  check(res, {
    'register status is 200': (r) => r.status === 200,
    'register has success': (r) => r.json('success') === true,
  });

  return res;
}

export function login(email, password) {
  const payload = JSON.stringify({ email, password });
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'login' },
  };

  const res = http.post(`${BASE_URL}/users/login`, payload, params);

  check(res, {
    'login status is 200': (r) => r.status === 200,
    'login has token': (r) => r.json('token') !== null,
  });

  return res;
}

export function getUser(userId, token) {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    tags: { name: 'get_user' },
  };

  const res = http.get(`${BASE_URL}/users/${userId}`, params);

  check(res, {
    'get user status is 200': (r) => r.status === 200,
  });

  return res;
}

export function refreshToken(refreshToken) {
  const payload = JSON.stringify({ refreshToken });
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'refresh' },
  };

  const res = http.post(`${BASE_URL}/users/refresh`, payload, params);

  check(res, {
    'refresh status is 200': (r) => r.status === 200,
    'refresh has new token': (r) => r.json('token') !== null,
  });

  return res;
}

export function verify2FA(email, code) {
  const payload = JSON.stringify({ code });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-2FA-Email': email,
    },
    tags: { name: '2fa_verify' },
  };

  const res = http.post(`${BASE_URL}/auth/2fa/verify`, payload, params);

  return res;
}
```

**lib/data.js**
```javascript
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export function generateUser() {
  const id = randomString(8);
  return {
    email: `loadtest_${id}@example.com`,
    password: `Password${id}!`,
  };
}

export function generateUsers(count) {
  const users = [];
  for (let i = 0; i < count; i++) {
    users.push(generateUser());
  }
  return users;
}
```

### 4.5 Test Scenarios

**scenarios/smoke.js**
```javascript
import { sleep } from 'k6';
import { login, getUser } from '../lib/api.js';
import { thresholds } from '../config/thresholds.js';

export const options = {
  vus: 1,
  duration: '1m',
  thresholds: thresholds,
};

// Pre-created test user
const TEST_USER = {
  email: 'loadtest@example.com',
  password: 'LoadTest123!',
};

export default function () {
  // Login
  const loginRes = login(TEST_USER.email, TEST_USER.password);

  if (loginRes.status === 200) {
    const token = loginRes.json('token');

    // Get user profile
    sleep(1);
    getUser(1, token);
  }

  sleep(1);
}
```

**scenarios/load.js**
```javascript
import { sleep, check } from 'k6';
import { login, getUser, refreshToken } from '../lib/api.js';
import { generateUser } from '../lib/data.js';
import { thresholds } from '../config/thresholds.js';

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

const TEST_USER = {
  email: 'loadtest@example.com',
  password: 'LoadTest123!',
};

export default function () {
  // Simulate user journey
  const journey = Math.random();

  if (journey < 0.7) {
    // 70%: Normal login flow
    const loginRes = login(TEST_USER.email, TEST_USER.password);

    if (loginRes.status === 200) {
      const token = loginRes.json('token');
      const refreshTkn = loginRes.json('refreshToken');

      // Browse user profile
      sleep(Math.random() * 3 + 1);
      getUser(1, token);

      // Simulate reading time
      sleep(Math.random() * 5 + 2);

      // Refresh token
      if (refreshTkn) {
        refreshToken(refreshTkn);
      }
    }
  } else {
    // 30%: Just login
    login(TEST_USER.email, TEST_USER.password);
  }

  sleep(Math.random() * 2 + 1);
}
```

**scenarios/stress.js**
```javascript
import { sleep } from 'k6';
import { login, getUser } from '../lib/api.js';
import { thresholds } from '../config/thresholds.js';

export const options = {
  stages: [
    { duration: '2m', target: 100 },   // Ramp up
    { duration: '5m', target: 100 },   // Normal load
    { duration: '2m', target: 300 },   // Push higher
    { duration: '5m', target: 300 },   // Sustain stress
    { duration: '2m', target: 500 },   // Peak stress
    { duration: '5m', target: 500 },   // Sustain peak
    { duration: '5m', target: 0 },     // Recovery
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],  // Relaxed for stress
    http_req_failed: ['rate<0.05'],     // Allow 5% errors
  },
};

const TEST_USER = {
  email: 'loadtest@example.com',
  password: 'LoadTest123!',
};

export default function () {
  const loginRes = login(TEST_USER.email, TEST_USER.password);

  if (loginRes.status === 200) {
    const token = loginRes.json('token');
    sleep(0.5);
    getUser(1, token);
  }

  sleep(0.5);
}
```

**scenarios/spike.js**
```javascript
import { sleep } from 'k6';
import { login } from '../lib/api.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },    // Baseline
    { duration: '10s', target: 500 },  // Spike!
    { duration: '2m', target: 500 },   // Stay at spike
    { duration: '10s', target: 10 },   // Scale down
    { duration: '2m', target: 10 },    // Recovery
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // Allow degradation
    http_req_failed: ['rate<0.10'],     // Allow 10% errors
  },
};

const TEST_USER = {
  email: 'loadtest@example.com',
  password: 'LoadTest123!',
};

export default function () {
  login(TEST_USER.email, TEST_USER.password);
  sleep(0.1);
}
```

**scenarios/soak.js**
```javascript
import { sleep } from 'k6';
import { login, getUser, refreshToken } from '../lib/api.js';
import { thresholds } from '../config/thresholds.js';

export const options = {
  stages: [
    { duration: '5m', target: 50 },     // Ramp up
    { duration: '2h', target: 50 },     // Soak for 2 hours
    { duration: '5m', target: 0 },      // Ramp down
  ],
  thresholds: thresholds,
};

const TEST_USER = {
  email: 'loadtest@example.com',
  password: 'LoadTest123!',
};

export default function () {
  const loginRes = login(TEST_USER.email, TEST_USER.password);

  if (loginRes.status === 200) {
    const token = loginRes.json('token');
    const refreshTkn = loginRes.json('refreshToken');

    // Simulate browsing session
    for (let i = 0; i < 5; i++) {
      sleep(Math.random() * 10 + 5);
      getUser(1, token);
    }

    // Refresh token
    if (refreshTkn) {
      refreshToken(refreshTkn);
    }
  }

  sleep(Math.random() * 30 + 10);
}
```

### 4.6 Run Script

**run.sh**
```bash
#!/bin/bash

# k6 Load Test Runner
# Usage: ./run.sh [scenario] [environment]

SCENARIO=${1:-smoke}
ENV=${2:-local}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_DIR="../results/k6"

mkdir -p $OUTPUT_DIR

echo "Running k6 $SCENARIO test against $ENV environment..."

k6 run \
  --env TARGET_ENV=$ENV \
  --out json=$OUTPUT_DIR/${SCENARIO}_${TIMESTAMP}.json \
  --summary-export=$OUTPUT_DIR/${SCENARIO}_${TIMESTAMP}_summary.json \
  scenarios/${SCENARIO}.js

echo "Results saved to $OUTPUT_DIR"
```

---

## 5. Gatling Setup & Tests

### 5.1 Installation

```bash
# Download Gatling
curl -L https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.5/gatling-charts-highcharts-bundle-3.9.5.zip -o gatling.zip
unzip gatling.zip
export GATLING_HOME=$(pwd)/gatling-charts-highcharts-bundle-3.9.5

# Or use Maven plugin (recommended for Java projects)
# Add to pom.xml - see below

# Verify installation
$GATLING_HOME/bin/gatling.sh --help
```

### 5.2 Maven Integration

Add to `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>io.gatling.highcharts</groupId>
        <artifactId>gatling-charts-highcharts</artifactId>
        <version>3.9.5</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.gatling</groupId>
        <artifactId>gatling-core-java</artifactId>
        <version>3.9.5</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.gatling</groupId>
            <artifactId>gatling-maven-plugin</artifactId>
            <version>4.3.7</version>
            <configuration>
                <simulationClass>simulations.LoadSimulation</simulationClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 5.3 Project Structure

```
/src/test/java/
├── simulations/
│   ├── SmokeSimulation.java
│   ├── LoadSimulation.java
│   ├── StressSimulation.java
│   └── SpikeSimulation.java
├── scenarios/
│   ├── AuthScenario.java
│   ├── UserScenario.java
│   └── TwoFactorScenario.java
├── config/
│   └── TestConfig.java
└── utils/
    └── DataGenerator.java
```

### 5.4 Configuration

**config/TestConfig.java**
```java
package config;

public class TestConfig {

    public static String getBaseUrl() {
        String env = System.getProperty("env", "local");
        return switch (env) {
            case "docker" -> "http://localhost:8080";
            case "staging" -> "https://staging-api.example.com";
            case "production" -> "https://api.example.com";
            default -> "http://localhost:8080";
        };
    }

    public static String getApiPath() {
        return "/api/v1";
    }

    // Test user (pre-created)
    public static final String TEST_EMAIL = "loadtest@example.com";
    public static final String TEST_PASSWORD = "LoadTest123!";

    // Thresholds
    public static final int RESPONSE_TIME_P95 = 200;
    public static final int RESPONSE_TIME_P99 = 500;
    public static final double MAX_ERROR_RATE = 0.01;
}
```

**utils/DataGenerator.java**
```java
package utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DataGenerator {

    public static String randomEmail() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return "loadtest_" + id + "@example.com";
    }

    public static String randomPassword() {
        return "Password" + ThreadLocalRandom.current().nextInt(10000, 99999) + "!";
    }

    public static int randomThinkTime(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
```

### 5.5 Scenarios

**scenarios/AuthScenario.java**
```java
package scenarios;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import config.TestConfig;

public class AuthScenario {

    public static ChainBuilder login = exec(
        http("Login")
            .post("/users/login")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                    "email": "${email}",
                    "password": "${password}"
                }
                """))
            .check(status().is(200))
            .check(jsonPath("$.token").saveAs("token"))
            .check(jsonPath("$.refreshToken").saveAs("refreshToken"))
    );

    public static ChainBuilder register = exec(
        http("Register")
            .post("/users/register")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                    "email": "${newEmail}",
                    "password": "${newPassword}"
                }
                """))
            .check(status().in(200, 409))  // 409 if exists
    );

    public static ChainBuilder refreshToken = exec(
        http("Refresh Token")
            .post("/users/refresh")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                    "refreshToken": "${refreshToken}"
                }
                """))
            .check(status().is(200))
            .check(jsonPath("$.token").saveAs("token"))
    );

    public static ChainBuilder forgotPassword = exec(
        http("Forgot Password")
            .post("/users/forgot-password")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                    "email": "${email}"
                }
                """))
            .check(status().is(200))
    );
}
```

**scenarios/UserScenario.java**
```java
package scenarios;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UserScenario {

    public static ChainBuilder getUser = exec(
        http("Get User")
            .get("/users/${userId}")
            .header("Authorization", "Bearer ${token}")
            .check(status().is(200))
            .check(jsonPath("$.id").exists())
    );

    public static ChainBuilder getAllUsers = exec(
        http("Get All Users")
            .get("/users")
            .header("Authorization", "Bearer ${token}")
            .check(status().in(200, 403))  // 403 if not admin
    );

    public static ChainBuilder updateUser = exec(
        http("Update User")
            .put("/users/${userId}")
            .header("Authorization", "Bearer ${token}")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                    "email": "${email}"
                }
                """))
            .check(status().in(200, 403))
    );
}
```

### 5.6 Simulations

**simulations/SmokeSimulation.java**
```java
package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import config.TestConfig;
import scenarios.AuthScenario;
import scenarios.UserScenario;

public class SmokeSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(TestConfig.getBaseUrl() + TestConfig.getApiPath())
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder smokeScenario = scenario("Smoke Test")
        .exec(session -> session
            .set("email", TestConfig.TEST_EMAIL)
            .set("password", TestConfig.TEST_PASSWORD)
            .set("userId", "1"))
        .exec(AuthScenario.login)
        .pause(1)
        .exec(UserScenario.getUser)
        .pause(1);

    {
        setUp(
            smokeScenario.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(500),
            global().failedRequests().percent().lt(1.0)
        );
    }
}
```

**simulations/LoadSimulation.java**
```java
package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

import config.TestConfig;
import scenarios.AuthScenario;
import scenarios.UserScenario;

public class LoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(TestConfig.getBaseUrl() + TestConfig.getApiPath())
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .shareConnections();

    // Standard user journey
    ScenarioBuilder standardJourney = scenario("Standard User Journey")
        .exec(session -> session
            .set("email", TestConfig.TEST_EMAIL)
            .set("password", TestConfig.TEST_PASSWORD)
            .set("userId", "1"))
        .exec(AuthScenario.login)
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(UserScenario.getUser)
        .pause(Duration.ofSeconds(2), Duration.ofSeconds(5))
        .exec(AuthScenario.refreshToken)
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    // Quick login check
    ScenarioBuilder quickLogin = scenario("Quick Login")
        .exec(session -> session
            .set("email", TestConfig.TEST_EMAIL)
            .set("password", TestConfig.TEST_PASSWORD))
        .exec(AuthScenario.login)
        .pause(Duration.ofSeconds(1));

    {
        setUp(
            standardJourney.injectOpen(
                rampUsers(50).during(Duration.ofMinutes(2)),
                constantUsersPerSec(10).during(Duration.ofMinutes(5)),
                rampUsers(50).during(Duration.ofMinutes(2)),
                constantUsersPerSec(20).during(Duration.ofMinutes(5)),
                rampUsersPerSec(20).to(0).during(Duration.ofMinutes(2))
            ),
            quickLogin.injectOpen(
                constantUsersPerSec(5).during(Duration.ofMinutes(14))
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(TestConfig.RESPONSE_TIME_P95),
            global().responseTime().percentile4().lt(TestConfig.RESPONSE_TIME_P99),
            global().failedRequests().percent().lt(TestConfig.MAX_ERROR_RATE * 100)
        );
    }
}
```

**simulations/StressSimulation.java**
```java
package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

import config.TestConfig;
import scenarios.AuthScenario;
import scenarios.UserScenario;

public class StressSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(TestConfig.getBaseUrl() + TestConfig.getApiPath())
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .shareConnections();

    ScenarioBuilder stressScenario = scenario("Stress Test")
        .exec(session -> session
            .set("email", TestConfig.TEST_EMAIL)
            .set("password", TestConfig.TEST_PASSWORD)
            .set("userId", "1"))
        .exec(AuthScenario.login)
        .pause(Duration.ofMillis(500))
        .exec(UserScenario.getUser)
        .pause(Duration.ofMillis(500));

    {
        setUp(
            stressScenario.injectOpen(
                // Ramp up
                rampUsers(100).during(Duration.ofMinutes(2)),
                constantUsersPerSec(50).during(Duration.ofMinutes(3)),
                // Push harder
                rampUsers(200).during(Duration.ofMinutes(2)),
                constantUsersPerSec(100).during(Duration.ofMinutes(3)),
                // Peak stress
                rampUsers(200).during(Duration.ofMinutes(2)),
                constantUsersPerSec(150).during(Duration.ofMinutes(3)),
                // Cool down
                rampUsersPerSec(150).to(0).during(Duration.ofMinutes(3))
            )
        ).protocols(httpProtocol)
        .assertions(
            // Relaxed thresholds for stress test
            global().responseTime().percentile3().lt(1000),
            global().failedRequests().percent().lt(5.0)
        );
    }
}
```

---

## 6. Running Tests

### 6.1 Prerequisites

```bash
# 1. Start the application
docker-compose up -d --build

# 2. Wait for health check
curl http://localhost:8080/actuator/health

# 3. Create test user (if not exists)
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"loadtest@example.com","password":"LoadTest123!"}'

# 4. Verify email (manually in DB for testing)
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "UPDATE users SET email_verified = true WHERE email = 'loadtest@example.com';"
```

### 6.2 Running k6 Tests

```bash
cd load-tests/k6

# Smoke test
k6 run scenarios/smoke.js

# Load test
k6 run scenarios/load.js

# Stress test
k6 run scenarios/stress.js

# Spike test
k6 run scenarios/spike.js

# With custom environment
k6 run --env TARGET_ENV=staging scenarios/load.js

# With output to file
k6 run --out json=results.json scenarios/load.js

# With InfluxDB output (for Grafana)
k6 run --out influxdb=http://localhost:8086/k6 scenarios/load.js
```

### 6.3 Running Gatling Tests

```bash
# Using Maven
mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation

# With environment
mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation -Denv=staging

# Using Gatling bundle
$GATLING_HOME/bin/gatling.sh -s simulations.LoadSimulation
```

### 6.4 Docker-based Execution

```bash
# k6 in Docker
docker run --rm -i \
  --network host \
  -v $(pwd)/load-tests/k6:/scripts \
  grafana/k6 run /scripts/scenarios/load.js

# With environment
docker run --rm -i \
  --network host \
  -v $(pwd)/load-tests/k6:/scripts \
  -e TARGET_ENV=docker \
  grafana/k6 run /scripts/scenarios/load.js
```

---

## 7. Interpreting Results

### 7.1 k6 Output Metrics

```
          /\      |‾‾| /‾‾/   /‾‾/
     /\  /  \     |  |/  /   /  /
    /  \/    \    |     (   /   ‾‾\
   /          \   |  |\  \ |  (‾)  |
  / __________ \  |__| \__\ \_____/ .io

  execution: local
     script: scenarios/load.js
     output: -

  scenarios: (100.00%) 1 scenario, 100 max VUs, 16m30s max duration
           * default: Up to 100 looping VUs for 16m0s

running (16m00.0s), 000/100 VUs, 12847 complete and 0 interrupted
default ✓ [======================================] 000/100 VUs  16m0s

     ✓ login status is 200
     ✓ login has token
     ✓ get user status is 200

     checks.........................: 100.00% ✓ 38541     ✗ 0
     data_received..................: 15 MB   16 kB/s
     data_sent......................: 4.2 MB  4.4 kB/s
     http_req_blocked...............: avg=1.2ms   min=0s      med=0s     max=124ms  p(90)=0s     p(95)=0s
     http_req_connecting............: avg=0.8ms   min=0s      med=0s     max=98ms   p(90)=0s     p(95)=0s
   ✓ http_req_duration..............: avg=45.2ms  min=5ms     med=32ms   max=890ms  p(90)=89ms   p(95)=142ms
       { name:get_user }............: avg=28.5ms  min=5ms     med=22ms   max=456ms  p(90)=52ms   p(95)=78ms
       { name:login }...............: avg=62.1ms  min=12ms    med=48ms   max=890ms  p(90)=125ms  p(95)=189ms
   ✓ http_req_failed................: 0.00%   ✓ 0         ✗ 25694
     http_req_receiving.............: avg=0.2ms   min=0s      med=0s     max=45ms   p(90)=0s     p(95)=1ms
     http_req_sending...............: avg=0.1ms   min=0s      med=0s     max=32ms   p(90)=0s     p(95)=0s
     http_reqs......................: 25694   26.76/s
     iteration_duration.............: avg=3.7s    min=2.1s    med=3.5s   max=8.2s   p(90)=5.2s   p(95)=5.8s
     iterations.....................: 12847   13.38/s
     vus............................: 1       min=1       max=100
     vus_max........................: 100     min=100     max=100
```

### 7.2 Key Metrics Explained

| Metric | Description | Target |
|--------|-------------|--------|
| `http_req_duration` | Total request time | p95 < 200ms |
| `http_req_failed` | Failed request rate | < 1% |
| `http_reqs` | Total requests / RPS | Based on load |
| `checks` | Passed assertions | 100% |
| `vus` | Virtual users | As configured |

### 7.3 Gatling Report

Gatling generates HTML reports automatically:

```
================================================================================
---- Global Information --------------------------------------------------------
> request count                                      25694 (OK=25694 KO=0)
> min response time                                      5 (OK=5     KO=-)
> max response time                                    890 (OK=890   KO=-)
> mean response time                                    45 (OK=45    KO=-)
> std deviation                                         62 (OK=62    KO=-)
> response time 50th percentile                         32 (OK=32    KO=-)
> response time 75th percentile                         56 (OK=56    KO=-)
> response time 95th percentile                        142 (OK=142   KO=-)
> response time 99th percentile                        285 (OK=285   KO=-)
> mean requests/sec                                  26.76 (OK=26.76 KO=-)
---- Response Time Distribution ------------------------------------------------
> t < 100 ms                                         22150 ( 86%)
> 100 ms < t < 500 ms                                 3425 ( 13%)
> t > 500 ms                                           119 (  0%)
> failed                                                 0 (  0%)
================================================================================
```

### 7.4 Performance Analysis Checklist

| Check | Pass Criteria | Action if Failed |
|-------|---------------|------------------|
| p95 response time | < 200ms | Profile slow queries |
| Error rate | < 1% | Check logs for errors |
| Throughput stable | No degradation over time | Check connection pool |
| Memory usage | No growth trend | Check for leaks |
| CPU usage | < 80% sustained | Scale up or optimize |

---

## 8. CI/CD Integration

### 8.1 GitHub Actions (k6)

```yaml
# .github/workflows/load-test.yml
name: Load Test

on:
  push:
    branches: [main]
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM

jobs:
  load-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: cruddb
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: root
        ports:
          - 5432:5432
        options: --health-cmd pg_isready --health-interval 10s

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build and start app
        run: |
          ./mvnw package -DskipTests
          java -jar target/*.jar &
          sleep 30

      - name: Setup test user
        run: |
          curl -X POST http://localhost:8080/api/v1/users/register \
            -H "Content-Type: application/json" \
            -d '{"email":"loadtest@example.com","password":"LoadTest123!"}'

          # Verify email in DB
          PGPASSWORD=root psql -h localhost -U postgres -d cruddb -c \
            "UPDATE users SET email_verified = true WHERE email = 'loadtest@example.com';"

      - name: Install k6
        run: |
          sudo gpg -k
          sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
            --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
          echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | \
            sudo tee /etc/apt/sources.list.d/k6.list
          sudo apt-get update && sudo apt-get install k6

      - name: Run smoke test
        run: k6 run load-tests/k6/scenarios/smoke.js

      - name: Upload results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: k6-results
          path: load-tests/results/
```

### 8.2 GitLab CI (Gatling)

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - load-test

load-test:
  stage: load-test
  image: maven:3.9-eclipse-temurin-17
  services:
    - postgres:15-alpine
  variables:
    POSTGRES_DB: cruddb
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: root
  script:
    - mvn package -DskipTests
    - java -jar target/*.jar &
    - sleep 30
    - mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation
  artifacts:
    paths:
      - target/gatling/
    expire_in: 7 days
  only:
    - main
    - schedules
```

---

## 9. Troubleshooting

### 9.1 Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Connection refused | App not running | Check docker-compose, wait for health |
| 401 on all requests | Invalid test user | Create and verify test user |
| Rate limit hit | 100 req/min limit | Reduce VUs or add think time |
| High error rate | Database connection pool | Increase pool size |
| Memory issues | Too many VUs | Reduce VUs or use distributed |

### 9.2 Debugging Commands

```bash
# Check application logs during test
docker-compose logs -f app

# Monitor resource usage
docker stats

# Check database connections
docker exec crud_postgres_db psql -U postgres -d cruddb -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname = 'cruddb';"

# Check HikariCP metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Monitor request rate
watch -n1 'curl -s http://localhost:8080/actuator/metrics/http.server.requests | jq'
```

### 9.3 Performance Tuning

```properties
# application.properties adjustments for load testing

# Increase connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10

# Increase server threads
server.tomcat.threads.max=400
server.tomcat.threads.min-spare=50

# Increase rate limit for testing
# (or disable temporarily)
```

---

## Appendix: Quick Reference

### k6 Commands

```bash
k6 run script.js                    # Run test
k6 run --vus 10 --duration 30s     # Override options
k6 run --out json=out.json         # Export results
k6 run --out influxdb=http://...   # Send to InfluxDB
k6 cloud script.js                  # Run in k6 Cloud
```

### Gatling Commands

```bash
mvn gatling:test                                    # Run all simulations
mvn gatling:test -Dgatling.simulationClass=...     # Run specific simulation
$GATLING_HOME/bin/gatling.sh                       # Interactive mode
$GATLING_HOME/bin/gatling.sh -s SimulationClass    # Run specific simulation
```

---

*This guide should be updated as performance requirements and testing strategies evolve.*

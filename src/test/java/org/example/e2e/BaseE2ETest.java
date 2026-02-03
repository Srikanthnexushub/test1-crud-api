package org.example.e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.example.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for Playwright E2E API tests.
 * Uses Playwright's APIRequestContext for testing REST APIs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseE2ETest {

    protected static Playwright playwright;
    protected APIRequestContext apiContext;

    @LocalServerPort
    protected int port;

    protected String baseUrl;

    @BeforeAll
    static void initPlaywright() {
        playwright = Playwright.create();
    }

    @BeforeEach
    void createApiContext() {
        baseUrl = "http://localhost:" + port;

        // Create API request context for direct API testing
        // Note: baseURL should end without trailing slash
        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
            .setBaseURL(baseUrl));
    }

    @AfterEach
    void closeApiContext() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    @AfterAll
    static void closePlaywright() {
        if (playwright != null) {
            playwright.close();
        }
    }

    /**
     * Helper method to make API POST requests
     */
    protected APIResponse apiPost(String endpoint, String jsonBody) {
        // Prepend /api if not already present
        String fullPath = endpoint.startsWith("/api") ? endpoint : "/api" + endpoint;
        return apiContext.post(fullPath,
            com.microsoft.playwright.options.RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(jsonBody));
    }

    /**
     * Helper method to make API GET requests
     */
    protected APIResponse apiGet(String endpoint) {
        String fullPath = endpoint.startsWith("/api") ? endpoint : "/api" + endpoint;
        return apiContext.get(fullPath);
    }

    /**
     * Helper method to make API PUT requests
     */
    protected APIResponse apiPut(String endpoint, String jsonBody) {
        String fullPath = endpoint.startsWith("/api") ? endpoint : "/api" + endpoint;
        return apiContext.put(fullPath,
            com.microsoft.playwright.options.RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(jsonBody));
    }

    /**
     * Helper method to make API DELETE requests
     */
    protected APIResponse apiDelete(String endpoint) {
        String fullPath = endpoint.startsWith("/api") ? endpoint : "/api" + endpoint;
        return apiContext.delete(fullPath);
    }

    /**
     * Helper method to extract JSON value from response
     */
    protected String getJsonField(APIResponse response, String field) {
        String body = new String(response.body());
        // Simple JSON parsing - for production use a JSON library
        if (body.contains("\"" + field + "\"")) {
            int startIndex = body.indexOf("\"" + field + "\":") + field.length() + 4;
            if (body.charAt(startIndex) == '"') {
                startIndex++;
                int endIndex = body.indexOf("\"", startIndex);
                return body.substring(startIndex, endIndex);
            } else {
                int endIndex = body.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = body.indexOf("}", startIndex);
                }
                return body.substring(startIndex, endIndex).trim();
            }
        }
        return null;
    }

    /**
     * Helper method to wait for API response
     */
    protected void waitForResponse(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Helper method to extract user ID from response
     */
    protected Long extractUserId(APIResponse response) {
        String idStr = getJsonField(response, "id");
        return idStr != null ? Long.parseLong(idStr) : null;
    }
}

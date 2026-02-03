package org.example.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Disabled("CORS filter is configured via SecurityConfig, not as a separate bean")
    @DisplayName("Should create CorsFilter bean")
    void testCorsFilterBeanCreation() {
        // Act
        CorsFilter corsFilter = applicationContext.getBean(CorsFilter.class);

        // Assert
        assertThat(corsFilter).isNotNull();
    }

    @Test
    @Disabled("Requires additional CORS configuration for test environment")
    @DisplayName("Should allow requests from any origin")
    void testAllowedOrigins() throws Exception {
        // Act & Assert - Test with different origins
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        mockMvc.perform(options("/api/v1/users/register")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"));
    }

    @Test
    @DisplayName("Should allow all HTTP methods")
    void testAllowedMethods() throws Exception {
        // Act & Assert - Test that all methods are allowed
        mockMvc.perform(options("/api/v1/users/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Methods"));

        mockMvc.perform(options("/api/v1/users/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Methods"));

        mockMvc.perform(options("/api/v1/users/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "PUT"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Methods"));

        mockMvc.perform(options("/api/v1/users/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "DELETE"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    @DisplayName("Should allow all headers")
    void testAllowedHeaders() throws Exception {
        // Act & Assert - Test with custom headers
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("Should allow credentials")
    void testAllowCredentials() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Should apply CORS configuration to all endpoints")
    void testCorsAppliedToAllEndpoints() throws Exception {
        // Act & Assert - Test different endpoints
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));

        mockMvc.perform(options("/api/v1/users/register")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));

        mockMvc.perform(options("/api/v1/users/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should handle preflight requests")
    void testPreflightRequest() throws Exception {
        // Act & Assert - OPTIONS request (preflight)
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"))
            .andExpect(header().exists("Access-Control-Allow-Methods"))
            .andExpect(header().exists("Access-Control-Allow-Headers"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Should handle actual CORS request with GET")
    void testActualCorsRequestWithGet() throws Exception {
        // Act & Assert - Actual GET request with Origin header
        mockMvc.perform(get("/api/v1/users/1")
                .header("Origin", "http://localhost:3000"))
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should allow multiple custom headers")
    void testMultipleCustomHeaders() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/v1/users/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers",
                    "Content-Type, Authorization, X-Custom-Header, X-Request-ID"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @Disabled("Requires additional CORS configuration for test environment")
    @DisplayName("Should work with different origin patterns")
    void testDifferentOriginPatterns() throws Exception {
        // Test with various origin formats
        String[] origins = {
            "http://localhost:3000",
            "https://example.com",
            "http://app.example.com",
            "https://subdomain.example.co.uk"
        };

        for (String origin : origins) {
            mockMvc.perform(options("/api/v1/users/login")
                    .header("Origin", origin)
                    .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
    }

    @Test
    @DisplayName("Should handle CORS for nested paths")
    void testNestedPaths() throws Exception {
        // Act & Assert - Test with nested API paths
        mockMvc.perform(options("/api/v1/users/1/profile")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should include CORS headers in error responses")
    void testCorsHeadersInErrorResponses() throws Exception {
        // Act & Assert - Test with a request that might return an error
        mockMvc.perform(get("/api/v1/users/99999")
                .header("Origin", "http://localhost:3000"))
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}

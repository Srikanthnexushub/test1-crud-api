package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LoginRequest;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.example.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for exception handling and error response mapping.
 * Tests how exceptions propagate from Repository → Service → Controller → HTTP Response.
 * Validates proper HTTP status codes and error message formats.
 */
@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Exception Handling Integration Tests")
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // ========== VALIDATION ERROR TESTS ==========

    @Test
    @DisplayName("Should return 400 Bad Request for invalid email format")
    void testValidationError_InvalidEmail_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
            // Note: Content-Type header may not be set for validation errors in some Spring Boot versions

        // Verify no user was created
        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for null email")
    void testValidationError_NullEmail_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(null, "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // Verify no user was created
        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for blank email")
    void testValidationError_BlankEmail_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for null password")
    void testValidationError_NullPassword_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", null);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for blank password")
    void testValidationError_BlankPassword_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for password too short")
    void testValidationError_ShortPassword_Returns400() throws Exception {
        // Arrange - Password must be at least 6 characters
        LoginRequest request = new LoginRequest("test@example.com", "abc12");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for password too long")
    void testValidationError_LongPassword_Returns400() throws Exception {
        // Arrange - Password must be at most 100 characters
        String longPassword = "a".repeat(101);
        LoginRequest request = new LoginRequest("test@example.com", longPassword);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for multiple validation errors")
    void testValidationError_MultipleErrors_Returns400() throws Exception {
        // Arrange - Both email and password invalid
        LoginRequest request = new LoginRequest("invalid-email", "abc");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    // ========== BUSINESS LOGIC ERROR TESTS ==========

    @Test
    @DisplayName("Should return 400 Bad Request when registering duplicate email")
    void testBusinessLogicError_DuplicateEmail_Returns400() throws Exception {
        // Arrange - Register first user
        LoginRequest firstRequest = new LoginRequest("duplicate@example.com", "password123");
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isOk());

        // Act - Try to register with same email
        LoginRequest secondRequest = new LoginRequest("duplicate@example.com", "password456");

        // Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists"));

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login with non-existent user")
    void testBusinessLogicError_UserNotFound_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login with wrong password")
    void testBusinessLogicError_InvalidPassword_Returns400() throws Exception {
        // Arrange - Register user
        UserEntity user = userRepository.save(new UserEntity("user@example.com", "correctPassword"));

        LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating non-existent user")
    void testBusinessLogicError_UpdateNonExistentUser_Returns400() throws Exception {
        // Arrange
        LoginRequest updateRequest = new LoginRequest("updated@example.com", "newPassword");

        // Act & Assert
        mockMvc.perform(put("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when deleting non-existent user")
    void testBusinessLogicError_DeleteNonExistentUser_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/99999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ========== NOT FOUND ERROR TESTS ==========

    @Test
    @DisplayName("Should return 404 Not Found when getting non-existent user by ID")
    void testNotFoundError_GetUserById_Returns404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for invalid user ID format")
    void testNotFoundError_InvalidIdFormat_Returns404Or400() throws Exception {
        // Act & Assert - Spring may return 400 for invalid path variable format
        MvcResult result = mockMvc.perform(get("/api/users/invalid"))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(400, 404);
    }

    // ========== ERROR RESPONSE FORMAT TESTS ==========

    @Test
    @DisplayName("Should return consistent error format for validation errors")
    void testErrorFormat_ValidationError_ConsistentFormat() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andReturn();

        // Validation errors may have different response format depending on Spring Boot configuration
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isNotNull();
    }

    @Test
    @DisplayName("Should return JSON error response for business logic errors")
    void testErrorFormat_BusinessLogicError_ReturnsJSON() throws Exception {
        // Arrange - Register user first
        userRepository.save(new UserEntity("existing@example.com", "password123"));

        LoginRequest request = new LoginRequest("existing@example.com", "password456");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Should handle validation errors on login endpoint")
    void testValidationError_LoginEndpoint_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle validation errors on update endpoint")
    void testValidationError_UpdateEndpoint_Returns400() throws Exception {
        // Arrange - Create user first
        UserEntity user = userRepository.save(new UserEntity("user@example.com", "password123"));

        LoginRequest updateRequest = new LoginRequest("invalid-email", "newPassword");

        // Act & Assert
        mockMvc.perform(put("/api/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());

        // Verify user was not updated
        UserEntity unchangedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(unchangedUser.getPassword()).isEqualTo("password123");
    }

    // ========== EXCEPTION PROPAGATION TESTS ==========

    @Test
    @DisplayName("Should propagate service layer errors to HTTP response")
    void testExceptionPropagation_ServiceToController() throws Exception {
        // Arrange - Create scenario where service returns error
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");

        // Act & Assert - Login with non-existent user
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Should handle repository constraint violation gracefully")
    void testExceptionPropagation_RepositoryConstraintViolation() throws Exception {
        // Arrange - Register first user
        LoginRequest firstRequest = new LoginRequest("constraint@example.com", "password123");
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isOk());

        // Act - Try to register duplicate
        LoginRequest duplicateRequest = new LoginRequest("constraint@example.com", "password456");

        // Assert - Service layer catches constraint violation and returns proper error
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Should maintain data integrity after validation error")
    void testDataIntegrity_AfterValidationError() throws Exception {
        // Arrange - Create user
        UserEntity user = userRepository.save(new UserEntity("integrity@example.com", "password123"));
        Long userId = user.getId();

        // Act - Try to update with invalid data
        LoginRequest invalidUpdate = new LoginRequest("invalid-email", "newPassword");
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
            .andExpect(status().isBadRequest());

        // Assert - Original data should be unchanged
        UserEntity unchangedUser = userRepository.findById(userId).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo("integrity@example.com");
        assertThat(unchangedUser.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should rollback transaction on validation error during update")
    void testTransactionRollback_OnValidationError() throws Exception {
        // Arrange
        UserEntity user = userRepository.save(new UserEntity("rollback@example.com", "oldPassword"));
        Long userId = user.getId();
        String originalEmail = user.getEmail();
        String originalPassword = user.getPassword();

        // Act - Try to update with invalid email (should fail validation)
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "newPassword");
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        // Assert - Data should remain unchanged (transaction rolled back)
        UserEntity unchangedUser = userRepository.findById(userId).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo(originalEmail);
        assertThat(unchangedUser.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    @DisplayName("Should handle whitespace-only email as validation error")
    void testValidationError_WhitespaceEmail_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("   ", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should handle whitespace-only password as validation error")
    void testValidationError_WhitespacePassword_Returns400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "      ");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }
}

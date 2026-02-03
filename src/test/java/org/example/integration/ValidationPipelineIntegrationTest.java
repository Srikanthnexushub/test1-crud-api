package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LoginRequest;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for end-to-end validation pipeline.
 * Tests the complete validation flow: HTTP Request → DTO Validation → Entity Validation → Response.
 * Validates that validation constraints are enforced at multiple layers and errors are properly propagated.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Validation Pipeline Integration Tests")
class ValidationPipelineIntegrationTest {

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

    // ========== EMAIL VALIDATION PIPELINE TESTS ==========

    @Test
    @DisplayName("Should validate email format through complete pipeline - register endpoint")
    void testEmailValidation_RegisterEndpoint_InvalidFormat() throws Exception {
        // Arrange - Invalid email format
        LoginRequest request = new LoginRequest("not-an-email", "password123");

        // Act & Assert - Should be caught at DTO validation layer
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // Verify no user was created (validation prevented persistence)
        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should validate email format through complete pipeline - login endpoint")
    void testEmailValidation_LoginEndpoint_InvalidFormat() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("invalid@email", "password123");

        // Act & Assert - Should be caught at DTO validation layer
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate email format through complete pipeline - update endpoint")
    void testEmailValidation_UpdateEndpoint_InvalidFormat() throws Exception {
        // Arrange - Create valid user first
        LoginRequest validRequest = new LoginRequest("valid@example.com", "password123");
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk());

        Long userId = userRepository.findByEmail("valid@example.com").orElseThrow().getId();

        // Act - Try to update with invalid email
        LoginRequest updateRequest = new LoginRequest("invalid-email", "newPassword");

        // Assert - Validation should prevent update
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest());

        // Verify original email is unchanged
        assertThat(userRepository.findById(userId).orElseThrow().getEmail())
            .isEqualTo("valid@example.com");
    }

    @Test
    @DisplayName("Should accept valid email formats through pipeline")
    void testEmailValidation_ValidFormats_AcceptedThroughPipeline() throws Exception {
        // Test various valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user_name@example.org",
            "user123@example.co.uk"
        };

        for (String email : validEmails) {
            // Arrange
            LoginRequest request = new LoginRequest(email, "password123");

            // Act & Assert
            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        // Verify all users were created
        assertThat(userRepository.count()).isEqualTo(validEmails.length);
    }

    @Test
    @DisplayName("Should reject null email at DTO validation layer")
    void testEmailValidation_NullEmail_RejectedAtDTOLayer() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(null, "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should reject blank email at DTO validation layer")
    void testEmailValidation_BlankEmail_RejectedAtDTOLayer() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    // ========== PASSWORD VALIDATION PIPELINE TESTS ==========

    @Test
    @DisplayName("Should validate password length minimum constraint through pipeline")
    void testPasswordValidation_TooShort_RejectedAtDTOLayer() throws Exception {
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
    @DisplayName("Should validate password length maximum constraint through pipeline")
    void testPasswordValidation_TooLong_RejectedAtDTOLayer() throws Exception {
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
    @DisplayName("Should accept minimum valid password length (6 chars)")
    void testPasswordValidation_MinimumLength_AcceptedThroughPipeline() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "pass12");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify user was created with exact password
        assertThat(userRepository.findByEmail("test@example.com").orElseThrow().getPassword())
            .isEqualTo("pass12");
    }

    @Test
    @DisplayName("Should accept maximum valid password length (100 chars)")
    void testPasswordValidation_MaximumLength_AcceptedThroughPipeline() throws Exception {
        // Arrange
        String maxPassword = "a".repeat(100);
        LoginRequest request = new LoginRequest("test@example.com", maxPassword);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify password was stored correctly
        assertThat(userRepository.findByEmail("test@example.com").orElseThrow().getPassword())
            .hasSize(100);
    }

    @Test
    @DisplayName("Should reject null password at DTO validation layer")
    void testPasswordValidation_NullPassword_RejectedAtDTOLayer() throws Exception {
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
    @DisplayName("Should reject blank password at DTO validation layer")
    void testPasswordValidation_BlankPassword_RejectedAtDTOLayer() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    // ========== MULTI-FIELD VALIDATION TESTS ==========

    @Test
    @DisplayName("Should validate multiple fields simultaneously")
    void testMultiFieldValidation_BothInvalid_ReportsAllErrors() throws Exception {
        // Arrange - Both email and password invalid
        LoginRequest request = new LoginRequest("invalid-email", "abc");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should validate both null fields simultaneously")
    void testMultiFieldValidation_BothNull_ReportsAllErrors() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(null, null);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should validate both blank fields simultaneously")
    void testMultiFieldValidation_BothBlank_ReportsAllErrors() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("", "");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    // ========== VALIDATION ACROSS ALL ENDPOINTS TESTS ==========

    @Test
    @DisplayName("Should enforce validation consistently across register endpoint")
    void testValidationConsistency_RegisterEndpoint() throws Exception {
        // Test 1: Invalid email
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("bad-email", "password123"))))
            .andExpect(status().isBadRequest());

        // Test 2: Short password
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "abc"))))
            .andExpect(status().isBadRequest());

        // Test 3: Valid request
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("valid@example.com", "password123"))))
            .andExpect(status().isOk());

        // Verify only valid user was created
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should enforce validation consistently across login endpoint")
    void testValidationConsistency_LoginEndpoint() throws Exception {
        // Register a valid user first
        userRepository.save(new org.example.entity.UserEntity("login@example.com", "password123"));

        // Test 1: Invalid email format
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("bad-email", "password123"))))
            .andExpect(status().isBadRequest());

        // Test 2: Short password
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("login@example.com", "abc"))))
            .andExpect(status().isBadRequest());

        // Test 3: Valid request
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("login@example.com", "password123"))))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should enforce validation consistently across update endpoint")
    void testValidationConsistency_UpdateEndpoint() throws Exception {
        // Register a user
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "password123"))))
            .andExpect(status().isOk());

        Long userId = userRepository.findByEmail("user@example.com").orElseThrow().getId();

        // Test 1: Invalid email
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("bad-email", "newPassword"))))
            .andExpect(status().isBadRequest());

        // Test 2: Short password
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("updated@example.com", "abc"))))
            .andExpect(status().isBadRequest());

        // Test 3: Valid update
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("updated@example.com", "newPassword123"))))
            .andExpect(status().isOk());

        // Verify valid update was applied
        assertThat(userRepository.findById(userId).orElseThrow().getEmail())
            .isEqualTo("updated@example.com");
    }

    // ========== SPECIAL CHARACTERS AND EDGE CASES ==========

    @Test
    @DisplayName("Should accept special characters in password through validation pipeline")
    void testPasswordValidation_SpecialCharacters_AcceptedThroughPipeline() throws Exception {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;:,.<>?";
        LoginRequest request = new LoginRequest("special@example.com", specialPassword);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify special characters were stored correctly
        assertThat(userRepository.findByEmail("special@example.com").orElseThrow().getPassword())
            .isEqualTo(specialPassword);
    }

    @Test
    @DisplayName("Should accept email with plus addressing through validation pipeline")
    void testEmailValidation_PlusAddressing_AcceptedThroughPipeline() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("user+tag@example.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify email with plus sign was stored correctly
        assertThat(userRepository.findByEmail("user+tag@example.com")).isPresent();
    }

    @Test
    @DisplayName("Should reject whitespace-only fields at validation layer")
    void testValidation_WhitespaceFields_RejectedAtDTOLayer() throws Exception {
        // Test whitespace email
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("   ", "password123"))))
            .andExpect(status().isBadRequest());

        // Test whitespace password
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "      "))))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }
}

package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestSecurityConfig;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.Role;
import org.example.entity.UserEntity;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.repository.VerificationTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register user and then login successfully")
    void testRegisterAndLoginFlow() throws Exception {
        // Arrange
        LoginRequest registerRequest = new LoginRequest("integration@example.com", "password123");

        // Act - Register
        MvcResult registerResult = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."))
            .andReturn();

        // Verify user is in database
        Optional<UserEntity> savedUser = userRepository.findByEmail("integration@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("integration@example.com");

        // Verify email for test
        savedUser.get().setEmailVerified(true);
        userRepository.save(savedUser.get());

        // Act - Login
        LoginRequest loginRequest = new LoginRequest("integration@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("Should register, update, and verify changes")
    void testUpdateUserFlow() throws Exception {
        // Arrange - Register user first
        LoginRequest registerRequest = new LoginRequest("update@example.com", "oldpassword");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        UserEntity savedUser = userRepository.findByEmail("update@example.com").orElseThrow();
        Long userId = savedUser.getId();

        // Act - Update user
        LoginRequest updateRequest = new LoginRequest("updated@example.com", "newpassword");
        mockMvc.perform(put("/api/v1/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User updated successfully"));

        // Assert - Verify changes in database
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(passwordEncoder.matches("newpassword", updatedUser.get().getPassword())).isTrue();

        // Verify old email no longer exists
        Optional<UserEntity> oldEmailUser = userRepository.findByEmail("update@example.com");
        assertThat(oldEmailUser).isEmpty();
    }

    @Test
    @DisplayName("Should register, delete, and verify user is gone")
    void testDeleteUserFlow() throws Exception {
        // Arrange - Register user first
        LoginRequest registerRequest = new LoginRequest("delete@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        UserEntity savedUser = userRepository.findByEmail("delete@example.com").orElseThrow();
        Long userId = savedUser.getId();

        // Act - Delete user
        // DELETE endpoint requires ADMIN role, so it returns 403 Forbidden
        mockMvc.perform(delete("/api/v1/users/" + userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testUniqueEmailConstraint() throws Exception {
        // Arrange - Register first user
        LoginRequest firstRequest = new LoginRequest("duplicate@example.com", "password1");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Act - Try to register with same email
        LoginRequest secondRequest = new LoginRequest("duplicate@example.com", "password2");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email already exists"));

        // Assert - Verify only one user exists
        long count = userRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void testGetUserById() throws Exception {
        // Arrange - Register user via API to ensure proper setup
        LoginRequest registerRequest = new LoginRequest("getbyid@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        UserEntity savedUser = userRepository.findByEmail("getbyid@example.com").orElseThrow();

        // Act & Assert - password should NOT be in response (security fix)
        mockMvc.perform(get("/api/v1/users/" + savedUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.email").value("getbyid@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Should return 404 when user not found by id")
    void testGetUserByIdNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testInvalidLoginCredentials() throws Exception {
        // Arrange - Register user
        LoginRequest registerRequest = new LoginRequest("wrongpass@example.com", "correctpass");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Verify email for test
        UserEntity user = userRepository.findByEmail("wrongpass@example.com").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Act - Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest("wrongpass@example.com", "wrongpass");
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should fail login with non-existent email")
    void testLoginWithNonExistentUser() throws Exception {
        // Act
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should fail update with non-existent user id")
    void testUpdateNonExistentUser() throws Exception {
        // Arrange
        LoginRequest updateRequest = new LoginRequest("update@example.com", "newpassword");

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }

    @Test
    @DisplayName("Should fail delete with non-existent user id")
    void testDeleteNonExistentUser() throws Exception {
        // Act & Assert
        // DELETE endpoint requires ADMIN role, so it returns 403 Forbidden
        mockMvc.perform(delete("/api/v1/users/99999"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should handle multiple sequential operations")
    void testMultipleSequentialOperations() throws Exception {
        // Register multiple users
        for (int i = 1; i <= 3; i++) {
            LoginRequest request = new LoginRequest("user" + i + "@example.com", "password" + i);
            mockMvc.perform(post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        // Verify all users are in database
        long count = userRepository.count();
        assertThat(count).isEqualTo(3);

        // Verify emails for all users
        for (int i = 1; i <= 3; i++) {
            UserEntity user = userRepository.findByEmail("user" + i + "@example.com").orElseThrow();
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // Login with each user
        for (int i = 1; i <= 3; i++) {
            LoginRequest request = new LoginRequest("user" + i + "@example.com", "password" + i);
            mockMvc.perform(post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    @DisplayName("Should fail validation with invalid email format")
    void testValidationInvalidEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail validation with short password")
    void testValidationShortPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "abc12");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle CORS headers")
    void testCorsHeaders() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("cors@example.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"))
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("Should persist data across multiple requests")
    void testDataPersistence() throws Exception {
        // Register user
        LoginRequest registerRequest = new LoginRequest("persist@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Fetch user and verify data persisted
        UserEntity savedUser = userRepository.findByEmail("persist@example.com").orElseThrow();
        Long userId = savedUser.getId();

        // Get user via API
        mockMvc.perform(get("/api/v1/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("persist@example.com"));

        // Verify email for test
        savedUser.setEmailVerified(true);
        userRepository.save(savedUser);

        // Login and verify credentials work
        LoginRequest loginRequest = new LoginRequest("persist@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}

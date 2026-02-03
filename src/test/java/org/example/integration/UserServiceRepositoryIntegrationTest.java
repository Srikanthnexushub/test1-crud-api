package org.example.integration;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.util.TestDataBuilder.*;

/**
 * Integration tests for UserService with real database (UserRepository).
 * Tests the interaction between Service layer and Repository layer with actual database operations.
 * Uses @DataJpaTest for real database context with H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("UserService + Repository Integration Tests")
class UserServiceRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Manually instantiate UserService with real repository
        userService = new UserService(userRepository);
        // Clean database before each test
        userRepository.deleteAll();
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    @DisplayName("Should register user successfully and persist to database")
    void testRegister_Success_PersistsToDatabase() {
        // Arrange
        LoginRequest request = createLoginRequest("newuser@example.com", "password123");

        // Act
        LoginResponse response = userService.register(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Registration successful");

        // Verify persistence in database
        Optional<UserEntity> savedUser = userRepository.findByEmail("newuser@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.get().getPassword()).isEqualTo("password123");
        assertThat(savedUser.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should fail registration when email already exists in database")
    void testRegister_DuplicateEmail_FailsWithDatabaseCheck() {
        // Arrange
        UserEntity existingUser = createUserEntity("duplicate@example.com", "password1");
        userRepository.save(existingUser);
        entityManager.flush();

        LoginRequest request = createLoginRequest("duplicate@example.com", "password2");

        // Act
        LoginResponse response = userService.register(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email already exists");

        // Verify only one user exists in database
        long count = userRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should register multiple users successfully with unique emails")
    void testRegister_MultipleUsers_AllPersisted() {
        // Arrange & Act
        LoginResponse response1 = userService.register(createLoginRequest("user1@example.com", "pass1"));
        LoginResponse response2 = userService.register(createLoginRequest("user2@example.com", "pass2"));
        LoginResponse response3 = userService.register(createLoginRequest("user3@example.com", "pass3"));

        // Assert
        assertThat(response1.isSuccess()).isTrue();
        assertThat(response2.isSuccess()).isTrue();
        assertThat(response3.isSuccess()).isTrue();

        // Verify all persisted
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user2@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user3@example.com")).isPresent();
    }

    @Test
    @DisplayName("Should handle registration with special characters in password")
    void testRegister_SpecialCharactersPassword_PersistsCorrectly() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%^&*()";
        LoginRequest request = createLoginRequest("special@example.com", specialPassword);

        // Act
        LoginResponse response = userService.register(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify special characters persisted correctly
        Optional<UserEntity> savedUser = userRepository.findByEmail("special@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).isEqualTo(specialPassword);
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Should login successfully with valid credentials from database")
    void testLogin_ValidCredentials_SuccessWithDatabaseLookup() {
        // Arrange
        UserEntity user = createUserEntity("login@example.com", "password123");
        userRepository.save(user);
        entityManager.flush();

        LoginRequest request = createLoginRequest("login@example.com", "password123");

        // Act
        LoginResponse response = userService.login(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Login successful");
    }

    @Test
    @DisplayName("Should fail login when user not found in database")
    void testLogin_UserNotFound_FailsWithDatabaseCheck() {
        // Arrange
        LoginRequest request = createLoginRequest("nonexistent@example.com", "password123");

        // Act
        LoginResponse response = userService.login(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should fail login when password does not match database record")
    void testLogin_InvalidPassword_FailsWithDatabaseComparison() {
        // Arrange
        UserEntity user = createUserEntity("user@example.com", "correctPassword");
        userRepository.save(user);
        entityManager.flush();

        LoginRequest request = createLoginRequest("user@example.com", "wrongPassword");

        // Act
        LoginResponse response = userService.login(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid password");
    }

    @Test
    @DisplayName("Should handle case-sensitive email lookup in database")
    void testLogin_CaseSensitiveEmail_HandledCorrectly() {
        // Arrange
        UserEntity user = createUserEntity("Test@Example.com", "password123");
        userRepository.save(user);
        entityManager.flush();

        // Act - try exact case
        LoginRequest requestExactCase = createLoginRequest("Test@Example.com", "password123");
        LoginResponse responseExact = userService.login(requestExactCase);

        // Assert
        assertThat(responseExact.isSuccess()).isTrue();
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    @DisplayName("Should retrieve user by ID from database")
    void testGetUserById_UserExists_RetrievesFromDatabase() {
        // Arrange
        UserEntity user = createUserEntity("getuser@example.com", "password123");
        UserEntity savedUser = userRepository.save(user);
        entityManager.flush();
        Long userId = savedUser.getId();

        // Act
        UserEntity retrievedUser = userService.getUserById(userId);

        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(userId);
        assertThat(retrievedUser.getEmail()).isEqualTo("getuser@example.com");
        assertThat(retrievedUser.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should return null when user ID not found in database")
    void testGetUserById_UserNotFound_ReturnsNull() {
        // Act
        UserEntity retrievedUser = userService.getUserById(999L);

        // Assert
        assertThat(retrievedUser).isNull();
    }

    @Test
    @DisplayName("Should retrieve correct user when multiple users exist")
    void testGetUserById_MultipleUsers_RetrievesCorrectOne() {
        // Arrange
        UserEntity user1 = userRepository.save(createUserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(createUserEntity("user2@example.com", "pass2"));
        UserEntity user3 = userRepository.save(createUserEntity("user3@example.com", "pass3"));
        entityManager.flush();

        // Act
        UserEntity retrieved = userService.getUserById(user2.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(user2.getId());
        assertThat(retrieved.getEmail()).isEqualTo("user2@example.com");
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    @DisplayName("Should update user email and persist to database")
    void testUpdateUser_UpdateEmail_PersistsToDatabase() {
        // Arrange
        UserEntity user = userRepository.save(createUserEntity("old@example.com", "password123"));
        entityManager.flush();
        Long userId = user.getId();

        LoginRequest updateRequest = createLoginRequest("new@example.com", "password123");

        // Act
        LoginResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User updated successfully");

        // Verify update persisted
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("new@example.com");

        // Verify old email no longer exists
        Optional<UserEntity> oldEmail = userRepository.findByEmail("old@example.com");
        assertThat(oldEmail).isEmpty();
    }

    @Test
    @DisplayName("Should update user password and persist to database")
    void testUpdateUser_UpdatePassword_PersistsToDatabase() {
        // Arrange
        UserEntity user = userRepository.save(createUserEntity("user@example.com", "oldPassword"));
        entityManager.flush();
        Long userId = user.getId();

        LoginRequest updateRequest = createLoginRequest("user@example.com", "newPassword");

        // Act
        LoginResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify new password persisted
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPassword()).isEqualTo("newPassword");

        // Verify can login with new password
        LoginRequest loginRequest = createLoginRequest("user@example.com", "newPassword");
        LoginResponse loginResponse = userService.login(loginRequest);
        assertThat(loginResponse.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should fail update when user ID not found in database")
    void testUpdateUser_UserNotFound_FailsWithDatabaseCheck() {
        // Arrange
        LoginRequest updateRequest = createLoginRequest("new@example.com", "newPassword");

        // Act
        LoginResponse response = userService.updateUser(999L, updateRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should update user and preserve ID in database")
    void testUpdateUser_PreservesId_InDatabase() {
        // Arrange
        UserEntity user = userRepository.save(createUserEntity("user@example.com", "password"));
        entityManager.flush();
        Long originalId = user.getId();

        LoginRequest updateRequest = createLoginRequest("updated@example.com", "newPassword");

        // Act
        userService.updateUser(originalId, updateRequest);

        // Assert - ID should remain the same
        Optional<UserEntity> updatedUser = userRepository.findById(originalId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getId()).isEqualTo(originalId);
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    // ========== DELETE USER TESTS ==========

    @Test
    @DisplayName("Should delete user from database")
    void testDeleteUser_UserExists_DeletesFromDatabase() {
        // Arrange
        UserEntity user = userRepository.save(createUserEntity("delete@example.com", "password123"));
        entityManager.flush();
        Long userId = user.getId();

        // Act
        LoginResponse response = userService.deleteUser(userId);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User deleted successfully");

        // Verify deletion from database
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();

        // Verify cannot find by email either
        Optional<UserEntity> byEmail = userRepository.findByEmail("delete@example.com");
        assertThat(byEmail).isEmpty();
    }

    @Test
    @DisplayName("Should fail delete when user not found in database")
    void testDeleteUser_UserNotFound_FailsWithDatabaseCheck() {
        // Act
        LoginResponse response = userService.deleteUser(999L);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should delete user and reduce database count")
    void testDeleteUser_ReducesDatabaseCount() {
        // Arrange
        UserEntity user1 = userRepository.save(createUserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(createUserEntity("user2@example.com", "pass2"));
        userRepository.save(createUserEntity("user3@example.com", "pass3"));
        entityManager.flush();

        assertThat(userRepository.count()).isEqualTo(3);

        // Act
        userService.deleteUser(user2.getId());

        // Assert
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(userRepository.findById(user1.getId())).isPresent();
        assertThat(userRepository.findById(user2.getId())).isEmpty();
    }

    // ========== WORKFLOW INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should handle complete user lifecycle: register -> login -> update -> delete")
    void testCompleteLifecycle_WithDatabasePersistence() {
        // Register
        LoginRequest registerRequest = createLoginRequest("lifecycle@example.com", "password123");
        LoginResponse registerResponse = userService.register(registerRequest);
        assertThat(registerResponse.isSuccess()).isTrue();

        // Verify user in database
        Optional<UserEntity> registeredUser = userRepository.findByEmail("lifecycle@example.com");
        assertThat(registeredUser).isPresent();
        Long userId = registeredUser.get().getId();

        // Login
        LoginRequest loginRequest = createLoginRequest("lifecycle@example.com", "password123");
        LoginResponse loginResponse = userService.login(loginRequest);
        assertThat(loginResponse.isSuccess()).isTrue();

        // Update
        LoginRequest updateRequest = createLoginRequest("updated@example.com", "newPassword");
        LoginResponse updateResponse = userService.updateUser(userId, updateRequest);
        assertThat(updateResponse.isSuccess()).isTrue();

        // Verify update in database
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");

        // Login with new credentials
        LoginRequest newLoginRequest = createLoginRequest("updated@example.com", "newPassword");
        LoginResponse newLoginResponse = userService.login(newLoginRequest);
        assertThat(newLoginResponse.isSuccess()).isTrue();

        // Delete
        LoginResponse deleteResponse = userService.deleteUser(userId);
        assertThat(deleteResponse.isSuccess()).isTrue();

        // Verify deletion from database
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should handle register -> failed login -> successful login workflow")
    void testRegisterAndLoginWorkflow_WithDatabaseValidation() {
        // Register
        LoginRequest registerRequest = createLoginRequest("workflow@example.com", "correctPassword");
        userService.register(registerRequest);

        // Failed login with wrong password
        LoginRequest wrongLoginRequest = createLoginRequest("workflow@example.com", "wrongPassword");
        LoginResponse failedLogin = userService.login(wrongLoginRequest);
        assertThat(failedLogin.isSuccess()).isFalse();

        // Successful login with correct password
        LoginRequest correctLoginRequest = createLoginRequest("workflow@example.com", "correctPassword");
        LoginResponse successLogin = userService.login(correctLoginRequest);
        assertThat(successLogin.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrity_AcrossMultipleOperations() {
        // Arrange - Create multiple users
        UserEntity user1 = userRepository.save(createUserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(createUserEntity("user2@example.com", "pass2"));
        entityManager.flush();

        // Act - Update user1
        LoginRequest updateRequest = createLoginRequest("user1updated@example.com", "newpass1");
        userService.updateUser(user1.getId(), updateRequest);

        // Assert - user2 should be unaffected
        Optional<UserEntity> unchangedUser = userRepository.findById(user2.getId());
        assertThat(unchangedUser).isPresent();
        assertThat(unchangedUser.get().getEmail()).isEqualTo("user2@example.com");
        assertThat(unchangedUser.get().getPassword()).isEqualTo("pass2");

        // Assert - user1 should be updated
        Optional<UserEntity> updatedUser = userRepository.findById(user1.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("user1updated@example.com");
    }

    @Test
    @DisplayName("Should handle register with minimum valid password length")
    void testRegister_MinimumPasswordLength_PersistsCorrectly() {
        // Arrange - 6 characters is minimum
        LoginRequest request = createLoginRequest("minpass@example.com", "pass12");

        // Act
        LoginResponse response = userService.register(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify persisted
        Optional<UserEntity> savedUser = userRepository.findByEmail("minpass@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).hasSize(6);
    }

    @Test
    @DisplayName("Should handle email with plus addressing in database")
    void testRegister_EmailWithPlusAddressing_PersistsCorrectly() {
        // Arrange
        LoginRequest request = createLoginRequest("user+tag@example.com", "password123");

        // Act
        LoginResponse response = userService.register(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Verify persisted with plus sign
        Optional<UserEntity> savedUser = userRepository.findByEmail("user+tag@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).contains("+");
    }
}

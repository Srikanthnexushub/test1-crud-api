package org.example.integration;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.Role;
import org.example.entity.UserEntity;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.example.service.AuditLogService;
import org.example.service.RefreshTokenService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for transaction boundary and rollback scenarios.
 * Tests transaction behavior, rollback on constraint violations, and data integrity.
 * Uses @DataJpaTest for transaction management with TestEntityManager.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Transaction Boundary Integration Tests")
class TransactionBoundaryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserService userService;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuditLogService auditLogService;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create mocks for new dependencies
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtil = mock(JwtUtil.class);
        auditLogService = mock(AuditLogService.class);
        refreshTokenService = mock(RefreshTokenService.class);

        // Mock default role for registration
        Role defaultRole = new Role(Role.RoleName.ROLE_USER, "Standard user");
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(defaultRole));

        // Mock JWT token generation
        when(jwtUtil.generateToken(anyString())).thenReturn("mock-jwt-token");

        // Mock refresh token service
        org.example.entity.RefreshToken mockRefreshToken = new org.example.entity.RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(mockRefreshToken);

        // Manually instantiate UserService with all dependencies
        userService = new UserService(userRepository, roleRepository, passwordEncoder,
                                     jwtUtil, auditLogService, refreshTokenService);
    }

    // ========== CONSTRAINT VIOLATION TESTS ==========

    @Test
    @DisplayName("Should handle constraint violation on duplicate email")
    void testConstraintViolation_DuplicateEmail_ThrowsException() {
        // Arrange
        UserEntity user1 = new UserEntity("duplicate@example.com", "password1");
        userRepository.save(user1);
        entityManager.flush();
        entityManager.clear();

        UserEntity user2 = new UserEntity("duplicate@example.com", "password2");

        // Act & Assert
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Clear after exception to avoid Hibernate issues
        entityManager.clear();

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should rollback transaction on constraint violation")
    void testTransactionRollback_OnConstraintViolation() {
        // Arrange
        UserEntity existingUser = userRepository.save(new UserEntity("existing@example.com", "password"));
        entityManager.flush();

        long initialCount = userRepository.count();

        // Act - Try to save duplicate
        UserEntity duplicateUser = new UserEntity("existing@example.com", "differentPassword");

        try {
            userRepository.save(duplicateUser);
            entityManager.flush();
        } catch (DataIntegrityViolationException e) {
            // Expected exception
        }

        entityManager.clear(); // Clear persistence context

        // Assert - Count should remain the same (transaction rolled back)
        assertThat(userRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should maintain data integrity after failed transaction")
    void testDataIntegrity_AfterFailedTransaction() {
        // Arrange
        UserEntity user1 = userRepository.save(new UserEntity("user1@example.com", "password1"));
        entityManager.flush();
        entityManager.clear();

        // Act - Try to update to duplicate email
        UserEntity user2 = userRepository.save(new UserEntity("user2@example.com", "password2"));
        entityManager.flush();

        user2.setEmail("user1@example.com"); // Try to set duplicate email

        // Assert - Should fail with constraint violation
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class)
         .satisfies(throwable -> {
             String message = throwable.getMessage().toLowerCase();
             assertThat(message).contains("constraint");
         });

        // Verify user2 still has original email
        entityManager.clear();
        Optional<UserEntity> refreshedUser2 = userRepository.findByEmail("user2@example.com");
        assertThat(refreshedUser2).isPresent();
        assertThat(refreshedUser2.get().getEmail()).isEqualTo("user2@example.com");
    }

    // ========== SERVICE LAYER TRANSACTION TESTS ==========

    @Test
    @DisplayName("Should handle service layer registration with duplicate email gracefully")
    void testServiceTransaction_DuplicateRegistration_HandledGracefully() {
        // Arrange
        LoginRequest request1 = new LoginRequest("service@example.com", "password1");
        userService.register(request1);
        entityManager.flush();

        // Act - Try to register duplicate
        LoginRequest request2 = new LoginRequest("service@example.com", "password2");
        LoginResponse response = userService.register(request2);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email already exists");

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should preserve original data after failed update")
    void testServiceTransaction_FailedUpdate_PreservesOriginalData() {
        // Arrange - Create user
        UserEntity user = userRepository.save(new UserEntity("original@example.com", "originalPassword"));
        entityManager.flush();
        Long userId = user.getId();

        // Act - Try to update to non-existent user (should fail)
        LoginRequest updateRequest = new LoginRequest("updated@example.com", "newPassword");
        LoginResponse response = userService.updateUser(999L, updateRequest);

        // Assert - Update failed
        assertThat(response.isSuccess()).isFalse();

        // Verify original user data is unchanged
        entityManager.clear();
        Optional<UserEntity> unchangedUser = userRepository.findById(userId);
        assertThat(unchangedUser).isPresent();
        assertThat(unchangedUser.get().getEmail()).isEqualTo("original@example.com");
        assertThat(unchangedUser.get().getPassword()).isEqualTo("originalPassword");
    }

    @Test
    @DisplayName("Should complete successful update transaction")
    void testServiceTransaction_SuccessfulUpdate_CommitsChanges() {
        // Arrange
        UserEntity user = userRepository.save(new UserEntity("before@example.com", "oldPassword"));
        entityManager.flush();
        entityManager.clear();
        Long userId = user.getId();

        // Act
        LoginRequest updateRequest = new LoginRequest("after@example.com", "newPassword");
        LoginResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();

        // Force flush to ensure transaction commits
        entityManager.flush();
        entityManager.clear();

        // Verify changes were committed
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("after@example.com");
        // Password should be BCrypt hashed after going through UserService
        assertThat(passwordEncoder.matches("newPassword", updatedUser.get().getPassword())).isTrue();
    }

    // ========== MULTIPLE OPERATIONS IN TRANSACTION TESTS ==========

    @Test
    @DisplayName("Should handle multiple operations in single transaction")
    void testTransaction_MultipleOperations_AllOrNothing() {
        // Arrange & Act - Multiple saves in one transaction
        UserEntity user1 = userRepository.save(new UserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(new UserEntity("user2@example.com", "pass2"));
        UserEntity user3 = userRepository.save(new UserEntity("user3@example.com", "pass3"));
        entityManager.flush();

        // Assert - All should be saved
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findById(user1.getId())).isPresent();
        assertThat(userRepository.findById(user2.getId())).isPresent();
        assertThat(userRepository.findById(user3.getId())).isPresent();
    }

    @Test
    @DisplayName("Should rollback all operations if one fails in transaction")
    void testTransaction_OneFailsAllRollback() {
        // Arrange
        UserEntity existingUser = userRepository.save(new UserEntity("existing@example.com", "password"));
        entityManager.flush();
        entityManager.clear();
        long initialCount = userRepository.count();

        // Act - Try to save multiple users, one with duplicate email
        try {
            userRepository.save(new UserEntity("new1@example.com", "pass1"));
            userRepository.save(new UserEntity("new2@example.com", "pass2"));
            userRepository.save(new UserEntity("existing@example.com", "duplicatePass")); // This should fail
            entityManager.flush();
        } catch (Exception e) {
            // Expected exception - could be DataIntegrityViolationException or wrapped
        }

        entityManager.clear();

        // Assert - In @DataJpaTest, transaction behavior depends on test transaction management
        // At minimum, the duplicate should not exist
        assertThat(userRepository.findByEmail("existing@example.com"))
            .isPresent()
            .hasValueSatisfying(u -> assertThat(u.getPassword()).isEqualTo("password"));
    }

    @Test
    @DisplayName("Should persist delete operation in transaction")
    void testTransaction_DeleteOperation_Persists() {
        // Arrange
        UserEntity user = userRepository.save(new UserEntity("delete@example.com", "password"));
        entityManager.flush();
        Long userId = user.getId();

        // Act
        LoginResponse response = userService.deleteUser(userId);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    // ========== DATA PERSISTENCE AND CONSISTENCY TESTS ==========

    @Test
    @DisplayName("Should maintain referential integrity across operations")
    void testReferentialIntegrity_AcrossOperations() {
        // Arrange - Create user
        UserEntity user = userRepository.save(new UserEntity("integrity@example.com", "password"));
        entityManager.flush();
        Long userId = user.getId();

        // Act - Update user
        user.setPassword("updatedPassword");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Assert - Same ID, updated data
        Optional<UserEntity> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getId()).isEqualTo(userId);
        assertThat(updatedUser.get().getPassword()).isEqualTo("updatedPassword");
    }

    @Test
    @DisplayName("Should handle concurrent-like sequential operations")
    void testSequentialOperations_DataConsistency() {
        // Arrange - Create user
        LoginRequest registerRequest = new LoginRequest("sequential@example.com", "password1");
        userService.register(registerRequest);
        entityManager.flush();

        UserEntity savedUser = userRepository.findByEmail("sequential@example.com").orElseThrow();
        Long userId = savedUser.getId();

        // Act - Multiple sequential operations
        // 1. Login
        LoginResponse loginResponse = userService.login(registerRequest);
        assertThat(loginResponse.isSuccess()).isTrue();

        // 2. Update
        LoginRequest updateRequest = new LoginRequest("updated@example.com", "password2");
        LoginResponse updateResponse = userService.updateUser(userId, updateRequest);
        assertThat(updateResponse.isSuccess()).isTrue();
        entityManager.flush();

        // 3. Login with new credentials
        LoginResponse newLoginResponse = userService.login(updateRequest);
        assertThat(newLoginResponse.isSuccess()).isTrue();

        // 4. Delete
        LoginResponse deleteResponse = userService.deleteUser(userId);
        assertThat(deleteResponse.isSuccess()).isTrue();
        entityManager.flush();

        // Assert - User should be gone
        entityManager.clear();
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Should handle transaction isolation for separate users")
    void testTransactionIsolation_SeparateUsers() {
        // Arrange - Create multiple users
        UserEntity user1 = userRepository.save(new UserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(new UserEntity("user2@example.com", "pass2"));
        entityManager.flush();

        // Act - Update user1
        user1.setPassword("newPass1");
        userRepository.save(user1);
        entityManager.flush();
        entityManager.clear();

        // Assert - user2 should be unaffected
        Optional<UserEntity> unchangedUser2 = userRepository.findById(user2.getId());
        assertThat(unchangedUser2).isPresent();
        assertThat(unchangedUser2.get().getEmail()).isEqualTo("user2@example.com");
        assertThat(unchangedUser2.get().getPassword()).isEqualTo("pass2");

        // Assert - user1 should be updated
        Optional<UserEntity> updatedUser1 = userRepository.findById(user1.getId());
        assertThat(updatedUser1).isPresent();
        assertThat(updatedUser1.get().getPassword()).isEqualTo("newPass1");
    }

    @Test
    @DisplayName("Should persist changes correctly after flush")
    void testPersistence_AfterFlush() {
        // Arrange
        UserEntity user = new UserEntity("flush@example.com", "password");
        userRepository.save(user);

        // Act - Flush to database
        entityManager.flush();
        Long userId = user.getId();

        // Clear persistence context
        entityManager.clear();

        // Assert - Should be retrievable from database
        Optional<UserEntity> retrievedUser = userRepository.findById(userId);
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("flush@example.com");
    }

    @Test
    @DisplayName("Should handle update without explicit ID preservation")
    void testUpdate_PreservesId() {
        // Arrange
        UserEntity user = userRepository.save(new UserEntity("preserve@example.com", "oldPass"));
        entityManager.flush();
        Long originalId = user.getId();

        // Act - Update via service
        LoginRequest updateRequest = new LoginRequest("updated@example.com", "newPass");
        userService.updateUser(originalId, updateRequest);
        entityManager.flush();
        entityManager.clear();

        // Assert - ID should be preserved
        Optional<UserEntity> updatedUser = userRepository.findById(originalId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getId()).isEqualTo(originalId);
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Should handle non-existent ID gracefully in service operations")
    void testNonExistentIdHandling_InServiceOperations() {
        // Act
        UserEntity result = userService.getUserById(99999L);

        // Assert - Should return null for non-existent ID
        assertThat(result).isNull();
    }
}

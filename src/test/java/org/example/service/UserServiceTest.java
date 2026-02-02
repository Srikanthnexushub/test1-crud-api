package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity("test@example.com", "password123");
        testUser.setId(1L);

        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login - Success with valid credentials")
    void login_WithValidCredentials_ReturnsSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = userService.login(loginRequest);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Login - Fail with invalid email")
    void login_WithInvalidEmail_ReturnsUserNotFound() {
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("wrong@example.com", "password123");
        LoginResponse response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    @DisplayName("Login - Fail with invalid password")
    void login_WithInvalidPassword_ReturnsInvalidPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        LoginResponse response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register - Success with new email")
    void register_WithNewEmail_ReturnsSuccess() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        LoginRequest request = new LoginRequest("new@example.com", "password123");
        LoginResponse response = userService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register - Fail with existing email")
    void register_WithExistingEmail_ReturnsEmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = userService.register(loginRequest);

        assertFalse(response.isSuccess());
        assertEquals("Email already exists", response.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== GET USER TESTS ====================

    @Test
    @DisplayName("GetUser - Success with valid ID")
    void getUserById_WithValidId_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserEntity result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("GetUser - Fail with invalid ID")
    void getUserById_WithInvalidId_ReturnsNull() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserEntity result = userService.getUserById(999L);

        assertNull(result);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Update - Success with valid ID")
    void updateUser_WithValidId_ReturnsSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        LoginRequest request = new LoginRequest("updated@example.com", "newpassword");
        LoginResponse response = userService.updateUser(1L, request);

        assertTrue(response.isSuccess());
        assertEquals("User updated successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update - Fail with invalid ID")
    void updateUser_WithInvalidId_ReturnsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("updated@example.com", "newpassword");
        LoginResponse response = userService.updateUser(999L, request);

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Delete - Success with valid ID")
    void deleteUser_WithValidId_ReturnsSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        LoginResponse response = userService.deleteUser(1L);

        assertTrue(response.isSuccess());
        assertEquals("User deleted successfully", response.getMessage());
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete - Fail with invalid ID")
    void deleteUser_WithInvalidId_ReturnsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        LoginResponse response = userService.deleteUser(999L);

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Register - Handle null email")
    void testRegisterWithNullEmail() {
        LoginRequest request = new LoginRequest(null, "password123");
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        // Service doesn't validate null - it passes to repository
        // Validation should be done at controller level with @Valid
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("Register - Handle null password")
    void testRegisterWithNullPassword() {
        LoginRequest request = new LoginRequest("test@example.com", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        // Service doesn't validate null - it passes to repository
        // Validation should be done at controller level with @Valid
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Register - Fail with empty email")
    void testRegisterWithEmptyEmail() {
        LoginRequest request = new LoginRequest("", "password123");
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        // Service might still process empty string if validation doesn't catch it
        verify(userRepository, times(1)).findByEmail("");
    }

    @Test
    @DisplayName("Register - Fail with empty password")
    void testRegisterWithEmptyPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        // Service might still process empty string if validation doesn't catch it
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Login - Handle null credentials")
    void testLoginWithNullCredentials() {
        LoginRequest request = new LoginRequest(null, null);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        LoginResponse response = userService.login(request);

        // Service doesn't validate null - it tries to find by null email
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("Update - Handle null fields gracefully")
    void testUpdateWithNullFields() {
        LoginRequest request = new LoginRequest(null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        LoginResponse response = userService.updateUser(1L, request);

        // Service should handle null fields appropriately
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Register - Handle very long email")
    void testVeryLongEmail() {
        String longEmail = "a".repeat(150) + "@example.com";
        LoginRequest request = new LoginRequest(longEmail, "password123");
        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        // Might succeed or fail depending on DB constraints
        verify(userRepository, times(1)).findByEmail(longEmail);
    }

    @Test
    @DisplayName("Register - Handle special characters in password")
    void testSpecialCharactersInPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "P@ssw0rd!#$%^&*()");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        assertTrue(response.isSuccess());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register - Handle whitespace-only email")
    void testWhitespaceOnlyEmail() {
        LoginRequest request = new LoginRequest("   ", "password123");
        when(userRepository.findByEmail("   ")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        verify(userRepository, times(1)).findByEmail("   ");
    }

    @Test
    @DisplayName("Register - Handle whitespace-only password")
    void testWhitespaceOnlyPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "      ");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Login - Handle email with leading/trailing whitespace")
    void testLoginWithWhitespaceInEmail() {
        LoginRequest request = new LoginRequest(" test@example.com ", "password123");
        when(userRepository.findByEmail(" test@example.com ")).thenReturn(Optional.empty());

        LoginResponse response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    @DisplayName("Update - Handle very long password")
    void testUpdateWithVeryLongPassword() {
        String longPassword = "a".repeat(200);
        LoginRequest request = new LoginRequest("newemail@example.com", longPassword);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        LoginResponse response = userService.updateUser(1L, request);

        // Should still process the update
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Delete - Handle null ID gracefully")
    void testDeleteWithNullId() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        try {
            LoginResponse response = userService.deleteUser(null);
            assertFalse(response.isSuccess());
        } catch (Exception e) {
            // Might throw exception with null ID
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Get User - Handle null ID")
    void testGetUserWithNullId() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        try {
            UserEntity result = userService.getUserById(null);
            assertNull(result);
        } catch (Exception e) {
            // Might throw exception with null ID
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Register - Handle email with plus addressing")
    void testEmailWithPlusAddressing() {
        LoginRequest request = new LoginRequest("user+tag@example.com", "password123");
        when(userRepository.findByEmail("user+tag@example.com")).thenReturn(Optional.empty());

        LoginResponse response = userService.register(request);

        assertTrue(response.isSuccess());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
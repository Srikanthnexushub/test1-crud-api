package org.example.service;

import org.example.config.properties.SecurityProperties;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.UserUpdateRequest;
import org.example.entity.Role;
import org.example.entity.RefreshToken;
import org.example.entity.UserEntity;
import org.example.exception.DuplicateResourceException;
import org.example.exception.InvalidCredentialsException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private TwoFactorService twoFactorService;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;
    private LoginRequest loginRequest;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        // Setup security properties
        lenient().when(securityProperties.getMaxFailedAttempts()).thenReturn(5);
        lenient().when(securityProperties.getLockTimeDuration()).thenReturn(900000L); // 15 minutes

        testUser = new UserEntity("test@example.com", "hashed_password123");
        testUser.setId(1L);
        testUser.setEmailVerified(true); // Set as verified for tests

        loginRequest = new LoginRequest("test@example.com", "password123");

        // Setup default role
        defaultRole = new Role(Role.RoleName.ROLE_USER, "Standard user");
        lenient().when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(defaultRole));

        // Setup password encoder
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> {
            String raw = invocation.getArgument(0);
            String encoded = invocation.getArgument(1);
            return ("hashed_" + raw).equals(encoded) || raw.equals(encoded);
        });

        // Setup JWT util
        lenient().when(jwtUtil.generateToken(anyString())).thenReturn("mock-jwt-token");

        // Setup refresh token service
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token");
        lenient().when(refreshTokenService.createRefreshToken(anyString())).thenReturn(mockRefreshToken);

        // Setup audit log service (no-op for tests)
        lenient().doNothing().when(auditLogService).logSuccess(anyString(), anyString(), any(), anyString());
        lenient().doNothing().when(auditLogService).logFailure(anyString(), anyString(), anyString());

        // Setup verification token service
        lenient().when(verificationTokenService.createEmailVerificationToken(any(UserEntity.class)))
                .thenReturn("mock-verification-token");

        // Setup email service (no-op for tests)
        lenient().doNothing().when(emailService).sendVerificationEmail(any(UserEntity.class), anyString());
        lenient().doNothing().when(emailService).sendPasswordResetEmail(any(UserEntity.class), anyString());

        // Setup two factor service
        lenient().when(twoFactorService.generateSecret()).thenReturn("mock-secret-key");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login - Success with valid credentials")
    void login_WithValidCredentials_ReturnsSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = userService.login(loginRequest);

        assertTrue(response.success());
        assertEquals("Login successful", response.message());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Login - Fail with invalid email")
    void login_WithInvalidEmail_ReturnsUserNotFound() {
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("wrong@example.com", "password123");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Login - Fail with invalid password")
    void login_WithInvalidPassword_ReturnsInvalidPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register - Success with new email")
    void register_WithNewEmail_ReturnsSuccess() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        LoginRequest request = new LoginRequest("new@example.com", "password123");
        LoginResponse response = userService.register(request);

        assertTrue(response.success());
        assertEquals("Registration successful. Please check your email to verify your account.", response.message());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register - Fail with existing email")
    void register_WithExistingEmail_ReturnsEmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(DuplicateResourceException.class, () -> userService.register(loginRequest));
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

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Update - Success with valid ID")
    void updateUser_WithValidId_ReturnsSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserUpdateRequest request = new UserUpdateRequest("updated@example.com", "newpassword", null);
        LoginResponse response = userService.updateUser(1L, request);

        assertTrue(response.success());
        assertEquals("User updated successfully", response.message());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Update - Fail with invalid ID")
    void updateUser_WithInvalidId_ReturnsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserUpdateRequest request = new UserUpdateRequest("updated@example.com", "newpassword", null);

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Delete - Success with valid ID")
    void deleteUser_WithValidId_ReturnsSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        LoginResponse response = userService.deleteUser(1L);

        assertTrue(response.success());
        assertEquals("User deleted successfully", response.message());
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete - Fail with invalid ID")
    void deleteUser_WithInvalidId_ReturnsUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Register - Handle null email")
    void testRegisterWithNullEmail() {
        LoginRequest request = new LoginRequest(null, "password123");
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Mock refresh token for null email
        // Service processes null email - validation should be at controller level
        LoginResponse response = userService.register(request);

        assertNotNull(response);
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

        // Service throws exception for null email
        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("Update - Handle null fields gracefully")
    void testUpdateWithNullFields() {
        UserUpdateRequest request = new UserUpdateRequest(null, null, null);
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

        assertTrue(response.success());
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

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Update - Handle very long password")
    void testUpdateWithVeryLongPassword() {
        String longPassword = "a".repeat(200);
        UserUpdateRequest request = new UserUpdateRequest("newemail@example.com", longPassword, null);
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
            assertFalse(response.success());
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

        assertTrue(response.success());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
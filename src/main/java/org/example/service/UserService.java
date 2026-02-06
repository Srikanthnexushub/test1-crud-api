package org.example.service;

import org.example.config.properties.SecurityProperties;
import org.example.dto.*;
import org.example.entity.Role;
import org.example.entity.UserEntity;
import org.example.entity.VerificationToken.TokenType;
import org.example.exception.*;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User service with maximum immutability:
 * - All fields are final
 * - Constructor injection only
 * - Immutable collections where possible
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Immutable service dependencies
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final TwoFactorService twoFactorService;

    // Immutable configuration values
    private final int maxFailedAttempts;
    private final long lockTimeDuration;

    // Constructor injection (no @Autowired needed - Spring 4.3+ auto-wires single constructor)
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuditLogService auditLogService,
            RefreshTokenService refreshTokenService,
            VerificationTokenService verificationTokenService,
            EmailService emailService,
            TwoFactorService twoFactorService,
            SecurityProperties securityProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
        this.maxFailedAttempts = securityProperties.getMaxFailedAttempts();
        this.lockTimeDuration = securityProperties.getLockTimeDuration();
    }

    public LoginResponse register(LoginRequest request) {
        logger.info("Registration attempt for email: {}", request.email());

        Optional<UserEntity> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            logger.warn("Registration failed - email already exists: {}", request.email());
            throw new DuplicateResourceException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(request.email(), encodedPassword);

        // Assign default ROLE_USER
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found. System initialization failed."));
        user.addRole(userRole);

        // User starts as unverified
        user.setEmailVerified(false);

        userRepository.save(user);

        // Send verification email
        String verificationToken = verificationTokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user, verificationToken);

        logger.info("User registered successfully (pending verification): {}", request.email());

        auditLogService.logSuccess("USER_REGISTER", "New user registered (pending verification): " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Registration successful. Please check your email to verify your account.");
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.email());

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", request.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Check if email is verified (blocking)
        if (!user.isEmailVerified()) {
            logger.warn("Login failed - email not verified for user: {}", request.email());
            throw new EmailNotVerifiedException("Email not verified. Please check your email and verify your account.");
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            if (unlockWhenTimeExpired(user)) {
                logger.info("Account automatically unlocked for user: {}", request.email());
            } else {
                logger.warn("Login failed - account locked for user: {}", request.email());
                throw new AccountLockedException("Account is locked due to multiple failed login attempts. Please try again later.");
            }
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", request.email());
            increaseFailedAttempts(user);
            auditLogService.logFailure("USER_LOGIN", "Failed login attempt for: " + request.email(),
                                       "Invalid credentials");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Successful login - reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            resetFailedAttempts(user);
        }

        // Check if 2FA is enabled
        if (user.isTwoFactorEnabled()) {
            // Generate a partial token that requires 2FA verification
            Map<String, Object> partialClaims = new HashMap<>();
            partialClaims.put("2fa_required", true);
            partialClaims.put("2fa_verified", false);
            partialClaims.put("partial", true);
            String partialToken = jwtUtil.generateToken(user.getEmail(), partialClaims);

            logger.info("2FA required for user: {}", request.email());
            throw new TwoFactorRequiredException("Two-factor authentication required", partialToken);
        }

        // Create immutable claims map for JWT
        Map<String, Object> claims = createJwtClaims(user.getRoles());

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
        logger.info("User logged in successfully: {}", request.email());

        auditLogService.logSuccess("USER_LOGIN", "User logged in: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Login successful", token, refreshToken);
    }

    /**
     * Creates an immutable map of JWT claims from user roles.
     * Extracted method for clarity and reusability.
     */
    private Map<String, Object> createJwtClaims(Collection<Role> roles) {
        List<String> roleNames = roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableList()); // Immutable list

        return Map.of("roles", roleNames); // Immutable map
    }

    /**
     * Increases failed login attempts and locks account if threshold exceeded.
     * Mutable operation on UserEntity (acceptable for JPA entities).
     */
    private void increaseFailedAttempts(UserEntity user) {
        int newFailAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailAttempts);

        if (newFailAttempts >= maxFailedAttempts) {
            lockUserAccount(user);
        }

        userRepository.save(user);
    }

    /**
     * Locks user account due to excessive failed attempts.
     * Extracted for clarity and single responsibility.
     */
    private void lockUserAccount(UserEntity user) {
        user.setAccountLocked(true);
        user.setLockTime(LocalDateTime.now());
        logger.warn("Account locked for user: {} after {} failed attempts",
                   user.getEmail(), user.getFailedLoginAttempts());
    }

    /**
     * Resets failed login attempts and unlocks account.
     * Extracted method for clarity.
     */
    private void resetFailedAttempts(UserEntity user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

    /**
     * Unlocks account if lock time has expired.
     * Returns true if unlock was performed.
     */
    private boolean unlockWhenTimeExpired(UserEntity user) {
        if (user.getLockTime() != null) {
            long lockTimeInMinutes = ChronoUnit.MINUTES.between(user.getLockTime(), LocalDateTime.now());
            if (lockTimeInMinutes >= lockTimeDuration) {
                resetFailedAttempts(user);
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public LoginResponse createUser(UserCreateRequest request) {
        logger.info("Admin creating user: {}", request.email());

        // Check if email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("User creation failed - email already exists: {}", request.email());
            throw new DuplicateResourceException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(request.email(), encodedPassword);

        // Assign role (default to ROLE_USER if not specified)
        Role role = determineUserRole(request.role());
        user.addRole(role);

        userRepository.save(user);
        logger.info("User created successfully by admin: {}", user.getEmail());

        auditLogService.logSuccess("USER_CREATE", "User created by admin: " + user.getEmail(),
                                    user.getId(), "ADMIN");

        return new LoginResponse(true, "User created successfully");
    }

    /**
     * Determines user role from request, with fallback to default.
     * Extracted method for role determination logic.
     */
    private Role determineUserRole(String requestedRole) {
        String roleName = (requestedRole != null && !requestedRole.isEmpty())
                ? requestedRole
                : "ROLE_USER";

        Role.RoleName roleEnum;
        try {
            roleEnum = Role.RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role specified: {}, defaulting to ROLE_USER", roleName);
            roleEnum = Role.RoleName.ROLE_USER;
        }

        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

    public LoginResponse updateUser(Long id, UserUpdateRequest request) {
        logger.info("Update attempt for user ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if email is being changed to an existing email
        if (!user.getEmail().equals(request.email())) {
            Optional<UserEntity> existingUser = userRepository.findByEmail(request.email());
            if (existingUser.isPresent()) {
                logger.warn("Update failed - email already exists: {}", request.email());
                throw new DuplicateResourceException("Email already exists");
            }
        }

        updateUserFields(user, request);
        userRepository.save(user);

        logger.info("User updated successfully: {}", id);
        auditLogService.logSuccess("USER_UPDATE", "User updated: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "User updated successfully");
    }

    /**
     * Updates user fields from request.
     * Extracted method for field update logic.
     */
    private void updateUserFields(UserEntity user, UserUpdateRequest request) {
        user.setEmail(request.email());

        // Only update password if provided
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        // Update role if provided (admin only)
        if (request.role() != null && !request.role().isEmpty()) {
            try {
                Role.RoleName roleEnum = Role.RoleName.valueOf(request.role());
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + request.role()));
                user.getRoles().clear();
                user.addRole(role);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role specified: {}", request.role());
            }
        }
    }

    public LoginResponse deleteUser(Long id) {
        logger.info("Delete attempt for user ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String userEmail = user.getEmail();
        userRepository.deleteById(id);
        logger.info("User deleted successfully: {}", id);

        auditLogService.logSuccess("USER_DELETE", "User deleted: " + userEmail,
                                    id, "USER");

        return new LoginResponse(true, "User deleted successfully");
    }

    // ==========================================
    // EMAIL VERIFICATION METHODS
    // ==========================================

    public LoginResponse verifyEmail(String token) {
        logger.info("Email verification attempt with token");

        UserEntity user = verificationTokenService.validateToken(token, TokenType.EMAIL_VERIFICATION);

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        verificationTokenService.markTokenUsed(token);

        logger.info("Email verified successfully for user: {}", user.getEmail());
        auditLogService.logSuccess("EMAIL_VERIFY", "Email verified: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Email verified successfully. You can now log in.");
    }

    public LoginResponse resendVerificationEmail(String email) {
        logger.info("Resend verification email request for: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            return new LoginResponse(true, "Email is already verified");
        }

        String verificationToken = verificationTokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user, verificationToken);

        logger.info("Verification email resent to: {}", email);
        return new LoginResponse(true, "Verification email sent. Please check your inbox.");
    }

    // ==========================================
    // PASSWORD RESET METHODS
    // ==========================================

    public LoginResponse forgotPassword(String email) {
        logger.info("Password reset request for: {}", email);

        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // Don't reveal if email exists for security
            logger.warn("Password reset requested for non-existent email: {}", email);
            return new LoginResponse(true, "If the email exists, a password reset link has been sent.");
        }

        UserEntity user = userOptional.get();
        String resetToken = verificationTokenService.createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user, resetToken);

        logger.info("Password reset email sent to: {}", email);
        return new LoginResponse(true, "If the email exists, a password reset link has been sent.");
    }

    public LoginResponse resetPassword(String token, String newPassword) {
        logger.info("Password reset attempt with token");

        UserEntity user = verificationTokenService.validateToken(token, TokenType.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationTokenService.markTokenUsed(token);

        // Invalidate all refresh tokens for security
        refreshTokenService.revokeAllUserTokens(user);

        logger.info("Password reset successfully for user: {}", user.getEmail());
        auditLogService.logSuccess("PASSWORD_RESET", "Password reset: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Password reset successfully. You can now log in with your new password.");
    }

    // ==========================================
    // 2FA LOGIN VERIFICATION
    // ==========================================

    public LoginResponse verify2FALogin(String email, String code) {
        logger.info("2FA verification attempt for: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new Invalid2FACodeException("2FA is not enabled for this user");
        }

        boolean isValid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);

        if (!isValid) {
            // Try backup code
            isValid = twoFactorService.useBackupCode(user, code);
        }

        if (!isValid) {
            logger.warn("Invalid 2FA code for user: {}", email);
            throw new Invalid2FACodeException("Invalid verification code");
        }

        // Generate full access token
        Map<String, Object> claims = createJwtClaims(user.getRoles());
        claims = new HashMap<>(claims);
        ((Map<String, Object>) claims).put("2fa_verified", true);

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        logger.info("2FA verified, user logged in: {}", email);
        auditLogService.logSuccess("USER_LOGIN_2FA", "User logged in with 2FA: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Login successful", token, refreshToken);
    }

    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}

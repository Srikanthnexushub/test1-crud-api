package org.example.service;

import org.example.config.properties.SecurityProperties;
import org.example.dto.*;
import org.example.entity.Role;
import org.example.entity.UserEntity;
import org.example.exception.AccountLockedException;
import org.example.exception.DuplicateResourceException;
import org.example.exception.InvalidCredentialsException;
import org.example.exception.ResourceNotFoundException;
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
            SecurityProperties securityProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
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

        userRepository.save(user);

        // Create immutable claims map for JWT
        Map<String, Object> claims = createJwtClaims(List.of(userRole));

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
        logger.info("User registered successfully: {}", request.email());

        auditLogService.logSuccess("USER_REGISTER", "New user registered: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Registration successful", token, refreshToken);
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.email());

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", request.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

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
}

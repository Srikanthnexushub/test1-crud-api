package org.example.service;

import org.example.dto.*;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.UserCreateRequest;
import org.example.dto.UserUpdateRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 30; // minutes

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lock-time-duration:30}")
    private long lockTimeDuration;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuditLogService auditLogService,
                      RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse register(LoginRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        Optional<UserEntity> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            logger.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity user = new UserEntity(request.getEmail(), encodedPassword);

        // Assign default ROLE_USER
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found. System initialization failed."));
        user.addRole(userRole);

        userRepository.save(user);

        // Add roles to JWT token claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of(userRole.getName().name()));

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
        logger.info("User registered successfully: {}", request.getEmail());

        auditLogService.logSuccess("USER_REGISTER", "New user registered: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Registration successful", token, refreshToken);
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Check if account is locked
        if (user.isAccountLocked()) {
            if (unlockWhenTimeExpired(user)) {
                logger.info("Account automatically unlocked for user: {}", request.getEmail());
            } else {
                logger.warn("Login failed - account locked for user: {}", request.getEmail());
                throw new AccountLockedException("Account is locked due to multiple failed login attempts. Please try again later.");
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", request.getEmail());
            increaseFailedAttempts(user);
            auditLogService.logFailure("USER_LOGIN", "Failed login attempt for: " + request.getEmail(),
                                       "Invalid credentials");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Successful login - reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            resetFailedAttempts(user);
        }

        // Add roles to JWT token claims
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toList());
        claims.put("roles", roles);

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
        logger.info("User logged in successfully: {}", request.getEmail());

        auditLogService.logSuccess("USER_LOGIN", "User logged in: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "Login successful", token, refreshToken);
    }

    private void increaseFailedAttempts(UserEntity user) {
        int newFailAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailAttempts);

        if (newFailAttempts >= maxFailedAttempts) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            logger.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), newFailAttempts);
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(UserEntity user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private boolean unlockWhenTimeExpired(UserEntity user) {
        if (user.getLockTime() != null) {
            long lockTimeInMinutes = ChronoUnit.MINUTES.between(user.getLockTime(), LocalDateTime.now());
            if (lockTimeInMinutes >= lockTimeDuration) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public java.util.List<UserEntity> getAllUsers() {
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
        logger.info("Admin creating user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("User creation failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity user = new UserEntity(request.getEmail(), encodedPassword);

        // Assign role (default to ROLE_USER if not specified)
        String roleName = request.getRole() != null && !request.getRole().isEmpty()
                ? request.getRole() : "ROLE_USER";

        Role.RoleName roleEnum;
        try {
            roleEnum = Role.RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            roleEnum = Role.RoleName.ROLE_USER;
        }

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.addRole(role);

        userRepository.save(user);
        logger.info("User created successfully by admin: {}", user.getEmail());

        auditLogService.logSuccess("USER_CREATE", "User created by admin: " + user.getEmail(),
                                    user.getId(), "ADMIN");

        return new LoginResponse(true, "User created successfully");
    }

    public LoginResponse updateUser(Long id, UserUpdateRequest request) {
        logger.info("Update attempt for user ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if email is being changed to an existing email
        if (!user.getEmail().equals(request.getEmail())) {
            Optional<UserEntity> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                logger.warn("Update failed - email already exists: {}", request.getEmail());
                throw new DuplicateResourceException("Email already exists");
            }
        }

        user.setEmail(request.getEmail());

        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update role if provided (admin only)
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                Role.RoleName roleEnum = Role.RoleName.valueOf(request.getRole());
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
                user.getRoles().clear();
                user.addRole(role);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role specified: {}", request.getRole());
            }
        }

        userRepository.save(user);

        logger.info("User updated successfully: {}", id);
        auditLogService.logSuccess("USER_UPDATE", "User updated: " + user.getEmail(),
                                    user.getId(), "USER");

        return new LoginResponse(true, "User updated successfully");
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

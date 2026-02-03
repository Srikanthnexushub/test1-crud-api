package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserEntity;
import org.example.exception.DuplicateResourceException;
import org.example.exception.InvalidCredentialsException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User registered successfully: {}", request.getEmail());

        return new LoginResponse(true, "Registration successful", token);
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User logged in successfully: {}", request.getEmail());

        return new LoginResponse(true, "Login successful", token);
    }

    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public LoginResponse updateUser(Long id, LoginRequest request) {
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
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        logger.info("User updated successfully: {}", id);
        return new LoginResponse(true, "User updated successfully");
    }

    public LoginResponse deleteUser(Long id) {
        logger.info("Delete attempt for user ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.deleteById(id);
        logger.info("User deleted successfully: {}", id);

        return new LoginResponse(true, "User deleted successfully");
    }
}

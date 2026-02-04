package org.example.service;

import org.example.config.properties.JwtProperties;
import org.example.entity.RefreshToken;
import org.example.entity.UserEntity;
import org.example.exception.InvalidCredentialsException;
import org.example.repository.RefreshTokenRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh token service with maximum immutability:
 * - All fields are final
 * - Constructor injection only
 * - Extracted methods for clarity
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    // Immutable repository dependencies
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Immutable configuration value
    private final long refreshTokenDurationMs;

    // Constructor injection (no @Autowired needed)
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenDurationMs = jwtProperties.getRefreshExpiration();
    }

    public RefreshToken createRefreshToken(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = buildNewRefreshToken(user);
        refreshToken = refreshTokenRepository.save(refreshToken);

        logger.info("Created new refresh token for user: {}", email);
        return refreshToken;
    }

    /**
     * Builds a new refresh token entity.
     * Extracted method for token creation logic.
     */
    private RefreshToken buildNewRefreshToken(UserEntity user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(calculateExpiryDate());
        refreshToken.setRevoked(false);
        return refreshToken;
    }

    /**
     * Calculates expiry date for refresh token.
     * Extracted method for clarity.
     */
    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (isExpired(token)) {
            handleExpiredToken(token);
        }

        if (token.isRevoked()) {
            handleRevokedToken(token);
        }

        return token;
    }

    /**
     * Checks if token is expired.
     */
    private boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }

    /**
     * Handles expired token by deleting it and throwing exception.
     * Extracted method for clarity.
     */
    private void handleExpiredToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
        logger.warn("Refresh token expired for user: {}", token.getUser().getEmail());
        throw new InvalidCredentialsException("Refresh token expired. Please login again.");
    }

    /**
     * Handles revoked token by throwing exception.
     * Extracted method for clarity.
     */
    private void handleRevokedToken(RefreshToken token) {
        logger.warn("Attempted to use revoked refresh token for user: {}", token.getUser().getEmail());
        throw new InvalidCredentialsException("Refresh token has been revoked. Please login again.");
    }

    public void revokeToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        refreshToken.ifPresent(this::markAsRevoked);
    }

    /**
     * Marks token as revoked and saves.
     * Extracted method for clarity.
     */
    private void markAsRevoked(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        logger.info("Revoked refresh token for user: {}", token.getUser().getEmail());
    }

    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }
}

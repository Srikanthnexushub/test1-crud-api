package org.example.service;

import org.example.entity.RefreshToken;
import org.example.entity.UserEntity;
import org.example.exception.InvalidCredentialsException;
import org.example.repository.RefreshTokenRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setRevoked(false);

        refreshToken = refreshTokenRepository.save(refreshToken);
        logger.info("Created new refresh token for user: {}", email);

        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            logger.warn("Refresh token expired for user: {}", token.getUser().getEmail());
            throw new InvalidCredentialsException("Refresh token expired. Please login again.");
        }

        if (token.isRevoked()) {
            logger.warn("Attempted to use revoked refresh token for user: {}", token.getUser().getEmail());
            throw new InvalidCredentialsException("Refresh token has been revoked. Please login again.");
        }

        return token;
    }

    public void revokeToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken rt = refreshToken.get();
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            logger.info("Revoked refresh token for user: {}", rt.getUser().getEmail());
        }
    }

    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }
}

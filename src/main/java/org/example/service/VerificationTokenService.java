package org.example.service;

import org.example.config.properties.EmailProperties;
import org.example.entity.UserEntity;
import org.example.entity.VerificationToken;
import org.example.entity.VerificationToken.TokenType;
import org.example.exception.InvalidVerificationTokenException;
import org.example.exception.TokenExpiredException;
import org.example.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class VerificationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenService.class);

    private final VerificationTokenRepository tokenRepository;
    private final EmailProperties emailProperties;

    public VerificationTokenService(VerificationTokenRepository tokenRepository, EmailProperties emailProperties) {
        this.tokenRepository = tokenRepository;
        this.emailProperties = emailProperties;
    }

    public String createEmailVerificationToken(UserEntity user) {
        // Delete any existing verification tokens for this user
        tokenRepository.deleteByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION);

        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusHours(emailProperties.getVerificationTokenExpiryHours());

        VerificationToken verificationToken = new VerificationToken(
                token, user, TokenType.EMAIL_VERIFICATION, expiryDate);
        tokenRepository.save(verificationToken);

        logger.info("Email verification token created for user: {}", user.getEmail());
        return token;
    }

    public String createPasswordResetToken(UserEntity user) {
        // Delete any existing password reset tokens for this user
        tokenRepository.deleteByUserAndTokenType(user, TokenType.PASSWORD_RESET);

        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusHours(emailProperties.getPasswordResetTokenExpiryHours());

        VerificationToken verificationToken = new VerificationToken(
                token, user, TokenType.PASSWORD_RESET, expiryDate);
        tokenRepository.save(verificationToken);

        logger.info("Password reset token created for user: {}", user.getEmail());
        return token;
    }

    @Transactional(readOnly = true)
    public UserEntity validateToken(String token, TokenType tokenType) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, tokenType)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new InvalidVerificationTokenException("Token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Token has expired");
        }

        return verificationToken.getUser();
    }

    public void markTokenUsed(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Token not found"));

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        logger.info("Token marked as used: {}", token.substring(0, 8) + "...");
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupExpiredTokens() {
        int deletedCount = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Cleaned up {} expired verification tokens", deletedCount);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

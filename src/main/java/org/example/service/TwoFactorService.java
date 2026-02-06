package org.example.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.example.config.properties.TotpProperties;
import org.example.entity.TwoFactorBackupCode;
import org.example.entity.UserEntity;
import org.example.exception.Invalid2FACodeException;
import org.example.repository.TwoFactorBackupCodeRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Transactional
public class TwoFactorService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorService.class);
    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    private final TwoFactorBackupCodeRepository backupCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpProperties totpProperties;

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;

    public TwoFactorService(
            TwoFactorBackupCodeRepository backupCodeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TotpProperties totpProperties
    ) {
        this.backupCodeRepository = backupCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.totpProperties = totpProperties;

        this.secretGenerator = new DefaultSecretGenerator();

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        this.qrGenerator = new ZxingPngQrGenerator();
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeUrl(UserEntity user, String secret) {
        try {
            QrData qrData = new QrData.Builder()
                    .label(user.getEmail())
                    .secret(secret)
                    .issuer(totpProperties.getIssuer())
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            byte[] imageData = qrGenerator.generate(qrData);
            String mimeType = qrGenerator.getImageMimeType();

            return getDataUriForImage(imageData, mimeType);
        } catch (Exception e) {
            logger.error("Failed to generate QR code for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    public List<String> enable2FA(UserEntity user, String code) {
        if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isEmpty()) {
            throw new Invalid2FACodeException("2FA setup not initiated. Please call setup endpoint first.");
        }

        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new Invalid2FACodeException("Invalid verification code");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        List<String> backupCodes = generateAndSaveBackupCodes(user);

        logger.info("2FA enabled for user: {}", user.getEmail());
        return backupCodes;
    }

    public void disable2FA(UserEntity user, String code) {
        if (!user.isTwoFactorEnabled()) {
            throw new Invalid2FACodeException("2FA is not enabled");
        }

        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new Invalid2FACodeException("Invalid verification code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        // Delete all backup codes
        backupCodeRepository.deleteByUser(user);

        logger.info("2FA disabled for user: {}", user.getEmail());
    }

    public List<String> regenerateBackupCodes(UserEntity user) {
        if (!user.isTwoFactorEnabled()) {
            throw new Invalid2FACodeException("2FA is not enabled");
        }

        // Delete existing backup codes
        backupCodeRepository.deleteByUser(user);

        List<String> backupCodes = generateAndSaveBackupCodes(user);

        logger.info("Backup codes regenerated for user: {}", user.getEmail());
        return backupCodes;
    }

    public boolean useBackupCode(UserEntity user, String code) {
        List<TwoFactorBackupCode> unusedCodes = backupCodeRepository.findByUserAndUsedFalse(user);

        for (TwoFactorBackupCode backupCode : unusedCodes) {
            if (passwordEncoder.matches(code, backupCode.getCode())) {
                backupCode.setUsed(true);
                backupCode.setUsedAt(LocalDateTime.now());
                backupCodeRepository.save(backupCode);

                logger.info("Backup code used for user: {}", user.getEmail());
                return true;
            }
        }

        return false;
    }

    public int getRemainingBackupCodeCount(UserEntity user) {
        return backupCodeRepository.countUnusedByUser(user);
    }

    public void setupTwoFactor(UserEntity user) {
        String secret = generateSecret();
        user.setTwoFactorSecret(secret);
        userRepository.save(user);
        logger.info("2FA setup initiated for user: {}", user.getEmail());
    }

    private List<String> generateAndSaveBackupCodes(UserEntity user) {
        List<String> plainTextCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            byte[] bytes = new byte[BACKUP_CODE_LENGTH];
            random.nextBytes(bytes);
            String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                    .substring(0, BACKUP_CODE_LENGTH).toUpperCase();
            plainTextCodes.add(code);

            TwoFactorBackupCode backupCode = new TwoFactorBackupCode(user, passwordEncoder.encode(code));
            backupCodeRepository.save(backupCode);
        }

        return plainTextCodes;
    }
}

package org.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.config.properties.EmailProperties;
import org.example.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailProperties = emailProperties;
    }

    @Async
    public void sendVerificationEmail(UserEntity user, String token) {
        String verificationUrl = emailProperties.getBaseUrl() + "/api/v1/users/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("userName", user.getEmail());
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expiryHours", emailProperties.getVerificationTokenExpiryHours());

        String htmlContent = templateEngine.process("email/verification-email", context);

        sendEmail(user.getEmail(), "Verify Your Email Address", htmlContent);
        logger.info("Verification email sent to: {}", user.getEmail());
    }

    @Async
    public void sendPasswordResetEmail(UserEntity user, String token) {
        String resetUrl = emailProperties.getBaseUrl() + "/api/v1/users/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("userName", user.getEmail());
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiryHours", emailProperties.getPasswordResetTokenExpiryHours());

        String htmlContent = templateEngine.process("email/password-reset-email", context);

        sendEmail(user.getEmail(), "Reset Your Password", htmlContent);
        logger.info("Password reset email sent to: {}", user.getEmail());
    }

    @Async
    public void send2FABackupCodesEmail(UserEntity user, List<String> backupCodes) {
        Context context = new Context();
        context.setVariable("userName", user.getEmail());
        context.setVariable("backupCodes", backupCodes);

        String htmlContent = templateEngine.process("email/2fa-backup-codes-email", context);

        sendEmail(user.getEmail(), "Your 2FA Backup Codes", htmlContent);
        logger.info("2FA backup codes email sent to: {}", user.getEmail());
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFromAddress(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.debug("Email sent successfully to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

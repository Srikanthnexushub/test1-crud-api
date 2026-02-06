package org.example.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable Email configuration properties using constructor binding.
 */
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private final String fromAddress;
    private final String fromName;
    private final int verificationTokenExpiryHours;
    private final int passwordResetTokenExpiryHours;
    private final String baseUrl;

    public EmailProperties(
            String fromAddress,
            String fromName,
            int verificationTokenExpiryHours,
            int passwordResetTokenExpiryHours,
            String baseUrl
    ) {
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.verificationTokenExpiryHours = verificationTokenExpiryHours;
        this.passwordResetTokenExpiryHours = passwordResetTokenExpiryHours;
        this.baseUrl = baseUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public int getVerificationTokenExpiryHours() {
        return verificationTokenExpiryHours;
    }

    public int getPasswordResetTokenExpiryHours() {
        return passwordResetTokenExpiryHours;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

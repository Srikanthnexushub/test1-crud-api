package org.example.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable security configuration properties using constructor binding.
 * Replaces @Value injection for maximum immutability.
 *
 * Maps to security.* properties in application.properties:
 * - security.max-failed-attempts
 * - security.lock-time-duration
 */
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private final int maxFailedAttempts;
    private final long lockTimeDuration;

    /**
     * Constructor binding - all properties are final and set via constructor.
     * Spring Boot 3.0+ automatically detects this as constructor binding.
     */
    public SecurityProperties(
            int maxFailedAttempts,
            long lockTimeDuration
    ) {
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockTimeDuration = lockTimeDuration;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public long getLockTimeDuration() {
        return lockTimeDuration;
    }
}

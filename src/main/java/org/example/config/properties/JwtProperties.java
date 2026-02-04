package org.example.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * Immutable JWT configuration properties using constructor binding.
 * Replaces @Value injection for maximum immutability.
 *
 * Maps to jwt.* properties in application.properties:
 * - jwt.secret
 * - jwt.expiration
 * - jwt.refresh-expiration
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private final String secret;
    private final long expiration;
    private final long refreshExpiration;

    /**
     * Constructor binding - all properties are final and set via constructor.
     * Spring Boot 3.0+ automatically detects this as constructor binding.
     */
    public JwtProperties(
            String secret,
            long expiration,
            long refreshExpiration
    ) {
        this.secret = secret;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}

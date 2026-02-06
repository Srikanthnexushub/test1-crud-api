package org.example.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable TOTP configuration properties using constructor binding.
 */
@ConfigurationProperties(prefix = "totp")
public class TotpProperties {

    private final String issuer;

    public TotpProperties(String issuer) {
        this.issuer = issuer;
    }

    public String getIssuer() {
        return issuer;
    }
}

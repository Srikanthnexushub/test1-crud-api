package org.example.config;

import org.example.config.properties.EmailProperties;
import org.example.config.properties.JwtProperties;
import org.example.config.properties.SecurityProperties;
import org.example.config.properties.TotpProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables immutable configuration properties.
 * All properties use constructor binding for maximum immutability.
 */
@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    SecurityProperties.class,
    EmailProperties.class,
    TotpProperties.class
})
public class PropertiesConfig {
    // Configuration properties are automatically registered as beans
}

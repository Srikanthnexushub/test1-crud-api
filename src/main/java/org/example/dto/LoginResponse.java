package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Immutable login response DTO using Java Record.
 * Optional fields (token, refreshToken) can be null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
    boolean success,
    String message,
    String token,
    String refreshToken
) {
    // Compact constructors for convenience
    public LoginResponse(boolean success, String message) {
        this(success, message, null, null);
    }

    public LoginResponse(boolean success, String message, String token) {
        this(success, message, token, null);
    }
}

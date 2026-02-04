package org.example.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Immutable refresh token request DTO using Java Record.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {
}

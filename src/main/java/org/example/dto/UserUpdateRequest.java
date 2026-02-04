package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Immutable user update request DTO using Java Record.
 * Password and role are optional fields (can be null).
 */
public record UserUpdateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password, // Optional - only update if provided

    String role // Optional - ROLE_USER, ROLE_ADMIN, ROLE_MANAGER
) {
}

package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.validation.NoHtml;

/**
 * Immutable user create request DTO using Java Record.
 * Role is optional (defaults to ROLE_USER if not provided).
 */
public record UserCreateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @NoHtml
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password,

    @Pattern(regexp = "^(ROLE_USER|ROLE_ADMIN|ROLE_MANAGER)?$", message = "Invalid role. Must be ROLE_USER, ROLE_ADMIN, or ROLE_MANAGER")
    String role // ROLE_USER (default), ROLE_ADMIN, ROLE_MANAGER
) {
}

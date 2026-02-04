package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Immutable error response DTO using Java Record.
 * Replaces Lombok @Data with Record for true immutability.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> validationErrors,
    String traceId
) {
    // Defensive copy for mutable Map parameter
    public ErrorResponse {
        validationErrors = validationErrors != null
            ? Map.copyOf(validationErrors)
            : null;
    }
}

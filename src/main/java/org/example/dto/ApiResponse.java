package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Immutable API response DTO using Java Record.
 * Replaces Lombok @Data and @Builder with Record and static factory methods.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp,
    String path
) {
    // Static factory methods for convenient creation
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            true,
            null,
            data,
            LocalDateTime.now(),
            null
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
            true,
            message,
            data,
            LocalDateTime.now(),
            null
        );
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
            false,
            message,
            null,
            LocalDateTime.now(),
            null
        );
    }
}

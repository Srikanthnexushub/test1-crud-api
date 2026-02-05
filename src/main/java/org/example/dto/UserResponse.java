package org.example.dto;

import org.example.entity.Role;
import org.example.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable user response DTO that excludes sensitive fields like password hash.
 */
public record UserResponse(
    Long id,
    String email,
    List<String> roles,
    boolean accountLocked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse fromEntity(UserEntity user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableList());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                roleNames,
                user.isAccountLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

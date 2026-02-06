package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorBackupCodeRequest(
    @NotBlank(message = "Backup code is required")
    String backupCode
) {}

package org.example.dto;

public record TwoFactorSetupResponse(
    boolean success,
    String message,
    String secret,
    String qrCodeUrl
) {}

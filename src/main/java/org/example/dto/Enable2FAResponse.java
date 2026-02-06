package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Enable2FAResponse(
    boolean success,
    String message,
    String secret,
    String qrCodeUrl,
    List<String> backupCodes
) {
    public Enable2FAResponse(boolean success, String message) {
        this(success, message, null, null, null);
    }
}

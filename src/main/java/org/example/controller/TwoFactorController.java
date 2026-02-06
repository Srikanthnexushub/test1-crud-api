package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.*;
import org.example.entity.UserEntity;
import org.example.service.EmailService;
import org.example.service.TwoFactorService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@Tag(name = "Two-Factor Authentication", description = "APIs for managing two-factor authentication")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final UserService userService;
    private final EmailService emailService;

    public TwoFactorController(TwoFactorService twoFactorService, UserService userService, EmailService emailService) {
        this.twoFactorService = twoFactorService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/setup")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Setup 2FA", description = "Initiates 2FA setup and returns QR code for authenticator app")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA setup initiated",
                    content = @Content(schema = @Schema(implementation = TwoFactorSetupResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TwoFactorSetupResponse> setup2FA() {
        UserEntity user = getCurrentUser();

        if (user.isTwoFactorEnabled()) {
            return ResponseEntity.ok(new TwoFactorSetupResponse(false, "2FA is already enabled", null, null));
        }

        // Generate and save secret
        twoFactorService.setupTwoFactor(user);

        // Reload user to get the new secret
        user = userService.getUserByEmail(user.getEmail());

        String qrCodeUrl = twoFactorService.generateQrCodeUrl(user, user.getTwoFactorSecret());

        return ResponseEntity.ok(new TwoFactorSetupResponse(
                true,
                "Scan the QR code with your authenticator app",
                user.getTwoFactorSecret(),
                qrCodeUrl
        ));
    }

    @PostMapping("/enable")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Enable 2FA", description = "Verifies the code and enables 2FA for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA enabled successfully",
                    content = @Content(schema = @Schema(implementation = Enable2FAResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid verification code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Enable2FAResponse> enable2FA(
            @Parameter(description = "2FA verification request", required = true)
            @Valid @RequestBody Verify2FARequest request) {
        UserEntity user = getCurrentUser();

        List<String> backupCodes = twoFactorService.enable2FA(user, request.code());

        // Send backup codes via email
        emailService.send2FABackupCodesEmail(user, backupCodes);

        return ResponseEntity.ok(new Enable2FAResponse(
                true,
                "2FA enabled successfully. Backup codes have been sent to your email.",
                null,
                null,
                backupCodes
        ));
    }

    @PostMapping("/disable")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Disable 2FA", description = "Disables 2FA after verifying the current code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA disabled successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid verification code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> disable2FA(
            @Parameter(description = "2FA disable request", required = true)
            @Valid @RequestBody Disable2FARequest request) {
        UserEntity user = getCurrentUser();

        twoFactorService.disable2FA(user, request.code());

        return ResponseEntity.ok(new LoginResponse(true, "2FA disabled successfully"));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify 2FA code", description = "Verifies 2FA code during login and returns full access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA verified successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid verification code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> verify2FA(
            @Parameter(description = "2FA verification request", required = true)
            @Valid @RequestBody Verify2FARequest request,
            @RequestHeader("X-2FA-Email") String email) {
        LoginResponse response = userService.verify2FALogin(email, request.code());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/backup-codes")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Regenerate backup codes", description = "Generates new backup codes (invalidates existing ones)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Backup codes regenerated",
                    content = @Content(schema = @Schema(implementation = Enable2FAResponse.class))),
            @ApiResponse(responseCode = "400", description = "2FA not enabled",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Enable2FAResponse> regenerateBackupCodes() {
        UserEntity user = getCurrentUser();

        List<String> backupCodes = twoFactorService.regenerateBackupCodes(user);

        // Send backup codes via email
        emailService.send2FABackupCodesEmail(user, backupCodes);

        return ResponseEntity.ok(new Enable2FAResponse(
                true,
                "New backup codes generated. They have been sent to your email.",
                null,
                null,
                backupCodes
        ));
    }

    @PostMapping("/backup-code")
    @Operation(summary = "Use backup code", description = "Uses a backup code to complete 2FA during login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Backup code accepted",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid backup code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> useBackupCode(
            @Parameter(description = "Backup code request", required = true)
            @Valid @RequestBody TwoFactorBackupCodeRequest request,
            @RequestHeader("X-2FA-Email") String email) {
        LoginResponse response = userService.verify2FALogin(email, request.backupCode());
        return ResponseEntity.ok(response);
    }

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByEmail(auth.getName());
    }
}

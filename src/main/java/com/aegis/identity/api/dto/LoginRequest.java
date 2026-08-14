package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user password authentication")
public record LoginRequest(
    @Schema(description = "User registered email address", example = "admin@acme-cyber.cloud")
        @NotBlank
        @Email
        String email,
    @Schema(description = "User password credentials", example = "P@ssw0rd2026!") @NotBlank
        String password) {}

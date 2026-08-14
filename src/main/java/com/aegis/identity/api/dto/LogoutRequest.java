package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for revoking session and refresh token during logout")
public record LogoutRequest(
    @Schema(
            description = "Refresh token to be invalidated and revoked",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank
        String refreshToken) {}

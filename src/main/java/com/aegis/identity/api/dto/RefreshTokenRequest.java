package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for renewing JWT access token using a valid refresh token")
public record RefreshTokenRequest(
    @Schema(
            description = "Opaque cryptographically secure refresh token string",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank
        String refreshToken) {}

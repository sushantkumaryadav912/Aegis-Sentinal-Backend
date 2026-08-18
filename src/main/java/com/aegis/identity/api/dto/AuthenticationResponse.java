package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload returned upon successful authentication or token refresh")
public record AuthenticationResponse(
    @Schema(
            description = "Short-lived JWT Access Token used in Authorization header",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
    @Schema(
            description = "Long-lived refresh token used to obtain new access tokens",
            example = "a9b8c7d6-e5f4-3210-fedc-ba9876543210")
        String refreshToken,
    @Schema(description = "OAuth2 authorization token type", example = "Bearer")
        String tokenType) {}

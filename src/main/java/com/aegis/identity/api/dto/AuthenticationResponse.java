package com.aegis.identity.api.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}

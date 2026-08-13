package com.aegis.identity.application.query;

public record AuthenticationResult(
        String accessToken,
        String refreshToken
) {}

package com.aegis.identity.application.port;

public record OAuthUserInfo(
        String subject,
        String email,
        String firstName,
        String lastName
) {}

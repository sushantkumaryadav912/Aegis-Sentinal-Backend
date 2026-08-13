package com.aegis.identity.application.query;

import com.aegis.identity.domain.entity.User;

public record OAuthAuthenticationResult(
        User user,
        String accessToken,
        String refreshToken,
        boolean newlyCreated
) {}

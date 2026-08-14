package com.aegis.identity.application.port;

import com.aegis.identity.domain.model.IdentityProvider;

public record OAuthUserInfo(
        IdentityProvider provider,
        String providerSubject,
        String email,
        String firstName,
        String lastName,
        String pictureUrl
) {
}

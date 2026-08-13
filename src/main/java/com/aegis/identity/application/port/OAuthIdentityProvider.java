package com.aegis.identity.application.port;

import com.aegis.identity.domain.model.IdentityProvider;

public interface OAuthIdentityProvider {

    IdentityProvider provider();

    OAuthUserInfo extractUserInfo(Object principal);
}

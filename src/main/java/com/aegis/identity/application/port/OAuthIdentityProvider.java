package com.aegis.identity.application.port;

import com.aegis.identity.domain.model.IdentityProvider;

public interface OAuthIdentityProvider {

  IdentityProvider provider();

  boolean supports(Object principal);

  OAuthUserInfo extractUserInfo(Object principal);
}

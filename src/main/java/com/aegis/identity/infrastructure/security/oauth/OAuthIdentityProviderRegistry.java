package com.aegis.identity.infrastructure.security.oauth;

import com.aegis.identity.application.port.OAuthIdentityProvider;
import com.aegis.identity.domain.model.IdentityProvider;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OAuthIdentityProviderRegistry {

  private final List<OAuthIdentityProvider> providers;

  public OAuthIdentityProviderRegistry(List<OAuthIdentityProvider> providers) {
    this.providers = providers;
  }

  public OAuthIdentityProvider resolve(Object principal) {
    return providers.stream()
        .filter(p -> p.supports(principal))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No matching OAuthIdentityProvider found for principal: " + principal));
  }

  public OAuthIdentityProvider getProvider(IdentityProvider provider) {
    return providers.stream()
        .filter(p -> p.provider() == provider)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No OAuthIdentityProvider found for provider: " + provider));
  }
}

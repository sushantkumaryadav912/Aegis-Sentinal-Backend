package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository {

  Optional<UserIdentity> findByProviderAndProviderSubject(
      IdentityProvider provider, String providerSubject);

  Optional<UserIdentity> findByUserIdAndProvider(UUID userId, IdentityProvider provider);

  UserIdentity save(UserIdentity userIdentity);
}

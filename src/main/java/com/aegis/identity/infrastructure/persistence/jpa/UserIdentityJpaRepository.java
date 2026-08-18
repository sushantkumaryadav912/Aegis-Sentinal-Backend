package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityJpaRepository extends JpaRepository<UserIdentity, UUID> {

  Optional<UserIdentity> findByProviderAndProviderSubject(
      IdentityProvider provider, String providerSubject);

  Optional<UserIdentity> findByUserIdAndProvider(UUID userId, IdentityProvider provider);
}

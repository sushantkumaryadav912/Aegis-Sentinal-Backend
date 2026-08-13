package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.infrastructure.persistence.jpa.UserIdentityJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserIdentityRepositoryAdapter
        implements UserIdentityRepository {

    private final UserIdentityJpaRepository repository;

    public UserIdentityRepositoryAdapter(
            UserIdentityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserIdentity> findByProviderAndProviderSubject(
            IdentityProvider provider,
            String providerSubject) {

        return repository.findByProviderAndProviderSubject(
                provider,
                providerSubject);
    }

    @Override
    public Optional<UserIdentity> findByUserIdAndProvider(
            UUID userId,
            IdentityProvider provider) {

        return repository.findByUserIdAndProvider(
                userId,
                provider);
    }

    @Override
    public UserIdentity save(UserIdentity userIdentity) {
        return repository.save(userIdentity);
    }
}

package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.repository.UserRoleRepository;
import com.aegis.identity.infrastructure.persistence.jpa.UserRoleJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleRepositoryAdapter implements UserRoleRepository {

    private final UserRoleJpaRepository repository;

    public UserRoleRepositoryAdapter(UserRoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserRole> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<UserRole> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<UserRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId) {
        return repository.findByUserIdAndOrganizationId(userId, organizationId);
    }

    @Override
    public UserRole save(UserRole userRole) {
        return repository.save(userRole);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}

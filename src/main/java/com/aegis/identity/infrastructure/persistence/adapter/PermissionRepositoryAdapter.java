package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.Permission;
import com.aegis.identity.domain.repository.PermissionRepository;
import com.aegis.identity.infrastructure.persistence.jpa.PermissionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository repository;

    public PermissionRepositoryAdapter(PermissionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public List<Permission> findAll() {
        return repository.findAll();
    }

    @Override
    public Permission save(Permission permission) {
        return repository.save(permission);
    }
}

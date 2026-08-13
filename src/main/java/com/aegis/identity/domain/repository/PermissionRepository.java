package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    Optional<Permission> findById(UUID id);

    Optional<Permission> findByName(String name);

    List<Permission> findAll();

    Permission save(Permission permission);
}

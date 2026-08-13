package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Permission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionJpaRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(String name);
}

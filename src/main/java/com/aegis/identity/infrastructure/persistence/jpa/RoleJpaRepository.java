package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(String name);
}

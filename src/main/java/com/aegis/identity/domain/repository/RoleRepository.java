package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    List<Role> findAll();

    Role save(Role role);
}

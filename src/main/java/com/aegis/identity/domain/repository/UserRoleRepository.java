package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository {

  Optional<UserRole> findById(UUID id);

  List<UserRole> findByUserId(UUID userId);

  List<UserRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

  UserRole save(UserRole userRole);

  void deleteById(UUID id);
}

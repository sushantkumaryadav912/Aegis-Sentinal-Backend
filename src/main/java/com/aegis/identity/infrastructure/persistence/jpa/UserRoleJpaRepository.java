package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}

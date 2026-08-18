package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceJpaRepository extends JpaRepository<Workspace, UUID> {

  List<Workspace> findByOrganizationId(UUID organizationId);

  Optional<Workspace> findByOrganizationIdAndSlug(UUID organizationId, String slug);

  boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug);
}

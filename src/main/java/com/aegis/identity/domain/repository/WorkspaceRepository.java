package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    Optional<Workspace> findById(UUID id);

    List<Workspace> findByOrganizationId(UUID organizationId);

    Optional<Workspace> findByOrganizationIdAndSlug(UUID organizationId, String slug);

    boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug);

    Workspace save(Workspace workspace);
}

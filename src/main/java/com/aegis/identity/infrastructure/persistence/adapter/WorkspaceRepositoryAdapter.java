package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.Workspace;
import com.aegis.identity.domain.repository.WorkspaceRepository;
import com.aegis.identity.infrastructure.persistence.jpa.WorkspaceJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceRepositoryAdapter implements WorkspaceRepository {

  private final WorkspaceJpaRepository repository;

  public WorkspaceRepositoryAdapter(WorkspaceJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Workspace> findById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public List<Workspace> findByOrganizationId(UUID organizationId) {
    return repository.findByOrganizationId(organizationId);
  }

  @Override
  public Optional<Workspace> findByOrganizationIdAndSlug(UUID organizationId, String slug) {
    return repository.findByOrganizationIdAndSlug(organizationId, slug);
  }

  @Override
  public boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug) {
    return repository.existsByOrganizationIdAndSlug(organizationId, slug);
  }

  @Override
  public Workspace save(Workspace workspace) {
    return repository.save(workspace);
  }
}

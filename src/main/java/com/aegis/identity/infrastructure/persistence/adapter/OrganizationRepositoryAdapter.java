package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.infrastructure.persistence.jpa.OrganizationJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationRepositoryAdapter implements OrganizationRepository {

  private final OrganizationJpaRepository repository;

  public OrganizationRepositoryAdapter(OrganizationJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Organization> findById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<Organization> findBySlug(String slug) {
    return repository.findBySlug(slug);
  }

  @Override
  public boolean existsBySlug(String slug) {
    return repository.existsBySlug(slug);
  }

  @Override
  public Organization save(Organization organization) {
    return repository.save(organization);
  }
}

package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Organization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationJpaRepository extends JpaRepository<Organization, UUID> {

  Optional<Organization> findBySlug(String slug);

  boolean existsBySlug(String slug);
}

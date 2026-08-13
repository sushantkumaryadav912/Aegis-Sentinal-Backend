package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Organization;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {

    Optional<Organization> findById(UUID id);

    Optional<Organization> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Organization save(Organization organization);
}

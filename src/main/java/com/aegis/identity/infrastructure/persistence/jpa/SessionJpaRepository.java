package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Session;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionJpaRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    void deleteByUserId(UUID userId);
}

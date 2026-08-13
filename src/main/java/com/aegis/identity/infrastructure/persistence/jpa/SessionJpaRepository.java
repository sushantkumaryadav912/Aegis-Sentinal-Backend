package com.aegis.identity.infrastructure.persistence.jpa;

import com.aegis.identity.domain.entity.Session;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionJpaRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    void deleteByUserId(UUID userId);

    @Modifying
    @Query("UPDATE Session s SET s.revoked = true WHERE s.user.id = :userId AND s.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);
}

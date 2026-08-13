package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Session;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {

    Optional<Session> findById(UUID id);

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    Session save(Session session);

    void deleteByUserId(UUID userId);
}

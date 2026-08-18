package com.aegis.identity.domain.repository;

import com.aegis.identity.domain.entity.Session;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {

  Session save(Session session);

  Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

  Optional<Session> findById(UUID id);

  void deleteById(UUID id);

  void deleteByUserId(UUID userId);

  void revokeAllByUserId(UUID userId);
}

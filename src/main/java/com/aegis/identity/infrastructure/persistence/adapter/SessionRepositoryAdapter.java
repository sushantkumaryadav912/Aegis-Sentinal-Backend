package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.repository.SessionRepository;
import com.aegis.identity.infrastructure.persistence.jpa.SessionJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepositoryAdapter implements SessionRepository {

    private final SessionJpaRepository repository;

    public SessionRepositoryAdapter(SessionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Session save(Session session) {
        return repository.save(session);
    }

    @Override
    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return repository.findByRefreshTokenHash(refreshTokenHash);
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }
}

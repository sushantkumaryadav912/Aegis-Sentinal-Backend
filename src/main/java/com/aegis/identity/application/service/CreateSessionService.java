package com.aegis.identity.application.service;

import com.aegis.identity.application.port.RefreshTokenHasher;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSessionService {

    private final SessionRepository sessionRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    public CreateSessionService(
            SessionRepository sessionRepository,
            RefreshTokenHasher refreshTokenHasher) {

        this.sessionRepository = sessionRepository;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    @Transactional
    public Session create(
            User user,
            String refreshToken) {

        String tokenHash =
                refreshTokenHasher.hash(refreshToken);

        Session session = Session.create(
                user,
                tokenHash
        );

        return sessionRepository.save(session);
    }
}

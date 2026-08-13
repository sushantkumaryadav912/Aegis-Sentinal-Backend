package com.aegis.identity.application.service;

import com.aegis.identity.application.command.RefreshTokenCommand;
import com.aegis.identity.application.port.RefreshTokenHasher;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.SessionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final TokenService tokenService;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SessionRepository sessionRepository;
    private final CreateSessionService createSessionService;

    public RefreshTokenService(
            TokenService tokenService,
            RefreshTokenHasher refreshTokenHasher,
            SessionRepository sessionRepository,
            CreateSessionService createSessionService) {

        this.tokenService = tokenService;
        this.refreshTokenHasher = refreshTokenHasher;
        this.sessionRepository = sessionRepository;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public AuthenticationResult execute(RefreshTokenCommand command) {

        UUID userId = tokenService.extractUserIdFromRefreshToken(command.refreshToken());
        String hash = refreshTokenHasher.hash(command.refreshToken());

        Session session = sessionRepository.findByRefreshTokenHash(hash).orElse(null);

        // Token Reuse Detection
        if (session == null || Boolean.TRUE.equals(session.getRevoked())) {
            sessionRepository.revokeAllByUserId(userId);
            throw new IllegalArgumentException("Invalid or revoked refresh token");
        }

        if (session.isExpired()) {
            session.revoke();
            sessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = session.getUser();
        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        // Revoke current session (rotation)
        session.revoke();
        sessionRepository.save(session);

        // Generate new token pair
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        // Persist new session
        createSessionService.create(user, newRefreshToken);

        return new AuthenticationResult(
                newAccessToken,
                newRefreshToken
        );
    }
}

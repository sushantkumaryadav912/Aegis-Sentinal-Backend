package com.aegis.identity.application.service;

import com.aegis.identity.application.command.LogoutCommand;
import com.aegis.identity.application.port.RefreshTokenHasher;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {

  private final RefreshTokenHasher refreshTokenHasher;
  private final SessionRepository sessionRepository;

  public LogoutService(RefreshTokenHasher refreshTokenHasher, SessionRepository sessionRepository) {

    this.refreshTokenHasher = refreshTokenHasher;
    this.sessionRepository = sessionRepository;
  }

  @Transactional
  public void execute(LogoutCommand command) {
    String hash = refreshTokenHasher.hash(command.refreshToken());
    Session session = sessionRepository.findByRefreshTokenHash(hash).orElse(null);

    if (session != null && Boolean.FALSE.equals(session.getRevoked())) {
      session.revoke();
      sessionRepository.save(session);
    }
  }
}

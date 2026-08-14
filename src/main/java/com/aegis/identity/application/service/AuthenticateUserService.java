package com.aegis.identity.application.service;

import com.aegis.identity.application.command.LoginUserCommand;
import com.aegis.identity.application.port.PasswordHasher;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.infrastructure.audit.AuditEventType;
import com.aegis.identity.infrastructure.audit.SecurityAuditEvent;
import com.aegis.identity.infrastructure.audit.SecurityAuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CreateSessionService createSessionService;
    private final SecurityAuditLogger auditLogger;

    public AuthenticateUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            CreateSessionService createSessionService,
            SecurityAuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.createSessionService = createSessionService;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public AuthenticationResult execute(LoginUserCommand command) {

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        if (!passwordHasher.matches(
                command.password(),
                user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        AuthenticationResult result = createSessionService.issueTokensAndCreateSession(user);
        auditLogger.logEvent(SecurityAuditEvent.of(AuditEventType.AUTH_LOGIN_SUCCESS, user.getId(), "SUCCESS", "Local password login successful"));
        return result;
    }
}

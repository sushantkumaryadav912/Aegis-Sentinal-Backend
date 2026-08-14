package com.aegis.identity.application.service;

import com.aegis.identity.application.command.LinkAccountCommand;
import com.aegis.identity.application.port.PasswordHasher;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAccountLinkService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final PasswordHasher passwordHasher;
    private final CreateSessionService createSessionService;

    public OAuthAccountLinkService(
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            PasswordHasher passwordHasher,
            CreateSessionService createSessionService) {

        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.passwordHasher = passwordHasher;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public AuthenticationResult execute(LinkAccountCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        IdentityProvider targetProvider = command.provider() != null ? command.provider() : IdentityProvider.GOOGLE;

        Optional<UserIdentity> existing = userIdentityRepository.findByProviderAndProviderSubject(targetProvider, command.providerSubject());
        if (existing.isPresent()) {
            if (existing.get().getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("External identity is already linked to this account");
            } else {
                throw new IllegalStateException("External identity is already linked to another account");
            }
        }

        UserIdentity newIdentity = new UserIdentity(
                user,
                targetProvider,
                command.providerSubject(),
                command.email()
        );
        userIdentityRepository.save(newIdentity);

        return createSessionService.issueTokensAndCreateSession(user);
    }

    @Transactional
    public UserIdentity linkAuthenticatedUser(UUID userId, IdentityProvider provider, String providerSubject, String providerEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        IdentityProvider targetProvider = provider != null ? provider : IdentityProvider.GOOGLE;

        Optional<UserIdentity> existing = userIdentityRepository.findByProviderAndProviderSubject(targetProvider, providerSubject);
        if (existing.isPresent()) {
            if (existing.get().getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("External identity is already linked to this account");
            } else {
                throw new IllegalStateException("External identity is already linked to another account");
            }
        }

        UserIdentity newIdentity = new UserIdentity(
                user,
                targetProvider,
                providerSubject,
                providerEmail != null ? providerEmail : user.getEmail()
        );
        return userIdentityRepository.save(newIdentity);
    }
}

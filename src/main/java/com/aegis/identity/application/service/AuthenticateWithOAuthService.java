package com.aegis.identity.application.service;

import com.aegis.identity.application.port.OAuthIdentityProvider;
import com.aegis.identity.application.port.OAuthUserInfo;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.application.query.OAuthAuthenticationResult;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateWithOAuthService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final TokenService tokenService;
    private final CreateSessionService createSessionService;

    public AuthenticateWithOAuthService(
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            TokenService tokenService,
            CreateSessionService createSessionService) {

        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.tokenService = tokenService;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public OAuthAuthenticationResult execute(
            OAuthIdentityProvider provider,
            OAuthUserInfo userInfo) {

        IdentityProvider identityProvider = provider.provider();

        UserIdentity existingIdentity =
                userIdentityRepository
                        .findByProviderAndProviderSubject(
                                identityProvider,
                                userInfo.subject())
                        .orElse(null);

        if (existingIdentity != null) {

            User user = existingIdentity.getUser();

            if (!user.isActive()) {
                throw new IllegalStateException(
                        "User account is inactive");
            }

            String accessToken = tokenService.generateAccessToken(user);
            String refreshToken = tokenService.generateRefreshToken(user);
            createSessionService.create(user, refreshToken);

            return new OAuthAuthenticationResult(
                    user,
                    accessToken,
                    refreshToken,
                    false
            );
        }

        User user = userRepository
                .findByEmail(userInfo.email())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "An Aegis account already exists for this email. "
                                        + "Explicit account linking is required."));

        UserIdentity identity = new UserIdentity(
                user,
                identityProvider,
                userInfo.subject(),
                userInfo.email()
        );

        userIdentityRepository.save(identity);

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        createSessionService.create(user, refreshToken);

        return new OAuthAuthenticationResult(
                user,
                accessToken,
                refreshToken,
                false
        );
    }
}

package com.aegis.identity.application.service;

import com.aegis.identity.application.command.LoginUserCommand;
import com.aegis.identity.application.port.PasswordHasher;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthenticateUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
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

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new AuthenticationResult(
                accessToken,
                refreshToken
        );
    }
}

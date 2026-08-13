package com.aegis.identity.application.service;

import com.aegis.identity.application.command.RegisterUserCommand;
import com.aegis.identity.application.port.PasswordHasher;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final CreateSessionService createSessionService;

    public RegisterUserService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordHasher passwordHasher,
            TokenService tokenService,
            CreateSessionService createSessionService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public AuthenticationResult execute(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Organization organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        String passwordHash = passwordHasher.hash(command.password());

        User user = User.create(
                organization,
                command.email(),
                passwordHash,
                command.firstName(),
                command.lastName()
        );

        User savedUser = userRepository.save(user);

        String accessToken = tokenService.generateAccessToken(savedUser);
        String refreshToken = tokenService.generateRefreshToken(savedUser);

        createSessionService.create(savedUser, refreshToken);

        return new AuthenticationResult(
                accessToken,
                refreshToken
        );
    }
}

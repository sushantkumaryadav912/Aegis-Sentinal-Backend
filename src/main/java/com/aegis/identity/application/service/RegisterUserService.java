package com.aegis.identity.application.service;

import com.aegis.identity.application.command.RegisterUserCommand;
import com.aegis.identity.application.port.PasswordHasher;
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

    public RegisterUserService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public User execute(RegisterUserCommand command) {
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

        return userRepository.save(user);
    }
}

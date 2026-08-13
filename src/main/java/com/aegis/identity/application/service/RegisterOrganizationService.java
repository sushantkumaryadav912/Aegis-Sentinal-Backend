package com.aegis.identity.application.service;

import com.aegis.identity.application.command.RegisterOrganizationCommand;
import com.aegis.identity.application.port.PasswordHasher;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.Role;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.entity.Workspace;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.domain.repository.RoleRepository;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.domain.repository.UserRoleRepository;
import com.aegis.identity.domain.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final CreateSessionService createSessionService;

    public RegisterOrganizationService(
            OrganizationRepository organizationRepository,
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordHasher passwordHasher,
            TokenService tokenService,
            CreateSessionService createSessionService) {

        this.organizationRepository = organizationRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public AuthenticationResult execute(RegisterOrganizationCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (organizationRepository.existsBySlug(command.organizationSlug())) {
            throw new IllegalArgumentException("Organization slug is already taken");
        }

        Organization organization = organizationRepository.save(
                Organization.create(command.organizationName(), command.organizationSlug())
        );

        if (workspaceRepository.existsByOrganizationIdAndSlug(organization.getId(), command.workspaceSlug())) {
            throw new IllegalArgumentException("Workspace slug is already taken for this organization");
        }

        Workspace workspace = workspaceRepository.save(
                Workspace.create(organization, command.workspaceName(), command.workspaceSlug())
        );

        String passwordHash = passwordHasher.hash(command.password());

        User user = userRepository.save(
                User.create(
                        organization,
                        command.email(),
                        passwordHash,
                        command.firstName(),
                        command.lastName()
                )
        );

        Role orgAdminRole = roleRepository.findByName("ORG_ADMIN")
                .orElseThrow(() -> new IllegalStateException("System role ORG_ADMIN not found"));

        userRoleRepository.save(
                UserRole.create(user, orgAdminRole, organization, workspace)
        );

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        createSessionService.create(user, refreshToken);

        return new AuthenticationResult(
                accessToken,
                refreshToken
        );
    }
}

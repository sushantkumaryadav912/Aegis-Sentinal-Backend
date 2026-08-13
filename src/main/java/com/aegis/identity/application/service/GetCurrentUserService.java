package com.aegis.identity.application.service;

import com.aegis.identity.application.query.UserContext;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.Permission;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.entity.Workspace;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.domain.repository.UserRoleRepository;
import com.aegis.identity.domain.repository.WorkspaceRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final WorkspaceRepository workspaceRepository;

    public GetCurrentUserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            WorkspaceRepository workspaceRepository) {

        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional(readOnly = true)
    public UserContext execute(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        List<String> roles = userRoles.stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Set<String> permissions = userRoles.stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Organization organization = user.getOrganization();
        if (organization != null) {
            organization.getName();
            organization.getSlug();
        }

        Workspace workspace = userRoles.stream()
                .map(UserRole::getWorkspace)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    if (organization != null) {
                        return workspaceRepository.findByOrganizationId(organization.getId())
                                .stream()
                                .findFirst()
                                .orElse(null);
                    }
                    return null;
                });

        if (workspace != null) {
            workspace.getName();
            workspace.getSlug();
        }

        return new UserContext(
                user,
                organization,
                workspace,
                roles,
                permissions
        );
    }
}

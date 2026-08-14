package com.aegis.identity.infrastructure.security.authorization;

import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.repository.UserRoleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("tenantSecurity")
public class TenantSecurityExpressionService {

    private final UserRoleRepository userRoleRepository;

    public TenantSecurityExpressionService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(UUID organizationId, String requiredPermission) {
        UUID userId = getCurrentUserId();
        if (userId == null || organizationId == null) {
            return false;
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
        if (userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .map(ur -> ur.getRole())
                .filter(role -> role != null && role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> perm.getName())
                .anyMatch(permName -> permName.equals(requiredPermission));
    }

    @Transactional(readOnly = true)
    public boolean hasWorkspacePermission(UUID organizationId, UUID workspaceId, String requiredPermission) {
        UUID userId = getCurrentUserId();
        if (userId == null || organizationId == null || workspaceId == null) {
            return false;
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
        if (userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .filter(ur -> ur.getWorkspace() == null || ur.getWorkspace().getId().equals(workspaceId))
                .map(ur -> ur.getRole())
                .filter(role -> role != null && role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> perm.getName())
                .anyMatch(permName -> permName.equals(requiredPermission));
    }

    @Transactional(readOnly = true)
    public boolean hasRole(UUID organizationId, String requiredRoleName) {
        UUID userId = getCurrentUserId();
        if (userId == null || organizationId == null) {
            return false;
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId);
        return userRoles.stream()
                .map(ur -> ur.getRole())
                .filter(role -> role != null)
                .map(role -> role.getName())
                .anyMatch(roleName -> roleName.equals(requiredRoleName));
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                return UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}

package com.aegis.identity.infrastructure.security.tenant;

import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.repository.UserRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantResolverFilter extends OncePerRequestFilter {

    public static final String HEADER_ORGANIZATION_ID = "X-Aegis-Organization-Id";
    public static final String HEADER_WORKSPACE_ID = "X-Aegis-Workspace-Id";

    private final UserRoleRepository userRoleRepository;

    public TenantResolverFilter(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String subject = jwt.getSubject();
                if (subject != null) {
                    UUID userId = UUID.fromString(subject);
                    UUID organizationId = parseUuidHeader(request, HEADER_ORGANIZATION_ID);
                    UUID workspaceId = parseUuidHeader(request, HEADER_WORKSPACE_ID);

                    if (organizationId == null || workspaceId == null) {
                        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
                        if (!userRoles.isEmpty()) {
                            UserRole primaryRole = userRoles.get(0);
                            if (organizationId == null && primaryRole.getOrganization() != null) {
                                organizationId = primaryRole.getOrganization().getId();
                            }
                            if (workspaceId == null && primaryRole.getWorkspace() != null) {
                                workspaceId = primaryRole.getWorkspace().getId();
                            }
                        }
                    }

                    TenantContext.set(new TenantContext(userId, organizationId, workspaceId));
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private UUID parseUuidHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.isBlank()) {
            try {
                return UUID.fromString(headerValue.trim());
            } catch (IllegalArgumentException e) {
                // Ignore invalid UUID header format
            }
        }
        return null;
    }
}

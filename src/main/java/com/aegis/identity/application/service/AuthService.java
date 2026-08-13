package com.aegis.identity.application.service;

import com.aegis.identity.api.dto.AuthResponse;
import com.aegis.identity.api.dto.LoginRequest;
import com.aegis.identity.api.dto.RefreshTokenRequest;
import com.aegis.identity.api.dto.RegisterRequest;
import com.aegis.identity.api.dto.UserProfileResponse;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.Permission;
import com.aegis.identity.domain.entity.Role;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.entity.Workspace;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.domain.repository.RoleRepository;
import com.aegis.identity.domain.repository.SessionRepository;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.domain.repository.UserRoleRepository;
import com.aegis.identity.domain.repository.WorkspaceRepository;
import com.aegis.identity.infrastructure.security.JwtTokenProvider;
import com.aegis.identity.infrastructure.security.UserPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }

        String orgSlug = generateSlug(request.getOrganizationName());
        if (organizationRepository.existsBySlug(orgSlug)) {
            orgSlug = orgSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Organization organization = organizationRepository.save(
            Organization.builder()
                .name(request.getOrganizationName())
                .slug(orgSlug)
                .build()
        );

        Workspace workspace = workspaceRepository.save(
            Workspace.builder()
                .organization(organization)
                .name("Default Workspace")
                .slug("default")
                .build()
        );

        User user = userRepository.save(
            User.builder()
                .organization(organization)
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isMfaEnabled(false)
                .build()
        );

        Role orgAdminRole = roleRepository.findByName("ORG_ADMIN")
            .orElseThrow(() -> new IllegalStateException("ORG_ADMIN system role not found"));

        userRoleRepository.save(
            UserRole.builder()
                .user(user)
                .role(orgAdminRole)
                .organization(organization)
                .workspace(workspace)
                .build()
        );

        Set<String> roles = Set.of(orgAdminRole.getName());
        Set<String> permissions = orgAdminRole.getPermissions().stream()
            .map(p -> p.getName())
            .collect(Collectors.toSet());

        return createAuthTokensAndSession(user, organization.getId(), roles, permissions, ipAddress, userAgent);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        for (UserRole ur : userRoles) {
            Role role = ur.getRole();
            roles.add(role.getName());
            for (Permission p : role.getPermissions()) {
                permissions.add(p.getName());
            }
        }

        return createAuthTokensAndSession(user, user.getOrganization().getId(), roles, permissions, ipAddress, userAgent);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        Session session = sessionRepository.findByRefreshTokenHash(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Session not found or revoked"));

        if (session.getRevoked() || session.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Session is expired or revoked");
        }

        User user = session.getUser();
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        for (UserRole ur : userRoles) {
            Role role = ur.getRole();
            roles.add(role.getName());
            for (Permission p : role.getPermissions()) {
                permissions.add(p.getName());
            }
        }

        // Revoke current session (Token rotation)
        session.setRevoked(true);
        sessionRepository.save(session);

        return createAuthTokensAndSession(user, user.getOrganization().getId(), roles, permissions, ipAddress, userAgent);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (StringUtils.isNotBlank(refreshToken)) {
            String tokenHash = hashToken(refreshToken);
            sessionRepository.findByRefreshTokenHash(tokenHash).ifPresent(session -> {
                session.setRevoked(true);
                sessionRepository.save(session);
            });
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        for (UserRole ur : userRoles) {
            Role role = ur.getRole();
            roles.add(role.getName());
            for (Permission p : role.getPermissions()) {
                permissions.add(p.getName());
            }
        }

        return UserProfileResponse.builder()
            .id(user.getId())
            .organizationId(user.getOrganization().getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .active(user.getIsActive())
            .mfaEnabled(user.getIsMfaEnabled())
            .roles(roles)
            .permissions(permissions)
            .build();
    }

    private AuthResponse createAuthTokensAndSession(
            User user, UUID orgId, Set<String> roles, Set<String> permissions, String ipAddress, String userAgent) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), orgId, user.getEmail(), roles, permissions);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        Instant expiresAt = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());

        sessionRepository.save(
            Session.builder()
                .user(user)
                .refreshTokenHash(hashToken(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(expiresAt)
                .revoked(false)
                .build()
        );

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .userId(user.getId())
            .organizationId(orgId)
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(roles)
            .build();
    }

    private String generateSlug(String input) {
        return input.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

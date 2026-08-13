package com.aegis.identity.api.controller;

import com.aegis.identity.api.dto.AuthenticationResponse;
import com.aegis.identity.api.dto.LoginRequest;
import com.aegis.identity.api.dto.LogoutRequest;
import com.aegis.identity.api.dto.OrganizationResponse;
import com.aegis.identity.api.dto.RefreshTokenRequest;
import com.aegis.identity.api.dto.RegisterRequest;
import com.aegis.identity.api.dto.UserResponse;
import com.aegis.identity.api.dto.WorkspaceResponse;
import com.aegis.identity.application.command.LoginUserCommand;
import com.aegis.identity.application.command.LogoutCommand;
import com.aegis.identity.application.command.RefreshTokenCommand;
import com.aegis.identity.application.command.RegisterOrganizationCommand;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.application.query.UserContext;
import com.aegis.identity.application.service.AuthenticateUserService;
import com.aegis.identity.application.service.GetCurrentUserService;
import com.aegis.identity.application.service.LogoutService;
import com.aegis.identity.application.service.RefreshTokenService;
import com.aegis.identity.application.service.RegisterOrganizationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aegis/v1/auth")
public class AuthController {

    private final RegisterOrganizationService registerOrganizationService;
    private final AuthenticateUserService authenticateUserService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;
    private final GetCurrentUserService getCurrentUserService;

    public AuthController(
            RegisterOrganizationService registerOrganizationService,
            AuthenticateUserService authenticateUserService,
            RefreshTokenService refreshTokenService,
            LogoutService logoutService,
            GetCurrentUserService getCurrentUserService) {

        this.registerOrganizationService = registerOrganizationService;
        this.authenticateUserService = authenticateUserService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
        this.getCurrentUserService = getCurrentUserService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse register(
            @Valid @RequestBody RegisterRequest request) {

        AuthenticationResult result =
                registerOrganizationService.execute(
                        new RegisterOrganizationCommand(
                                request.organizationName(),
                                request.organizationSlug(),
                                request.workspaceName(),
                                request.workspaceSlug(),
                                request.email(),
                                request.password(),
                                request.firstName(),
                                request.lastName()
                        )
                );

        return new AuthenticationResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer"
        );
    }

    @PostMapping("/login")
    public AuthenticationResponse login(
            @Valid @RequestBody LoginRequest request) {

        AuthenticationResult result =
                authenticateUserService.execute(
                        new LoginUserCommand(
                                request.email(),
                                request.password()
                        )
                );

        return new AuthenticationResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer"
        );
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthenticationResult result =
                refreshTokenService.execute(
                        new RefreshTokenCommand(
                                request.refreshToken()
                        )
                );

        return new AuthenticationResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer"
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody LogoutRequest request) {

        logoutService.execute(
                new LogoutCommand(
                        request.refreshToken()
                )
        );
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalArgumentException("Unauthenticated request");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        UserContext context = getCurrentUserService.execute(userId);

        OrganizationResponse orgResponse = context.organization() != null
                ? new OrganizationResponse(
                        context.organization().getId(),
                        context.organization().getName(),
                        context.organization().getSlug())
                : null;

        WorkspaceResponse wsResponse = context.workspace() != null
                ? new WorkspaceResponse(
                        context.workspace().getId(),
                        context.workspace().getName(),
                        context.workspace().getSlug())
                : null;

        return new UserResponse(
                context.user().getId(),
                context.user().getEmail(),
                context.user().getFirstName(),
                context.user().getLastName(),
                orgResponse,
                wsResponse,
                context.roles(),
                context.permissions()
        );
    }
}

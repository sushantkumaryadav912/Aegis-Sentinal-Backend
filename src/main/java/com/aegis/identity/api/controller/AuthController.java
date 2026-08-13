package com.aegis.identity.api.controller;

import com.aegis.identity.api.dto.AuthenticationResponse;
import com.aegis.identity.api.dto.LoginRequest;
import com.aegis.identity.api.dto.RegisterRequest;
import com.aegis.identity.application.command.LoginUserCommand;
import com.aegis.identity.application.command.RegisterOrganizationCommand;
import com.aegis.identity.application.query.AuthenticationResult;
import com.aegis.identity.application.service.AuthenticateUserService;
import com.aegis.identity.application.service.RegisterOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    public AuthController(
            RegisterOrganizationService registerOrganizationService,
            AuthenticateUserService authenticateUserService) {

        this.registerOrganizationService = registerOrganizationService;
        this.authenticateUserService = authenticateUserService;
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
}

package com.aegis.identity.infrastructure.security.oauth;

import com.aegis.identity.application.query.OAuthAuthenticationResult;
import com.aegis.identity.application.service.AuthenticateOAuthUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticateOAuthUserService authenticateOAuthUserService;
    private final ObjectMapper objectMapper;

    public OAuth2SuccessHandler(AuthenticateOAuthUserService authenticateOAuthUserService) {
        this.authenticateOAuthUserService = authenticateOAuthUserService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuthAuthenticationResult result = authenticateOAuthUserService.execute(authentication.getPrincipal());

        if (result.status() == OAuthAuthenticationResult.Status.SUCCESS) {
            String redirectUrl = String.format(
                    "/auth/oauth2/callback?accessToken=%s&refreshToken=%s",
                    URLEncoder.encode(result.authResult().accessToken(), StandardCharsets.UTF_8),
                    URLEncoder.encode(result.authResult().refreshToken(), StandardCharsets.UTF_8)
            );
            response.sendRedirect(redirectUrl);
            return;
        }

        if (result.status() == OAuthAuthenticationResult.Status.LINKING_REQUIRED) {
            String redirectUrl = String.format(
                    "/auth/link-account?email=%s&providerSubject=%s",
                    URLEncoder.encode(result.userInfo().email(), StandardCharsets.UTF_8),
                    URLEncoder.encode(result.userInfo().providerSubject(), StandardCharsets.UTF_8)
            );
            response.sendRedirect(redirectUrl);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result);
    }
}

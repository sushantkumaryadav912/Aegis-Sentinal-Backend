package com.aegis.identity.infrastructure.security.oauth;

import com.aegis.identity.application.port.OAuthIdentityProvider;
import com.aegis.identity.application.port.OAuthUserInfo;
import com.aegis.identity.application.service.AuthenticateWithOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticateWithOAuthService authenticationService;
    private final Map<String, OAuthIdentityProvider> providers;

    public OAuth2SuccessHandler(
            AuthenticateWithOAuthService authenticationService,
            GoogleOAuthIdentityProvider googleProvider) {

        this.authenticationService = authenticationService;

        this.providers = Map.of(
                "google", googleProvider
        );
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauth)) {
            throw new IllegalStateException(
                    "Unsupported OAuth authentication");
        }

        String registrationId =
                oauth.getAuthorizedClientRegistrationId();

        OAuthIdentityProvider provider =
                providers.get(registrationId);

        if (provider == null) {
            throw new IllegalStateException(
                    "Unsupported OAuth provider: " + registrationId);
        }

        OAuthUserInfo userInfo =
                provider.extractUserInfo(
                        oauth.getPrincipal());

        authenticationService.execute(
                provider,
                userInfo);

        response.sendRedirect("/");
    }
}

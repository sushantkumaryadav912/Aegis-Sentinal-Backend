package com.aegis.identity.infrastructure.security.oauth;

import com.aegis.identity.application.port.OAuthIdentityProvider;
import com.aegis.identity.application.port.OAuthUserInfo;
import com.aegis.identity.domain.model.IdentityProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuthIdentityProvider
        implements OAuthIdentityProvider {

    @Override
    public IdentityProvider provider() {
        return IdentityProvider.GOOGLE;
    }

    @Override
    public OAuthUserInfo extractUserInfo(Object principal) {

        if (!(principal instanceof OAuth2User user)) {
            throw new IllegalArgumentException(
                    "Unsupported OAuth principal");
        }

        String subject = user.getAttribute("sub");
        String email = user.getAttribute("email");
        String firstName = user.getAttribute("given_name");
        String lastName = user.getAttribute("family_name");

        if (subject == null || email == null) {
            throw new IllegalStateException(
                    "Google did not provide required identity information");
        }

        return new OAuthUserInfo(
                subject,
                email,
                firstName,
                lastName
        );
    }
}

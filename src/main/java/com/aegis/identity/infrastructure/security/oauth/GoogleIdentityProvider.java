package com.aegis.identity.infrastructure.security.oauth;

import com.aegis.identity.application.port.OAuthIdentityProvider;
import com.aegis.identity.application.port.OAuthUserInfo;
import com.aegis.identity.domain.model.IdentityProvider;
import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdentityProvider implements OAuthIdentityProvider {

    @Override
    public IdentityProvider provider() {
        return IdentityProvider.GOOGLE;
    }

    @Override
    public boolean supports(Object principal) {
        if (principal instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Object sub = attributes.get("sub");
            return sub instanceof String s && !s.isBlank();
        }
        return false;
    }

    @Override
    public OAuthUserInfo extractUserInfo(Object principal) {
        if (!(principal instanceof OAuth2User oAuth2User)) {
            throw new IllegalArgumentException("Principal must be an instance of OAuth2User");
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String sub = (String) attributes.get("sub");
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("Google OAuth principal missing required 'sub' attribute");
        }

        String email = (String) attributes.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google OAuth principal missing required 'email' attribute");
        }

        Object emailVerifiedObj = attributes.get("email_verified");
        boolean emailVerified = true;
        if (emailVerifiedObj instanceof Boolean b) {
            emailVerified = b;
        } else if (emailVerifiedObj instanceof String s) {
            emailVerified = Boolean.parseBoolean(s);
        }

        if (!emailVerified) {
            throw new IllegalArgumentException("Google OAuth account email is unverified");
        }

        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");

        if (firstName == null && attributes.get("name") instanceof String name) {
            String[] parts = name.split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        return new OAuthUserInfo(
                IdentityProvider.GOOGLE,
                sub,
                email,
                firstName != null ? firstName : "Google",
                lastName != null ? lastName : "User",
                picture
        );
    }
}

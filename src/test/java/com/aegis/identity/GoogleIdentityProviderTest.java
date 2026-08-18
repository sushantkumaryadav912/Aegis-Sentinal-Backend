package com.aegis.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aegis.identity.application.port.OAuthUserInfo;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.infrastructure.security.oauth.GoogleIdentityProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class GoogleIdentityProviderTest {

  private GoogleIdentityProvider provider;

  @BeforeEach
  void setUp() {
    provider = new GoogleIdentityProvider();
  }

  @Test
  @DisplayName("Should extract valid Google OAuthUserInfo correctly")
  void testExtractUserInfo_validGooglePrincipal() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", "google-sub-12345");
    attributes.put("email", "user@example.com");
    attributes.put("email_verified", true);
    attributes.put("given_name", "Jane");
    attributes.put("family_name", "Doe");
    attributes.put("picture", "https://example.com/avatar.jpg");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    assertThat(provider.supports(principal)).isTrue();

    OAuthUserInfo userInfo = provider.extractUserInfo(principal);

    assertThat(userInfo.provider()).isEqualTo(IdentityProvider.GOOGLE);
    assertThat(userInfo.providerSubject()).isEqualTo("google-sub-12345");
    assertThat(userInfo.email()).isEqualTo("user@example.com");
    assertThat(userInfo.firstName()).isEqualTo("Jane");
    assertThat(userInfo.lastName()).isEqualTo("Doe");
    assertThat(userInfo.pictureUrl()).isEqualTo("https://example.com/avatar.jpg");
  }

  @Test
  @DisplayName("Should reject Google principal with missing or blank sub attribute")
  void testExtractUserInfo_missingSub_throwsException() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "user@example.com");
    attributes.put("given_name", "Jane");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "email");

    assertThat(provider.supports(principal)).isFalse();
    assertThatThrownBy(() -> provider.extractUserInfo(principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing required 'sub' attribute");
  }

  @Test
  @DisplayName("Should reject Google principal with unverified email")
  void testExtractUserInfo_unverifiedEmail_throwsException() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", "google-sub-999");
    attributes.put("email", "unverified@example.com");
    attributes.put("email_verified", false);

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    assertThat(provider.supports(principal)).isTrue();
    assertThatThrownBy(() -> provider.extractUserInfo(principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email is unverified");
  }

  @Test
  @DisplayName("Should return false for non-matching principal in supports()")
  void testSupports_invalidPrincipal_returnsFalse() {
    assertThat(provider.supports("invalid-principal-string")).isFalse();

    Map<String, Object> attributesWithoutSub = new HashMap<>();
    attributesWithoutSub.put("id", "github-12345");
    OAuth2User principalWithoutSub =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributesWithoutSub,
            "id");
    assertThat(provider.supports(principalWithoutSub)).isFalse();
  }
}

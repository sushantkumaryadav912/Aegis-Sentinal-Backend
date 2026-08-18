package com.aegis.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aegis.identity.application.query.OAuthAuthenticationResult;
import com.aegis.identity.application.service.AuthenticateOAuthUserService;
import com.aegis.identity.application.service.OAuthAccountLinkService;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.SessionRepository;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.infrastructure.security.oauth.GoogleIdentityProvider;
import com.aegis.identity.infrastructure.security.oauth.OAuth2FailureHandler;
import com.aegis.identity.infrastructure.security.oauth.OAuth2SuccessHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@SpringBootTest
class OAuthSecurityHardeningTest {

  @Autowired private GoogleIdentityProvider googleIdentityProvider;

  @Autowired private AuthenticateOAuthUserService authenticateOAuthUserService;

  @Autowired private OAuthAccountLinkService oAuthAccountLinkService;

  @Autowired private UserRepository userRepository;

  @Autowired private UserIdentityRepository userIdentityRepository;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private com.aegis.identity.application.port.RefreshTokenHasher refreshTokenHasher;

  @Autowired private OAuth2SuccessHandler oAuth2SuccessHandler;

  @Autowired private OAuth2FailureHandler oAuth2FailureHandler;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();
  }

  @Test
  @DisplayName("Security: Google identity extraction must reject missing sub attribute")
  void testSecurity_missingSub_rejected() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "nosub@google.test");
    attributes.put("email_verified", true);

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "email");

    assertThat(googleIdentityProvider.supports(principal)).isFalse();
    assertThatThrownBy(() -> googleIdentityProvider.extractUserInfo(principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing required 'sub' attribute");
  }

  @Test
  @DisplayName("Security: Google identity extraction must reject unverified email addresses")
  void testSecurity_unverifiedEmail_rejected() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", "sub-unverified-123");
    attributes.put("email", "unverified@google.test");
    attributes.put("email_verified", false);

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    assertThatThrownBy(() -> googleIdentityProvider.extractUserInfo(principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email is unverified");
  }

  @Test
  @DisplayName(
      "Security: Account linking must reject identity already linked to another user with Conflict error")
  void testSecurity_linkIdentityOwnedByAnotherUser_throwsConflict() {
    String sharedSub = "shared-security-sub-" + UUID.randomUUID();
    String user1Email = "sec1-" + UUID.randomUUID() + "@aegis.test";
    String user2Email = "sec2-" + UUID.randomUUID() + "@aegis.test";

    User u1 =
        userRepository.save(
            User.create(testUser.getOrganization(), user1Email, "pwd", "Sec", "One"));
    User u2 =
        userRepository.save(
            User.create(testUser.getOrganization(), user2Email, "pwd", "Sec", "Two"));

    oAuthAccountLinkService.linkAuthenticatedUser(
        u1.getId(), IdentityProvider.GOOGLE, sharedSub, user1Email);

    assertThatThrownBy(
            () ->
                oAuthAccountLinkService.linkAuthenticatedUser(
                    u2.getId(), IdentityProvider.GOOGLE, sharedSub, user2Email))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already linked to another account");
  }

  @Test
  @DisplayName("Security: Account linking must reject duplicate linking attempts to the same user")
  void testSecurity_duplicateIdentityLinking_throwsConflict() {
    String dupSub = "dup-security-sub-" + UUID.randomUUID();

    oAuthAccountLinkService.linkAuthenticatedUser(
        testUser.getId(), IdentityProvider.GOOGLE, dupSub, testUser.getEmail());

    assertThatThrownBy(
            () ->
                oAuthAccountLinkService.linkAuthenticatedUser(
                    testUser.getId(), IdentityProvider.GOOGLE, dupSub, testUser.getEmail()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already linked to this account");
  }

  @Test
  @DisplayName("Security: Disabled Aegis user must be rejected from OAuth authentication")
  void testSecurity_disabledUser_rejected() {
    String googleSub = "disabled-user-sub-" + UUID.randomUUID();
    String disabledEmail = "disabled-" + UUID.randomUUID() + "@aegis.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", disabledEmail);
    attributes.put("given_name", "Disabled");
    attributes.put("family_name", "User");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    authenticateOAuthUserService.execute(principal);

    User user = userRepository.findByEmail(disabledEmail).orElseThrow();
    user.setIsActive(false);
    userRepository.save(user);

    assertThatThrownBy(() -> authenticateOAuthUserService.execute(principal))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("inactive");
  }

  @Test
  @DisplayName("Security: Raw refresh token must NEVER be stored unhashed in database sessions")
  void testSecurity_rawRefreshToken_neverStoredInDb() {
    String googleSub = "refresh-hash-sub-" + UUID.randomUUID();
    String email = "hash-test-" + UUID.randomUUID() + "@aegis.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", email);
    attributes.put("given_name", "Hash");
    attributes.put("family_name", "Test");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult result = authenticateOAuthUserService.execute(principal);
    assertThat(result.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);

    String rawRefreshToken = result.authResult().refreshToken();
    assertThat(rawRefreshToken).isNotBlank();

    Optional<Session> rawMatch = sessionRepository.findByRefreshTokenHash(rawRefreshToken);
    assertThat(rawMatch).isEmpty();

    String expectedHash = refreshTokenHasher.hash(rawRefreshToken);
    Optional<Session> hashedMatch = sessionRepository.findByRefreshTokenHash(expectedHash);
    assertThat(hashedMatch).isPresent();
  }

  @Test
  @DisplayName(
      "Security: Identical providerSubject across different IdentityProvider enums must remain isolated")
  void testSecurity_providerSubject_isolatedByProviderEnum() {
    String sharedSubject = "shared-sub-key-" + UUID.randomUUID();

    userIdentityRepository.save(
        new UserIdentity(testUser, IdentityProvider.GOOGLE, sharedSubject, testUser.getEmail()));

    Optional<UserIdentity> googleMatch =
        userIdentityRepository.findByProviderAndProviderSubject(
            IdentityProvider.GOOGLE, sharedSubject);
    Optional<UserIdentity> githubMatch =
        userIdentityRepository.findByProviderAndProviderSubject(
            IdentityProvider.GITHUB, sharedSubject);

    assertThat(googleMatch).isPresent();
    assertThat(githubMatch).isEmpty();
  }

  @Test
  @DisplayName(
      "Security: OAuth2FailureHandler outputs clean 401 JSON without exposing stack traces")
  void testSecurity_oAuth2FailureHandler_cleanResponse() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    OAuth2AuthenticationException ex =
        new OAuth2AuthenticationException(
            new OAuth2Error("invalid_state"), "Invalid state parameter");

    oAuth2FailureHandler.onAuthenticationFailure(request, response, ex);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("OAuth Authentication Failed");
    assertThat(response.getContentAsString()).doesNotContain("Exception");
  }

  @Test
  @DisplayName(
      "Security: OAuth2SuccessHandler prevents open redirects by redirecting exclusively to internal application paths")
  void testSecurity_oAuth2SuccessHandler_preventsOpenRedirect() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("redirect", "https://attacker-website.com/steal-tokens");
    MockHttpServletResponse response = new MockHttpServletResponse();

    String googleSub = "open-redirect-sub-" + UUID.randomUUID();
    String email = "redirect-test-" + UUID.randomUUID() + "@aegis.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", email);

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken auth =
        new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
            principal, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), "google");

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, auth);

    assertThat(response.getRedirectedUrl()).isNotNull();
    assertThat(response.getRedirectedUrl()).startsWith("/auth/oauth2/callback");
    assertThat(response.getRedirectedUrl()).doesNotContain("attacker-website.com");
  }
}

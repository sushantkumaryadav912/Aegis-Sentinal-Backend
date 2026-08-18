package com.aegis.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.identity.api.dto.LinkAccountRequest;
import com.aegis.identity.application.query.OAuthAuthenticationResult;
import com.aegis.identity.application.service.AuthenticateOAuthUserService;
import com.aegis.identity.application.service.OAuthAccountLinkService;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.domain.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class OAuthAuthenticationIntegrationTest {

  @Autowired private WebApplicationContext context;

  @Autowired private AuthenticateOAuthUserService authenticateOAuthUserService;

  @Autowired private UserRepository userRepository;

  @Autowired private UserIdentityRepository userIdentityRepository;

  @Autowired private UserRoleRepository userRoleRepository;

  @Autowired private OAuthAccountLinkService oAuthAccountLinkService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("Should trigger automated tenant onboarding for new Google OAuth user")
  void testNewGoogleUser_triggersTenantOnboarding() {
    String uniqueSub = "google-sub-" + UUID.randomUUID();
    String uniqueEmail = "new-oauth-user-" + UUID.randomUUID() + "@google.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", uniqueSub);
    attributes.put("email", uniqueEmail);
    attributes.put("given_name", "OAuth");
    attributes.put("family_name", "Tester");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult result = authenticateOAuthUserService.execute(principal);

    assertThat(result.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);
    assertThat(result.authResult()).isNotNull();
    assertThat(result.authResult().accessToken()).isNotBlank();
    assertThat(result.authResult().refreshToken()).isNotBlank();

    Optional<User> createdUser = userRepository.findByEmail(uniqueEmail);
    assertThat(createdUser).isPresent();
    assertThat(createdUser.get().getFirstName()).isEqualTo("OAuth");
    assertThat(createdUser.get().getLastName()).isEqualTo("Tester");

    Optional<UserIdentity> createdIdentity =
        userIdentityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, uniqueSub);
    assertThat(createdIdentity).isPresent();
    assertThat(createdIdentity.get().getProvider()).isEqualTo(IdentityProvider.GOOGLE);

    var roles = userRoleRepository.findByUserId(createdUser.get().getId());
    assertThat(roles).isNotEmpty();
    assertThat(roles.get(0).getRole().getName()).isEqualTo("ORG_ADMIN");
  }

  @Test
  @DisplayName(
      "Should enforce account linking requirement when Google email matches local user without linked identity")
  void testGoogleLoginWithMatchingLocalEmail_requiresAccountLinking() {
    String googleSub = "unlinked-sub-" + UUID.randomUUID();
    String existingEmail = "admin@aegis-demo.local";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", existingEmail);
    attributes.put("given_name", "Admin");
    attributes.put("family_name", "User");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult result = authenticateOAuthUserService.execute(principal);

    assertThat(result.status()).isEqualTo(OAuthAuthenticationResult.Status.LINKING_REQUIRED);
    assertThat(result.userInfo().email()).isEqualTo(existingEmail);
  }

  @Test
  @DisplayName("Should link Google identity to local user after password confirmation via REST API")
  void testAccountLinking_withValidPassword_linksIdentityAndIssuesTokens() throws Exception {
    String providerSubject = "link-sub-" + UUID.randomUUID();
    LinkAccountRequest request =
        new LinkAccountRequest("admin@aegis-demo.local", "StrongPassword123!", providerSubject);

    mockMvc
        .perform(
            post("/api/aegis/v1/auth/link-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Optional<UserIdentity> linkedIdentity =
        userIdentityRepository.findByProviderAndProviderSubject(
            IdentityProvider.GOOGLE, providerSubject);
    assertThat(linkedIdentity).isPresent();
  }

  @Test
  @DisplayName(
      "Should successfully authenticate existing Google OAuth identity and issue session + JWT pair")
  void testExistingOAuthUser_logsInSuccessfully() {
    String googleSub = "existing-sub-" + UUID.randomUUID();
    String googleEmail = "existing-oauth-" + UUID.randomUUID() + "@google.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", googleEmail);
    attributes.put("given_name", "Existing");
    attributes.put("family_name", "OAuthUser");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult firstResult = authenticateOAuthUserService.execute(principal);
    assertThat(firstResult.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);

    OAuthAuthenticationResult secondResult = authenticateOAuthUserService.execute(principal);

    assertThat(secondResult.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);
    assertThat(secondResult.authResult()).isNotNull();
    assertThat(secondResult.authResult().accessToken()).isNotBlank();
    assertThat(secondResult.authResult().refreshToken()).isNotBlank();

    Optional<UserIdentity> identity =
        userIdentityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, googleSub);
    assertThat(identity).isPresent();
    assertThat(identity.get().getProviderEmail()).isEqualTo(googleEmail);
  }

  @Test
  @DisplayName("Should reject authentication for inactive user with existing OAuth identity")
  void testExistingOAuthUser_inactiveAccount_throwsException() {
    String googleSub = "inactive-sub-" + UUID.randomUUID();
    String googleEmail = "inactive-oauth-" + UUID.randomUUID() + "@google.test";

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleSub);
    attributes.put("email", googleEmail);
    attributes.put("given_name", "Inactive");
    attributes.put("family_name", "OAuthUser");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult setupResult = authenticateOAuthUserService.execute(principal);
    assertThat(setupResult.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);

    User user = userRepository.findByEmail(googleEmail).orElseThrow();
    user.setIsActive(false);
    userRepository.save(user);

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> authenticateOAuthUserService.execute(principal))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("inactive");
  }

  @Test
  @DisplayName(
      "Should successfully link Google identity with different email and preserve tenant membership without alteration")
  void testLinkAuthenticatedUser_success_doesNotAlterTenantContext() {
    User user = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();
    UUID originalOrgId = user.getOrganization().getId();

    String providerSubject = "google-personal-sub-" + UUID.randomUUID();
    String personalEmail = "personal-oauth-" + UUID.randomUUID() + "@gmail.com";

    UserIdentity linkedIdentity =
        oAuthAccountLinkService.linkAuthenticatedUser(
            user.getId(), IdentityProvider.GOOGLE, providerSubject, personalEmail);

    assertThat(linkedIdentity).isNotNull();
    assertThat(linkedIdentity.getUser().getId()).isEqualTo(user.getId());
    assertThat(linkedIdentity.getProviderEmail()).isEqualTo(personalEmail);

    User updatedUser = userRepository.findById(user.getId()).orElseThrow();
    assertThat(updatedUser.getOrganization().getId()).isEqualTo(originalOrgId);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", providerSubject);
    attributes.put("email", personalEmail);
    attributes.put("given_name", "Admin");

    OAuth2User principal =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");

    OAuthAuthenticationResult authResult = authenticateOAuthUserService.execute(principal);
    assertThat(authResult.status()).isEqualTo(OAuthAuthenticationResult.Status.SUCCESS);
  }

  @Test
  @DisplayName("Should reject linking when Google identity already belongs to another user")
  void testLinkAccount_conflictWhenIdentityAlreadyBelongsToAnotherUser() {
    String sharedSub = "shared-sub-" + UUID.randomUUID();
    String user1Email = "user1-" + UUID.randomUUID() + "@aegis.test";
    String user2Email = "user2-" + UUID.randomUUID() + "@aegis.test";

    User user1 =
        userRepository.save(
            User.create(
                userRepository
                    .findByEmail("admin@aegis-demo.local")
                    .orElseThrow()
                    .getOrganization(),
                user1Email,
                "hash",
                "User",
                "One"));
    User user2 =
        userRepository.save(
            User.create(
                userRepository
                    .findByEmail("admin@aegis-demo.local")
                    .orElseThrow()
                    .getOrganization(),
                user2Email,
                "hash",
                "User",
                "Two"));

    oAuthAccountLinkService.linkAuthenticatedUser(
        user1.getId(), IdentityProvider.GOOGLE, sharedSub, user1Email);

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () ->
                oAuthAccountLinkService.linkAuthenticatedUser(
                    user2.getId(), IdentityProvider.GOOGLE, sharedSub, user2Email))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already linked to another account");
  }

  @Test
  @DisplayName("Should reject linking when Google identity is already linked to the same user")
  void testLinkAccount_conflictWhenIdentityAlreadyLinkedToSameUser() {
    String ownSub = "own-sub-" + UUID.randomUUID();
    User user = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();

    oAuthAccountLinkService.linkAuthenticatedUser(
        user.getId(), IdentityProvider.GOOGLE, ownSub, user.getEmail());

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () ->
                oAuthAccountLinkService.linkAuthenticatedUser(
                    user.getId(), IdentityProvider.GOOGLE, ownSub, user.getEmail()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already linked to this account");
  }
}

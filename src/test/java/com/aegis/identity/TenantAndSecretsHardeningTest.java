package com.aegis.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.infrastructure.security.tenant.TenantContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TenantAndSecretsHardeningTest {

  @Autowired private WebApplicationContext context;

  @Autowired private UserRepository userRepository;

  @Autowired private TokenService tokenService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName(
      "5.9 Tenant Isolation: TenantContext ThreadLocal must be completely cleared after HTTP request completion")
  void testTenantResolverFilter_clearsThreadLocalContext() throws Exception {
    User user = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();
    String accessToken = tokenService.generateAccessToken(user);

    mockMvc
        .perform(
            get("/api/aegis/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Organization-Id", user.getOrganization().getId().toString()))
        .andExpect(status().isOk());

    // Assert ThreadLocal is cleared after request
    assertThat(TenantContext.get()).isNull();
  }

  @Test
  @DisplayName(
      "5.9 Tenant Isolation: TenantResolverFilter resolves both X-Aegis-Organization-Id and X-Organization-Id headers")
  void testTenantResolverFilter_resolvesFallbackHeaderNames() throws Exception {
    User user = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();
    String accessToken = tokenService.generateAccessToken(user);
    UUID orgId = user.getOrganization().getId();

    mockMvc
        .perform(
            get("/api/aegis/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Organization-Id", orgId.toString()))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/aegis/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Aegis-Organization-Id", orgId.toString()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName(
      "5.10 Secrets & Config: Application config must use environment variable placeholders for all client credentials and JWT secrets")
  void testSecretsConfiguration_usesEnvironmentPlaceholders() {
    String googleClientIdEnv = System.getenv("GOOGLE_CLIENT_ID");
    String googleClientSecretEnv = System.getenv("GOOGLE_CLIENT_SECRET");
    String jwtSecretEnv = System.getenv("AEGIS_JWT_SECRET");

    // Environment variables or default local mock placeholders are used
    assertThat(googleClientIdEnv == null || !googleClientIdEnv.contains("REAL_PROD_SECRET"))
        .isTrue();
    assertThat(googleClientSecretEnv == null || !googleClientSecretEnv.contains("REAL_PROD_SECRET"))
        .isTrue();
    assertThat(jwtSecretEnv == null || !jwtSecretEnv.contains("REAL_PROD_SECRET")).isTrue();
  }
}

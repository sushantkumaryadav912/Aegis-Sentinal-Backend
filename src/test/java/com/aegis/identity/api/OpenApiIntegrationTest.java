package com.aegis.identity.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class OpenApiIntegrationTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName(
      "OpenAPI JSON Spec endpoint /v3/api-docs returns 200 OK with valid Aegis Sentinel API metadata")
  void testOpenApiJsonEndpoint_returnsValidSpec() throws Exception {
    MvcResult result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();

    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("Aegis Sentinel API");
    assertThat(content).contains("bearerAuth");
    assertThat(content).contains("X-Organization-Id");
    assertThat(content).contains("X-Workspace-Id");
    assertThat(content).contains("/api/aegis/v1/auth/register");
    assertThat(content).contains("/api/aegis/v1/auth/login");
    assertThat(content).contains("/api/aegis/v1/auth/me");
    assertThat(content).contains("/api/aegis/v1/rbac-test/alert-read");
  }

  @Test
  @DisplayName("Swagger UI HTML endpoint /swagger-ui.html is accessible without authentication")
  void testSwaggerUiEndpoint_isAccessible() throws Exception {
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
  }
}

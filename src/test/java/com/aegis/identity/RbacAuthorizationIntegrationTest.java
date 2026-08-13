package com.aegis.identity;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class RbacAuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private MockMvc mockMvc;
    private String validAccessToken;
    private UUID validOrgId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User adminUser = userRepository.findByEmail("admin@aegis-demo.local")
                .orElseThrow(() -> new IllegalStateException("Test seed user not found"));

        validAccessToken = tokenService.generateAccessToken(adminUser);

        Organization org = organizationRepository.findBySlug("aegis-demo")
                .orElseThrow(() -> new IllegalStateException("Test organization seed not found"));
        validOrgId = org.getId();
    }

    @Test
    @DisplayName("Should return 200 OK when user possesses required permission authority")
    void testValidPermission_returns200OK() throws Exception {
        mockMvc.perform(get("/api/aegis/v1/rbac-test/alert-read")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user lacks unassigned permission authority")
    void testMissingPermission_returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/aegis/v1/rbac-test/unassigned-permission")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 200 OK when user has permission in target organization")
    void testValidOrganization_returns200OK() throws Exception {
        mockMvc.perform(get("/api/aegis/v1/rbac-test/orgs/" + validOrgId + "/alerts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user attempts cross-tenant access to unassigned organization")
    void testWrongOrganization_returns403Forbidden() throws Exception {
        UUID wrongOrgId = UUID.randomUUID();

        mockMvc.perform(get("/api/aegis/v1/rbac-test/orgs/" + wrongOrgId + "/alerts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user attempts access to unassigned workspace")
    void testWrongWorkspace_returns403Forbidden() throws Exception {
        UUID wrongWorkspaceId = UUID.randomUUID();

        mockMvc.perform(get("/api/aegis/v1/rbac-test/orgs/" + validOrgId + "/workspaces/" + wrongWorkspaceId + "/alerts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when request lacks JWT Authorization header")
    void testNoJwt_returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/aegis/v1/rbac-test/alert-read"))
                .andExpect(status().isUnauthorized());
    }
}

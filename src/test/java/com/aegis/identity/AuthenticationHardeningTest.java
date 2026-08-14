package com.aegis.identity;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.identity.api.dto.LoginRequest;
import com.aegis.identity.api.dto.RegisterRequest;
import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthenticationHardeningTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Hardening: Using a refresh token as an access token on protected routes must be rejected")
    void testRefreshJwt_usedAsAccessJwt_isRejected() throws Exception {
        User user = userRepository.findByEmail("admin@aegis-demo.local").orElseThrow();
        String refreshToken = tokenService.generateRefreshToken(user);

        mockMvc.perform(get("/api/aegis/v1/auth/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Hardening: Password length policy (8-128 chars) must be enforced on registration")
    void testPasswordLengthValidation_shortPassword_rejected() throws Exception {
        RegisterRequest shortPwdRequest = new RegisterRequest(
                "Short Pwd Org", "short-pwd-org-" + UUID.randomUUID(),
                "Default Workspace", "default",
                "shortpwd-" + UUID.randomUUID() + "@aegis.test",
                "short", // < 8 characters
                "Short", "User"
        );

        mockMvc.perform(post("/api/aegis/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPwdRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Hardening: Excessively long password (>128 chars) must be rejected to prevent BCrypt DoS")
    void testPasswordLengthValidation_longPassword_rejected() throws Exception {
        String longPwd = "a".repeat(129); // > 128 characters
        RegisterRequest longPwdRequest = new RegisterRequest(
                "Long Pwd Org", "long-pwd-org-" + UUID.randomUUID(),
                "Default Workspace", "default",
                "longpwd-" + UUID.randomUUID() + "@aegis.test",
                longPwd,
                "Long", "User"
        );

        mockMvc.perform(post("/api/aegis/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longPwdRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Hardening: Invalid login credentials must return 401 Unauthorized without leaking detail")
    void testInvalidLogin_returns401() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("admin@aegis-demo.local", "WrongPassword123!");

        mockMvc.perform(post("/api/aegis/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }
}

package com.aegis.identity.infrastructure.security.config;

import com.aegis.identity.infrastructure.security.authorization.DatabaseGrantedAuthoritiesConverter;
import com.aegis.identity.infrastructure.security.jwt.JwtProperties;
import com.aegis.identity.infrastructure.security.tenant.TenantResolverFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

    private final DatabaseGrantedAuthoritiesConverter authoritiesConverter;
    private final TenantResolverFilter tenantResolverFilter;

    public SecurityConfiguration(
            DatabaseGrantedAuthoritiesConverter authoritiesConverter,
            TenantResolverFilter tenantResolverFilter) {

        this.authoritiesConverter = authoritiesConverter;
        this.tenantResolverFilter = tenantResolverFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health/**",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/api/aegis/v1/auth/login",
                                "/api/aegis/v1/auth/register",
                                "/api/aegis/v1/auth/refresh",
                                "/api/aegis/v1/auth/logout"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .addFilterAfter(tenantResolverFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}

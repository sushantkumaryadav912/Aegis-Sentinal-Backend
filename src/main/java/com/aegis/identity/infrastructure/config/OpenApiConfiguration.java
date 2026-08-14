package com.aegis.identity.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Aegis Sentinel API")
                .description(
                    "Enterprise Cloud Security Platform — Identity, Access"
                        + " Management, and Operational Security Domains API"
                        + " Specification.")
                .version("v1.0.0")
                .contact(new Contact().name("Aegis Security Team").email("security@aegis.cloud"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "JWT Access Token authorization header using" + " Bearer scheme."))
                .addSecuritySchemes(
                    "X-Organization-Id",
                    new SecurityScheme()
                        .name("X-Organization-Id")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Organization identifier UUID for tenant" + " scoping."))
                .addSecuritySchemes(
                    "X-Workspace-Id",
                    new SecurityScheme()
                        .name("X-Workspace-Id")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Workspace identifier UUID for sub-tenant" + " scoping.")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}

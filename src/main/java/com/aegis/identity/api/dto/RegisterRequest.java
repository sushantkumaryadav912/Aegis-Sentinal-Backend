package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description =
        "Request payload for registering a new tenant organization, workspace, and initial admin user")
public record RegisterRequest(
    @Schema(
            description = "Full display name of the organization",
            example = "Acme Cyber Security Corp")
        @NotBlank
        @Size(max = 255)
        String organizationName,
    @Schema(
            description = "URL-friendly slug identifier for the organization",
            example = "acme-cyber")
        @NotBlank
        @Size(max = 100)
        String organizationSlug,
    @Schema(
            description = "Default workspace name within the organization",
            example = "Production Operations")
        @NotBlank
        @Size(max = 255)
        String workspaceName,
    @Schema(description = "URL-friendly slug identifier for the workspace", example = "prod-ops")
        @NotBlank
        @Size(max = 100)
        String workspaceSlug,
    @Schema(description = "Admin user email address", example = "admin@acme-cyber.cloud")
        @NotBlank
        @Email
        String email,
    @Schema(description = "Admin user account password (min 8 chars)", example = "P@ssw0rd2026!")
        @NotBlank
        @Size(min = 8, max = 128)
        String password,
    @Schema(description = "Admin user first name", example = "Jane") @NotBlank @Size(max = 100)
        String firstName,
    @Schema(description = "Admin user last name", example = "Doe") @NotBlank @Size(max = 100)
        String lastName) {}

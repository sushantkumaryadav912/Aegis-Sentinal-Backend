package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(
    description =
        "Detailed user profile response with assigned tenant context, roles, and granted permissions")
public record UserResponse(
    @Schema(
            description = "User account UUID primary key",
            example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
    @Schema(description = "User email address", example = "admin@acme-cyber.cloud") String email,
    @Schema(description = "User first name", example = "Jane") String firstName,
    @Schema(description = "User last name", example = "Doe") String lastName,
    @Schema(description = "Associated tenant organization details")
        OrganizationResponse organization,
    @Schema(description = "Associated tenant workspace details") WorkspaceResponse workspace,
    @Schema(
            description = "List of assigned RBAC role names",
            example = "[\"ORG_ADMIN\", \"WORKSPACE_ADMIN\"]")
        List<String> roles,
    @Schema(
            description = "Set of fine-grained granted permission strings",
            example = "[\"alert:read\", \"alert:write\", \"workflow:execute\"]")
        Set<String> permissions) {}

package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Tenant organization metadata")
public record OrganizationResponse(
    @Schema(
            description = "Organization UUID primary key",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,
    @Schema(description = "Organization display name", example = "Acme Cyber Security Corp")
        String name,
    @Schema(description = "Organization URL-friendly slug", example = "acme-cyber") String slug) {}

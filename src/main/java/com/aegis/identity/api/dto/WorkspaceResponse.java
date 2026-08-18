package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Tenant workspace metadata")
public record WorkspaceResponse(
    @Schema(
            description = "Workspace UUID primary key",
            example = "f8e7d6c5-b4a3-9281-7065-43210fedcba9")
        UUID id,
    @Schema(description = "Workspace display name", example = "Production Operations") String name,
    @Schema(description = "Workspace URL-friendly slug", example = "prod-ops") String slug) {}

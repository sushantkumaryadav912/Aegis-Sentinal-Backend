package com.aegis.identity.api.dto;

import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        String slug
) {
}

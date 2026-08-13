package com.aegis.identity.api.dto;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug
) {
}

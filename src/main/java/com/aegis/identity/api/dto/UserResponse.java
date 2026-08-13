package com.aegis.identity.api.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        OrganizationResponse organization,
        WorkspaceResponse workspace,
        List<String> roles,
        Set<String> permissions
) {
}

package com.aegis.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(max = 255)
        String organizationName,

        @NotBlank
        @Size(max = 100)
        String organizationSlug,

        @NotBlank
        @Size(max = 255)
        String workspaceName,

        @NotBlank
        @Size(max = 100)
        String workspaceSlug,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 128)
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName
) {
}

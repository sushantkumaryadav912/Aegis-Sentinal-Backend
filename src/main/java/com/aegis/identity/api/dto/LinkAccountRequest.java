package com.aegis.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LinkAccountRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String providerSubject
) {
}

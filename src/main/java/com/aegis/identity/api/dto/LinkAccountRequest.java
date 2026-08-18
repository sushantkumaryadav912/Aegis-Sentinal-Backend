package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description =
        "Request payload for linking a third-party OAuth provider subject to an existing password user account")
public record LinkAccountRequest(
    @Schema(description = "Target user account email address", example = "admin@acme-cyber.cloud")
        @NotBlank
        @Email
        String email,
    @Schema(
            description = "Target user account password for verification",
            example = "P@ssw0rd2026!")
        @NotBlank
        String password,
    @Schema(
            description =
                "Unique provider subject ID returned from OAuth provider (e.g. Google sub ID)",
            example = "google-sub-123456789")
        @NotBlank
        String providerSubject) {}

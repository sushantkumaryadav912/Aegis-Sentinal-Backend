package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Internal and administrative user profile view")
public class UserProfileResponse {

  @Schema(description = "User ID UUID", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "Organization ID UUID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
  private UUID organizationId;

  @Schema(description = "User email address", example = "admin@acme-cyber.cloud")
  private String email;

  @Schema(description = "First name", example = "Jane")
  private String firstName;

  @Schema(description = "Last name", example = "Doe")
  private String lastName;

  @Schema(description = "User active status flag", example = "true")
  private Boolean active;

  @Schema(description = "Multi-Factor Authentication enabled status flag", example = "false")
  private Boolean mfaEnabled;

  @Schema(description = "Set of assigned RBAC role names", example = "[\"ORG_ADMIN\"]")
  private Set<String> roles;

  @Schema(
      description = "Set of fine-grained permission authorities",
      example = "[\"alert:read\", \"workflow:execute\"]")
  private Set<String> permissions;
}

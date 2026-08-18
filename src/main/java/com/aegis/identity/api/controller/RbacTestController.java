package com.aegis.identity.api.controller;

import com.aegis.identity.api.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "RBAC & Multi-Tenancy",
    description = "Role-Based Access Control verification and tenant isolation test endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/aegis/v1/rbac-test")
public class RbacTestController {

  @Operation(
      summary = "Verify alert:read authority",
      description = "Requires user to hold alert:read authority granted by assigned roles.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Permission granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied due to missing authority",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/alert-read")
  @PreAuthorize("hasAuthority('alert:read')")
  public String alertRead() {
    return "alert:read permission granted";
  }

  @Operation(
      summary = "Verify workflow:execute authority",
      description = "Requires user to hold workflow:execute authority.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Permission granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied due to missing authority",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/workflow-execute")
  @PreAuthorize("hasAuthority('workflow:execute')")
  public String workflowExecute() {
    return "workflow:execute permission granted";
  }

  @Operation(
      summary = "Verify ORG_ADMIN role",
      description = "Requires user to have ORG_ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Role granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied due to missing role",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/org-admin")
  @PreAuthorize("hasRole('ORG_ADMIN')")
  public String orgAdmin() {
    return "ORG_ADMIN role granted";
  }

  @Operation(
      summary = "Verify system:super-admin authority",
      description = "Requires super-admin authority (used for testing forbidden 403 access).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Permission granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden — unassigned permission",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/unassigned-permission")
  @PreAuthorize("hasAuthority('system:super-admin')")
  public String unassignedPermission() {
    return "system:super-admin permission granted";
  }

  @Operation(
      summary = "Verify tenant organization-scoped permission",
      description = "Evaluates tenant isolation security for the specified organization UUID.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant organization permission granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Tenant isolation error or permission missing",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/orgs/{orgId}/alerts")
  @PreAuthorize("@tenantSecurity.hasPermission(#orgId, 'alert:read')")
  public String tenantAlertsRead(
      @Parameter(
              description = "Organization UUID",
              example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
          @PathVariable
          UUID orgId) {
    return "Tenant alert:read permission granted for organization: " + orgId;
  }

  @Operation(
      summary = "Verify tenant workspace-scoped permission",
      description =
          "Evaluates tenant and sub-tenant workspace isolation for specified org and workspace UUIDs.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Workspace permission granted"),
        @ApiResponse(
            responseCode = "403",
            description = "Workspace tenant isolation mismatch or missing permission",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  @GetMapping("/orgs/{orgId}/workspaces/{wsId}/alerts")
  @PreAuthorize("@tenantSecurity.hasWorkspacePermission(#orgId, #wsId, 'alert:read')")
  public String workspaceAlertsRead(
      @Parameter(
              description = "Organization UUID",
              example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
          @PathVariable
          UUID orgId,
      @Parameter(description = "Workspace UUID", example = "f8e7d6c5-b4a3-9281-7065-43210fedcba9")
          @PathVariable
          UUID wsId) {
    return "Workspace alert:read permission granted for org: " + orgId + ", workspace: " + wsId;
  }
}

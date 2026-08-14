package com.aegis.identity.application.command;

public record RegisterOrganizationCommand(
    String organizationName,
    String organizationSlug,
    String workspaceName,
    String workspaceSlug,
    String email,
    String password,
    String firstName,
    String lastName) {}

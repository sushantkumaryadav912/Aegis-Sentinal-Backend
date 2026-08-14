package com.aegis.identity.application.command;

import java.util.UUID;

public record RegisterUserCommand(
    UUID organizationId, String email, String password, String firstName, String lastName) {}

package com.aegis.identity.application.command;

public record LoginUserCommand(
        String email,
        String password
) {}

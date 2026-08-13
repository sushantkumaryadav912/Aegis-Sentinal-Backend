package com.aegis.identity.application.command;

public record LogoutCommand(
        String refreshToken
) {
}

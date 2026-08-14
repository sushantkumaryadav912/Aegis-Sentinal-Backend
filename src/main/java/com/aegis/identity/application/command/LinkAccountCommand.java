package com.aegis.identity.application.command;

import com.aegis.identity.domain.model.IdentityProvider;

public record LinkAccountCommand(
        String email,
        String password,
        String providerSubject,
        IdentityProvider provider
) {
    public LinkAccountCommand(String email, String password, String providerSubject) {
        this(email, password, providerSubject, IdentityProvider.GOOGLE);
    }
}

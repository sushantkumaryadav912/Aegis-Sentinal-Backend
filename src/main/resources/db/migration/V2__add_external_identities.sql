-- Aegis Sentinel
-- V2: External Identity Providers

-- Allow users authenticated exclusively through an external
-- identity provider to exist without a local password.
ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

-- External authentication identities linked to an Aegis user.
CREATE TABLE user_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL
        REFERENCES users(id)
        ON DELETE CASCADE,

    provider VARCHAR(50) NOT NULL,

    provider_subject VARCHAR(255) NOT NULL,

    provider_email VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_identity_provider_subject
        UNIQUE (provider, provider_subject)
);

CREATE INDEX idx_user_identities_user
    ON user_identities(user_id);

CREATE INDEX idx_user_identities_provider_email
    ON user_identities(provider, provider_email);

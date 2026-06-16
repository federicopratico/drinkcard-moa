CREATE TABLE refresh_tokens (
        id UUID PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        token_hash VARCHAR(64) NOT NULL UNIQUE,
        family_id UUID NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
        last_used_at TIMESTAMP WITH TIME ZONE,
        revoked_at TIMESTAMP WITH TIME ZONE,
        replaced_by_token_id UUID,

        CONSTRAINT fk_refresh_tokens_user
            FOREIGN KEY (user_id) REFERENCES users(id),

        CONSTRAINT fk_refresh_tokens_replacement
            FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id)
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_family_id
    ON refresh_tokens(family_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);
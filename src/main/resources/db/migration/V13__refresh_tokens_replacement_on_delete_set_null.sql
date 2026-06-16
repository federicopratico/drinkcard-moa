ALTER TABLE refresh_tokens
    DROP CONSTRAINT fk_refresh_tokens_replacement;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_replacement
        FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id)
        ON DELETE SET NULL;

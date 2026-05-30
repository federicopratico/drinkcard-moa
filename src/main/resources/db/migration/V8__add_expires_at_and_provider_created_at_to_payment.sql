ALTER TABLE payments
    ADD COLUMN expires_at TIMESTAMP,
    ADD COLUMN provider_created_at TIMESTAMP;

UPDATE payments
SET expires_at = created_at + INTERVAL '15 minutes'
WHERE expires_at IS NULL;

ALTER TABLE payments
    ALTER COLUMN expires_at SET NOT NULL;

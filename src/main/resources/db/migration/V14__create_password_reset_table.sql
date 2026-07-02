CREATE TABLE password_reset (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_reset_token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    used_at TIMESTAMP
)
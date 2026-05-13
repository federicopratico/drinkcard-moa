CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    volunteer_id UUID NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    provider_checkout_id VARCHAR(255) UNIQUE,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payments_volunteer_id ON payments(volunteer_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_provider_checkout_id ON payments(provider_checkout_id);
CREATE INDEX idx_payments_idempotency_key ON payments(idempotency_key);

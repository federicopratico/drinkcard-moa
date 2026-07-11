CREATE TABLE turns (
    turn_id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    turn_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_turns_email_date UNIQUE (email, turn_date)
);

CREATE INDEX idx_turns_email ON turns (email);
CREATE INDEX idx_turns_turn_date ON turns (turn_date);

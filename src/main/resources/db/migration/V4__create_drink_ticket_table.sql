CREATE TABLE drink_tickets (
    drink_ticket_id UUID PRIMARY KEY,
    volunteer_id UUID NOT NULL,
    drink_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    consumed_by_staff_id VARCHAR(50)
);
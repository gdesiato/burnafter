CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_id UUID NOT NULL,
                               event_type VARCHAR(50) NOT NULL,
                               payload TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               processed BOOLEAN DEFAULT FALSE
);

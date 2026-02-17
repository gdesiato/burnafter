CREATE TABLE audit_events (
                              id BIGSERIAL PRIMARY KEY,
                              event_id VARCHAR(100) NOT NULL UNIQUE,
                              aggregate_id VARCHAR(100) NOT NULL,
                              event_type VARCHAR(50) NOT NULL,
                              created_at TIMESTAMP NOT NULL
);

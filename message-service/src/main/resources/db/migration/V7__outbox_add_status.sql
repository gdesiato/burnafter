ALTER TABLE outbox_events
DROP COLUMN processed;

ALTER TABLE outbox_events
    ADD COLUMN first_attempt_at TIMESTAMP,
    ADD COLUMN last_error TEXT;

CREATE INDEX idx_outbox_processing
    ON outbox_events(status, next_attempt_at, created_at);

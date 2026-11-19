-- Make Flyway the single owner of the schema (ddl-auto: validate).
-- Until now Hibernate's ddl-auto=update silently added these columns.

-- When the event was created in the message-service outbox, and when it was
-- stored here: the difference is the consistency lag (divergence metric).
-- DEFAULT only backfills any existing rows; it's dropped right after.
ALTER TABLE audit_events
    ADD COLUMN IF NOT EXISTS outbox_created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS consumed_at       TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE audit_events
    ALTER COLUMN outbox_created_at DROP DEFAULT,
    ALTER COLUMN consumed_at       DROP DEFAULT;

-- Entity uses java.time.Instant -> timestamptz (values were written in UTC).
ALTER TABLE audit_events
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

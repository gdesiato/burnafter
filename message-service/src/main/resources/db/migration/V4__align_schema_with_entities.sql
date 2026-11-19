-- Make Flyway the single owner of the schema (ddl-auto: validate).
-- Until now Hibernate's ddl-auto=update silently patched the differences below.

-- Column only Hibernate was creating (outbox correlation id for tracing).
ALTER TABLE outbox_events ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(64);

-- Never used by the application.
ALTER TABLE outbox_events DROP COLUMN IF EXISTS first_attempt_at;

-- Entities use java.time.Instant (an absolute point in time) -> timestamptz.
-- Plain TIMESTAMP has no zone and is interpreted in the JVM's zone; existing
-- values were written by UTC containers, so they're converted as UTC.
ALTER TABLE outbox_events
    ALTER COLUMN created_at            TYPE TIMESTAMPTZ USING created_at            AT TIME ZONE 'UTC',
    ALTER COLUMN next_attempt_at       TYPE TIMESTAMPTZ USING next_attempt_at       AT TIME ZONE 'UTC',
    ALTER COLUMN processing_started_at TYPE TIMESTAMPTZ USING processing_started_at AT TIME ZONE 'UTC';

ALTER TABLE pastes
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN expire_at  TYPE TIMESTAMPTZ USING expire_at  AT TIME ZONE 'UTC';

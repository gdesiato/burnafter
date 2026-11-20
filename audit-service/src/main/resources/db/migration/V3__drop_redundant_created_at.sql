-- created_at was always written with the same value as consumed_at (both are
-- "when the audit service stored the event"). Keep consumed_at, whose name
-- pairs with outbox_created_at for the consistency-lag calculation.
ALTER TABLE audit_events DROP COLUMN created_at;

-- Only text was ever supported, and burn-after-read was the same as views = 1
-- (the row is deleted when the last view is used).
ALTER TABLE pastes
    DROP COLUMN IF EXISTS kind,
    DROP COLUMN IF EXISTS burn_after_read;

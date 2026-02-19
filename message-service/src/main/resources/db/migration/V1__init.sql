CREATE TABLE pastes (
                        id UUID PRIMARY KEY,
                        kind VARCHAR(50) NOT NULL,
                        ciphertext TEXT NOT NULL,
                        iv VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        expire_at TIMESTAMP NOT NULL,
                        views_left INTEGER,
                        burn_after_read BOOLEAN NOT NULL,
                        version BIGINT
);

CREATE INDEX idx_pastes_expire_at ON pastes (expire_at);


CREATE TABLE api_keys (
                          id BIGSERIAL PRIMARY KEY,
                          key_hash TEXT NOT NULL,
                          prefix VARCHAR(12) NOT NULL UNIQUE,
                          enabled BOOLEAN NOT NULL,
                          rate_limit_per_hour INTEGER NOT NULL,
                          resilience_profile VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_api_keys_prefix ON api_keys(prefix);
CREATE INDEX idx_api_keys_enabled ON api_keys(enabled);


CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_id UUID NOT NULL,
                               event_type VARCHAR(50) NOT NULL,
                               payload TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               retry_count INTEGER NOT NULL DEFAULT 0,
                               next_attempt_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                               first_attempt_at TIMESTAMP,
                               last_error TEXT
);

CREATE INDEX idx_outbox_processing
    ON outbox_events(status, next_attempt_at, created_at);

CREATE TABLE api_keys (
                          id BIGSERIAL PRIMARY KEY,
                          key_hash TEXT NOT NULL,
                          prefix VARCHAR(12) NOT NULL UNIQUE,
                          enabled BOOLEAN NOT NULL,
                          rate_limit_per_hour INTEGER NOT NULL,
                          resilience_profile VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_api_keys_prefix
    ON api_keys(prefix);

CREATE INDEX idx_api_keys_enabled
    ON api_keys(enabled);

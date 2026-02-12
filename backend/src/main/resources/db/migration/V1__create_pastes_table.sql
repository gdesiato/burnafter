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

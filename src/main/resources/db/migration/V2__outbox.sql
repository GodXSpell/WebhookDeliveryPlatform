-- Keep upgrades safe for databases that already applied the original V1.
CREATE TABLE IF NOT EXISTS outbox (
    id         uuid PRIMARY KEY,
    event_id   uuid        NOT NULL REFERENCES events (id),
    status     text        NOT NULL DEFAULT 'PENDING'
               CHECK (status IN ('PENDING', 'PROCESSING')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS outbox_pending ON outbox (created_at) WHERE status = 'PENDING';

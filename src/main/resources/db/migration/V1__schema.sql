-- The event is the durable payload and the outbox row is the work to poll.
CREATE TABLE events (
    id         uuid PRIMARY KEY,
    type       text        NOT NULL,
    body       text        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE outbox (
    id         uuid PRIMARY KEY,
    event_id   uuid        NOT NULL REFERENCES events (id),
    status     text        NOT NULL DEFAULT 'PENDING'
               CHECK (status IN ('PENDING', 'PROCESSING')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX outbox_pending ON outbox (created_at) WHERE status = 'PENDING';

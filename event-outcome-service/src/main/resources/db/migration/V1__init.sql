CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE events (
    id              BIGINT       DEFAULT NEXT VALUE FOR events_seq PRIMARY KEY,
    event_id        BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    event_winner_id BIGINT,
    recorded_at     TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_events_event_id UNIQUE (event_id)
);

CREATE SEQUENCE outbox_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE outbox (
    id             BIGINT       DEFAULT NEXT VALUE FOR outbox_seq PRIMARY KEY,
    event_id       BIGINT       NOT NULL,
    payload        TEXT         NOT NULL,
    correlation_id VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL,
    sent_at        TIMESTAMP,
    retry_count    INT          NOT NULL DEFAULT 0
);

CREATE SEQUENCE failed_outbox_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE failed_outbox (
    id          BIGINT    DEFAULT NEXT VALUE FOR failed_outbox_seq PRIMARY KEY,
    event_id    BIGINT    NOT NULL,
    payload     TEXT      NOT NULL,
    failed_at   TIMESTAMP NOT NULL,
    last_error  TEXT
);

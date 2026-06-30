CREATE SEQUENCE bets_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE bets (
    id              BIGINT         DEFAULT NEXT VALUE FOR bets_seq PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    event_id        BIGINT         NOT NULL,
    event_market_id BIGINT         NOT NULL,
    event_winner_id BIGINT         NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    status          VARCHAR(255)   NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_bets_event_id_status ON bets (event_id, status);

CREATE SEQUENCE outbox_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE outbox (
    id          BIGINT     DEFAULT NEXT VALUE FOR outbox_seq PRIMARY KEY,
    bet_id         BIGINT       NOT NULL,
    payload        TEXT         NOT NULL,
    correlation_id VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL,
    sent_at     TIMESTAMP,
    retry_count INT        NOT NULL DEFAULT 0
);

CREATE SEQUENCE failed_outbox_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE failed_outbox (
    id          BIGINT    DEFAULT NEXT VALUE FOR failed_outbox_seq PRIMARY KEY,
    bet_id      BIGINT    NOT NULL,
    payload     TEXT      NOT NULL,
    failed_at   TIMESTAMP NOT NULL,
    last_error  TEXT
);

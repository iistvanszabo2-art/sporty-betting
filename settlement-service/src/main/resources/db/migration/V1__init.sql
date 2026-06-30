CREATE SEQUENCE settled_bets_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE settled_bets (
    id         BIGINT         DEFAULT NEXT VALUE FOR settled_bets_seq PRIMARY KEY,
    bet_id     BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    event_id   BIGINT         NOT NULL,
    won        BOOLEAN        NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL,
    settled_at TIMESTAMP      NOT NULL,
    CONSTRAINT uq_settled_bets_bet_id UNIQUE (bet_id)
);

CREATE SEQUENCE payout_outbox_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE payout_outbox (
    id              BIGINT         DEFAULT NEXT VALUE FOR payout_outbox_seq PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    idempotency_key VARCHAR(255)   NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    sent_at         TIMESTAMP,
    retry_count     INT            NOT NULL DEFAULT 0
);

CREATE SEQUENCE failed_payouts_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE failed_payouts (
    id              BIGINT         DEFAULT NEXT VALUE FOR failed_payouts_seq PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    idempotency_key VARCHAR(255)   NOT NULL,
    failed_at       TIMESTAMP      NOT NULL,
    last_error      TEXT
);

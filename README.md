# Sporty Betting — Settlement System

Three Spring Boot microservices simulating sports betting event outcome handling and settlement via **Kafka** and **RocketMQ**.

---

## Quick start

**Prerequisites:** Docker & Docker Compose

```bash
docker compose up -d
```

Docker builds all three services locally and starts Kafka, RocketMQ.

---

## Try it

Run the included demo script to trigger both seed events:

```bash
./sporty-betting.sh
```

Or manually:

```bash
# Trigger event 1 — winner is team 10 → Bet 1 WON ($50), Bet 2 LOST ($30)
curl -s -X POST http://localhost:8081/api/v1/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{"eventId":1,"eventName":"Champions League Final","eventWinnerId":10}'
```

Then inspect state via the H2 consoles:

| Service | URL | Query |
|---------|-----|-------|
| bet-matcher-service | http://localhost:8082/h2-console (`jdbc:h2:mem:matcherdb`) | `SELECT * FROM bets` → status `PENDING → DISPATCHED` |
| settlement-service | http://localhost:8083/h2-console (`jdbc:h2:mem:settlementdb`) | `SELECT * FROM settled_bets` |

Username: `sa` | Password: *(empty)*

---

## Architecture

[HLD](docs/HLD.md) contains more details → done before implementation.

```
┌─────────────────────────────┐
│   event-outcome-service     │  :8081
│   POST /api/v1/event-outcomes│
└────────────┬────────────────┘
             │ Kafka: event-outcomes
             ▼
┌─────────────────────────────┐
│   bet-matcher-service       │  :8082
│   - matches pending bets    │
│   - marks bets DISPATCHED   │
└────────────┬────────────────┘
             │ RocketMQ: bet-settlements
             ▼
┌─────────────────────────────┐
│   settlement-service        │  :8083
│   - idempotent settlement   │
│   - triggers payout         │
└─────────────────────────────┘
```

Each service owns its own H2 in-memory database. The only shared state is through the message brokers.

---

## Pre-loaded seed data

| Bet | User | Event | Wagered on | Amount  |
|-----|------|-------|------------|---------|
| 1   | 1    | 1     | winner 10  | $50.00  |
| 2   | 2    | 1     | winner 20  | $30.00  |
| 3   | 3    | 2     | winner 30  | $100.00 |
| 4   | 1    | 2     | winner 40  | $75.00  |

POST event 1 with `eventWinnerId=10` → Bet 1 **WON**, Bet 2 **LOST**.

---

## Key design decisions

**Transactional Outbox** — all three services write to an outbox table within the same DB transaction as the business operation. A scheduled relay publishes the message and marks it sent. Guarantees at-least-once delivery without holding a transaction open during broker I/O.

**Idempotency** — `settled_bets.bet_id` has a `UNIQUE` constraint. `existsByBetId()` provides a fast-path early return; a `DataIntegrityViolationException` catch guards against the concurrent race. Duplicate RocketMQ deliveries are silently skipped.

**Double-dispatch prevention** — after outbox entries are created, bets are marked `DISPATCHED`. A re-delivered Kafka message for the same event finds no `PENDING` bets and is a no-op.

**DLQ** — `BetSettlementConsumer` sets `maxReconsumeTimes=3`. After exhausting retries, RocketMQ routes to `%DLQ%bet-settlement-consumer-group`, consumed by `BetSettlementDlqConsumer` (logs at ERROR — production would alert and persist to a dead-letter table).

**Optimistic locking** — `Event.version` (`@Version`) prevents two concurrent requests from recording the same event outcome simultaneously.

---

## Local development (optional)

Requires **Java 17+** on the host. No local Gradle install needed — use the included wrapper.

```bash
# Start brokers only
docker compose up -d kafka rocketmq-namesrv rocketmq-broker

# Run each service in a separate terminal
./gradlew :event-outcome-service:bootRun
./gradlew :bet-matcher-service:bootRun
./gradlew :settlement-service:bootRun

# Run all tests
./gradlew test
```

---

## Observability + Kafka/H2 insight (optional)

Loki + Promtail + Grafana log aggregation:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

Grafana: http://localhost:3000

Kafka UI: http://localhost:8088

## H2 Consoles

| Service | URL | JDBC URL |
|---------|-----|----------|
| event-outcome-service | http://localhost:8081/h2-console | `jdbc:h2:mem:outcomedb` |
| bet-matcher-service | http://localhost:8082/h2-console | `jdbc:h2:mem:matcherdb` |
| settlement-service | http://localhost:8083/h2-console | `jdbc:h2:mem:settlementdb` |

Username: `sa` | Password: *(empty)*

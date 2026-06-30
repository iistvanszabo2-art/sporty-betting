# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Gradle wrapper is at the repo root — use `../gradlew` from inside a service directory, or `./gradlew` from root.

```bash
# Build all services
./gradlew build -x test

# Run a specific service
./gradlew :event-outcome-service:bootRun
./gradlew :bet-matcher-service:bootRun
./gradlew :settlement-service:bootRun

# Test a specific service
./gradlew :bet-matcher-service:test

# Run a single test class
./gradlew :bet-matcher-service:test --tests "com.sporty.matcher.BetMatcherServiceTest"
```

Infrastructure (required before starting services):
```bash
docker compose up -d          # Kafka + RocketMQ NameServer + Broker
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d  # + Loki, Promtail, Grafana
```

## Architecture

Three independent Spring Boot services; each has its own H2 in-memory database. Communication is exclusively through message brokers.

```
event-outcome-service (:8081)
  POST /api/v1/event-outcomes → Kafka: event-outcomes

bet-matcher-service (:8082)
  Kafka consumer: event-outcomes
    → queries H2 for PENDING bets by eventId
    → marks matched bets DISPATCHED (prevents double-dispatch on Kafka re-delivery)
    → RocketMQ producer: bet-settlements

settlement-service (:8083)
  RocketMQ consumer: bet-settlements
    → idempotency check (unique betId constraint) before persisting SettledBet
    → maxReconsumeTimes=3 → DLQ consumer on %DLQ%bet-settlement-consumer-group
```

## Key design decisions

**Idempotency in settlement-service:** `settled_bets.bet_id` has a DB-level `UNIQUE` constraint. `SettlementService.settle()` calls `existsByBetId()` before inserting. Duplicate messages (RocketMQ at-least-once) are silently skipped.

**Double-dispatch prevention in bet-matcher-service:** After sending to RocketMQ, `BetMatcherService` marks the bet `DISPATCHED`. A re-delivered Kafka message for the same event finds no `PENDING` bets and is a no-op.

**DLQ flow:** `BetSettlementConsumer` sets `maxReconsumeTimes=3`. After exhausting retries, RocketMQ moves the message to `%DLQ%bet-settlement-consumer-group`. `BetSettlementDlqConsumer` logs at ERROR. Production follow-up: persist to `dead_letter_bets` table + alert.

**RocketMQ Docker note:** `config/broker-docker.conf` sets `brokerIP1=127.0.0.1` so the broker advertises the loopback. Works on Docker Desktop (Mac/Windows). On Linux, change `brokerIP1` to the host's LAN IP.

## Package layout

| Service | Root package | Port |
|---------|-------------|------|
| event-outcome-service | `com.sporty.outcome` | 8081 |
| bet-matcher-service | `com.sporty.matcher` | 8082 |
| settlement-service | `com.sporty.settlement` | 8083 |

Each service follows the same internal structure: `api/`, `config/`, `dto/`, `kafka/` or `rocketmq/`, `model/`, `repository/`, `service/`.

#!/usr/bin/env bash
set -euo pipefail

# Event 1: winner 10 → Bet 1 WON ($50), Bet 2 LOST ($30)
curl -s -X POST "http://localhost:8081/api/v1/event-outcomes" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: $(uuidgen)" \
  -d '{"eventId":1,"eventName":"Champions League Final","eventWinnerId":10}' | cat

# Event 2: winner 30 → Bet 3 WON ($100), Bet 4 LOST ($75)
curl -s -X POST "http://localhost:8081/api/v1/event-outcomes" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: $(uuidgen)" \
  -d '{"eventId":2,"eventName":"Premier League GW38","eventWinnerId":30}' | cat

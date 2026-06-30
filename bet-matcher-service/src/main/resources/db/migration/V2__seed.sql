-- Event 1: User 1 bets $50 on winner 10 | User 2 bets $30 on winner 20
-- Event 2: User 3 bets $100 on winner 30 | User 1 bets $75 on winner 40
-- Try: POST /api/v1/event-outcomes with eventId=1, eventWinnerId=10 → Bet 1 WON, Bet 2 LOST
INSERT INTO bets (user_id, event_id, event_market_id, event_winner_id, amount, status)
VALUES
    (1, 1, 101, 10,  50.00, 'PENDING'),
    (2, 1, 101, 20,  30.00, 'PENDING'),
    (3, 2, 202, 30, 100.00, 'PENDING'),
    (1, 2, 202, 40,  75.00, 'PENDING');

-- STALE_REJECT: rechazo automático por abandono (sin penalización de trust score)
INSERT INTO action_weights (action_type, weight) VALUES ('STALE_REJECT', 0);

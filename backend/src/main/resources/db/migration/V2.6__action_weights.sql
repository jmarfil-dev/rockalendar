-- ============================================================
-- Tabla de pesos por tipo de acción para el cálculo derivado del trust score.
-- El trust score ya no se persiste en users; se calcula en tiempo real
-- como SUM(aw.weight) JOIN moderation_actions + events + action_weights.
-- ============================================================

CREATE TABLE action_weights (
    action_type TEXT PRIMARY KEY,
    weight      INTEGER NOT NULL
);

INSERT INTO action_weights (action_type, weight) VALUES
    ('APPROVE',              15),
    ('REJECT',              -20),
    ('AUTO_REJECT',         -40),
    ('REQUEST_CHANGES',      -5),
    ('MODERATOR_EDITED',     -8),
    ('ADMIN_EDITED',         -3),
    ('HIDE',                 -5),
    ('ADMIN_STATE_OVERRIDE',  0),
    ('OWNER_EDITED_PENDING',  0)
;

ALTER TABLE users DROP COLUMN trust_score;

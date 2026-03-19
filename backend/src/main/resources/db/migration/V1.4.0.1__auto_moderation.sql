-- ============================================================
-- Flyway moderación automática
-- Version: 1.4.0.1
-- Description: Estado FLAGGED, reglas de blacklist y configuración de moderación automática
-- ============================================================


-- 1. Añadir AUTO_REJECT al check constraint de moderation_actions.action_type
ALTER TABLE moderation_actions DROP CONSTRAINT chk_moderation_action;

ALTER TABLE moderation_actions ADD CONSTRAINT chk_moderation_action CHECK (
    action_type = ANY (ARRAY[
        'APPROVE'::varchar,
        'REJECT'::varchar,
        'HIDE'::varchar,
        'REQUEST_CHANGES'::varchar,
        'AUTO_REJECT'::varchar
    ])
);


-- 2. Añadir FLAGGED al check constraint de events.status
ALTER TABLE public.events
    DROP CONSTRAINT chk_events_status,
    ADD CONSTRAINT chk_events_status CHECK (
        status::text = ANY (ARRAY[
            'PENDING_MODERATION',
            'APPROVED',
            'REJECTED',
            'DRAFT',
            'NEEDS_CHANGES',
            'HIDDEN',
            'CANCELED',
            'ERASED',
            'FLAGGED'
        ]::text[])
    );


-- 2. Tabla de reglas de moderación automática (blacklist de términos, artistas y regex)
CREATE TABLE moderation_rules (
    id          UUID        PRIMARY KEY,
    rule_type   TEXT        NOT NULL,
    value       TEXT        NOT NULL,
    reason      TEXT        NOT NULL,
    active      BOOLEAN     NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_rule_type CHECK (rule_type IN ('TEXT_TERM', 'ARTIST_SLUG', 'REGEX'))
);

CREATE INDEX idx_moderation_rules_active ON moderation_rules (active) WHERE active = true;


-- 3. Configuración de moderación automática (clave-valor, editable sin reiniciar)
CREATE TABLE moderation_config (
    key         TEXT PRIMARY KEY,
    value       TEXT NOT NULL,
    description TEXT NULL
);

INSERT INTO moderation_config (key, value, description) VALUES
    ('spam_rejection_count',          '5',  'Nº de rechazos en ventana de tiempo para activar anti-spam.'),
    ('spam_window_days',              '30', 'Ventana de tiempo en días para el conteo anti-spam.'),
    ('flagged_rejection_delay_hours', '12', 'Horas que un evento permanece FLAGGED antes de ser rechazado automáticamente.');


-- 4. Log de moderación automática (separado de moderation_actions, que es para acciones humanas)
CREATE TABLE auto_moderation_log (
    id            UUID        PRIMARY KEY,
    event_id      UUID        NOT NULL,
    rule_id       UUID        NULL,
    rule_type     TEXT        NOT NULL,
    matched_value TEXT        NULL,
    flagged_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_auto_mod_log_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_auto_mod_log_rule  FOREIGN KEY (rule_id)  REFERENCES moderation_rules(id) ON DELETE SET NULL
);

CREATE INDEX idx_auto_mod_log_event ON auto_moderation_log (event_id);

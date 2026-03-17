-- ============================================================
-- Flyway: renombrar COMMENT a REQUEST_CHANGES en moderation_actions
-- Version: 1.2.0.6
-- Description: COMMENT era semánticamente incorrecto para la acción
--              de solicitar cambios al autor. Se renombra a REQUEST_CHANGES.
--              También se amplía action_type de varchar(10) a varchar(20).
-- ============================================================

ALTER TABLE moderation_actions ALTER COLUMN action_type TYPE varchar(20);

ALTER TABLE moderation_actions DROP CONSTRAINT chk_moderation_action;

UPDATE moderation_actions SET action_type = 'REQUEST_CHANGES' WHERE action_type = 'COMMENT';

ALTER TABLE moderation_actions ADD CONSTRAINT chk_moderation_action CHECK (
    action_type = ANY (ARRAY[
        'APPROVE'::varchar,
        'REJECT'::varchar,
        'HIDE'::varchar,
        'REQUEST_CHANGES'::varchar
    ])
);

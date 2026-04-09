-- ============================================================
-- Flyway: añadir ADMIN_EDITED al constraint de moderation_actions
-- Version: 2.5
-- Description: El admin puede editar datos de cualquier evento; registra ADMIN_EDITED.
-- ============================================================

ALTER TABLE moderation_actions DROP CONSTRAINT chk_moderation_action;

ALTER TABLE moderation_actions ADD CONSTRAINT chk_moderation_action CHECK (
    action_type = ANY (ARRAY[
        'APPROVE'::varchar,
        'REJECT'::varchar,
        'HIDE'::varchar,
        'REQUEST_CHANGES'::varchar,
        'AUTO_REJECT'::varchar,
        'MODERATOR_EDITED'::varchar,
        'ADMIN_STATE_OVERRIDE'::varchar,
        'ADMIN_EDITED'::varchar,
        'OWNER_EDITED_PENDING'::varchar
    ])
);

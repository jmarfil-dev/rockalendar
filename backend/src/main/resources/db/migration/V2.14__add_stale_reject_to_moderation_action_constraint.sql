-- ============================================================
-- Flyway: añadir STALE_REJECT al constraint de moderation_actions
-- Version: 2.14
-- Description: STALE_REJECT se usó en el scheduler desde V2.7 pero no se
--              incluyó en chk_moderation_action, causando violación del
--              constraint al rechazar eventos NEEDS_CHANGES por abandono.
-- ============================================================

ALTER TABLE moderation_actions DROP CONSTRAINT chk_moderation_action;

ALTER TABLE moderation_actions ADD CONSTRAINT chk_moderation_action CHECK (
    action_type = ANY (ARRAY[
        'APPROVE'::varchar,
        'REJECT'::varchar,
        'HIDE'::varchar,
        'REQUEST_CHANGES'::varchar,
        'AUTO_REJECT'::varchar,
        'STALE_REJECT'::varchar,
        'MODERATOR_EDITED'::varchar,
        'ADMIN_STATE_OVERRIDE'::varchar,
        'ADMIN_EDITED'::varchar,
        'OWNER_EDITED_PENDING'::varchar
    ])
);

COMMENT ON CONSTRAINT chk_moderation_action ON moderation_actions IS 'Valores válidos de action_type. Las acciones del sistema (AUTO_REJECT, STALE_REJECT) permiten moderated_by_user_id nulo.';

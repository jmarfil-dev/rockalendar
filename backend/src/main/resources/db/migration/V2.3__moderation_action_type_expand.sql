-- ============================================================
-- Flyway: ampliar CHECK constraint de moderation_actions
-- Version: 2.3
-- Description: Añade AUTO_REJECT (que faltaba en el constraint),
--              y los nuevos tipos MODERATOR_EDITED, ADMIN_STATE_OVERRIDE,
--              OWNER_EDITED_PENDING. Amplía varchar(20) a varchar(25).
-- ============================================================

ALTER TABLE moderation_actions ALTER COLUMN action_type TYPE varchar(25);

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
        'OWNER_EDITED_PENDING'::varchar
    ])
);

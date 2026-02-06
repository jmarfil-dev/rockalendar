-- ============================================================
-- Flyway tabla de historial de moderación (v1)
-- Version: 1.2.0.2
-- Description: Corrige la tabla moderation_actions
-- ============================================================


DROP TABLE moderation_actions;

CREATE TABLE moderation_actions (
    id uuid NOT NULL PRIMARY KEY,
    event_id uuid NOT NULL,
    action_type varchar(10) NOT NULL,
    reason TEXT NULL,
    moderated_by_user_id uuid NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT chk_moderation_action CHECK (
        (
            (action_type)::TEXT = ANY (
                (
                    ARRAY['APPROVE'::CHARACTER VARYING,
                    'REJECT'::CHARACTER VARYING,
                    'HIDE'::CHARACTER VARYING,
                    'COMMENT'::CHARACTER VARYING]
                )::TEXT[]
            )
        )
    ),
    CONSTRAINT fk_moderation_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_moderation_moderator FOREIGN KEY (moderated_by_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_moderation_actions_event_id ON moderation_actions(event_id);
CREATE INDEX IF NOT EXISTS idx_moderation_actions_created_at ON moderation_actions(created_at);

ALTER TABLE events ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 0;

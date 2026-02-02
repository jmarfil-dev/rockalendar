-- ============================================================
-- Flyway campos para moderación (v1)
-- Version: 1.2.0.1
-- Description: Modifica la tabla events para las funciones de moderación
-- ============================================================


ALTER TABLE public.events
    DROP COLUMN IF EXISTS approved_by_user_id,
    ADD COLUMN submitted_at timestamptz NOT NULL,
    ADD COLUMN moderated_at timestamptz NULL,
    ADD COLUMN moderated_by_user_id uuid NULL,
    DROP CONSTRAINT IF EXISTS chk_events_status,
    ADD CONSTRAINT fk_event_moderated_by FOREIGN KEY (moderated_by_user_id) REFERENCES public.users(id),
    ADD CONSTRAINT chk_events_status CHECK (
        status::text = ANY (ARRAY[
            'PENDING_MODERATION',
            'APPROVED',
            'REJECTED',
            'DRAFT',
            'NEEDS_CHANGES',
            'HIDDEN',
            'CANCELED',
            'ERASED'
        ]::text[])
    );

ALTER TABLE public.events RENAME COLUMN rejection_reason TO moderation_message;

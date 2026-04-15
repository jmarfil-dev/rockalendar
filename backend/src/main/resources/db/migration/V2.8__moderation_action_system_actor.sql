-- ============================================================
-- Permite que moderated_by_user_id sea NULL en acciones del sistema
-- (AUTO_REJECT, STALE_REJECT). NULL indica que el actor es el sistema,
-- no un moderador humano.
-- Version: 2.8
-- ============================================================

ALTER TABLE moderation_actions ALTER COLUMN moderated_by_user_id DROP NOT NULL;

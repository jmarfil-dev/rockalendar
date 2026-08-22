-- ============================================================
-- Flyway: backfill de moderated_at en eventos auto-aprobados por admin
-- Version: 2.16
-- Description: EventCommandService.propose()/update() ponían status=APPROVED
--   para eventos creados/editados por un admin sin rellenar moderated_at ni
--   moderated_by_user_id (se corrige en el código a partir de esta versión).
--   El moderated_at nulo hacía que el frontend mostrara 1970-01-01
--   (new Date(null) en JavaScript). Este script corrige los eventos ya
--   existentes en ese estado: se asume que el propio creador fue quien
--   los auto-aprobó, y se usa submitted_at como fecha de aprobación.
-- ============================================================

UPDATE events
SET moderated_at = submitted_at,
    moderated_by_user_id = COALESCE(moderated_by_user_id, created_by_user_id)
WHERE status = 'APPROVED'
  AND moderated_at IS NULL;

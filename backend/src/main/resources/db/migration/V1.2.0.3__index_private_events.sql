-- ============================================================
-- Flyway índice nuevo (v1)
-- Version: 1.2.0.3
-- Description: Ayuda en el filtro y ordenación de eventos privados (listMine).
-- ============================================================


CREATE INDEX idx_events_created_by_status_start_date ON events (created_by_user_id, status, start_date_time);

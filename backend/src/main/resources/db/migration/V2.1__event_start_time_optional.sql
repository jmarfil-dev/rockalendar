-- ============================================================
-- Flyway seeds_producción (v2)
-- Version: 2.1
-- Description: Modificar campos de fecha porque las horas no son obligatorias para el usuario
-- ============================================================

-- Permite que la hora de inicio de un evento sea desconocida.
-- Cuando start_time_unknown = true, la parte de hora de start_date_time es 00:00:00Z (sentinel).
ALTER TABLE events ADD COLUMN start_time_unknown BOOLEAN NOT NULL DEFAULT FALSE;

-- Simplifica la fecha de fin a solo fecha (sin hora).
-- La hora de fin casi nunca se conoce y genera datos incorrectos (festivales que "terminan el día siguiente").
ALTER TABLE events ADD COLUMN end_date DATE;
UPDATE events SET end_date = (end_date_time AT TIME ZONE 'UTC')::date WHERE end_date_time IS NOT NULL;
ALTER TABLE events DROP COLUMN end_date_time;

-- ============================================================
-- Flyway artistas (v1)
-- Version: 1.0.0.3
-- Description: Corregida tabla artists y agregadas columnas de auditoría
-- ============================================================


ALTER TABLE artists
  ALTER COLUMN name TYPE varchar(200),
  ALTER COLUMN slug TYPE varchar(200),
  ADD COLUMN created_by_user_id UUID NULL,
  ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE artists
  ADD CONSTRAINT fk_artists_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_artists_created_by_user_id ON artists (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_artists_created_at ON artists (created_at);
CREATE INDEX IF NOT EXISTS idx_artists_slug_lower ON artists (lower(slug)); -- Para búsquedas

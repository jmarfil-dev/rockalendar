-- ============================================================
-- Flyway migration
-- Version: 2.3
-- Description: Usa ine_code como PK de provinces en lugar del UUID artificial.
--   - events.province_id pasa de UUID a SMALLINT (referencia directa al ine_code).
--   - Se elimina el campo id UUID de provinces.
--   - Se elimina el namespace UUIDv5 que ya no tiene utilidad.
-- ============================================================

-- 1. Añadir columna temporal en events con el tipo definitivo
ALTER TABLE events ADD COLUMN province_ine_code SMALLINT;

-- 2. Rellenar con el ine_code correspondiente usando JOIN
UPDATE events
SET province_ine_code = p.ine_code
FROM provinces p
WHERE p.id = events.province_id;

-- 3. Eliminar FK y columna UUID en events
ALTER TABLE events DROP CONSTRAINT fk_event_province;
ALTER TABLE events DROP COLUMN province_id;

-- 4. Promover la columna temporal a definitiva
ALTER TABLE events RENAME COLUMN province_ine_code TO province_id;
ALTER TABLE events ALTER COLUMN province_id SET NOT NULL;

-- 5. Cambiar el PK de provinces: eliminar UUID y hacer ine_code el PK
ALTER TABLE provinces DROP CONSTRAINT provinces_pkey;
ALTER TABLE provinces DROP COLUMN id;
-- La constraint UNIQUE de ine_code es redundante con el PK; se elimina si existe
ALTER TABLE provinces DROP CONSTRAINT IF EXISTS provinces_ine_code_key;
ALTER TABLE provinces ADD CONSTRAINT provinces_pkey PRIMARY KEY (ine_code);

-- 6. Recrear FK
ALTER TABLE events
    ADD CONSTRAINT fk_event_province FOREIGN KEY (province_id) REFERENCES provinces(ine_code);

-- 7. Actualizar índices
-- idx_provinces_ine_code ya no es necesario: el PK tiene su propio índice implícito
DROP INDEX IF EXISTS idx_provinces_ine_code;

-- Recrear índices compuestos sobre province_id (la columna cambió de UUID a SMALLINT)
DROP INDEX IF EXISTS idx_events_province_city_start;
DROP INDEX IF EXISTS idx_events_province_id;
CREATE INDEX idx_events_province_id ON events (province_id);
CREATE INDEX idx_events_province_city_start ON events (province_id, city_slug, start_date_time);

-- 8. Recrear funciones de búsqueda: p_province_id cambia de uuid a smallint
-- CREATE OR REPLACE no permite cambiar los tipos de los parámetros; hay que DROP+CREATE
DROP FUNCTION IF EXISTS search_public_events(text, double precision, double precision, double precision, timestamptz, timestamptz, uuid, text, uuid);
DROP FUNCTION IF EXISTS search_public_events_fallback(text, double precision, double precision, double precision, timestamptz, timestamptz, uuid, text, uuid);

CREATE FUNCTION search_public_events(
  p_q_raw text,
  p_min_similarity double precision,
  p_fts_weight double precision,
  p_trgm_weight double precision,
  p_date_from timestamptz,
  p_date_to timestamptz,
  p_province_id smallint,
  p_city_slug text,
  p_artist_id uuid
)
RETURNS TABLE(event_id uuid, score double precision)
LANGUAGE sql
STABLE
AS $$
  WITH q AS (
    SELECT
      unaccent(lower(trim(coalesce(p_q_raw, '')))) AS q_norm,
      CASE
        WHEN unaccent(lower(trim(coalesce(p_q_raw, '')))) = '' THEN NULL
        ELSE websearch_to_tsquery('spanish', unaccent(coalesce(p_q_raw, '')))
      END AS tsq
  )
  SELECT
    e.id AS event_id,
    (
      CASE WHEN q.tsq IS NULL THEN 0
           ELSE ts_rank_cd(e.search_document, q.tsq) * p_fts_weight
      END
      +
      CASE WHEN q.tsq IS NULL THEN 0
           ELSE greatest(
             similarity(e.search_text, q.q_norm),
             word_similarity(e.search_text, q.q_norm)
           ) * p_trgm_weight
      END
    ) AS score
  FROM events e
  CROSS JOIN q
  WHERE
    e.status = 'APPROVED'

    AND (
      (e.end_date IS NOT NULL AND e.end_date >= CURRENT_DATE)
      OR (e.end_date IS NULL AND e.start_date_time >= now())
    )

    AND (p_date_from IS NULL OR e.start_date_time >= p_date_from)
    AND (p_date_to   IS NULL OR e.start_date_time <= p_date_to)
    AND (p_province_id IS NULL OR e.province_id = p_province_id)
    AND (p_city_slug IS NULL OR e.city_slug = p_city_slug)
    AND (
      p_artist_id IS NULL
      OR EXISTS (
        SELECT 1
        FROM event_artists ea
        WHERE ea.event_id = e.id
          AND ea.artist_id = p_artist_id
      )
    )

    AND (
      q.tsq IS NULL
      OR (
        e.search_document @@ q.tsq
        OR (length(q.q_norm) <= 10 AND e.search_text LIKE '%' || q.q_norm || '%')
        OR word_similarity(e.search_text, q.q_norm) >=
           CASE
             WHEN length(q.q_norm) <= 3 THEN 0.10
             WHEN length(q.q_norm) <= 6 THEN 0.12
             WHEN length(q.q_norm) <= 10 THEN 0.14
             WHEN length(q.q_norm) <= 12 THEN 0.18
             ELSE p_min_similarity
           END
        OR e.search_text % q.q_norm
        OR similarity(e.search_text, q.q_norm) >= p_min_similarity
      )
    );
$$;


CREATE FUNCTION search_public_events_fallback(
  p_q_raw text,
  p_min_similarity double precision,
  p_fts_weight double precision,
  p_trgm_weight double precision,
  p_date_from timestamptz,
  p_date_to timestamptz,
  p_province_id smallint,
  p_city_slug text,
  p_artist_id uuid
)
RETURNS TABLE(event_id uuid, score double precision)
LANGUAGE sql
STABLE
AS $$
  WITH q AS (
    SELECT unaccent(lower(trim(coalesce(p_q_raw, '')))) AS q_norm
  ),
  terms AS (
    SELECT term
    FROM q
    CROSS JOIN LATERAL unnest(regexp_split_to_array(q.q_norm, '\s+')) AS term
    WHERE term <> ''
  )
  SELECT
    e.id AS event_id,
    COALESCE((
      SELECT SUM(
        (CASE WHEN e.search_document @@ websearch_to_tsquery('spanish', t.term)
              THEN 1 ELSE 0 END) * p_fts_weight
        +
        greatest(
          word_similarity(e.search_text, t.term),
          similarity(e.search_text, t.term)
        ) * p_trgm_weight
      )
      FROM terms t
    ), 0) AS score
  FROM events e
  CROSS JOIN q
  WHERE
    e.status = 'APPROVED'

    AND (
      (e.end_date IS NOT NULL AND e.end_date >= CURRENT_DATE)
      OR (e.end_date IS NULL AND e.start_date_time >= now())
    )

    AND (p_date_from IS NULL OR e.start_date_time >= p_date_from)
    AND (p_date_to   IS NULL OR e.start_date_time <= p_date_to)
    AND (p_province_id IS NULL OR e.province_id = p_province_id)
    AND (p_city_slug IS NULL OR e.city_slug = p_city_slug)
    AND (
      p_artist_id IS NULL
      OR EXISTS (
        SELECT 1
        FROM event_artists ea
        WHERE ea.event_id = e.id
          AND ea.artist_id = p_artist_id
      )
    )
    AND (
      q.q_norm = ''
      OR EXISTS (
        SELECT 1
        FROM terms t
        WHERE
          e.search_document @@ websearch_to_tsquery('spanish', t.term)
          OR e.search_text LIKE '%' || t.term || '%'
          OR word_similarity(e.search_text, t.term) >=
             CASE
               WHEN length(q.q_norm) <= 3 THEN 0.10
               WHEN length(q.q_norm) <= 6 THEN 0.12
               WHEN length(q.q_norm) <= 10 THEN 0.14
               WHEN length(q.q_norm) <= 12 THEN 0.18
               ELSE p_min_similarity
             END
      )
    );
$$;

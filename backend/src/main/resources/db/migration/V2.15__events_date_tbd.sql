-- ============================================================
-- Flyway: fecha por confirmar en eventos aprobados
-- Version: 2.15
-- Description: Añade date_tbd para poder mantener público un evento
--   APPROVED cuya fecha real todavía no está confirmada (p. ej. un
--   festival aplazado sin nueva fecha). Solo lo gestiona el administrador
--   (AdminEventCommandService.edit). Se actualizan las funciones de
--   búsqueda pública para que estos eventos no desaparezcan al quedar
--   su fecha almacenada en el pasado.
-- ============================================================

ALTER TABLE events ADD COLUMN date_tbd BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN events.date_tbd IS 'Indica que la fecha almacenada no es la fecha real del evento (p. ej. aplazado sin fecha confirmada). Solo editable por un administrador.';

CREATE OR REPLACE FUNCTION search_public_events(
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

    -- No muestra los eventos pasados, salvo que la fecha esté por confirmar
    AND (
      e.date_tbd = true
      OR (e.end_date IS NOT NULL AND e.end_date >= CURRENT_DATE)
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

CREATE OR REPLACE FUNCTION search_public_events_fallback(
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

    -- No muestra los eventos pasados, salvo que la fecha esté por confirmar
    AND (
      e.date_tbd = true
      OR (e.end_date IS NOT NULL AND e.end_date >= CURRENT_DATE)
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

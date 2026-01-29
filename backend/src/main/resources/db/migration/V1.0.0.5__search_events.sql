-- ============================================================
-- Flyway función search_events (v1)
-- Version: 1.0.0.3
-- Description: Búsqueda pública de eventos mediante FTS + pg_trgm (GIN)
-- ============================================================


-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;


-- Columnas nuevas para la búsqueda
--    - search_text
--    - search_document
ALTER TABLE events
  ADD COLUMN search_text text,
  ADD COLUMN search_document tsvector;

-- Trigger que calcula automáticamente el valor de las columnas nuevas
CREATE OR REPLACE FUNCTION events_search_refresh()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  -- TRGM: SIN description
  NEW.search_text :=
    lower(
      unaccent(
        concat_ws(' ',
          coalesce(NEW.title, ''),
          coalesce(NEW.venue_name, ''),
          coalesce(NEW.venue_slug, ''),
          coalesce(NEW.city_name, ''),
          coalesce(NEW.city_slug, '')
        )
      )
    );

  -- FTS: CON description (peso bajo)
  NEW.search_document :=
      setweight(to_tsvector('spanish', unaccent(coalesce(NEW.title, ''))), 'A')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.venue_name, ''))), 'B')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.city_name, ''))), 'B')
    || setweight(to_tsvector('simple',  unaccent(coalesce(NEW.venue_slug, ''))), 'C')
    || setweight(to_tsvector('simple',  unaccent(coalesce(NEW.city_slug, ''))), 'C')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.description, ''))), 'D');

  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_events_search_refresh
BEFORE INSERT OR UPDATE OF
  title, description, venue_name, venue_slug, city_name, city_slug
ON events
FOR EACH ROW
EXECUTE FUNCTION events_search_refresh();


-- Índices (GIN) para búsqueda
CREATE INDEX IF NOT EXISTS idx_events_search_document_gin ON events USING gin (search_document);
CREATE INDEX IF NOT EXISTS idx_events_search_text_trgm ON events USING gin (search_text gin_trgm_ops);

-- Índices para filtros / joins
-- event_artists: para EXISTS por artista y join con events
CREATE INDEX IF NOT EXISTS idx_event_artists_event_artist ON event_artists (event_id, artist_id);
CREATE INDEX IF NOT EXISTS idx_event_artists_artist_event ON event_artists (artist_id, event_id);


-- Función: búsqueda pública (solo APPROVED) con filtros:
-- query, fechas, provincia, ciudad(slug), artista(slug)
CREATE FUNCTION search_events(
  p_q_raw text,
  p_min_similarity double precision,
  p_fts_weight double precision,
  p_trgm_weight double precision,
  p_date_from timestamptz,
  p_date_to timestamptz,
  p_province_id uuid,
  p_city_slug text,
  p_artist_slug text
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
      -- score: FTS + TRGM (para ranking), si no hay query => 0
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
    -- pública: solo aprobados
    e.status = 'APPROVED'

    -- filtros exactos
    AND (p_date_from IS NULL OR e.start_date_time >= p_date_from)
    AND (p_date_to   IS NULL OR e.start_date_time <= p_date_to)
    AND (p_province_id IS NULL OR e.province_id = p_province_id)
    AND (p_city_slug IS NULL OR e.city_slug = p_city_slug)
    AND (
      p_artist_slug IS NULL
      OR EXISTS (
        SELECT 1
        FROM event_artists ea
        WHERE ea.event_id = e.id
          AND ea.artist_id = (SELECT a.id FROM artists a WHERE a.slug = p_artist_slug)
      )
    )

    -- búsqueda:
    -- - si query vacía => devuelve todo (score 0)
    -- - si no vacía => AND (FTS) + fallback para prefijos/typos con TRGM/LIKE
    AND (
      q.tsq IS NULL
      OR (
        -- AND por defecto (muy eficiente con GIN sobre search_document)
        e.search_document @@ q.tsq

        -- Fallback para prefijos/fragmentos en query corta ("milen" -> "milenrama")
        -- Se activa SOLO para queries cortas para mantener rendimiento.
        OR (length(q.q_norm) <= 6 AND e.search_text LIKE '%' || q.q_norm || '%')

        -- TRGM tolerante (sirve para typos y para query corta en texto largo)
        OR word_similarity(e.search_text, q.q_norm) >=
           CASE
             WHEN length(q.q_norm) <= 3 THEN 0.12
             WHEN length(q.q_norm) <= 6 THEN 0.18
             WHEN length(q.q_norm) <= 12 THEN 0.25
             ELSE p_min_similarity
           END
        OR e.search_text % q.q_norm
        OR similarity(e.search_text, q.q_norm) >= p_min_similarity
      )
    );
$$;


-- Función más pesada pero que solo se ejecutará si la principal search_events() no devuelve resultados
CREATE FUNCTION search_events_or(
  p_q_raw text,
  p_min_similarity double precision,
  p_fts_weight double precision,
  p_trgm_weight double precision,
  p_date_from timestamptz,
  p_date_to timestamptz,
  p_province_id uuid,
  p_city_slug text,
  p_artist_slug text
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
        -- ranking: si un término aparece en el documento, sube
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
    AND (p_date_from IS NULL OR e.start_date_time >= p_date_from)
    AND (p_date_to   IS NULL OR e.start_date_time <= p_date_to)
    AND (p_province_id IS NULL OR e.province_id = p_province_id)
    AND (p_city_slug IS NULL OR e.city_slug = p_city_slug)
    AND (
      p_artist_slug IS NULL
      OR EXISTS (
        SELECT 1
        FROM event_artists ea
        WHERE ea.event_id = e.id
          AND ea.artist_id = (SELECT a.id FROM artists a WHERE a.slug = p_artist_slug)
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
               WHEN length(t.term) <= 3 THEN 0.12
               WHEN length(t.term) <= 6 THEN 0.18
               WHEN length(t.term) <= 12 THEN 0.25
               ELSE p_min_similarity
             END
      )
    );
$$;

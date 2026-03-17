-- ============================================================
-- Flyway: incluir artistas en búsqueda de texto libre
-- Version: 1.2.0.5
-- Description: Desnormaliza nombres de artistas en search_text y search_document
--   para que la búsqueda libre encuentre eventos por nombre de artista.
--   Se añade un trigger en event_artists que refresca las columnas de búsqueda
--   del evento padre al añadir o quitar artistas.
-- ============================================================


-- -------------------------------------------------------
-- 1. Actualizar events_search_refresh para incluir artistas
--    (se ejecuta en INSERT/UPDATE de campos del evento)
--    En INSERT el evento aún no tiene artistas vinculados;
--    el trigger de event_artists los añadirá después.
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION events_search_refresh()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  v_artists text;
BEGIN
  -- Artistas ya vinculados al evento (vacío en INSERT inicial, correcto en UPDATE)
  SELECT coalesce(string_agg(a.name, ' '), '')
  INTO v_artists
  FROM event_artists ea
  JOIN artists a ON a.id = ea.artist_id
  WHERE ea.event_id = NEW.id;

  -- TRGM: título, recinto, ciudad + artistas
  NEW.search_text :=
    lower(
      unaccent(
        concat_ws(' ',
          coalesce(NEW.title, ''),
          coalesce(NEW.venue_name, ''),
          coalesce(NEW.venue_slug, ''),
          coalesce(NEW.city_name, ''),
          coalesce(NEW.city_slug, ''),
          coalesce(v_artists, '')
        )
      )
    );

  -- FTS: artistas con peso B (nombres propios → config 'simple', sin stemming)
  NEW.search_document :=
      setweight(to_tsvector('spanish', unaccent(coalesce(NEW.title, ''))),       'A')
    || setweight(to_tsvector('simple',  unaccent(coalesce(v_artists, ''))),       'B')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.venue_name, ''))), 'B')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.city_name, ''))),  'B')
    || setweight(to_tsvector('simple',  unaccent(coalesce(NEW.venue_slug, ''))), 'C')
    || setweight(to_tsvector('simple',  unaccent(coalesce(NEW.city_slug, ''))),  'C')
    || setweight(to_tsvector('spanish', unaccent(coalesce(NEW.description, ''))), 'D');

  RETURN NEW;
END;
$$;


-- -------------------------------------------------------
-- 2. Trigger en event_artists: refresca búsqueda del evento
--    padre cuando se añaden/eliminan artistas.
--    Usa UPDATE directo de search_text/search_document para
--    no disparar trg_events_search_refresh (que escucha solo
--    title/venue/city/description).
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION event_artists_refresh_event_search()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  v_event_id      uuid;
  v_artists       text;
  v_search_text   text;
  v_search_doc    tsvector;
BEGIN
  v_event_id := COALESCE(NEW.event_id, OLD.event_id);

  -- Todos los artistas del evento tras el cambio
  SELECT coalesce(string_agg(a.name, ' '), '')
  INTO v_artists
  FROM event_artists ea
  JOIN artists a ON a.id = ea.artist_id
  WHERE ea.event_id = v_event_id;

  -- Recalcular search_text y search_document con datos actuales del evento
  SELECT
    lower(unaccent(concat_ws(' ',
      coalesce(e.title, ''),
      coalesce(e.venue_name, ''),
      coalesce(e.venue_slug, ''),
      coalesce(e.city_name, ''),
      coalesce(e.city_slug, ''),
      coalesce(v_artists, '')
    ))),
        setweight(to_tsvector('spanish', unaccent(coalesce(e.title, ''))),       'A')
      || setweight(to_tsvector('simple',  unaccent(coalesce(v_artists, ''))),     'B')
      || setweight(to_tsvector('spanish', unaccent(coalesce(e.venue_name, ''))), 'B')
      || setweight(to_tsvector('spanish', unaccent(coalesce(e.city_name, ''))),  'B')
      || setweight(to_tsvector('simple',  unaccent(coalesce(e.venue_slug, ''))), 'C')
      || setweight(to_tsvector('simple',  unaccent(coalesce(e.city_slug, ''))),  'C')
      || setweight(to_tsvector('spanish', unaccent(coalesce(e.description, ''))), 'D')
  INTO v_search_text, v_search_doc
  FROM events e
  WHERE e.id = v_event_id;

  -- Actualizar solo las columnas de búsqueda (no dispara trg_events_search_refresh)
  UPDATE events
  SET
    search_text     = v_search_text,
    search_document = v_search_doc
  WHERE id = v_event_id;

  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_event_artists_refresh_search
AFTER INSERT OR UPDATE OR DELETE
ON event_artists
FOR EACH ROW
EXECUTE FUNCTION event_artists_refresh_event_search();


-- -------------------------------------------------------
-- 3. Refrescar todos los eventos existentes con artistas
-- -------------------------------------------------------
UPDATE events e
SET
  search_text =
    lower(unaccent(concat_ws(' ',
      coalesce(e.title, ''),
      coalesce(e.venue_name, ''),
      coalesce(e.venue_slug, ''),
      coalesce(e.city_name, ''),
      coalesce(e.city_slug, ''),
      coalesce((
        SELECT string_agg(a.name, ' ')
        FROM event_artists ea
        JOIN artists a ON a.id = ea.artist_id
        WHERE ea.event_id = e.id
      ), '')
    ))),
  search_document =
      setweight(to_tsvector('spanish', unaccent(coalesce(e.title, ''))),       'A')
    || setweight(to_tsvector('simple',  unaccent(coalesce((
         SELECT string_agg(a.name, ' ')
         FROM event_artists ea
         JOIN artists a ON a.id = ea.artist_id
         WHERE ea.event_id = e.id
       ), ''))),                                                                'B')
    || setweight(to_tsvector('spanish', unaccent(coalesce(e.venue_name, ''))), 'B')
    || setweight(to_tsvector('spanish', unaccent(coalesce(e.city_name, ''))),  'B')
    || setweight(to_tsvector('simple',  unaccent(coalesce(e.venue_slug, ''))), 'C')
    || setweight(to_tsvector('simple',  unaccent(coalesce(e.city_slug, ''))),  'C')
    || setweight(to_tsvector('spanish', unaccent(coalesce(e.description, ''))), 'D');

-- ============================================================
-- Flyway limpieza y comentarios en Índices (v1)
-- Version: 1.0.0.6
-- Description: Limpieza de índices que se han ido creando en varios scripts.
--              Comentarios en los importantes.
--              Creación de nuevos índices útiles para búsquedas.
--              Estan inventariados en el fichero docs/db/indices_base_de_datos.md
-- ============================================================


-- =========================
-- Índices recomendados
-- =========================

-- Para la pantalla home: filtra por status, ordena por fecha, título, provincia y ciudad
-- Ordena por province_id en lugar de por pronvince.name para no hacer JOIN
CREATE INDEX idx_events_public_home_order ON events (status, start_date_time, title, province_id, city_name);

-- Para "Mis eventos": WHERE created_by_user_id=? + ORDER BY start_date_time
CREATE INDEX idx_events_created_by_start_date ON events (created_by_user_id, start_date_time);

-- Listado público típico por ciudad: WHERE status + city_slug + ORDER BY start_date_time ASC
CREATE INDEX idx_events_status_city_start ON events (status, city_slug, start_date_time);

-- Listado público por provincia sin ciudad
CREATE INDEX idx_events_status_province_start ON events (status, province_id, start_date_time);

COMMENT ON INDEX idx_events_public_home_order IS 'Home: filtra por status y ordena por start_date_time, title, province_id y city_slug.';
COMMENT ON INDEX idx_events_created_by_start_date IS 'Mis eventos: filtra por created_by_user_id y ordena por start_date_time.';
COMMENT ON INDEX idx_events_status_city_start IS 'Listado público: filtra por status + city_slug y ordena por start_date_time ASC.';
COMMENT ON INDEX idx_events_status_province_start IS 'Listado público: filtra por status + province_is y ordena por start_date_time ASC.';


-- =========================
-- Comentarios
-- =========================

-- artists
COMMENT ON INDEX idx_artists_created_at IS 'Ordenación/listados por fecha de creación de artistas (admin/moderación).';
COMMENT ON INDEX idx_artists_created_by_user_id IS 'Filtrar/listar artistas creados por un usuario (created_by_user_id).';
COMMENT ON INDEX idx_artists_name_lower IS 'Búsqueda case-insensitive por nombre de artista: lower(name).';
COMMENT ON INDEX uk_artist_slug IS 'UNIQUE + lookup rápido por slug exacto (filtros por artista, integridad).';

-- event_artists
COMMENT ON INDEX idx_event_artists_artist_event IS 'Acceso rápido a eventos por artista (artist_id -> event_id).';
COMMENT ON INDEX pk_event_artists IS 'PK/UNIQUE de la tabla puente (event_id, artist_id). Acelera joins y EXISTS por evento.';

-- events
COMMENT ON INDEX idx_events_status_start_date IS 'Listados por estado + fecha (público APPROVED y backoffice).';
COMMENT ON INDEX idx_events_province_city_start IS 'Filtros combinados provincia+ciudad y orden/rango por start_date_time.';
COMMENT ON INDEX idx_events_city_slug IS 'Filtro por ciudad (slug) en listados.';
COMMENT ON INDEX idx_events_venue_slug IS 'Filtro por recinto/sala (venue_slug).';
COMMENT ON INDEX idx_events_search_document_gin IS 'FTS (tsvector) para búsqueda pública: search_document @@ tsquery (GIN).';
COMMENT ON INDEX idx_events_search_text_trgm IS 'Tolerancia a typos/prefijos/contiene con pg_trgm sobre search_text (GIN trgm).';

-- provinces
COMMENT ON INDEX provinces_ine_code_key IS 'UNIQUE + lookup por código INE.';

-- users
COMMENT ON INDEX uk_users_email_lower IS 'UNIQUE + lookup case-insensitive por email: lower(email).';


-- =========================
-- Limpieza de índices redundantes
-- =========================

-- Redundante con provinces_ine_code_key (UNIQUE sobre el mismo campo)
DROP INDEX IF EXISTS idx_provinces_ine_code;

-- Redundante con pk_event_artists (event_id, artist_id)
DROP INDEX IF EXISTS idx_event_artists_event_artist;

-- Redundante con idx_events_province_city_start (prefijo izquierdo province_id)
DROP INDEX IF EXISTS idx_events_province_id;

-- Redundante porque SIEMPRE se consulta por slug exacto normalizado (y ya existe uk_artist_slug)
DROP INDEX IF EXISTS idx_artists_slug_lower;

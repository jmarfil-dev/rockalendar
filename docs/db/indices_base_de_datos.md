# Índices de base de datos (inventario + propósito)

Este documento lista TODOS los índices (excluyendo PKs), su propósito, y qué consultas típicas los aprovechan.

> Fuente: inventario hasta Flyway V1.2.0.2__moderation_actions.sql.

## Tabla: artists

### idx_artists_created_at (btree) (created_at)
**Propósito:** acelerar listados/ordenaciones por fecha de creación (p.ej. admin, “últimos artistas añadidos”).\
**Útil en:** hacer `ORDER BY created_at` o filtros por rango de fechas.

### idx_artists_created_by_user_id (btree) (created_by_user_id)
**Propósito:** acelerar consultas “artistas creados por X”.\
**Útil en:** pantallas de “mis artistas” o moderación por autor.

### idx_artists_name_lower (btree) (lower(name))
**Propósito:** búsquedas/casos de autocompletado case-insensitive por nombre.\
**Útil en :** hacer `WHERE lower(name) LIKE 'met%'` o `lower(name) = lower(?)`.\
**Nota:** usaando TRGM para autocompletar nombres, este índice puede quedarse corto para `'%term%'`. (Por ahora no se usa TRGM)

### uk_artist_slug (btree, UNIQUE) (slug)
**Propósito:** garantizar unicidad del slug y acelerar búsquedas exactas por slug.\
**Usado por:** filtros exactos tipo `WHERE slug = ?`, y en tu búsqueda pública para filtrar por artista vía slug.

## Tabla: event_artists (tabla puente)

### idx_event_artists_artist_event (btree) (artist_id, event_id)
**Propósito:** obtener eventos de un artista de forma eficiente.\
**Usado por:** `WHERE artist_id = ?` + joins hacia events.

### pk_event_artists (btree) (event_id, artist_id)
**Propósito:** PK de la tabla.\
**Nota importante:** acelera el “EXISTS”/join de artistas para un evento.\
**Usado por:** `WHERE event_id = ?` y joins hacia artists.\

## Tabla: events

### idx_events_created_by_start_date (btree) (created_by_user_id, start_date_time)
**Propósito:** obtener "mis eventos" de forma eficiente.\
**Usado por:** `WHERE created_by_user_id=?` + `ORDER BY start_date_time`.

### idx_events_city_slug (btree) (city_slug)
**Propósito:** filtrar por ciudad (slug) en listados públicos o internos.\
**Usado por:** `WHERE city_slug = ?`.

### idx_events_province_city_start (btree) (province_id, city_slug, start_date_time)
**Propósito:** filtros combinados por provincia + ciudad, y orden/rango por fecha.\
**Usado por:** `WHERE province_id = ? AND city_slug = ?` y además rangos/ordenación por start_date_time.

### idx_events_public_home_order (btree) (status, start_date_time, title, province_id, city_name)
**Propósito:** filtra por status, ordena por fecha, título, provincia y ciudad.\
**Usado por:** la pantalla home de la aplicación.

### idx_events_search_document_gin (gin) (search_document)
**Propósito:** Full Text Search (FTS) rápida sobre el documento de búsqueda.\
**Usado por:** `search_document @@ tsquery` dentro de `search_events(...)`.\
**Nota:** es la base del “AND” eficiente por FTS.

### idx_events_search_text_trgm (gin) (search_text gin_trgm_ops)
**Propósito:** tolerancia a errores (typos), prefijos, y “contiene” sobre un texto normalizado.\
**Usado por:** operadores TRGM (`%`, `similarity`, `word_similarity`) y también acelera patrones tipo `LIKE '%term%'` en muchos casos.

### idx_events_status_city_start (btree) (status, city_slug, start_date_time)
**Propósito:** listados por estado (APPROVED, etc.) y ciudad ordenados/filtrados por fecha.\
**Usado por:** listados públicos (solo APPROVED) y backend/admin con filtros de status.

### idx_events_status_province_start (btree) (status, province_id, start_date_time)
**Propósito:** listados por estado (APPROVED, etc.) y provincia ordenados/filtrados por fecha.\
**Usado por:** listados públicos (solo APPROVED) y backend/admin con filtros de status.

### idx_events_status_start_date (btree) (status, start_date_time)
**Propósito:** listados por estado (APPROVED, etc.) ordenados/filtrados por fecha.\
**Usado por:** listados públicos (solo APPROVED) y backend/admin con filtros de status.

### idx_events_venue_slug (btree) (venue_slug)
**Propósito:** filtrar por sala/recinto por slug.\
**Usado por:** `WHERE venue_slug = ?`.\
**Nota:** si más adelante se añade filtro + orden por fecha, puede interesar un índice compuesto (venue_slug, start_date_time).

### idx_events_created_by_status_start_date (btree) (created_by_user_id, status, start_date_time)
**Propósito:** filtrar por usuario, estado y fecha.\
**Usado por:** búsquedas de eventos privados.\

## Tabla: moderation_actions

### idx_moderation_actions_event_id (btree) (event_id)
**Propósito:** acelerar listados/ordenaciones por eventos.\
**Útil en:** hacer `ORDER BY event_id`.

### idx_moderation_actions_created_at (btree) (created_at)
**Propósito:** acelerar listados/ordenaciones por fecha de creación.\
**Útil en:** hacer `ORDER BY created_at` o filtros por rango de fechas.

## Tabla: provinces

### provinces_ine_code_key (btree, UNIQUE) (ine_code)
**Propósito:** garantizar unicidad del INE y acelerar búsquedas exactas por ine_code.

## Tabla: users

### uk_users_email_lower (btree, UNIQUE) (lower(email))
**Propósito:** unicidad y búsqueda case-insensitive por email.\
**Usado por:** login/registro (`WHERE lower(email)=lower(?)`).

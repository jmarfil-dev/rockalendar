# Modelo de dominio y datos

## 1\. Propósito y alcance

Este documento define la **especificación del modelo de dominio y datos** de *Rockalendar*.

- Describe las entidades, relaciones y reglas del sistema independientemente de la implementación.
- El modelo relacional aquí descrito debe entenderse como la fuente de verdad funcional del sistema.

El objetivo es que cualquier implementación concreta (ORM, migraciones, scripts) sea una consecuencia directa de esta especificación, y no al revés.

## 2\. Conceptos clave del dominio

*Rockalendar* gira alrededor de tres ideas:

- **Eventos** (conciertos/festivales) como elemento principal consultable.
- **Curación mediante moderación**: un evento propuesto no es público hasta ser aprobado.
- **Colaboración con confianza progresiva**: usuarios aportan contenido; el sistema refuerza calidad (score y roles).

En el MVP, el producto prioriza descubrimiento y planificación sobre funciones sociales.

## 3\. Entidades del dominio (conceptual)

### 3.1 User

Representa a una persona con capacidad de autenticación y acciones dentro del sistema.

Atributos conceptuales:

- Identidad: email/username (según diseño), credenciales.
- Roles: USER / MODERATOR / ADMIN.
- Score interno (confianza).
- `preferred_language` (anticipación): campo existente para evolución futura, sin condicionar el comportamiento del MVP.

Relaciones:

- Puede proponer eventos.
- Puede interactuar con eventos (me interesa / asistiré).
- Puede ser moderador de eventos (si tiene rol).

### 3.2 Event

Un evento es un concierto o festival con información estructurada.

Atributos conceptuales:

- Título (texto libre) y descripción (opcional).
- Fecha/hora (o rango si aplica, según evolución).
- Ubicación: ciudad/provincia (y opcionalmente recinto/dirección en futuro).
- Artista(s): al menos uno (en MVP suele ser un artista principal).
- Estado de moderación.
- Autor (usuario que propone).

Relaciones:

- Asociado a uno o varios artistas.
- Asociado a una localización (ciudad/provincia).
- Tiene un historial de decisiones de moderación (mínimo: estado actual + motivo).

### 3.3 Artist

Representa un artista o grupo. Existe para mejorar búsquedas y consistencia.

Atributos conceptuales:

- Nombre canónico.
- Aliases/variantes (opcional futuro).

Reglas:

- En MVP, se permite creación "al vuelo" al proponer un evento.
- Moderadores pueden ajustar/normalizar datos básicos.

### 3.4 Geo (Province + ciudad embebida en Event)

En el MVP, el modelo geográfico se diseña de forma híbrida para reducir complejidad inicial sin perder capacidad de evolución:

- Province existe como entidad propia (catálogo fijo con código INE y UUIDs deterministas).
- La ciudad no se normaliza todavía como entidad: se guarda en el propio evento como city_name y city_slug.

Este enfoque evita depender de datasets de ciudades o integraciones externas desde el inicio, y permite evolucionar más adelante hacia una tabla cities si el producto lo valida.

Atributos conceptuales:

- Province: ine_code, name.
- Ciudad (en evento): city_name, city_slug.

Reglas:

- Integraciones externas (geocoding/places) se consideran fuera del MVP.

### 3.5 Moderation (decisión de moderación)

Representa el acto de aprobar/rechazar una propuesta.

Atributos conceptuales:

- Moderador (quién decide).
- Decisión: APPROVED / REJECTED.
- Motivo (texto) y timestamp.

Relación:

- Un evento tiene estado de moderación y puede tener historial de decisiones.

### 3.6 UserEvent (interacción)

Representa la relación usuario-evento para agenda.

Atributos conceptuales:

- Estado de interacción: INTERESTED / GOING.
- Timestamp.

Reglas:

- No es público ni gamificado.
- Un usuario puede tener como máximo una interacción "activa" por evento (según diseño: Interested o Going).

## 4\. Estados y reglas de negocio

### 4.1 Estados de Event

Estados implementados:

- **PENDING_MODERATION**: propuesto, en espera de revisión; no visible públicamente.
- **APPROVED**: visible y consultable.
- **REJECTED**: rechazado por moderación; no visible; conserva motivo.
- **NEEDS_CHANGES**: el moderador solicita correcciones al autor; no visible.
- **DRAFT**: borrador guardado por el autor antes de enviar a moderación; no visible.
- **FLAGGED**: marcado automáticamente para revisión por el sistema de automodera­ción; no visible.
- **HIDDEN**: ocultado manualmente por un moderador; no visible.
- **CANCELED**: cancelado (p. ej. por el autor o un admin); no visible.
- **ERASED**: borrado lógico; no visible ni recuperable desde la interfaz.

Reglas:

- Un evento solo es visible públicamente si está en estado `APPROVED`.
- Un moderador no puede moderar su propio evento.
- Las decisiones de moderación ajustan el score del autor.

### 4.2 Score de usuario (conceptual)

- Score interno, no visible al público.
- Se ajusta principalmente por calidad de propuestas (aprobadas / rechazadas / duplicadas).
- Se usa como criterio para otorgar o retirar rol de moderador (además del criterio de administradores).

## 5\. Modelo de datos (relacional)

**Nota**: este apartado define el modelo relacional objetivo para el MVP y versiones inmediatas, manteniendo el foco en estructura, relaciones y reglas.

### 5.1 Diagrama E/R (alto nivel)

~~~
users (1) ────────< events (N)
  │                  │
  │                  ├── (N) >────< (N) artists   (via event_artists)
  │                  │
  │                  ├── (N) >──── (1) provinces
  │                  │
  │                  └── approved_by_user_id (FK opcional) ──> users
  │
  └── (N) >────< (N) events (via user_events)

moderation_actions (N) ───── (1) events
moderation_actions (N) ───── (1) users (moderator)
~~~
~~~
  │
  └── (N) >────< (N) events (via user_events)   [planificado]

moderation_actions (N) ───── (1) events
moderation_actions (N) ───── (1) users (moderator)
~~~

## 6. Tablas (MVP actual) + planificadas

### 6.1 `users`

Campos:
- `id` (UUID, PK)
- `email` (varchar(254), único case-insensitive vía índice)
- `password_hash` (varchar(255))
- `role` (varchar(20)) con constraint `USER|MODERATOR|ADMIN`
- `trust_score` (int, default 0, `>= 0`)
- `preferred_language` (varchar(10), nullable)
- `created_at` (timestamptz)

Índices/constraints:
- Índice único: `uk_users_email_lower` sobre `lower(email)`.
- `chk_users_roles`, `chk_users_trust_score`.

### 6.2 `provinces`

Campos:
- `id` (UUID, PK) **determinista** (UUIDv5) basado en `ine_code`.
- `ine_code` (smallint, UNIQUE, 1..52)
- `name` (varchar(80))

Índices/constraints:
- `chk_provinces_ine_code`.
- `idx_provinces_ine_code`.


- Se usa `uuid-ossp` y `uuid_generate_v5()` con un namespace fijo del proyecto.

### 6.3 Ciudades (decisión del MVP)

En el MVP no se modela una tabla `cities`.

La ciudad se almacena en `events` como:
- `city_name`
- `city_slug`

Esto simplifica el arranque y evita dependencias. El diseño admite evolución posterior hacia una tabla `cities` (dataset INE/GeoNames o integraciones Places) si aporta valor.

### 6.4 `artists`

Campos:
- `id` (UUID, PK)
- `name` (varchar(200))
- `slug` (varchar(200), UNIQUE)
- `created_by_user_id` (UUID, nullable, FK → users.id)
- `created_at` (timestamptz, default now())

Índices/constraints:
- `uk_artist_slug`.
- Índices de apoyo: `idx_artists_name_lower`, `idx_artists_slug_lower`, `idx_artists_created_by_user_id`, `idx_artists_created_at`.

### 6.5 `events`

Campos:
- `id` (UUID, PK)
- `title` (varchar(200))
- `description` (text, nullable)
- `start_date_time` (timestamptz)
- `end_date_time` (timestamptz, nullable; constraint `end >= start`)
- `venue_name` (varchar(200))
- `venue_slug` (varchar(200))
- `province_id` (UUID, FK → provinces.id)
- `city_name` (varchar(120))
- `city_slug` (varchar(120))
- `status` (varchar(30)) con valores:
  - `PENDING_MODERATION`, `APPROVED`, `REJECTED`, `CANCELLED`, `HIDDEN`
- `source_url` (text, nullable)
- `created_by_user_id` (UUID, nullable, FK → users.id)
- `approved_by_user_id` (UUID, nullable, FK → users.id)
- `rejection_reason` (text, nullable)
- `created_at`, `updated_at` (timestamptz)

Índices existentes:
- `idx_events_status_start_date` (status, start_date_time)
- `idx_events_province_city_start` (province_id, city_slug, start_date_time)
- `idx_events_city_slug`
- `idx_events_venue_slug`
- `idx_events_province_id`

Búsqueda:
- La búsqueda se implementa con **PostgreSQL FTS** y **pg_trgm** para tolerancia a errores y similitud.
- Extensiones:
  - `CREATE EXTENSION IF NOT EXISTS pg_trgm;`
  - (opcional) `CREATE EXTENSION IF NOT EXISTS unaccent;`
- FTS:
  - Columna `tsvector` (generada o materializada) e índice GIN.
- Trigram:
  - Índices GIN/GiST trigram en `events.title` y (opcionalmente) `artists.name` / `artists.slug`.

### 6.6 `event_artists` (tabla puente)

- `event_id` (FK → events.id, ON DELETE CASCADE)
- `artist_id` (FK → artists.id)

Índices/constraints:
- PK compuesta: `(event_id, artist_id)`
- Índice: `idx_event_artists_artist_id`.

### 6.7 `moderation_actions`

Campos:
- `id` (UUID, PK)
- `event_id` (UUID, FK → events.id, ON DELETE CASCADE)
- `moderator_id` (UUID, FK → users.id)
- `action_type` (varchar(30)) con valores: `APPROVE|REJECT|HIDE|REQUEST_CHANGES|AUTO_REJECT`
- `reason` (text, nullable)
- `created_at` (timestamptz)

### 6.8 `user_events` (interacción/agenda)

Tabla que soporta la agenda personal y las marcas “me interesa / asistiré”.

Campos:
- `user_id` (FK → users.id, ON DELETE CASCADE)
- `event_id` (FK → events.id, ON DELETE CASCADE)
- `status` (`INTERESTED|GOING`)
- `created_at` (timestamptz)

Índices/constraints:
- PK compuesta `(user_id, event_id)`
- `idx_user_events_user_id` para listar la agenda de un usuario eficientemente.
- Para cambiar de `INTERESTED` a `GOING` se hace upsert actualizando el registro existente.

## 7. Internacionalización y datos

- En MVP, el idioma efectivo se determina por `Accept-Language` con fallback al inglés.
- `users.preferred_language` existe por anticipación.

Regla de documentación: la existencia de este campo no implica UI ni comportamiento adicional en el MVP.

## 8. Notas de evolución (post-MVP)

Sin comprometer el roadmap, el modelo admite evolución hacia:
- Catálogo de lugares/recintos.
- Aliases de artistas y normalización avanzada.
- Dataset de ciudades y códigos externos.
- Auditoría más completa.

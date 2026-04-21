# Backlog no comprometido

Este documento recoge ideas, mejoras y posibles evoluciones surgidas durante el desarrollo de *Rockalendar*.

**No constituye un compromiso de implementación ni un orden de prioridad.**

Son ideas sujetas a validación, disponibilidad de tiempo y coherencia con la filosofía del proyecto. Su función es preservar contexto y decisiones futuras, no definir el rumbo del proyecto.

## Arquitectura y base técnica

Estas implementaciones tienen prioridad sobre las demás.

### Seguridad

- Guardar token en httpOnly cookies: al parecer es un agujero de seguridad importante.
- Agregar rate limiting a todas las Request no GET.
- Refresh token + httpOnly cookies: si almacenamos más datos personales y/o sensibles.
  - Si alguien roba un token tiene acceso al email y puede proponer conciertos en su nombre. No es catastrófico.
  - Logout en backend para evitar tokens robados.

### Backend

- Cuando el tráfico crezca y no haga falta monitoriear: eliminar TrafficLoggingInterceptor y WebMvcConfig.
- Base común para búsquedas, filtros y mapas.
- Reemplazar `Page<T>` por un DTO propio de paginación: `PageResponse`:
  - Control total del contrato JSON
  - Desacoplar API pública de Spring Data
  - Mantener `@EnableSpringDataWebSupport(VIA_DTO)` solo como solución temporal
  - Estructura estable: `content`, `page`, `size`, `totalElements`, `totalPages`
- Devolver `ResponseEntity` en los controladores y en `GlobalExceptionHandler` (`@ControllerAdvice`).
- Anotaciones de validación compuestas si hay repetición real.
- Validators propios solo si hay reglas reutilizables.
- TRGM para Autocompletar de `artist`.

### Mejora de búsquedas

**Mejora búsqueda de eventos públicos**

**PRIORITARIO:** cuando haya volumen alto de datos:
- Medir con `EXPLAIN ANALYZE` sobre 2–3 consultas reales (búsqueda + listados)
- Si hay muchos eventos REJECTED acumulados:
  - Migrar GIN global → GIN parcial APPROVED (y borrar el global)

~~~
-- Índices parciales para búsqueda pública (si tienes muchos no-APPROVED)
CREATE INDEX idx_events_search_document_gin_approved ON events USING gin (search_document) WHERE status = 'APPROVED';
CREATE INDEX idx_events_search_text_trgm_approved ON events USING gin (search_text gin_trgm_ops) WHERE status = 'APPROVED';
~~~

**Mejora búsqueda de artistas (autocomplete)**

Migrar búsqueda LIKE '%term%' (como está ahora en ArtistRepository) a pg_trgm:
- Crear índices GIN (lower(name) gin_trgm_ops) y GIN (slug gin_trgm_ops)
- Ajustar query para aprovechar similarity / % operator
- Mantener límite Top 10 y orden por relevancia
Motivo: escalar con cientos/miles de artistas sin degradar autocomplete.

Cuándo hacerlo:
- artistas > 5 000–10 000
- autocomplete empieza a “rascar” en producción
- EXPLAIN muestra Seq Scan constante en artists

## Cookies

- Banner de aviso del uso de cookies cuando se use alguna que no sea de sistema, por ahora no es obligatorio.

## Gestión del ciclo de vida de eventos

- Lista de eventos públicos pasados.
- Permitir eventos pasados en Administración.
- EventAutoRejectionScheduler.REJECTION_MESSAGE configurable y traducible.
- Desarrollar estado `DRAFT`.
- Historial de estados para auditoría.
- Estado `REQUEST_CANCEL` para solicitar cancelación de evento.
  - Acción `CANCEL` para moderadores: cancelar eventos en estados `APPROVED`.

## Eventos (otros)

- Opción: cartel de evento mediante URL externa. En lugar de subir una imagen, aceptar url y referenciarla sin guardarla.
- Varios carteles por evento.
- Agregar lista de artistas desde cartel mediante Vision API (Claude/GPT-4o).

### Explicación de Vision API

~~~
Flujo híbrido: el modelo de visión extrae una propuesta de lista de artistas, y el usuario la revisa y confirma antes de guardar. Así:
  1. Usuario sube el cartel
  2. La API llama a Claude con visión y prompt tipo "extrae todos los nombres de bandas/artistas de este cartel de festival"
  3. Se pre-rellena el campo de artistas con el resultado
  4. El usuario añade/quita los que estén mal
~~~

**Problema**\
Cada llamada al API tiene un coste aproximado de ~2.000 tokens → $0.006 (medio céntimo) a fecha marzo de 2026 (puede subir y es probable que lo haga).\
Es decir, unos ~6€ al mes si se llama 1000 veces al API.\
Siendo realistas, no se van a proponer 1000 festivales al mes, pero puede haber trampas / abusos (bots, bugs).

**Solución / protección (siendo optimistas)**
- Rate limiting: ya está implementado en POST públicos (login, registro, reset-password).

> Lo bueno es que solo los usuarios registrados pueden proponer / editar eventos y que el tamaño de la imagen está limitado a 1200px (el tamaño afecta al coste de la llamada al API).

## Artistas

- Agregar al detalle de artista enlaces útiles (spoty, youtube, redes sociales).
- Formulario para usuarios de "sugerir cambios".

## Administración

- Gestión de perfiles:
  - Ascender a moderador
  - Banear moderador
  - Ascender a admin
- Historial de moderación en detalle de eventos.

## Moderación

**Sistema disciplinario de moderadores**
- Tabla moderator_warnings (id, user_id, assigned_by, reason, created_at): warnings asignados manualmente por admin.
- Campo suspended_until (timestamp nullable) en users: lógica de suspensión incremental (5 días → 20 días → indefinida).
- Ban permanente tras 3 suspensiones.
- Detección de sesgo (>50% decisiones sobre mismo usuario/sala/grupo en 30 días).
- Rehabilitación manual por admin.
- Reportes de eventos mal moderados → bandeja de admin (cuando exista panel).

**Automoderación**
- Si un artista tiene X eventos rechazados por blacklist (no por otros motivos), el sistema sugerirá añadirlo a la blacklist de artistas.
- Si el sistema anti-spam no se queda corto, agregar regla: auto rechazo eventos propuestos por usuarios con trust_score igual al redflag para ascender a moderador (actualmente -200).

## Traducciones

- Traducción a las lenguas cooficiales de España: catalán, euskera, valenciano y gallego. Necesitaremos voluntarios.

## Funciones sociales (base)

- Seguir artistas.
- Notificación cuando se publica un nuevo evento de un artista seguido.
- Compartir eventos / artistas por:
  - WhatsApp
  - X
  - Instagram
  - Facebook

## Notificaciones

- Confirmar cuenta al registrarse.
  - Para usuarios ya registrado, confirmar cuenta la primera vez que se logueen tras implementar la funcionalidad de notificaciones o en la configuración de cuenta que salgan todas las notificaciones deshabilitadas por defecto y que para activarlas sea necesario confirmar la cuenta.
  - Modificación de email en settings (depende de la confirmación de cuenta).
- Canales: para esto se crea la interfaz NotificationChannel, hay que agregar un @Component nuevo por cada canal.
  - Email
  - Push (prioridad muy baja)
- Preferencias de notificaciones por usuario para nuevos canales, las in-app no se pueden quitar.
- Recordatorios cuando se acerca la fecha de un evento marcado como interés o asistencia.

## Crews (grupos)

- Creación de grupos privados (crew).
- Acceso solo mediante invitación.
- Notificaciones internas cuando:
  - Un integrante publica un evento
  - Un integrante marca asistencia
  - Un integrante marca un evento como "opción interesante"
- Preferencias de notificaciones por usuario y por crew.
- Base pensada para futura apertura a crews públicas.

## Geografía y localización

### Geocodificación y normalización de ubicaciones

Actualmente `city_name` y `city_slug` son texto libre en `events` (ADR-006).

**Decisión de diseño:** crear una tabla `locations` que crece orgánicamente con el uso, sin precarga de datos.

Cuando alguien propone un evento:
1. El usuario escribe la ciudad → autocomplete en tiempo real contra **Nominatim** (OpenStreetMap, gratuito).
2. Se busca si la ubicación ya existe en `locations` (por slug).
3. Si no existe: se geocodifica con Nominatim, se crea la fila y se vincula al evento.
4. Si ya existe: se reutiliza la FK directamente.

```
locations (id, name, slug, province_id, latitude, longitude)
events.location_id → locations.id
```

Ventajas:
- Las coordenadas se almacenan **una sola vez** por lugar (no por evento).
- `events` no se convierte en un cajón de sastre.
- El catálogo solo contiene lugares que realmente tienen eventos.
- Cubre cualquier granularidad: municipio, barrio, pedanía.
- Permite consultas directas sobre ubicaciones ("salas / ciudades con más eventos").

Nota: `city_name` y `city_slug` en `events` pueden mantenerse como caché desnormalizado para evitar JOINs en listados, a valorar en el momento de la implementación.

Riesgo: dependencia de Nominatim en el flujo de creación. Mitigación: degradación elegante — guardar el evento sin coordenadas si la geocodificación falla.

**Búsqueda por proximidad (consecuencia directa)**

Una vez que `locations` tenga coordenadas, la búsqueda por radio queda desbloqueada:
- Parámetros nuevos en la búsqueda pública: `lat`, `lon`, `radiusKm`.
- Filtro mediante fórmula de Haversine en la función PostgreSQL `search_public_events`, sin necesidad de PostGIS.
- Si en el futuro se añade PostGIS, la migración es trivial (columna `GEOGRAPHY(POINT)` + `ST_DWithin`).

**Mapas de eventos**
- Mapa interactivo por ciudad / provincia (requiere coordenadas en `locations`).
- Candidatos: Leaflet + OpenStreetMap (sin coste), Mapbox (más pulido, freemium).

## Integraciones externas

- hCaptcha progresivo en registro y login si el spam se vuelve un problema.
- Spotify:
  - Enlazar artista
  - Mostrar canciones populares
  - Verificación de artista
- Plataformas de venta de entradas (Ticketmaster, Entradium, etc.).
- Enlaces externos desde eventos.

## Monetización y sostenibilidad

- Espacios para publicidad (ética y no intrusiva).

## Clientes nativos

- App móvil nativa (iOS / Android).

## Funciones sociales avanzadas

- Darle una vuelta a que un usuario pueda poner que tiene plazas libres en el coche para ir a un evento y su ciudad de partida.
- Recomendaciones personalizadas.
- Comentarios y valoraciones.

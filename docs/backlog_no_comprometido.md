# Backlog no comprometido

Este documento recoge ideas, mejoras y posibles evoluciones surgidas durante el desarrollo de *Rockalendar*.

**No constituye un compromiso de implementación ni un orden de prioridad.**

Son ideas sujetas a validación, disponibilidad de tiempo y coherencia con la filosofía del proyecto. Su función es preservar contexto y decisiones futuras, no definir el rumbo del proyecto.

## Arquitectura y base técnica

Estas implementaciones tienen prioridad sobre las demás.

### Backend

- Wrapper `CurrentUser` para centralizar autenticación y acceso al usuario actual.
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

## Gestión del ciclo de vida de eventos

- Constraint `UNIQUE` y evitar duplicados en eventos (por provincia, ciudad, recinto y fecha).
- Devolver error 409 Conflict cuando se detecte un duplicado a `MeEventApi.propose()`.
- Cron job cada 24 horas para cerrar eventos pasados.
- Nuevo estado `REALIZADO` o `PASADO` (nombre por decidir).
- Historial de estados para auditoría.

## Artistas y contenido de eventos

- Detalle de artista.
- Imágenes (cartel del evento).

## Moderación automática y calidad

- Reachazo automático de eventos mediante blacklist:
  - Palabras clave para el título
  - Combinación de fecha + ciudad (ejemplo: primero de abril + Villarobledo = Viñarock)
- Gestión de blacklist por administradores.

## Funciones sociales (base)

- Seguir artistas.
- Notificación cuando se publica un nuevo evento de un artista seguido.
- Compartir eventos / artistas por:
  - WhatsApp
  - X
  - Instagram
  - Facebook

## Notificaciones

- Preferencias de notificaciones por usuario.
- Canales:
  - Email
  - Notificaciones in-app
  - Push (prioridad muy baja)
- Recordatorios cuando se acerca la fecha del evento.

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

- Precargar ciudades desde dataset (INE / GeoNames / otro).
- Autocompletado de direcciones y lugares.
- Integración con Places / Geocoding (Google / Mapbox / OpenStreetMap).
- Mapas de eventos (por ciudad / provincia).

## Integraciones externas

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

- Recomendaciones personalizadas.
- Comentarios y valoraciones.

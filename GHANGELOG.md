# Changelog

Este documento resume los cambios relevantes por versión Rockalendar.
El versionado de releases se marca mediante tags (p. ej. `v0.1-backend`).

## [v0.3.0-frontend]
### Added
- [FRONT] Layout privado y hub `/me` con cards de agenda y mis conciertos.
- [FRONT] Página `/me/events` con pestañas, ordenación y paginación.
- [FRONT] Formulario para proponer concierto con selector de artistas.
- [FRONT] Detalle y edición de evento propio en `/me/events`.
- [FRONT] Sección de moderación completa (`/moderation`).
- [FRONT] Accesibilidad WCAG AA en toda la app.
- [FRONT] Link a Google Maps en el recinto del detalle de evento.
- [BACK] Endpoint `GET /me/events/{id}` con `EventPrivateDto` completo.
- [BACK] Endpoint `GET /moderation/events/{id}`.
- [BACK] Indexación de nombres de artistas en búsqueda de texto libre.
- [BACK] Logs mínimos en servicios de escritura (`@Slf4j`).

### Changed
- [FRONT] Extraer utilidad `formatEventDate` y usarla en las vistas.
- [FRONT] Extraer composable `useAuthForm` y componente `AuthEmailField`.
- [FRONT] `AppPaginator` extraído como componente; `useSortOptions` en página pública de eventos.
- [BACK] Mover `propose` a `/api/me/events`, renombrar `COMMENT` → `REQUEST_CHANGES` y `artistSlug` → `artistId`.
- [BACK] Refactorización de suite de tests; nuevos tests de `ArtistCommandService`.

### Fixed
- [FRONT] Logout, flash SSR en auth y manejo de errores de carga.
- [BACK] Race condition en creación de artistas concurrentes.
- [BACK] Validar longitud mínima de JWT secret y mejorar manejo de errores de auth.
- [BACK] Propagar `userId` al crear artistas nuevos desde propuesta de evento.
- [BACK] Queries de mis eventos (`submittedAt`) y claves de sort en panel de moderación.
- [BACK] Orden de operaciones en migración `V1.2.0.6`.

## [v0.2.0-frontend]
### Added
- [FRONT] Manejo de errores.
- [FRONT] Internacionalización i18n con selección de idioma.
- [FRONT] Login, sesión y logout.

### Changed
- [FRONT] Refactorización: types y rutas en ficheros externos, cambios en layouts y shell.
- [BACK] Cambiar mensajes literales de excepciones por códigos i18n.

## [v0.1.0-frontend]
### Added
- [FRONT] Base de Vue + Nuxt.
- [FRONT] Funcionalidad básica.
- [FRONT] Búsqueda de eventos públicos.
- [FRONT] Detalle de evento público.
- [BACK] Lista de provincias para combos.

### Changed
- [BACK] Eventos públicos: modificada función de búsqueda para usar id de artista.

## [v0.3.0-backend]
### Added
- Registro de usuarios.
- Eventos privados: proponer, actualizar, eliminar y consultar.
- Moderación: aprobar, rechazar, ocultar y solicitar cambios.
- Moderación: listado de eventos pendientes y archivados.
- Endpoint Home de eventos públicos.
- Ordenación configurable desde frontend (whitelist) en:
  - Home pública.
  - Búsqueda pública.
  - Eventos privados.
  - Listas de moderación.
- Tests:
  - Base común con Testcontainers.
  - Tests de contrato API.
  - Tests de servicio.

### Changed
- Búsqueda pública avanzada con ranking (FTS + trigram).
- Refactor del manejador global de errores y mensajes.
- Simplificación del uso de JWT en endpoints y servicios.
- Limpieza y mejora de índices en base de datos.

### Fixed
- Correcciones en seguridad y manejo de roles.
- Ajustes en autocompletado de artistas.

## [v0.2.0-backend]
### Added
- Autenticación y seguridad con JWT (login).
- Protección de endpoints y control de acceso para operaciones sensibles.

### Changed
- Crear artista requiere autenticación.

## [v0.1.1-backend]
### Fixed
- Ajustes y afinado de la búsqueda pública de eventos.

### Changed
- Refactor de nombres en eventos para mejorar claridad y consistencia.

## [v0.1-backend]
### Added
- Base del backend Spring Boot.
- Infraestructura local con Docker Compose para PostgreSQL.
- Perfiles de ejecución y configuración inicial de Flyway.
- Provincias: entidad y repositorio (base geográfica).
- Manejo de errores: excepciones custom + ControllerAdvice / handler global.
- Flyway: scripts y opciones orientadas a desarrollo.
- Artistas: creación de artistas y búsqueda/autocompletado para eventos.
- Eventos: búsqueda pública inicial.

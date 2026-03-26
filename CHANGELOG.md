# Changelog

Este documento resume los cambios relevantes por versión Rockalendar.
El versionado de releases se marca mediante tags (p. ej. `v0.1-backend`).

## [v0.5.0]
### Added
- [BACK] Cartel de evento: subida de imagen con almacenamiento en S3.
- [BACK] Rate limiting en endpoints de autenticación y honeypot en formulario de registro.
- [BACK] Renovación silenciosa de JWT con TTL de 48h.
- [BACK] Recuperación de contraseña vía email.
- [BACK] Formulario de contacto: endpoint `POST /api/contact` y rate limiting.
- [FRONT] Cartel en formulario de propuesta/edición, detalle de evento y miniatura en listado público.
- [FRONT] Idioma preferido del usuario en registro y ajustes de cuenta.
- [FRONT] Aceptación obligatoria de política de privacidad en el registro.
- [FRONT] Cambio de contraseña en ajustes de cuenta.
- [FRONT] Solicitud de eliminación de cuenta con período de gracia de 7 días.
- [FRONT] Página de detalle de artista con sus próximos eventos.
- [FRONT] Gestión de artistas en moderación: autocomplete, eliminación y lista de huérfanos.
- [FRONT] Formulario de creación de artista para moderadores.
- [FRONT] Bypass de restricciones de negocio para rol ADMIN en formularios de evento.
- [FRONT] Páginas `/about`, `/privacy` y `/contact`, y CTAs en footer.
- [FRONT] Sección de colaboración y código abierto.
- [FRONT] Renombrado de artista desde su detalle en moderación.
- [FRONT] Pegado masivo de artistas en formulario de propuesta/edición.
- [FRONT] Formulario de contacto conectado al backend; navega a él desde el detalle de evento.
- [FRONT] CSP dinámica: `http:` permitido en imágenes solo en desarrollo (MinIO local); `blob:` permitido siempre para previsualizaciones de cartel.

### Fixed
- [FRONT] Eventos pasados filtrados de la agenda personal.
- [FRONT] Mismatch de hidratación SSR en el bottom nav (dependía de `isAuthenticated`, solo disponible en cliente).
- [FRONT] Fuentes de Google Fonts (`fonts.gstatic.com`) permitidas en la CSP.

### Changed
- [FRONT] ESLint configurado con `@nuxt/eslint` y `eslint-plugin-sonarjs`; todos los issues corregidos.
- [BACK+FRONT] Refactorizaciones de calidad: centralización de constantes, limpieza de issues de SonarQube.

## [v0.4.0]
### Added
- [BACK] Trust score de usuario y endpoint `GET /api/me` con datos del usuario autenticado.
- [BACK] Ascenso a moderador: endpoint `POST /api/me/promote` con validación de trust score mínimo.
- [BACK] Agenda personal: `UserEvent` con estados `INTERESTED` / `GOING`, endpoints `POST/DELETE /api/me/agenda/{id}` y `GET /api/me/agenda`.
- [BACK] Moderación automática: blacklist de palabras, detección de spam (mismo usuario, mismo día) y auto-rechazo por tercera solicitud de cambios.
- [BACK] Detección de eventos duplicados al proponer: similitud por título + artistas + recinto (pg_trgm > 0.3) en el mismo día; campo `possible_duplicate_of` en la entidad.
- [FRONT] Agenda personal completa en `/me/agenda` con estados, confirmación de borrado y paginación.
- [FRONT] Botón condicional de solicitud de ascenso a moderador en `/me`.
- [FRONT] Aviso de posible duplicado al proponer un evento (con enlace si el original está aprobado).
- [FRONT] Badge de posible duplicado en el listado de moderación.
- [FRONT] Drawer de consejos de moderación en el detalle de evento con criterios de rechazo y de solicitud de cambios.

### Changed
- [FRONT] El email del usuario en el panel de cuenta muestra solo el nombre de usuario (sin dominio).
- [FRONT] `isModerator` incluye también el rol `ROLE_ADMIN`.
- [FRONT] Fechas pasadas deshabilitadas en los datepickers de propuesta y edición de eventos.
- [FRONT] Eliminado `useApiFetch`; errores 4xx en el detalle de evento público se normalizan a 404.

### Fixed
- [BACK] Refactorización de mensajes de error y limpieza de códigos i18n en respuestas.

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

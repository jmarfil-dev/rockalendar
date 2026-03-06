# Changelog

Este documento resume los cambios relevantes por versión Rockalendar.
El versionado de releases se marca mediante tags (p. ej. `v0.1-backend`).

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

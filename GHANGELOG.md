# Changelog

Este documento resume los cambios relevantes por versión Rockalendar.
El versionado de releases se marca mediante tags (p. ej. `v0.1-backend`).

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

## [Unreleased]
### Added
- Eventos: endpoint home.
- Eventos: proponer evento.
- Eventos: consultar mis eventos.

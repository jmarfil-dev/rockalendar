# Backlog no comprometido

Este documento recoge ideas, mejoras y posibles evoluciones surgidas durante el desarrollo de *Rockalendar*.

**No constituye un compromiso de implementación ni un orden de prioridad.**

Son ideas sujetas a validación, disponibilidad de tiempo y coherencia con la filosofía del proyecto. Su función es preservar contexto y decisiones futuras, no definir el rumbo del proyecto.

## V2

### Arquitectura

- Wrapper `CurrentUser` para centralizar autenticación y acceso al usuario actual.
- Base común para búsquedas, filtros y mapas.
- Reemplazar `Page<T>` por un DTO propio de paginación: `PageResponse`:
  - Control total del contrato JSON
  - Desacoplar API pública de Spring Data
  - Mantener `@EnableSpringDataWebSupport(VIA_DTO)` solo como solución temporal
  - Estructura estable: `content`, `page`, `size`, `totalElements`, `totalPages`
- Devolver `ResponseEntity` en los controladores y en `GlobalExceptionHandler` (`@ControllerAdvice`).
- Constraint `UNIQUE` y evitar duplicados en eventos (por provincia, ciudad, recinto y fecha). Agregar también el error 409 cuando se detecte un duplicado a `MeEventApi.propose()`.
- Anotaciones de validación compuestas si hay repetición real; Validators propios solo si hay reglas reutilizables.
- Cron job cada 24 horas para cerrar eventos pasados. Agregar estado REALIZADO o PASADO o parecido.
- Historial de estados para auditoría.

### Funciones de eventos

- Detalle de artista.
- Imágenes (cartel del evento).
- Reachazo automático de eventos por blacklist o similar:
  - La Blacklist podría incluir palabras clave para el título y combinación de fecha + ciudad (ejemplo: primero de abril + Villarobledo = Viñarock)
  - Funciones para agregar a la blacklist los administradores

### Funciones sociales y notificaciones

- Preferencias de notificaciones por usuario.
- Seguir artistas.
- Notificación cuando se publica un nuevo evento de un artista seguido.
- Canales: email, notificaciones in-app.
- Crews:
  - Creación de grupos privados (crew)
  - Acceso solo mediante invitación
  - Notificaciones internas cuando: un integrante publica un evento, un integrante marca que asistirá a un evento, un integrante marca un evento como "opción interesante"
  - Preferencias de notificaciones por usuario dentro de cada crew: elegir qué acciones generan notificación, ejemplo: recibir solo notificaciones de asistencia, pero no de creación de eventos
  - Base pensada para futura apertura a crews públicas

## V3

- Recordatorios cuando se acerca la fecha del evento.
- Notificaciones push.
- Espacios para publicidad.
- Autocompletado de direcciones y lugares.

### Integración con plataformas externas

- Precargar ciudades desde un dataset (INE / GeoNames / otro) en tabla cities.
- Integración con Places / Geocoding (Google / Mapbox / OpenStreetMap).
- Spotify: enlazar artista.
- Compartir evento / artista por WhatsApp, X, Instagram, Facebook.
- Entradas: enlaces a plataformas de venta (Ticketmaster, Entradium, etc.).

## V4 y posteriores

- App móvil nativa.

### Funciones sociales

- Recomendaciones personalizadas.
- Comentarios y valoraciones.

### Integración con plataformas externas

- Spotify: mostrar canciones populares, verificación de artista.
- Mapas de eventos (por ciudad / provincia).
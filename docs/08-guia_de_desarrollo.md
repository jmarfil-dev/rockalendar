# Guía de desarrollo

## 1. Propósito

Este documento establece convenciones prácticas para desarrollar *Rockalendar* de forma consistente. No pretende imponer un “estilo universal”, sino capturar acuerdos que:
- Reducen fricción.
- Evitan debates repetidos.
- Mantienen el proyecto simple y mantenible.

Cuando este documento entre en conflicto con un ADR, **manda el ADR**.

## 2. Estructura del backend (paquetes/módulos)

La estructura del backend se organiza por **slices** (cortes verticales) alineados con el dominio y los casos de uso.

Paquetes principales:
- `auth`: registro/login, JWT, seguridad de acceso.
- `events`: consulta, búsqueda, propuesta y ciclo de vida del evento.
- `artists`: entidades de artista y consistencia.
- `geo`: provincias y geografía usada en eventos.
- `moderation`: moderación, acciones e historial.
- `users`: perfil, área personal y relaciones usuario-evento.

Paquetes transversales:
- `common`: utilidades compartidas, tipos base, excepciones comunes.
- `config`: configuración técnica (security, CORS, i18n, serialización, etc.).

Regla práctica:
- Si una clase pertenece claramente a un slice, debe vivir dentro de ese slice.
- Evitar “cajones desastre”: `common` es para reutilización real, no para cosas sin sitio.

## 3. Convención de endpoints y APIs (interfaces)

### 3.1 Namespaces de la API

La API se organiza por namespaces (ADR-008):
- `/api/...` → público
- `/api/me/...` → área personal autenticada
- `/api/admin/...` → administración

### 3.2 Contrato en interfaces (OpenAPI code-first)

Convención principal:
- Cada recurso expuesto tiene una **interfaz API** (`*Api`) que define:
  - Rutas/mappings
  - Contrato (OpenAPI)
  - Firma de métodos

- El controller concreto implementa esa interfaz.

Ejemplos:
- `EventApi` → `EventController`
- `ArtistApi` → `ArtistController`
- `ArtistAdminApi` → `ArtistAdminController`
- `MeEventApi` → `MeEventController`

Reglas de anotación:
- Controllers: anotaciones mínimas (p. ej. `@RestController`, `@PreAuthorize`).
- Interfaces: mappings y anotaciones OpenAPI.

OpenAPI (convención):
- `@Operation`: siempre.
- `@ApiResponse`: siempre (mínimo).
- `@Parameter`: solo cuando aporta valor.
- `@Content`: solo casos especiales.
- Seguridad: global y aplicada por interfaz.

## 4. Convención de nombres (backend)

### 4.1 Clases

- Controllers: `*Controller`
- Interfaces de contrato HTTP: `*Api`
- Servicios:
  - Separados en CommandService y QueryService.
  - `*CommandService`: operaciones que **modifican estado** (crear, actualizar, moderar, etc.).
  - `*QueryService`: operaciones de **lectura** (búsquedas, consultas públicas, agenda).
  - Evitar mezclar lectura y escritura en el mismo servicio.
- Repositorios: `*Repository`

### 4.2 DTOs

- Requests: `*Request`
- Responses: `*Response`
- DTOs internos (si se usan): `*Dto`

DTOs solo para documentación:
- Permitidos únicamente para:
  - paginación (`*PageDoc`)
  - (opcional) errores tipados (`*ErrorDoc`)
- Ej.: `EventPublicPageDoc`

Regla base:
- Los DTOs de documentación no deben “infectar” el dominio.

### 4.3 Persistencia

- Entidades JPA: nombres del dominio (`Event`, `Artist`, `User`, …).
- Tablas: snake_case (`events`, `artists`, `event_artists`, …).
- Columnas: snake_case (`start_date_time`, `city_slug`, …).

## 5. Errores y excepciones

- Se usan excepciones custom para representar errores de dominio o aplicación.
- El mapeo a HTTP se centraliza en un `GlobalExceptionHandler`.

Reglas:
- No propagar excepciones técnicas al cliente.
- Mantener respuestas de error consistentes.
- Evitar mensajes con datos sensibles.

## 6. Búsqueda

- La búsqueda se implementa con PostgreSQL **FTS + pg_trgm**.
- Las consultas deben ser:
  - Tolerantes a errores (móvil)
  - Rápidas
  - Predecibles

Regla práctica:
- Priorizar claridad de consulta e índices antes de soluciones externas.

## 7. Estrategia de tests

Se prioriza integración realista (ADR-002).

- `@SpringBootTest`:
  - tests de contrato y flujos end-to-end (HTTP → DB).
- `@DataJpaTest`:
  - repositorios, queries, constraints, búsquedas.

Infra:
- DB en contenedor + migraciones.
- Contenedor singleton compartido para estabilidad.

## 8. Git: forma de trabajo

### 8.1 Ramas

- `main`: estable.
- `dev`: **no se usa por ahora** (se reserva para posible integración futura).
- `feature/<tema>`: cambios funcionales.
- `fix/<tema>`: correcciones.
- `docs/<tema>`: cambios exclusivamente de documentación.

### 8.2 Pull requests

- Todo merge a `main` entra por PR.
- PRs pequeñas y revisables.
- Descripción clara: qué cambia y por qué.

### 8.3 Commits

Convención recomendada: Conventional Commits (adaptado).

**Formato:**
```
tipo (scope opcional): descripción en presente
```

- El **scope es opcional**, pero recomendable cuando aporta contexto.
- Los commits se escriben **en español**.
- La descripción debe ser clara y en **presente**.

**Tipos permitidos:**
- `feat`: funcionalidad
- `fix`: bug
- `chore`: mantenimiento / tooling / dependencias
- `refactor`: cambio interno sin cambio funcional
- `docs`: documentación
- `test`: tests
- `ci`: pipelines / automatización

**Ejemplos:**
- `feat (events): crear endpoint de eventos`
- `fix (security): permitir GET /api/events en desarrollo`
- `chore (postman): agregar environment rockalendar-local`
- `refactor (api): renombrar EventController a MeEventController`
- `docs (readme): agregar instrucciones de arranque`

Reglas prácticas:
- Un commit debe compilar/pasar tests.
- Preferir commits pequeños con intención clara.

### 8.4 Tags, releases y changelog

La estrategia de releases sigue los hitos del roadmap.
- Cada hito relevante (p. ej. v0.1, v0.2, …, v1.0) se marca con un tag en main.
- El tag se crea solo cuando el hito está completo según su definición (no por “sensación”).
- Formato de tags:
  - v0.1.0, v0.2.0, …
  - v1.0.0 para el MVP.

**Release notes / changelog**
- Se mantiene un CHANGELOG.md en el repositorio.
- Cada release añade una entrada con:
  - Agregado (funcionalidades)
  - Modificado (cambios relevantes)
  - Corregido (correcciones)
  - Breaking changes (si aplica)

Regla práctica: si un cambio “importa a un usuario” o afecta al contrato API, debe aparecer en el changelog.

## 9. Qué hacer cuando hay dudas

Orden de referencia recomendado:
1. Visión y alcance
2. Filosofía del proyecto
3. Usuarios y casos de uso
4. Roadmap y MVP
5. ADRs
6. Arquitectura
7. Modelo de dominio y datos
8. Esta guía

Si una propuesta no encaja en los documentos de arriba, probablemente no encaja en el proyecto.

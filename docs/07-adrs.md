# ADRs

> **Estado:** Aceptados (salvo que se indique lo contrario).  
> **Propósito:** registrar decisiones técnicas relevantes y su racional, para mantener coherencia y facilitar cambios conscientes.

## ADR-001 - Contrato OpenAPI (code-first)

**Decisión**

- *Rockalendar* adopta un enfoque code-first para OpenAPI.
- La especificación OpenAPI se genera a partir del código, no se mantiene como fichero manual versionado.
- El contrato HTTP se versiona únicamente por versión mayor en la URL (`/api/v1/...`).

**Contexto**

El proyecto busca:
- Evitar duplicación entre código y documentación.
- Mantener el contrato siempre alineado con la implementación real.
- Reducir fricción en un proyecto de tamaño contenido.

**Alcance y convenciones**

- Se definen interfaces dedicadas al contrato OpenAPI (p. ej. `EventApi`).
- Los controllers concretos (p. ej. `EventController`) implementan dichas interfaces.
- Las interfaces contienen:
    - Mappings HTTP
    - Anotaciones OpenAPI
    - Definición explícita del contrato

**Reglas de anotación OpenAPI**

- `@Operation`: siempre presente.
- `@ApiResponse`: siempre presente (mínimo una respuesta).
- `@Parameter`: solo cuando aporta valor semántico.
- `@Content`: solo en casos especiales (errores tipados, schemas no deducibles).
- Seguridad:
    - Declarada de forma global
    - Aplicada por interfaz, no por implementación

**DTOs y documentación**

- Se permiten DTOs dedicados solo a documentación (`*Doc`, `*PageDoc`) exclusivamente para:
    - Paginación
    - (Opcionalmente) errores tipados.
- Ejemplo: `EventPublicPageDoc`.
- El objetivo es mantener un contrato estable y legible, sin contaminar el dominio.

**Alternativas consideradas**

- OpenAPI spec manual versionada (`openapi/v1.0.0.yaml`).
- Diseño contract-first con generación de código.

**Por qué se descartan**

- Specs manuales tienden a desincronizarse.
- Contract-first añade fricción innecesaria en este contexto.

**Consecuencias**

- El código es la fuente de verdad del contrato.
- La documentación OpenAPI refleja siempre el estado real de la API.
- Cambios incompatibles requieren nueva versión mayor (`/api/v2`).

## ADR-002 - Estrategia de tests (SpringBootTest vs DataJpaTest)

**Decisión**

- Se priorizan tests de integración por encima de unit tests aislados.
- La base de datos de tests se ejecuta en contenedor y el esquema se gestiona con migraciones.
- Se utilizan dos familias principales, ambas extienden la clase `AbstractPostgresTest`:
  1. **Tests de contrato de API** con `@SpringBootTest`: cubren casos de uso
  2. **Tests de persistencia/servicios** con `@DataJpaTest` (siempre que se pueda, si no usar`@SpringBootTest`): mínimos, para cubrir lo que no se pueda con los tests de contrato (validaciones, concurrencia, etc)

> Nota: los servicios que no tienen lógica de negocio o de dominio propia se pueden saltar esta norma **como excepción**.\
> Si no valida datos reales, el test se pueden mockear.\
> Ejemplos: `AuthService`, `JwtTokenService`.

**Infra de tests (DB + migraciones)**

- PostgreSQL en contenedor y esquema aplicado mediante migraciones.
- Uso de container singleton compartido para evitar inestabilidades con el pool de conexiones.

**Alternativas consideradas**

- Uso extensivo de unit tests con mocks.
- Uso exclusivo de `@SpringBootTest` para todo.
- Bases de datos en memoria (H2).

**Por qué se descartan**

- Mocks excesivos reducen confianza en consultas reales.
- `@SpringBootTest` para todo penaliza tiempos y feedback.
- H2 no representa correctamente comportamiento de PostgreSQL (FTS, constraints).

**Consecuencias**

- Menos mocks, más pruebas realistas.
- Mayor confianza al refactorizar queries y reglas.

## ADR-003 - UUID deterministas con namespace (UUIDv5)

**Decisión**

- Uso de UUID deterministas (UUIDv5) para catálogos canónicos.

**Namespace del proyecto**

- `4b1c3d3a-9f7b-4f7a-8c2e-0d3d9e6f2a11`

**Uso** Ejemplo:

- Los IDs de provincias se generan como:
    - uuid_generate_v5(<namespace>, 'rockalendar:province:<INE_CODE>')
~~~ 
uuid_generate_v5('4b1c3d3a-9f7b-4f7a-8c2e-0d3d9e6f2a11'::uuid, 'rockalendar:province:' || ine_code::TEXT)
~~~

**Alternativas consideradas**

- IDs autoincrementales.
- UUIDv4 aleatorios.

**Por qué se descartan**

- IDs secuenciales acoplan datos al entorno.
- UUIDv4 no permite reproducibilidad.

**Consecuencias**

- Identificadores estables entre entornos.
- UUIDs predecibles (uso limitado a catálogos no sensibles).

> Los UUIDv5 son predecibles a partir de la entrada (no aptos para secretos).
> Se limita su uso a catálogos y entidades no sensibles.

## ADR-004 - Búsqueda: PostgreSQL FTS + pg_trgm

**Decisión**

- Uso combinado de FTS y pg_trgm en PostgreSQL.

**Alternativas consideradas**

- Motores externos (Elastic/OpenSearch).
- Búsqueda simple con `ILIKE`.

**Por qué se descartan**

- Motores externos añaden coste y complejidad temprana.
- `ILIKE` no escala ni tolera errores.

**Consecuencias**

- Buen equilibrio entre calidad de búsqueda y simplicidad operativa.
- Necesidad de tuning de índices.

## ADR-005 - i18n en backend (idioma efectivo)

**Decisión**

- Idioma determinado por `Accept-Language` con fallback a inglés.

**Alternativas consideradas**

- Selector manual de idioma.
- Idioma persistido y forzado por usuario.

**Por qué se descartan**

- Añaden fricción y complejidad temprana.

**Consecuencias**

- UX simple y sin configuración.

## ADR-006 - Ciudad embebida en events

**Decisión**

- Ciudad embebida en evento; provincia normalizada.

**Alternativas consideradas**

- Tabla `cities` normalizada desde el inicio.
- Integración temprana con servicios de geocoding.

**Por qué se descartan**

- Mayor coste inicial sin valor inmediato.

**Consecuencias**

- Modelo simple y evolutivo.

## ADR-007 - Anotaciones mínimas en controllers y contrato en interfaces

**Decisión**

- Controllers con anotaciones mínimas.
- Contrato HTTP definido en interfaces.

**Alternativas consideradas**

- Controllers con todas las anotaciones.
- Generación automática de controllers desde OpenAPI.

**Por qué se descartan**

- Controllers muy cargados y difícil mantenimiento.
- Generación automática reduce flexibilidad.

**Consecuencias**

- Código más limpio y contrato centralizado.

## ADR-008 - Convención de rutas y namespaces de la API

**Decisión**

La API de *Rockalendar* se organiza en namespaces claros y semánticos, en función del tipo de recurso y del contexto de uso:

- `/api/...` → recursos públicos
  - Ej.: `/api/events`, `/api/artists`
  - Interfaces: `EventApi`, `ArtistApi`, `AuthApi`

- `/api/me/...` → recursos ligados al usuario autenticado (área personal)
  - Ej.: `/api/me/events`, `/api/me/profile`
  - Interfaces: `MeEventApi`, `MeProfileApi`

- `/api/admin/...` → recursos de moderación y administración del sistema
  - Ej.: `/api/admin/artists`
  - Interfaces: `ArtistAdminApi`

**Contexto**

El proyecto necesita una estructura de rutas que:
- Sea fácil de entender para frontend y consumidores de la API.
- Refleje claramente el contexto de seguridad y autorización.
- Evite endpoints ambiguos o con múltiples responsabilidades.

**Reglas asociadas**

- El namespace `me` siempre implica autenticación (JWT obligatorio).
- El namespace `admin` siempre implica autorización elevada (rol administrador o moderador, según el recurso).
- El namespace público (`/api/...`) no depende de identidad, aunque puede aceptar JWT de forma opcional.
- Un endpoint no debe mezclar responsabilidades de distintos namespaces.

**Motivación**

- Separar claramente lectura pública, área personal y administración.
- Facilitar el razonamiento sobre permisos sin inspeccionar implementación.
- Mantener coherencia entre rutas, interfaces y documentación OpenAPI.

**Alternativas consideradas**

- Un único namespace (`/api/...`) con control de permisos interno.
- Uso de parámetros o flags para diferenciar contexto (p. ej. `?mine=true`).

**Por qué se descartan**

- Mezclar contextos en un mismo namespace reduce claridad y aumenta errores de autorización.
- Parámetros de contexto generan contratos ambiguos y difíciles de documentar.

**Consecuencias**

- Las rutas comunican intención y nivel de acceso de forma explícita.
- El frontend puede razonar fácilmente qué endpoints usar en cada vista.
- Se reduce el riesgo de exponer operaciones sensibles por error.

## ADR-009 - Estrategia de consumo de API en frontend (Nuxt 4)

**Contexto**

Rockalendar utiliza páginas públicas donde el SEO es relevante, el SSR aporta valor y se requiere manejo de estados de carga y error.\
También existen acciones autenticadas (crear, editar, eliminar eventos) y páginas privadas.

**Decisión**

Se adopta la siguiente estrategia:
- `useApiFetch` (wrapper de useFetch + manejo de errores) es el mecanismo estándar para lectura de datos en páginas.
  - Página que carga datos para renderizar.
  - `useFetch` se usará cuando un error no deba lanzar página de errores (por ejemplo, al cargar un dropdown de provincias).
- `$fetch` se utilizará exclusivamente para acciones imperativas o mutaciones.
  - Acción iniciada por usuario (submit, delete, etc.) (POST, PUT, DELETE).
  - Lógica en stores o composables sin SSR.

**Alternativas consideradas**

- Usar solo $fetch para todo.
- Usar solo useFetch para todo.

**Por qué se descartan**

- Usar solo $fetch degrada la integración con SSR y SEO, que son relevantes en páginas públicas.
- Usar solo useFetch introduce complejidad innecesaria en mutaciones.

**Consecuencias**

- Consistencia clara en el código.

- SSR y SEO correcto en páginas públicas.
- Separación clara entre lectura de datos (reactiva, declarativa) y mutaciones (imperativas).
- Escalabilidad limpia a medida que crezca el proyecto.
- Existen dos mecanismos en el proyecto pero es un coste asumible.

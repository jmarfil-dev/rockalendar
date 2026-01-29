# Arquitectura

## 1\. Propósito del documento

Este documento describe la arquitectura de *Rockalendar* a un nivel suficientemente alto para orientar el desarrollo, pero lo bastante concreto como para justificar decisiones técnicas y evitar deriva.

Se organiza en torno a:

- Diagramas tipo C4 (Contexto, Contenedores y, cuando aplica, Componentes).
- Explicación por slices (cortes verticales) que conectan producto → API → dominio → persistencia.
- Decisiones tecnológicas y operativas (seguridad, despliegue, observabilidad).

## 2\. Visión C4

### 2.1 C4 - Contexto

*Rockalendar* es una plataforma web para consultar y publicar conciertos/festivales, con moderación previa. El sistema se integra con servicios externos solo cuando aportan valor claro (p. ej. geocoding), y evita dependencias innecesarias.

**Actores principales:**

- **Usuario anónimo**: consulta y busca eventos.
- **Usuario registrado**: propone eventos y gestiona agenda.
- **Moderador/Administrado**r: revisa eventos pendientes y gestiona el sistema.

**Diagrama (contexto):**

~~~
[Usuario] ──(HTTPS)──> [Rockalendar]
   │                     │
   │                     ├──> [Base de datos PostgreSQL]
   │                     └──> (futuro) [Geocoding/Places] / [Email]
   │
[Moderador/Admin] ─(HTTPS)──> [Rockalendar]
~~~

### 2.2 C4 - Contenedores

*Rockalendar* se compone de dos contenedores principales:

- **Frontend Web**: aplicación web (SPA) responsable de la experiencia de usuario.
- **Backend API**: API REST responsable del dominio, seguridad, moderación, búsquedas y persistencia.

**Diagrama (contenedores):**

~~~
[Browser]
   │
   └──(HTTPS)──> [Frontend Web (SPA)]
                      │
                      └──(HTTPS/JSON)──> [Backend API (Spring Boot)] ───> [PostgreSQL]
~~~

**Decisiones de diseño:**

- La API es el punto único de verdad (single source of truth).
- El frontend no contiene reglas de negocio críticas, solo validaciones "de comodidad".
- La persistencia se gestiona desde el backend de forma transaccional.

### 2.3 C4 - Componentes (backend)

En el backend, los componentes se agrupan por responsabilidad, manteniendo las dependencias en una dirección clara.

**Capas/componentes:**

- **API (Controllers + DTOs)**: contrato HTTP y validación de entrada.
- **Application (Use Cases)**: orquestación de flujos (proponer evento, moderar, marcar asistencia…).
- **Domain (Entidades + Reglas)**: invariantes del negocio.
- **Infrastructure (Repos, DB, integraciones)**: persistencia e IO.

**Diagrama (componentes backend):**

~~~
[Controllers] -> [Use Cases] -> [Domain]
      │              │            │
      └────DTOs──────┘            └──> [Policies / Rules]
                     │
                     └──> [Repositories / DB] -> [PostgreSQL]
                     └──> (futuro) [External Services]
~~~

**Regla base:** el dominio no depende de infraestructura.

## 3. Arquitectura por slices (cortes verticales)

Los slices permiten razonar sobre el sistema por funcionalidades completas, alineadas con la forma en la que el código está organizado. En *Rockalendar*, la separación principal se refleja en paquetes/módulos como: auth, artists, events, geo, moderation, users, además de capas transversales como common y config.

A continuación se describen los slices principales.

### Slice Auth – Identidad y autorización

**Responsabilidad:** autenticación, emisión/validación de JWT, y control de acceso por roles.

**Flujo típico:**

1. El usuario se registra o hace login.
2. El backend valida credenciales y emite un JWT.
3. El frontend usa el JWT en peticiones autenticadas.

**Puntos clave:**

- Acceso anónimo permitido para lectura/búsqueda.
- Acceso autenticado obligatorio para escritura.
- Autorización por rol para moderación.

### Slice Events – Descubrir, buscar y consultar eventos (consulta pública)

**Flujo:**

1. El usuario busca por fecha/ciudad/artista/texto.
2. El frontend solicita resultados al backend.
3. El backend aplica filtros, relevancia y tolerancia a errores.
4. Se devuelven eventos aprobados.

**Puntos clave:**

- Solo eventos aprobados son visibles públicamente.
- La relevancia no depende de pagos.
- Búsqueda tolerante pensada para escritura rápida en móvil.

### Slice Artists – Gestión mínima del catálogo de artistas

**Responsabilidad:** representar artistas/grupos como entidad de dominio para búsquedas y consistencia.

**Flujos típicos:**

- Creación al vuelo al proponer un evento (si el artista no existe).
- Administración por moderadores (corregir/normalizar datos básicos cuando sea necesario).

**Puntos clave:**

- En el MVP, la creación al vuelo es un mecanismo funcional, no una gestión completa del catálogo.
- La existencia de artistas mejora búsquedas y reduce duplicidades por escritura.

### Slice Geo – Ciudades, provincias y consistencia geográfica

**Responsabilidad:** modelo geográfico interno (ciudad/provincia) y soporte a búsquedas/filtros.

**Flujo típico:**

1. El usuario filtra por ciudad/provincia.
2. El backend resuelve la búsqueda usando el modelo geográfico.

**Puntos clave:**

- En MVP puede ser un modelo simple (sin depender de integraciones externas).
- Futuras integraciones (geocoding/places) se evaluarán fuera del MVP.

### Slice Moderation – Moderación y gobierno de contenido

**Flujo:**

1. Moderador revisa eventos pendientes.
2. Aprueba o rechaza con motivo.
3. Se actualiza el estado del evento.
4. El sistema ajusta el score del usuario creador.

**Puntos clave:**

- Nadie puede moderar su propio evento.
- Moderación como mecanismo de calidad y coherencia ética.
- Trazabilidad básica frente a complejidad.

### Slice Users – Perfil, agenda e interacciones

**Flujo:**

1. Usuario marca “me interesa” / “asistiré”.
2. El backend registra la relación usuario-evento.
3. El usuario consulta su agenda.

**Puntos clave:**

- No hay gamificación pública.
- La agenda es herramienta de planificación (no red social).

### Transversales – common / config

- **common**: utilidades, tipos compartidos, errores comunes, validaciones reutilizables, y componentes base.
- **config**: configuración técnica (seguridad, CORS, serialización, i18n, etc.).
- **Manejo de errores (contrato API)**: excepciones custom (dominio/aplicación) y un **GlobalExceptionHandler** como punto único para traducir errores a respuestas HTTP consistentes (códigos, payload y mensajes).

Regla de oro: lo transversal existe para reducir duplicación, no para crear un “cajón desastre”.

## 4. Tecnologías y por qué

### Frontend

- **Por decidir**. Objetivo: el stack más sencillo posible de mantener, priorizando productividad y claridad.
- Candidatos (mencionados): **Node + Next.js**, y queda por decidir si se usa React u otra opción compatible.

**Por qué:**

- Minimizar barreras (no se asume experiencia profunda en frontend).
- Reutilizar convenciones y tooling ampliamente adoptados.
- Mantener una UI simple, enfocada a búsqueda y lectura.

### Backend

- **Java 21 + Spring Boot**.

**Por qué:**

- Ecosistema robusto para APIs REST.
- Integración natural con seguridad (Spring Security) y persistencia.
- Buen encaje con arquitectura por capas y separación de responsabilidades.
- Se alinea con el JDK instalado y evita fricción operativa.

### Persistencia

- **PostgreSQL**.

**Por qué:**

- Modelo relacional claro para eventos, usuarios, artistas y geografía.
- Buen soporte para índices y búsquedas avanzadas.
- Escala razonablemente sin complicar infraestructura.

### Búsqueda

- **PostgreSQL Full-Text Search (FTS) + ****pg\_trgm**.

**Por qué:**

- Permite búsquedas tolerantes a errores tipográficos.
- Evita dependencias externas (Elastic/OpenSearch) en el MVP.
- Encaja con volúmenes previstos y reduce complejidad operativa.

### Testing

- **Tests unitarios** para reglas de dominio y validaciones.
- **Tests de integración** para repositorios, consultas y flujos API críticos.

**Principio:** priorizar pruebas donde hay reglas y consultas complejas (búsqueda, moderación), evitando sobrecoste innecesario.

## 5\. Seguridad y acceso

*Rockalendar* combina acceso **anónimo** para consulta con **JWT** para operaciones autenticadas.

- Lectura/búsqueda: accesible sin login.
- Escritura (proponer evento, marcar asistencia/interés, agenda): requiere autenticación.
- Moderación: requiere rol moderador/administrador.

**JWT:**

- Se emite en login.
- Se envía en cabecera Authorization.
- El backend valida token y roles para autorizar acciones.

Principios:

- El frontend no decide permisos: solo refleja lo que la API permite.
- Reglas críticas de autorización siempre en backend.

## 6\. Internacionalización

El sistema ofrece soporte multilenguaje en el MVP con estas reglas:

- El idioma efectivo se determina por la cabecera HTTP del navegador (p. ej. `Accept-Language`).
- Si el idioma no está soportado, el sistema hace fallback a **inglés**.

**Nota importante:** el campo `preferred_language` existe por anticipación en el modelo de usuarios, pero no condiciona el comportamiento del MVP. Su presencia permite evolución futura sin reestructurar el dominio.

## 7\. Integraciones y dependencias externas

En el MVP, *Rockalendar* minimiza dependencias externas.

**Integraciones previstas (post-MVP o condicionadas):**

- Geocoding / Places (Google, Mapbox u OpenStreetMap) para autocompletado y consistencia.
- Email (notificaciones básicas) si el producto lo valida.
- Spotify u otras plataformas para enriquecer artistas (no crítico).

Criterio de adopción:

- Cada integración debe aportar valor real y no comprometer la filosofía del proyecto.

## 8. Testing

La estrategia de tests busca un equilibrio: suficiente cobertura para refactorizar con confianza, sin convertir el proyecto en un “framework de testing”. Se priorizan tests realistas frente a mocks excesivos.

Tipos de tests utilizados:

- **Tests de contrato de la API**:

  - Ejecutados con `@SpringBootTest`.
  - Verifican el comportamiento observable de la API (endpoints, códigos HTTP, payloads, errores).
  - Cubren flujos completos de lectura, escritura y moderación.

- **Tests de servicios y persistencia**:

  - Ejecutados con `@DataJpaTest`.
  - Validan reglas de negocio a nivel de servicio y repositorio.
  - Cubren consultas complejas (búsquedas, filtros, tolerancia a errores) y constraints.

Principios:

- Priorizar tests de integración frente a unit tests aislados.
- Evitar mocks cuando se pueda probar comportamiento real.
- Proteger especialmente las zonas de riesgo: búsqueda, moderación y autorización.

## 9. Datos y búsqueda

La experiencia de búsqueda es un pilar del producto (tolerante a errores y rápida). La implementación se apoya en PostgreSQL:

- **Full-Text Search (FTS)** para consulta por texto.
- **pg\_trgm** para tolerancia a errores, similitud y búsquedas “tipo Google”.

Se espera usar índices adecuados para sostener rendimiento conforme crezca el volumen.

**Modelo E/R:** el detalle del modelo entidad-relación y constraints (incluyendo índices/únicos) se documenta en un documento específico de *Modelo de datos*, manteniendo este documento de arquitectura centrado en estructura y decisiones.

## 10. Modelo de despliegue (dev/prod) (dev/prod)

### Entorno de desarrollo

- Ejecución local del frontend.
- Backend local.
- Base de datos en contenedor (Docker), con migraciones automáticas.

Objetivo: reproducibilidad y facilidad para iterar.

### Entorno de producción

- Despliegue simple y de bajo coste.
- Backend desplegado como servicio (contenedor o proceso) con configuración por variables de entorno.
- Base de datos PostgreSQL persistente.

Principios:

- Configuración separada del código.
- Migraciones controladas.
- Logs accesibles y retenidos.

## 11. Observabilidad (logs)

La observabilidad inicial se basa en logs estructurados y consistentes.

**Herramientas:**

- SLF4J como API de logging (compatible con Lombok).
- Implementación concreta según configuración del proyecto (p. ej. Logback por defecto en Spring Boot), manteniendo el código desacoplado mediante SLF4J.

**Qué se registra:**

- Inicio y fin de peticiones relevantes (especialmente escritura y moderación).
- Errores con contexto suficiente (sin exponer datos sensibles).
- Cambios de estado (moderación) con identificadores.

**Qué se evita:**

- Logs con tokens JWT.
- Datos personales innecesarios.

Objetivo: poder depurar, auditar moderación básica y detectar fallos sin infraestructura compleja.
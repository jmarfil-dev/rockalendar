# Rockalendar

Agenda de conciertos y festivales

**Rockalendar** es una aplicación web para **descubrir conciertos y festivales de forma clara, colaborativa y sin ruido**.
Su foco está en el descubrimiento y la planificación, evitando anuncios, rankings artificiales o dinámicas sociales innecesarias.

Este repositorio es un **monorepo** que contiene:

- backend (Spring Boot / Java)
- frontend (framework por decidir)
- tooling local (Docker, Postman)
- documentación del proyecto

## Índice

- [Estado del proyecto](#estado-del-proyecto)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Documentación](#documentacion)
- [Tecnologías utilizadas](#tecnologias-utilizadas)
- [Arranque rápido (entorno local)](#arranque-rapido-\(entorno-local\))
- [Seguridad (JWT)](#seguridad-\(jwt\))
- [Flujo de trabajo (Git)](#flujo-de-trabajo-\(git\))
- [Desarrolladores](#desarrolladores)
- [Licencia](#licencia)

## Estado del proyecto

🛠️ En desarrollo activo\
🎯 Objetivo: **MVP 1.0**

El alcance y la hoja de ruta están definidos en la documentación.

## Estructura del repositorio

- `backend/` — API Spring Boot (Java, Maven)
- `frontend/` — Aplicación web (tecnología por decidir)
- `docker/` — Infraestructura local
- `postman/` — Colecciones y entornos de Postman
- `docs/` — Documentación funcional y técnica

## Documentación

La documentación del proyecto vive en la carpeta [`docs/`](docs/) y está pensada para leerse en orden.

### Índice de documentos

- [**01 – Visión y alcance**](docs/01-vision_y_alcance.md)
  Qué es *Rockalendar*, para quién es y qué problema resuelve (y qué no).

- [**02 – Filosofía del proyecto**](docs/02-filosofia_del_proyecto.md)
  Principios y valores que guían las decisiones del proyecto.

- [**03 – Usuarios y casos de uso**](docs/03-usuarios_y_casos_de_uso.md)
  Tipos de usuarios y escenarios de uso de la aplicación.

- [**04 – Roadmap y MVP**](docs/04-roadmap_y_mvp.md)
  Definición del MVP y evolución por versiones.

- [**05 – Arquitectura**](docs/05-arquitectura.md)
  Arquitectura técnica, stack, seguridad, despliegue y observabilidad.

- [**06 – Modelo de dominio y datos**](docs/06-modelo_de_dominio_y_datos.md)
  Especificación del dominio, entidades y relaciones de datos.

- [**07 – ADRs (Architecture Decision Records)**](docs/07-adrs.md)
  Decisiones técnicas clave y alternativas consideradas.

- [**08 – Guía de desarrollo)**](docs/07-guia_de_desarrollo.md)
  Manual de desarrollo y convenciones, y forma de trabajo en GIT para contribuciones.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- PostgreSQL
- Docker / Docker Compose
- OpenAPI (code-first)
- Flyway
- Frontend: por decidir

## Arranque rápido (entorno local)

> Las instrucciones completas de desarrollo están documentadas en [**Guía de desarrollo**](docs/08-guia-de-desarrollo.md).

### Requisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose

### 1) Levantar infraestructura (PostgreSQL)

Desde la raíz del repositorio:

~~~
docker compose -f docker/compose.yml -p rockalendar up -d
~~~

Para parar:

~~~
docker compose -f docker/compose.yml -p rockalendar down
~~~

Para resetear datos (borra volumen):

~~~
docker compose -f docker/compose.yml -p rockalendar down -v
docker compose -f docker/compose.yml -p rockalendar up -d
~~~

### 2) Backend

~~~
cd backend
mvn clean spring-boot:run
~~~

La API estará disponible en:

- [http://localhost:8080](http://localhost:8080)

Si usas perfiles: `-Dspring-boot.run.profiles=dev` o mediante variable de entorno `SPRING_PROFILES_ACTIVE=dev`

Perfiles disponibles:

- `dev`
- `local`

### 3) Frontend

🚧 Próximamente

## Seguridad (JWT)

🚧 Próximamente

## Flujo de trabajo (Git)

- Rama principal: `main`
- Todo cambio entra mediante **Pull Request**
- Ramas:
  - `feature/<tema>`
  - `fix/<tema>`
  - `docs/<tema>`
- Los releases siguen los hitos del roadmap (`v0.x`, `v1.0.0`)
- Se mantiene un `CHANGELOG.md`

> Detalles completos en la
> [**Guía de desarrollo**](docs/08-guia-de-desarrollo.md)

## Desarrolladores

Proyecto personal mantenido por su autor: jmarfil

## Licencia

Por definir

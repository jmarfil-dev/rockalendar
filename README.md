<p align="center">
  <img src="assets/images/banner.png" alt="Rockalendar banner">
</p>

---

![Badge Status](https://img.shields.io/badge/STATUS-Work%20in%20Progress-green)
![GitHub Release](https://img.shields.io/github/v/release/jmarfil-dev/rockalendar)
![Static Badge](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white)
![Static Badge](https://img.shields.io/badge/Vue3-4FC08D?logo=vue.js&logoColor=white)
![Static Badge](https://img.shields.io/badge/Nuxt.js-00DC82?logo=nuxtdotjs&logoColor=fff)

:musical_note: Agenda de conciertos y festivales :musical_note:

**Rockalendar** es una aplicación web para **descubrir conciertos y festivales de forma clara, colaborativa y sin ruido**.
Su foco está en el descubrimiento y la planificación, evitando anuncios, rankings artificiales o dinámicas sociales innecesarias.

Este repositorio es un **monorepo** que contiene:

- backend (Spring Boot / Java)
- frontend (framework por decidir)
- tooling local (Docker, Postman)
- documentación del proyecto

## :arrow_forward: Índice

- [Estado del proyecto](#estado-del-proyecto)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Documentación](#documentación)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arranque rápido (entorno local)](#arranque-rápido-entorno-local)
- [Seguridad (JWT)](#seguridad-jwt)
- [Flujo de trabajo (Git)](#flujo-de-trabajo-git)
- [Desarrolladores](#desarrolladores)
- [Licencia](#licencia)

## :arrow_forward: Estado del proyecto

**MVP 1.0 en fase de pruebas**\
:hammer_and_wrench: En desarrollo activo de nuevas funcionalidades :hammer_and_wrench:\

El alcance y la hoja de ruta están definidos en la documentación.

## :arrow_forward: Estructura del repositorio

- `backend/` — API Spring Boot (Java, Maven)
- `frontend/` — Aplicación web (tecnología por decidir)
- `docker/` — Infraestructura local
- `postman/` — Colecciones y entornos de Postman
- `docs/` — Documentación funcional y técnica

## :arrow_forward: Documentación

La documentación del proyecto vive en la carpeta [`docs/`](docs/) y está pensada para leerse en orden.

### Índice de documentos

- [**01 – Visión y alcance**](docs/01-vision_y_alcance.md)
  Qué es *Rockalendar*, para quién es y qué problema resuelve (y qué no).

- [**02 – Filosofía del proyecto**](docs/02-filosofia_del_proyecto.md)
  Principios y valores que guían las decisiones del proyecto :small_red_triangle_down:.

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

## :arrow_forward: Tecnologías utilizadas

- Java 21
- Spring Boot
- PostgreSQL
- Docker / Docker Compose
- OpenAPI (code-first)
- Flyway
- Frontend: Vue 3 + Nuxt 4 (Primeflex, PrimeVue)

## :arrow_forward: Arranque rápido (entorno local)

> Las instrucciones completas de desarrollo están documentadas en [**Guía de desarrollo**](docs/08-guia-de-desarrollo.md).

### Requisitos

- Docker + Docker Compose
- Java 21
- Maven 3.9+
- Node.js 22

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

> **Email en local:** el compose incluye [Mailhog](https://github.com/mailhog/MailHog), un servidor SMTP ficticio que captura todos los correos salientes sin enviarlos. La bandeja de entrada está disponible en [http://localhost:8025](http://localhost:8025).

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
- `local`: sobreescribe las propiedades de dev para trabajar en local

#### Variables de entorno

Es necesario configurar las siguientes variables de entorno:
- `SPRING_PROFILES_ACTIVE`: para local usa el valor `dev,local`
- `EMAIL_FROM`: usa tu correo personal, es en local, no hay problema
- `BREVO_SMTP_USER` y `BREVO_SMTP_PASSWORD`: configuración del servidor SMTP, solicitar credenciales por privado al administrador.

### 3) Frontend

~~~
cd frontend
npm install
~~~

Crear un fichero .env en la raíz del proyecto y agregar
~~~
NUXT_PUBLIC_API_BASE=http://localhost:8080/api
~~~

Iniciar la aplicación
~~~
npm run dev
~~~

La aplicación estará disponible en:

- [http://localhost:3000](http://localhost:3000)

## :arrow_forward: Seguridad (JWT)

*Rockalendar* utiliza autenticación **JWT (HS256)**.

La clave de firma **NO está incluida en el repositorio** y debe definirse mediante la variable de entorno:

~~~
JWT_SECRET
~~~

### Desarrollo

~~~
export JWT_SECRET="dev-secret-change-me-please-dev-secret-change-me"
~~~

O configurar un valor por defecto en `application-dev.yml`.

## :arrow_forward: Flujo de trabajo (Git)

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

## :arrow_forward: Desarrolladores

Proyecto personal mantenido por su autor: jmarfil

## :arrow_forward: Licencia

Por definir

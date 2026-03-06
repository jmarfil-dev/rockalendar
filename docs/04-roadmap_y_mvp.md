# Roadmap y MVP

## 1\. Introducción

Este documento define **qué se considera una versión mínima viable (MVP)** de *Rockalendar* y cómo se llega a ella de forma progresiva y controlada.

El objetivo del roadmap no es predecir el futuro con exactitud, sino reducir incertidumbre, evitar crecimiento desordenado y servir como herramienta para decidir **qué construir ahora y qué posponer conscientemente**.

## 2\. Definición exacta del MVP

El MVP de *Rockalendar* es una versión **útil, estable y coherente con la filosofía del proyecto**, aunque limitada en funcionalidades.

### Qué incluye el MVP

El MVP debe permitir:

- Consultar conciertos y festivales sin necesidad de registro.
- Buscar eventos aprobados por:
  - Fecha o rango de fechas.
  - Ciudad o provincia.
  - Artista o grupo.
  - Texto libre tolerante a errores.
- Registro y autenticación de usuarios.
- Creación de eventos por usuarios registrados.
- Sistema de moderación previa de eventos.
- Publicación únicamente de eventos aprobados.
- Creación y administración de artistas por moderadores.
- Creación de artistas al vuelo al crear eventos. La creación de artistas al vuelo está pensada como mecanismo funcional, no como gestión completa del catálogo.
- Interacción básica:
  - Marcar eventos como "me interesa".
  - Marcar eventos como "asistiré".
  - Agenda personal básica.
- Sistema interno de score de usuarios.
- Roles diferenciados: usuario, moderador y administrador.
- Gestión básica de usuarios:
  - Edición mínima de perfil
  - Desactivación / bloqueo (admin)
  - Nada de settings complejos
- Soporte multilenguaje basado en el idioma del navegador (cabecera HTTP), con fallback automático al inglés cuando el idioma no esté soportado.

### Qué NO incluye el MVP

De forma explícita, el MVP no incluye:

- Sistemas de recomendación algorítmica.
- Rankings, puntuaciones públicas o gamificación.
- Mensajería entre usuarios.
- Cuentas premium.

Todo lo que quede fuera de esta lista **no forma parte del objetivo del MVP**.

## 3\. Roadmap por versiones

El roadmap se divide explícitamente en **backend** y **frontend**. Esta separación no implica desarrollos independientes, sino reconocer que ambos avanzan a ritmos distintos y presentan riesgos diferentes.

Las versiones no representan entregas públicas cerradas, sino hitos técnicos y funcionales.

### Backend v0.1 - Núcleo del dominio

Primera versión centrada en sentar las bases del sistema.

Incluye:

- Modelo de dominio inicial (usuarios, eventos, estados, artistas).
- Persistencia básica.
- Modelo geo (provincias).
- API REST para creación de Artistas.
- API REST para consulta pública de eventos.

**Riesgos principales:**

- Modelo de dominio incompleto o mal definido.
- Cambios tempranos que obliguen a refactorizar capas profundas.

### Frontend v0.1 - Lectura y exploración

Primera interfaz utilizable, aunque limitada.

Incluye:

- Listado de eventos.
- Vista de detalle de evento.
- Navegación básica.

**Riesgos principales:**

- Decisiones de UI difíciles de revertir.
- Experiencia confusa en búsquedas iniciales.

### Backend v0.2 - Identidad y control

Introduce usuarios y control de acceso.

Incluye:

- Registro y autenticación.
- Roles básicos.
- Seguridad mínima.

**Riesgos principales:**

- Complejidad innecesaria en autenticación.
- Acoplamiento excesivo con el frontend.

### Frontend v0.2 - Usuarios

Hace visible la identidad sin romper el acceso libre.

Incluye:

- Registro y login.
- Gestión básica de sesión.

**Riesgos principales:**

- Fricción excesiva en el onboarding.
- Mala gestión de estados de sesión.

### Backend v0.3 - Creación y moderación

Convierte el sistema en colaborativo.

Incluye:

- Creación de eventos.
- Estados del evento (pendiente / aprobado / rechazado).
- Flujos de moderación.

**Riesgos principales:**

- Sobrecarga operativa de moderación.
- Reglas de negocio mal definidas.

### Frontend v0.3 - Publicación de eventos

Permite crear y seguir eventos.

Incluye:

- Formularios de creación.
- Feedback de estado (pendiente / aprobado / rechazado).

**Riesgos principales:**

- Formularios complejos o poco claros.
- Mala comunicación del estado del evento.

### Backend v0.4 - Interacción y score

Refuerza el control de calidad y la confianza.

Incluye:

- Interacciones (me interesa / asistiré).
- Agenda personal.
- Sistema interno de score.
- Gestión avanzada de roles.

**Riesgos principales:**

- Rendimiento con aumento de interacciones.
- Definición incorrecta del score.

### Frontend v0.4 - Agenda y feedback

Aporta valor directo al usuario recurrente.

Incluye:

- Agenda personal.
- Visualización clara de interacciones.

**Riesgos principales:**

- Complejidad visual.
- Saturación de información.

### Backend v0.5 - Otras funcionalidades

Completa la aplicación con detalles vistos durante el desarrollo.

Incluye:

- Refresh token
- Logout
- Recuperar contraseña
- Añadir rate limiting + captcha tras varios intentos para evitar accesos por fuerza bruta

### Frontend v0.5 - Otras funcionalidades

Complementa la v0.5 de Backend.

### v1.0 - MVP

Versión considerada **mínima viable y públicamente usable**.

Incluye:

- Backend y frontend alineados funcionalmente.
- Cobertura completa de los casos de uso definidos.
- Estabilidad y rendimiento aceptables.
- Documentación mínima.

## 4\. Criterios de salida del MVP

*Rockalendar* se considera en estado MVP cuando:

- Un usuario anónimo puede encontrar un concierto relevante en menos de 60 segundos.
- Un usuario registrado puede crear un evento sin asistencia externa.
- El flujo de moderación funciona de principio a fin.
- La agenda personal es utilizable como herramienta real de planificación.
- No existen errores bloqueantes conocidos.
- El sistema soporta al menos 1.000 eventos sin degradación perceptible.

A partir de este punto, el desarrollo deja de centrarse en "completar" y pasa a mejorar o evolucionar.

## 5\. Riesgos globales

Más allá de los riesgos por versión, existen riesgos transversales:

- Sobrediseño prematuro.
- Introducir complejidad antes de validar uso real.
- Derivar hacia funcionalidades propias de redes sociales.
- Falta de tiempo o recursos para mantenimiento.

Este roadmap debe revisarse periódicamente para asegurar que sigue alineado con la visión y la filosofía del proyecto.

## 6\. Líneas de evolución

Tras finalizar el MVP y observar durante un tiempo cómo funciona públicamente la aplicación, se podrían abordar diferentes líneas de evolución.

Versiones posteriores explorarían integraciones externas, funciones sociales avanzadas y posibles clientes nativos, siempre que no contradigan la filosofía del proyecto.
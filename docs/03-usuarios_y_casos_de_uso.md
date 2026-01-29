# Usuarios y casos de uso

## 1\. Introducción

Este documento describe **quiénes usan *Rockalendar*, cómo lo usan y bajo qué reglas**, sirviendo como puente entre la visión del producto y las decisiones funcionales y técnicas.

No pretende detallar pantallas ni flujos de UI, sino **definir comportamientos esperados**, responsabilidades y límites. Debe leerse como una guía de sentido común: si una funcionalidad no encaja aquí, probablemente no encaje en el producto.

## 2\. Perfiles de usuario

> Nota: el sistema contempla un score de usuario interno, que refleja el comportamiento histórico del usuario al crear eventos. Este score no es público ni gamificado y se utiliza exclusivamente para control de calidad y confianza dentro de la plataforma.

### 2.1 Usuario anónimo

Usuario que accede sin autenticarse.

**Permisos principales:**

- Consultar eventos.
- Buscar eventos por fecha, ubicación, artista o texto libre.
- Ver información completa de un evento.

**Restricciones:**

- No puede crear eventos.
- No puede interactuar (marcar interés / asistencia).
- No dispone de agenda personal.

Este perfil existe para eliminar fricción y facilitar el acceso libre a la información cultural.

### 2.2 Usuario registrado

Usuario autenticado con cuenta propia.

**Permisos principales:**

- Todo lo disponible para usuarios anónimos.
- Crear eventos.
- Marcar eventos como "me interesa" o "asistiré".
- Consultar y gestionar su agenda personal.

**Score de usuario:**

- Cada evento aprobado incrementa el score.
- Eventos rechazados, duplicados o que incumplen normas lo reducen.
- El score no es visible para otros usuarios.

**Restricciones:**

- Los eventos creados no se publican automáticamente.
- No puede moderar ni aprobar eventos.

El usuario registrado es el **motor colaborativo** de *Rockalendar*.

### 2.3 Moderador

Usuario con permisos adicionales de control y curación de contenido.

Este rol **no se solicita libremente**: se otorga a usuarios con historial positivo y score suficiente, a criterio de los administradores.

**Permisos principales:**

- Revisar eventos pendientes.
- Aprobar o rechazar eventos.
- Solicitar correcciones o aclaraciones.

**Responsabilidad principal:** Garantizar que los eventos publicados cumplen las normas éticas, de contenido y calidad definidas en el proyecto.

### 2.4 Administrador

Rol con control total sobre la plataforma.

**Permisos principales:**

- Todos los permisos de moderador.
- Gestión de usuarios y roles.
- Configuración global del sistema.

Este rol está pensado para mantenimiento y gobernanza, no para uso cotidiano.

## 3\. Viajes de usuario (escenarios típicos)

### 3.1 Descubrir un concierto

- El usuario accede a la aplicación (autenticado o no).
- Introduce una búsqueda rápida o filtra por fecha y ciudad.
- Revisa una lista de eventos clara y sin ruido.
- Accede al detalle de un evento concreto.
- (Opcional) Se registra para guardar el evento en su agenda.

Objetivo: **encontrar algo interesante en el menor tiempo posible**.

### 3.2 Publicar un evento

- Usuario registrado accede al formulario de creación.
- Introduce datos básicos del evento.
- Envía el evento para revisión.
- El evento queda en estado PENDIENTE.
- Tras moderación, el evento se publica o se rechaza con motivo.

Objetivo: **publicar sin fricción pero con control de calidad**.

### 3.3 Planificar agenda personal

- Usuario autenticado explora eventos.
- Marca algunos como "me interesa" o "asistiré".
- Consulta su agenda personal.
- Usa la agenda como referencia para planificar fechas.

Objetivo: **ayudar a decidir qué hacer, no forzar interacción social**.

## 4\. Casos de uso

No se utilizan historias de usuario clásicas en esta fase. En su lugar, se definen casos de uso funcionales, más adecuados para un proyecto pequeño y controlado.

### Ejemplos clave

- Consultar eventos disponibles.
- Buscar eventos con texto libre tolerante a errores.
- Crear un evento.
- Moderar un evento.
- Marcar interés o asistencia.
- Gestionar agenda personal.

Estos casos de uso deben mantenerse limitados y claros. Cualquier ampliación debe justificarse contra la visión del proyecto.

## 5\. Reglas de negocio clave

- Todos los eventos creados pasan por moderación previa.
- Un evento solo es visible cuando está aprobado.
- Un usuario no puede aprobar su propio evento.
- Existe un sistema de score interno de usuarios basado en la calidad de los eventos creados.
- El score puede aumentar o disminuir según decisiones de moderación.
- El score se utiliza como criterio para otorgar (o retirar) el rol de moderador.
- La relevancia de los eventos no depende de pagos.
- No existen cuentas premium.
- El acceso a la consulta de eventos es libre.

Estas reglas tienen prioridad sobre decisiones técnicas o de UX.

## 6\. Requisitos funcionales

### Gestión de eventos

- Crear eventos con información estructurada.
- Editar eventos mientras estén en estado pendiente.
- Consultar eventos publicados.

### Búsqueda

- Búsqueda por fecha o rango de fechas.
- Búsqueda por ubicación (ciudad / provincia).
- Búsqueda por artista o grupo.
- Búsqueda por texto libre tolerante a errores.

### Interacción de usuario

- Registro y autenticación.
- Marcar eventos como "me interesa" o "asistiré".
- Agenda personal consultable.

### Moderación

- Listado de eventos pendientes.
- Aprobación o rechazo con motivo.
- Trazabilidad básica de decisiones.

## 7\. Consideraciones finales

Este documento no describe el "cómo" técnico, sino el qué y el por qué del comportamiento del sistema.

Debe revisarse cuando:

- Se detecten nuevos patrones de uso reales.
- Se planteen funcionalidades fuera del alcance inicial.
- Cambie la visión o filosofía del proyecto.

Mientras tanto, actúa como referencia para mantener *Rockalendar* simple, coherente y útil.
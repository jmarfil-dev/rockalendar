# Usuarios y casos de uso

## 1. Introducción

Este documento describe **quiénes usan *Rockalendar*, cómo lo usan y bajo qué reglas**, sirviendo como puente entre la visión del producto y las decisiones funcionales y técnicas.

No pretende detallar pantallas ni flujos de UI, sino **definir comportamientos esperados**, responsabilidades y límites. Debe leerse como una guía de sentido común: si una funcionalidad no encaja aquí, probablemente no encaje en el producto.

---

## 2. Perfiles de usuario

> El sistema mantiene un **score interno de confianza** por usuario. Refleja el historial de propuestas: sube con aprobaciones, baja con rechazos. No es público ni gamificado; se usa como criterio para otorgar o retirar el rol de moderador.

### 2.1 Usuario anónimo

Usuario que accede sin autenticarse.

**Puede:**
- Consultar el listado de eventos aprobados.
- Buscar eventos por fecha, rango de fechas, ciudad, provincia, artista o texto libre.
- Ver el detalle completo de un evento.
- Ver el detalle de un artista.

**No puede:**
- Crear eventos.
- Marcar eventos como "me interesa" o "asistiré".
- Acceder a agenda personal.

Este perfil existe para eliminar fricción y garantizar acceso libre a la información cultural.

---

### 2.2 Usuario registrado

Usuario autenticado con cuenta propia.

**Puede:**
- Todo lo disponible para usuarios anónimos.
- Registrarse y autenticarse.
- Recuperar contraseña.
- Proponer eventos (quedan en estado pendiente hasta moderación).
- Editar sus propios eventos mientras están en estados editables (`NEEDS_CHANGES`, `APPROVED`).
- Borrar sus propios eventos mientras están en `PENDING_MODERATION` o `NEEDS_CHANGES`.
- Marcar eventos como "me interesa" o "asistiré" (solo eventos aprobados).
- Cambiar una marca de `INTERESTED` a `GOING` (y viceversa).
- Desmarcar un evento de su agenda.
- Consultar su agenda personal.

**No puede:**
- Publicar eventos directamente: todos pasan por moderación.
- Borrar un evento ya aprobado (debe contactar con moderación).
- Moderar ni aprobar eventos.

El usuario registrado es el **motor colaborativo** de *Rockalendar*.

---

### 2.3 Moderador

Usuario con permisos de control y curación de contenido.

Este rol **no se solicita libremente**: se otorga a usuarios con historial positivo y score suficiente, a criterio de los administradores.

**Puede (además de lo del usuario registrado):**
- Ver el panel de eventos pendientes de moderación.
- Aprobar eventos (`PENDING_MODERATION` → `APPROVED`).
- Rechazar eventos con motivo obligatorio (`PENDING_MODERATION` → `REJECTED`).
- Ocultar eventos aprobados (`APPROVED` → `HIDDEN`).
- Solicitar cambios al autor (`PENDING_MODERATION` → `NEEDS_CHANGES`), con motivo obligatorio.
- Ver el histórico de moderación (eventos archivados: rechazados, ocultos, cancelados).
- Ver y gestionar el catálogo de artistas (crear, editar).

**No puede:**
- Moderar sus propios eventos (el sistema lo impide).
- Aprobar directamente eventos en estado `FLAGGED` (deben pasar primero a `PENDING_MODERATION`).

**Responsabilidad principal:** Garantizar que los eventos publicados cumplen las normas éticas, de contenido y calidad del proyecto.

---

### 2.4 Administrador

Rol con control total sobre la plataforma.

**Puede (además de lo del moderador):**
- Sus propios eventos **se publican directamente** (`APPROVED`) sin pasar por moderación.
- Editar eventos en cualquier estado; al guardar, el evento queda `APPROVED`.
- Gestionar usuarios y roles.
- Configurar reglas de automoderaci­ón (lista negra de términos, patrones regex, artistas bloqueados, umbrales anti-spam).

Este rol está pensado para mantenimiento y gobernanza, no para uso cotidiano.

---

## 3. Viajes de usuario

### 3.1 Descubrir un concierto

1. El usuario accede a la aplicación (autenticado o no).
2. Introduce una búsqueda o aplica filtros (fecha, ciudad/provincia, artista, texto libre).
3. Revisa el listado de eventos aprobados.
4. Accede al detalle de un evento concreto.
5. *(Opcional, si está autenticado)* Marca el evento como "me interesa" o "asistiré".

Objetivo: **encontrar algo interesante en el menor tiempo posible**.

---

### 3.2 Publicar un evento

1. Usuario registrado accede al formulario de creación.
2. Rellena los datos del evento (título, fecha, lugar, artistas, descripción…).
3. Envía el evento para revisión.
4. El sistema evalúa automáticamente el contenido (automoderaci­ón):
   - Si pasa: el evento queda en `PENDING_MODERATION`.
   - Si contiene términos, patrones o artistas bloqueados, o si el usuario supera el umbral de rechazos recientes: el evento queda en `FLAGGED` y pasa a revisión por autorechazo programado.
5. Un moderador revisa el evento pendiente y lo aprueba, rechaza o solicita cambios.
6. El autor recibe el resultado. Si se solicitan cambios, puede editar y reenviar.

Objetivo: **publicar sin fricción pero con control de calidad**.

---

### 3.3 Ciclo de correcciones (NEEDS\_CHANGES)

1. El moderador detecta que un evento tiene datos incorrectos o incompletos.
2. Solicita cambios al autor con un motivo explicado; el evento pasa a `NEEDS_CHANGES`.
3. El autor edita el evento y lo reenvía; vuelve a `PENDING_MODERATION`.
4. El moderador lo revisa de nuevo.

**Límite de correcciones:** Si un mismo evento acumula tres solicitudes de cambios, la tercera dispara un **rechazo automático** (`AUTO_REJECT`). Esto evita ciclos indefinidos y penaliza propuestas de baja calidad.

---

### 3.4 Flujo de automoderaci­ón y FLAGGED

Los eventos marcados como `FLAGGED` por el sistema automático no entran en la cola de moderación normal. Si transcurren 24 horas sin acción manual de un moderador, el sistema los rechaza automáticamente con el motivo de la regla que los marcó.

Las reglas de automoderaci­ón son configurables por los administradores y cubren:
- Términos de texto prohibidos.
- Patrones regex.
- Artistas bloqueados.
- Umbral anti-spam: usuarios con un número elevado de rechazos recientes.

---

### 3.5 Planificar agenda personal

1. Usuario autenticado explora eventos aprobados.
2. Marca algunos como "me interesa" o "asistiré" (ambas marcas son mutuamente excluyentes por evento).
3. Puede cambiar una marca o eliminarla en cualquier momento.
4. Consulta su agenda personal como referencia para planificar fechas.

Objetivo: **ayudar a decidir qué hacer, no forzar interacción social**.

---

### 3.6 Recuperar contraseña

1. El usuario solicita recuperación indicando su email.
2. Si el email existe, el sistema envía un enlace de un solo uso (válido 15 minutos).
3. El usuario accede al enlace y establece una nueva contraseña.
4. El token queda invalidado tras su uso.

La respuesta del paso 2 es siempre la misma independientemente de si el email existe, para no revelar qué cuentas hay registradas.

---

### 3.7 Moderar un evento

1. El moderador accede al panel de moderación y ve los eventos en `PENDING_MODERATION`.
2. Revisa el detalle y elige una acción:
   - **Aprobar:** el evento se publica (`APPROVED`).
   - **Rechazar:** el evento queda `REJECTED` con motivo obligatorio; el score del autor baja.
   - **Solicitar cambios:** el evento pasa a `NEEDS_CHANGES` con motivo obligatorio; el autor puede corregirlo y reenviar.
   - **Ocultar** (solo eventos ya aprobados): el evento pasa a `HIDDEN` sin notificación al autor.
3. Todas las acciones quedan registradas en el histórico de moderación.

Restricción clave: **un moderador no puede moderar sus propios eventos**.

---

## 4. Ciclo de vida de un evento

```
                         ┌─────────────────────────────────────────┐
                         │           [Usuario propone]              │
                         └─────────────┬───────────────────────────┘
                                       │
                         ┌─────────────▼──────────────┐
                         │  Automoderacion evalúa       │
                         └──┬──────────────────────┬───┘
                            │ Pasa                 │ Flag
                            ▼                      ▼
              PENDING_MODERATION              FLAGGED
              (cola de moderación)       (sin acción en 24h)
                    │                          │
          ┌─────────┼────────────┐             ▼
          │         │            │         REJECTED
          ▼         ▼            ▼       (auto-rechazo)
       APPROVED  REJECTED  NEEDS_CHANGES
          │                    │
          │           (usuario edita y reenvía)
          │                    │
          │                    ▼
          │          PENDING_MODERATION
          │               [3.ª vez] → AUTO_REJECT → REJECTED
          │
          ▼
        HIDDEN
    (moderador oculta)
```

**Notas:**
- `ERASED`: borrado lógico. El usuario puede borrar sus eventos en `PENDING_MODERATION` o `NEEDS_CHANGES`. No se puede recuperar.
- `CANCELED`: estado reservado para cancelaciones (por el autor o administración).
- Admins: sus propios eventos van directamente a `APPROVED` al proponerlos; al editar cualquier evento también queda `APPROVED`.

---

## 5. Reglas de negocio

- Todos los eventos creados por usuarios regulares pasan por moderación previa.
- Un evento solo es visible públicamente cuando está en estado `APPROVED`.
- Un moderador no puede moderar sus propios eventos.
- Los administradores publican sus propios eventos sin pasar por moderación.
- Tres solicitudes de cambios sobre un mismo evento desencadenan un rechazo automático.
- Los eventos `FLAGGED` son rechazados automáticamente tras 24 horas sin acción manual.
- Un usuario solo puede interactuar con eventos en estado `APPROVED`.
- Un usuario puede tener como máximo una marca activa por evento (`INTERESTED` o `GOING`).
- Un usuario no puede borrar un evento ya aprobado sin intervención de moderación.
- El score de usuario sube con aprobaciones y baja con rechazos; es interno, no visible al público.
- La relevancia de los eventos no depende de pagos. No existen cuentas premium.
- El acceso a la consulta de eventos es completamente libre.

---

## 6. Requisitos funcionales

### Consulta y búsqueda
- Listado de eventos aprobados con paginación.
- Búsqueda por fecha o rango de fechas.
- Búsqueda por ciudad o provincia.
- Búsqueda por artista o grupo.
- Búsqueda por texto libre, tolerante a errores tipográficos.
- Detalle de evento.
- Detalle de artista.

### Autenticación y cuenta
- Registro con email y contraseña.
- Login y logout.
- Renovación silenciosa de sesión (sin intervención del usuario).
- Recuperación de contraseña por email.

### Gestión de eventos propios
- Proponer un evento nuevo.
- Editar un evento propio en estados editables (`NEEDS_CHANGES`, `APPROVED`).
- Borrar un evento propio en `PENDING_MODERATION` o `NEEDS_CHANGES`.
- Consultar el listado de eventos propios con sus estados actuales.

### Agenda personal
- Marcar evento como "me interesa" o "asistiré" (solo eventos aprobados).
- Cambiar entre marcas o desmarcar.
- Consultar la agenda personal.

### Moderación
- Panel de eventos pendientes (`PENDING_MODERATION`).
- Aprobar, rechazar, ocultar o solicitar cambios sobre un evento.
- Histórico de eventos moderados (rechazados, ocultos, cancelados).
- Gestión del catálogo de artistas.

### Administración
- Configuración de reglas de automoderaci­ón.
- Gestión de usuarios y roles.

---

## 7. Consideraciones finales

Este documento describe el **qué** y el **por qué** del comportamiento del sistema, no el cómo técnico.

Debe revisarse cuando:
- Se implementen nuevas funcionalidades o flujos.
- Cambien las reglas de negocio o la filosofía del proyecto.
- Se detecten patrones de uso reales que no estén reflejados aquí.

Cualquier funcionalidad que no encaje en este documento debería cuestionarse antes de implementarse.

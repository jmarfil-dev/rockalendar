# Moderación y confianza

> Este documento describe el sistema de trust score de usuarios y el sistema de moderación automática de eventos.

## 1. Trust score

### Rango y límites

- **Rango efectivo:** -200 a ∞
- **Cap funcional:** 100 (umbral para ascenso a moderador; el score puede seguir subiendo pero no tiene efecto adicional por encima de ese valor)
- **Ban permanente automático** al llegar a -200

### Variaciones de puntuación

| Evento                                                | Cambio           |
|-------------------------------------------------------|------------------|
| Evento aprobado sin solicitudes de cambios            | +10              |
| Evento aprobado con al menos una solicitud de cambios | +5               |
| Evento rechazado                                      | -15              |
| Evento rechazado tras tres solicitudes de cambios     | -30              |
| Cancelar, borrar o dejar en DRAFT                     | sin penalización |

### Requisitos de ascenso a moderador

Los requisitos son **ocultos** al usuario. El botón de solicitud de ascenso aparece silenciosamente cuando se cumplen todas las condiciones:

- `trust_score` ≥ 100
- Antigüedad de la cuenta > 90 días
- ≤ 3 eventos de la misma sala en los últimos 30 días
- ≤ 3 eventos del mismo grupo en los últimos 30 días
- ≤ 10 eventos totales de la misma sala
- ≤ 10 eventos totales del mismo grupo

## 2. Moderación automática

El sistema aplica **solo rechazos automáticos**. Si un evento supera todos los filtros, pasa a la cola de moderación humana (`PENDING_MODERATION`).

### 2.1. Estado FLAGGED

Los eventos que no superan los filtros automáticos se marcan con el estado `FLAGGED` en lugar de rechazarse de inmediato. Este estado:

- **No aparece** en la bandeja de moderación humana.
- Es indistinguible para el usuario de `PENDING_MODERATION` (ve su evento "en revisión").
- Es procesado por un scheduler que, pasadas X horas (configurable), mueve el evento a `REJECTED` con un mensaje genérico: *"El evento no cumple los requisitos de publicación."*
- Puede ser revisado y sobreescrito por un administrador desde el panel de administración antes de que actúe el scheduler.

Esto impide que los usuarios aprendan qué activa los filtros y adapten sus propuestas para evadir la moderación.

### 2.2. Reglas de rechazo automático

#### a) Blacklist de términos

Si el **título** o la **descripción** del evento contienen algún término de la blacklist, el evento se marca como `FLAGGED`.

- Soporte para términos exactos y patrones regex.
- La blacklist se almacena en base de datos y es modificable sin reiniciar la aplicación.

#### b) Blacklist de artistas

Si alguno de los artistas del evento figura en la blacklist de artistas, el evento se marca como `FLAGGED`.

- La blacklist de artistas también está en base de datos.

#### c) Trust score por debajo del umbral mínimo

Si el `trust_score` del usuario proponente está por debajo de un umbral configurable (almacenado en BD), el evento se marca como `FLAGGED` automáticamente.

#### d) Ráfaga de propuestas rechazadas (anti-spam)

Si el usuario tiene N eventos rechazados en los últimos X días (ambos valores configurables en BD), el evento se marca como `FLAGGED`.

#### e) Tercera solicitud de cambios → rechazo inmediato

Este caso no pasa por `FLAGGED`: es una decisión de flujo explícita que se resuelve en el momento.

Cuando un moderador intenta poner un evento en estado `NEEDS_CHANGES`, el sistema comprueba si ya existen dos solicitudes de cambios previas para ese evento. Si es así:
- El evento se rechaza inmediatamente (estado `REJECTED`), sin pasar por `FLAGGED`.
- Se aplica la penalización de trust score correspondiente: **-30**.
- El usuario recibe notificación de rechazo (fuera del scope actual).

### 2.3. Validación previa: fecha en el pasado

Antes incluso de llegar al filtro automático, la capa de validación del request rechaza eventos con fecha ya pasada. Esta validación vive en el record `SubmitEventRequest` con la anotación `@Future` (o `@FutureOrPresent` según se decida).

### 2.4. Modelo de datos

#### Tabla `moderation_rules`

Almacena las reglas activas de la blacklist.

| Campo        | Tipo      | Descripción                               |
|--------------|-----------|-------------------------------------------|
| `id`         | UUID      | Clave primaria                            |
| `rule_type`  | enum      | `TEXT_TERM`, `ARTIST_SLUG`, `REGEX`       |
| `value`      | text      | El término, slug o patrón a filtrar       |
| `reason`     | text      | Motivo interno (visible solo para admins) |
| `active`     | boolean   | Permite desactivar reglas sin borrarlas   |
| `created_at` | timestamp | Fecha de creación                         |

#### Tabla `moderation_config`

Almacena parámetros configurables del sistema de moderación automática.

| Clave                           | Descripción                                                    |
|---------------------------------|----------------------------------------------------------------|
| `min_trust_score`               | Trust score mínimo para proponer eventos                       |
| `spam_rejection_count`          | Nº de rechazos en ventana de tiempo para activar anti-spam     |
| `spam_window_days`              | Ventana de tiempo del anti-spam (en días)                      |
| `flagged_rejection_delay_hours` | Horas que un evento permanece en `FLAGGED` antes de rechazarse |

#### Tabla `auto_moderation_log`

Registro de eventos marcados automáticamente. Separada de `moderation_actions` (que registra acciones humanas).

| Campo           | Tipo      | Descripción                                           |
|-----------------|-----------|-------------------------------------------------------|
| `id`            | UUID      | Clave primaria                                        |
| `event_id`      | UUID      | FK a `events`                                         |
| `rule_id`       | UUID      | FK a `moderation_rules` (nullable si es config-based) |
| `rule_type`     | enum      | Tipo de regla que activó el flag                      |
| `matched_value` | text      | Valor concreto que activó el filtro                   |
| `flagged_at`    | timestamp | Momento del flag                                      |

### 2.5. Servicios

- **`AutoModerationService`** — evalúa todas las reglas activas para un evento dado. Devuelve `AutoModerationResult(PASS | FLAG, ruleType, matchedValue)`. Sin estado; fácil de testear.
- **`EventAutoRejectionScheduler`** — `@Scheduled` de Spring. Busca eventos en estado `FLAGGED` con más de `flagged_rejection_delay_hours` horas, los mueve a `REJECTED` y registra una `ModerationAction` con un actor `SYSTEM`.
- **`ModerationCommandService.requestChanges()`** — antes de aplicar `NEEDS_CHANGES`, cuenta las solicitudes previas del evento. Si ya son dos, rechaza directamente con penalización de -30.

## 3. Ciclo de vida del evento (actualizado)

```
DRAFT
  └─► PENDING_MODERATION  ──► APPROVED
  │         │               └─► NEEDS_CHANGES ──► (ciclo de moderación)
  │         │               └─► REJECTED
  │         └─► FLAGGED ──────► REJECTED  (vía scheduler, tras delay)
  └─► CANCELLED / ERASED
```

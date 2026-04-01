# Arquitectura Frontend

## Presentación (UX)

### Header

### Sidebar de búsqueda

*En Vue se llama Drawer*

### Navbar bottom:

**3–4 items máximo**

> Buscar / Proponer / Mi área / Gestión
> Símbolos: lupa, +, casa, (pensar)

- Buscar (siempre)
- Proponer (siempre que haya auth)
- Mi área (auth) -> Agenda y Mis eventos
- Gestión (solo moderador) -> Panel de moderación
- Gestión (solo admin) -> Panel de administración + panel de moderación

Esquema simplificado:
- `isAuthenticated=false` -> [Buscar]
- `isAuthenticated=true` -> [Buscar, Proponer, Mi área]
- `isModerator=true` -> [Buscar, Proponer, Mi área, Gestión (moderación)]
- `isAdmin=true` -> [Buscar, Proponer, Mi área, Gestión (moderación + admin)]

> Gestión no va dentro de Mi área porque son tareas distintas.

## Arquitectura

**AppShell**

Contenedor de toda la página.

Renderiza:
- Header
- Navbar
- Sidebar de búsqueda
- Páginas mediante layouts.

> Por decidir si los layouts de gestión (moderación y admin) tendrán este AppShell.

**Layouts separados**

- `layouts/public.vue` -> páginas públicas (listado, detalle, etc.) y actualmente también `/admin/**`
- `layouts/private.vue` -> área personal (`/me/**`)
- `layouts/moderation.vue` -> moderación (`/moderation/**`)
- `layouts/minimal.vue` -> páginas de acceso (login, registro, recuperación de contraseña)

> `layouts/admin.vue` está pendiente de crear. Por ahora `/admin/**` reutiliza el layout `public`.

**Navbar como componente + composable (para centralizar)**

- components/BottomNav.vue (render)
- composables/useBottomNav.ts (decide items según auth + roles + contexto)

Centralizarlo ahorra duplicación.

## Navegación

Estructura de rutas (y carpetas)
- `/me` -> Hub personal (cards/botones: Agenda / Mis eventos)
- `/me/agenda` -> Pestañas (me interesa / asistiré / todo)
- `/me/events` -> Pestañas (pendiente / aprobado / needs_changes / etc.)
- `/moderation` -> Panel de moderación
- `/admin` -> Hub de gestión (cards/botones: moderación / administración)

El navbar no hace “push” directo a rutas internas de cada área:
- Botón Mi área -> `/me` (hub)
- Botón Moderación -> `/moderation`
- Botón Admin -> `/admin`

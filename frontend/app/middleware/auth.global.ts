import { ROUTES } from "~/constants/routes";
import { sanitizeRedirect } from "~/utils/safeRedirect";
import type { Rule } from "~/types/user-roles";

const RULES: Rule[] = [
  { prefix: ROUTES.me, auth: true },
  { prefix: ROUTES.moderation, auth: true, anyOfRoles: ["ROLE_MODERATOR", "ROLE_ADMIN"] },
  { prefix: ROUTES.admin, auth: true, anyOfRoles: ["ROLE_ADMIN"] },
];

function matchRule(path: string): Rule | null {
  const matches = RULES.filter((r) => path === r.prefix || path.startsWith(r.prefix + "/")).sort(
    (a, b) => b.prefix.length - a.prefix.length,
  );
  return matches[0] ?? null;
}

/**
 * Aplica reglas de middleware.
 * Solo se ejecuta en cliente: la autenticación es client-side (localStorage),
 * por lo que el servidor nunca puede conocer el estado real del usuario.
 */
export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return;

  const auth = useAuth();
  if (to.path === ROUTES.login && auth.isAuthenticated.value) {
    // Si ruta es login y ya está autenticado, redir a página privada o ruta original
    const redirect = sanitizeRedirect(
      typeof to.query.redirect === "string" ? to.query.redirect : undefined,
      ROUTES.meEvents,
    );
    return navigateTo(redirect);
  }

  const rule = matchRule(to.path);
  if (!rule) return; // Si la ruta no tiene regla, acceso libre

  if (rule.auth && !auth.isAuthenticated.value) {
    // Si hay regla y no hay auth, redir a login
    return navigateTo({
      path: ROUTES.login,
      query: { redirect: to.fullPath },
    });
  }

  if (rule.anyOfRoles && !rule.anyOfRoles.some((r) => auth.roles.value.includes(r))) {
    // Si la ruta tiene regla + rol y no se cumple el rol, error forbidden
    return navigateTo(ROUTES.errorForbidden, { replace: true });
  }
});

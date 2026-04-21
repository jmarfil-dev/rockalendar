import { ROUTES } from "~/constants/routes";

export default defineNuxtPlugin((_nuxtApp) => {
  const auth = useAuth();
  const notifications = useNotifications();

  // Cargamos el token síncronamente para que el middleware de ruta pueda evaluar
  // el estado de auth en la carga inicial (refresh en página privada).
  // Los elementos dependientes de auth en páginas SSR deben estar en <ClientOnly>
  // para evitar hydration mismatches.
  auth.loadFromStorage();

  if (auth.isAuthenticated.value) {
    notifications.fetchUnreadCount();
  }

  watch(auth.isAuthenticated, (authenticated) => {
    if (authenticated) notifications.fetchUnreadCount();
  });

  notifications.initPolling(auth.isAuthenticated);

  window.addEventListener("storage", (event) => {
    if (event.key !== "rk:auth:event") return;

    try {
      const parsed = JSON.parse(event.newValue ?? "{}");
      if (parsed.type === "logout") {
        auth.token.value = null;
        auth.expiresAtMs.value = null;

        navigateTo(ROUTES.login);
      }
    } catch {
      // JSON malformado: ignorar para no romper la sincronización cross-tab
    }
  });
});

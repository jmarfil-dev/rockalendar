import { ROUTES } from "~/constants/routes";

export default defineNuxtPlugin((nuxtApp) => {
  const auth = useAuth();

  // Diferimos la carga del token al hook app:mounted para evitar hydration mismatches.
  // Durante la hidratación, isAuthenticated = false en cliente y servidor — sin discrepancias.
  // Tras el montaje, Vue hace un re-render reactivo normal con el estado real.
  nuxtApp.hook("app:mounted", () => {
    auth.loadFromStorage();
  });

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

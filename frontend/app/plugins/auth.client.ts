import { ROUTES } from "~/constants/routes";

export default defineNuxtPlugin(() => {
  const auth = useAuth();

  auth.loadFromStorage();

  window.addEventListener("storage", (event) => {
    if (event.key !== "rk:auth:event") return;

    if (event.newValue === "logout") {
      auth.token.value = null;
      auth.expiresAtMs.value = null;

      navigateTo(ROUTES.login);
    }
  });
});

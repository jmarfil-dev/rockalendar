import { es } from "primelocale/es.json"; // locale español, PrimeVue ya viene en inglés por defecto
import type { I18nLike } from "~/types/languages";

export default defineNuxtPlugin((nuxtApp) => {
  const primevue = usePrimeVue();

  const i18n = nuxtApp.$i18n as unknown as I18nLike;
  const defaultLocale = { ...(primevue.config.locale as Record<string, unknown>) };

  watchEffect(() => {
    const loc = (i18n.locale.value || "en").split("-")[0];

    primevue.config.locale = loc === "es" ? { ...defaultLocale, ...(es as Record<string, unknown>) } : { ...defaultLocale };
  });
});

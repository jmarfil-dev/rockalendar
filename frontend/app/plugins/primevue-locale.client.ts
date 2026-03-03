const LOCALE_OVERRIDES = {
  es: {
    firstDayOfWeek: 1,
    dayNames: ["domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"],
    dayNamesShort: ["dom", "lun", "mar", "mié", "jue", "vie", "sáb"],
    dayNamesMin: ["D", "L", "M", "X", "J", "V", "S"],
    monthNames: [
      "Enero",
      "Febrero",
      "Marzo",
      "Abril",
      "Mayo",
      "Junio",
      "Julio",
      "Agosto",
      "Septiembre",
      "Octubre",
      "Noviembre",
      "Diciembre",
    ],
    monthNamesShort: ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"],
    today: "Hoy",
    clear: "Limpiar",
    dateFormat: "dd/mm/yy",
    weekHeader: "Sm",
  },
} as const;

type SupportedLocale = keyof typeof LOCALE_OVERRIDES;

function pickLocaleFromBrowser(): string {
  // client-only; si lo usas en SSR, protege con import.meta.client
  const lang = (navigator.language || "en").toLowerCase();
  return lang.split("-")[0] || "en";
}

export default defineNuxtPlugin(() => {
  if (!import.meta.client) return;

  const primevue = usePrimeVue();
  const base = pickLocaleFromBrowser();

  if (base === "es") {
    primevue.config.locale = {
      ...(primevue.config.locale as any),
      ...LOCALE_OVERRIDES.es,
    };
  }
});

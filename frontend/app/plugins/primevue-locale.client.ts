import { defineNuxtPlugin } from "#app";
import { usePrimeVue } from "primevue/config";

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

function pickLocaleFromBrowser(): SupportedLocale {
  const lang = (navigator.language || "es").toLowerCase();
  const base = (lang.split("-")[0] ?? "es") as string;

  return base in LOCALE_OVERRIDES ? (base as SupportedLocale) : "es";
}

export default defineNuxtPlugin(() => {
  const primevue = usePrimeVue();
  const localeKey = pickLocaleFromBrowser();

  // IMPORTANT: partimos del locale existente (incluye defaults como fileSizeTypes)
  // y solo sobrescribimos lo necesario.
  primevue.config.locale = {
    ...(primevue.config.locale as any),
    ...LOCALE_OVERRIDES[localeKey],
  };
});

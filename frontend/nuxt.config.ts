// https://nuxt.com/docs/api/configuration/nuxt-config
import Aura from "@primeuix/themes/aura";
import { definePreset } from "@primeuix/themes";

const RockalendarPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: "{purple.50}",
      100: "{purple.100}",
      200: "{purple.200}",
      300: "{purple.300}",
      400: "{purple.400}",
      500: "{purple.500}",
      600: "{purple.600}",
      700: "{purple.700}",
      800: "{purple.800}",
      900: "{purple.900}",
      950: "{purple.950}",
    },
    colorScheme: {
      dark: {
        surface: {
          0: "#ffffff",
          50: "{zinc.50}",
          100: "{zinc.100}",
          200: "{zinc.200}",
          300: "{zinc.300}",
          400: "{zinc.400}",
          500: "{zinc.500}",
          600: "{zinc.600}",
          700: "{zinc.700}",
          800: "{zinc.800}",
          900: "{zinc.900}",
          950: "{zinc.950}",
        },
        formField: {
          background: "{surface.800}",
        },
      },
    },
  },
});

export default defineNuxtConfig({
  routeRules: {
    "/api/**": { proxy: "http://localhost:8080/api/**" },
  },
  runtimeConfig: {
    public: {
      apiBase: "", // Se inyecta del fichero .env
    },
  },
  modules: ["@primevue/nuxt-module"],
  css: ["primeicons/primeicons.css", "primeflex/primeflex.css", "~/assets/css/main.css"],
  primevue: {
    options: {
      theme: {
        preset: RockalendarPreset,
        options: {
          darkModeSelector: ".dark",
        },
      },
    },
  },
});

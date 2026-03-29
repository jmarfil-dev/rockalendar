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
        // zinc.300 (#d4d4d8) en lugar de zinc.400 (#a1a1aa) — mejora contraste
        // sobre fondos que se aclaran en hover de Card
        text: {
          mutedColor: "{surface.300}",
          hoverMutedColor: "{surface.200}",
        },
      },
    },
  },
  components: {
    message: {
      colorScheme: {
        dark: {
          // blue.300/red.300 en lugar de blue.400/red.400 — mayor contraste sobre fondos coloreados
          info: { color: "{blue.300}" },
          error: { color: "{red.300}" },
          warn: { color: "{yellow.200}" },
          success: { color: "{green.300}" },
        },
      },
    },
  },
});

export default defineNuxtConfig({
  compatibilityDate: "2026-03-23",
  app: {
    head: {
      titleTemplate: "%s — Rockalendar",
      meta: [
        {
          name: "description",
          content:
            "Encuentra conciertos y festivales de punk, rock y metal en España. Impulsado por la comunidad, sin algoritmos ni tonterías corporativas.",
        },
        { property: "og:site_name", content: "Rockalendar" },
        { property: "og:type", content: "website" },
        { name: "twitter:card", content: "summary_large_image" },
      ],
    },
  },
  routeRules: {
    "/api/**": { proxy: "http://localhost:8080/api/**" },
    "/me/**": { headers: { "X-Robots-Tag": "noindex, nofollow" } },
    "/moderation/**": { headers: { "X-Robots-Tag": "noindex, nofollow" } },
    "/admin/**": { headers: { "X-Robots-Tag": "noindex, nofollow" } },
  },
  runtimeConfig: {
    public: {
      apiBase: "", // Se inyecta del fichero .env
    },
  },
  modules: ["@nuxt/eslint", "@primevue/nuxt-module", "@nuxtjs/i18n"],
  css: ["~/assets/css/main.css"],
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
  i18n: {
    baseUrl: process.env.NUXT_PUBLIC_SITE_URL || "http://localhost:3000",
    strategy: "no_prefix", // No queremos /es/... /en/... por ahora
    locales: [
      { code: "en", language: "en-US", file: "en.json", name: "English" },
      { code: "es", language: "es-ES", file: "es.json", name: "Español" },
    ],
    defaultLocale: "en", // Inglés por defecto
    detectBrowserLanguage: {
      // Detecta navegador (client) o Accept-Language (SSR) y recuerda con cookie
      useCookie: true,
      cookieKey: "rockalendar_locale",
      redirectOn: "root", // recomendado
      fallbackLocale: "en",
    },
  },
});

import { defineEventHandler, setResponseHeaders } from "h3";

/**
 * Middleware de seguridad: aplica cabeceras HTTP a todas las respuestas HTML del servidor Nuxt.
 * Nota: no se aplica a las rutas proxy (/api/**), que son transparentes a Nitro.
 *
 * script-src requiere 'unsafe-inline' para el runtime de Nuxt (hidratación SSR).
 * Para eliminar 'unsafe-inline' de forma segura, usar el módulo nuxt-security con nonces.
 */
const isDev = process.env.NODE_ENV === "development";

export default defineEventHandler((event) => {
  const imgSrc = isDev
    ? "img-src 'self' data: blob: https: http:"
    : "img-src 'self' data: blob: https:";

  setResponseHeaders(event, {
    "Content-Security-Policy":
      `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; ${imgSrc}; font-src 'self' data: https://fonts.gstatic.com; connect-src 'self' ws: wss:; frame-ancestors 'none';`,
    "X-Frame-Options": "DENY",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "strict-origin-when-cross-origin",
    "Permissions-Policy": "camera=(), microphone=(), geolocation=()",
  });
});

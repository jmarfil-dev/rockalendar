/**
 * Sanitiza un parámetro de redirección para prevenir Open Redirect.
 * Solo permite rutas relativas que empiecen por "/" pero no por "//".
 */
export function sanitizeRedirect(raw: string | undefined, fallback: string): string {
  if (!raw) return fallback;
  if (/^\/(?!\/)/.test(raw)) return raw;
  return fallback;
}

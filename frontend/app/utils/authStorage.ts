const STORAGE_TOKEN = "rockalendar_token";
const STORAGE_EXPIRES = "rockalendar_token_expires_at";
const STORAGE_EVENT_KEY = "rk:auth:event";

/**
 * Convierte una fecha ISO (string) a milisegundos.
 * Devuelve null si viene vacío o no es parseable.
 */
export function parseExpiresAt(expiresAt: string | null): number | null {
  if (!expiresAt) return null;

  let ms = Date.parse(expiresAt);
  if (Number.isFinite(ms)) return ms;

  // soporta +0100 / -0230 -> +01:00 / -02:30
  const normalized = expiresAt.replace(/([+-]\d{2})(\d{2})$/, "$1:$2");
  ms = Date.parse(normalized);

  return Number.isFinite(ms) ? ms : null;
}

export function getExpiresAtMsFromToken(token: string): number | null {
  const payload = decodeJwtPayload(token);
  return extractExpiresAtMs(payload);
}

/**
 * Emite un "evento" en localStorage para que otras pestañas puedan reaccionar
 * con el listener que se inicia en auth.client.ts
 */
export function broadcastAuthEvent(type: "login" | "logout") {
  if (!import.meta.client) return;
  localStorage.setItem(STORAGE_EVENT_KEY, JSON.stringify({ type, at: Date.now() }));
}

/**
 * Persiste token + expiración en localStorage (solo cliente).
 */
export function writeAuthToStorage(token: string | null, expiresAtMs: number | null) {
  if (!import.meta.client) return;

  if (token) localStorage.setItem(STORAGE_TOKEN, token);
  else localStorage.removeItem(STORAGE_TOKEN);

  if (expiresAtMs) localStorage.setItem(STORAGE_EXPIRES, new Date(expiresAtMs).toISOString());
  else localStorage.removeItem(STORAGE_EXPIRES);
}

/**
 * Lee desde localStorage al iniciar (solo cliente).
 * - Si falta expires, intenta extraerlo del JWT.
 * - Si está expirado, limpia.
 */
export function readAuthFromStorage() {
  if (!import.meta.client) return { token: null as string | null, expiresAtMs: null as number | null };

  return {
    token: localStorage.getItem(STORAGE_TOKEN),
    expiresAtMs: parseExpiresAt(localStorage.getItem(STORAGE_EXPIRES)),
  };
}

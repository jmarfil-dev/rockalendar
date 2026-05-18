const STORAGE_SESSION = "rk:auth:session";
const STORAGE_EVENT_KEY = "rk:auth:event";

export interface StoredSession {
  userId: string;
  email: string;
  role: string;
  expiresAtMs: number;
}

export function parseExpiresAt(expiresAt: string | null): number | null {
  if (!expiresAt) return null;

  let ms = Date.parse(expiresAt);
  if (Number.isFinite(ms)) return ms;

  // soporta +0100 / -0230 -> +01:00 / -02:30
  const normalized = expiresAt.replace(/([+-]\d{2})(\d{2})$/, "$1:$2");
  ms = Date.parse(normalized);

  return Number.isFinite(ms) ? ms : null;
}

/**
 * Emite un "evento" en localStorage para que otras pestañas puedan reaccionar
 * con el listener que se inicia en auth.client.ts
 */
export function broadcastAuthEvent(type: "login" | "logout"): void {
  if (!import.meta.client) return;
  localStorage.setItem(STORAGE_EVENT_KEY, JSON.stringify({ type, at: Date.now() }));
}

/**
 * Persiste token + expiración en localStorage (solo cliente).
 */
export function writeSessionToStorage(session: StoredSession | null): void {
  if (!import.meta.client) return;
  if (session) {
    localStorage.setItem(STORAGE_SESSION, JSON.stringify(session));
  } else {
    localStorage.removeItem(STORAGE_SESSION);
  }
}

/**
 * Lee desde localStorage al iniciar (solo cliente).
 * - Si falta expires, intenta extraerlo del JWT.
 * - Si está expirado, limpia.
 */
export function readSessionFromStorage(): StoredSession | null {
  if (!import.meta.client) return null;
  const raw = localStorage.getItem(STORAGE_SESSION);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    return null;
  }
}

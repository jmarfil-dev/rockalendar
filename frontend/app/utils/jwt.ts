import type { Role } from "~/types/user-roles";

/**
 * Decodifica el base64 extraído de la url.
 */
function base64Decode(b64: string): string {
  // Browser
  if (typeof globalThis.atob === "function") {
    return globalThis.atob(b64);
  }

  // Node (SSR)
  // eslint-disable-next-line n/no-unsupported-features/node-builtins
  return Buffer.from(b64, "base64").toString("binary");
}

/**
 * Decodifica base64url (JWT) a string.
 */
function base64UrlDecode(input: string): string {
  // base64url -> base64
  const b64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const pad = b64.length % 4 ? "=".repeat(4 - (b64.length % 4)) : "";
  const str = b64 + pad;

  // decode base64 -> binary string
  // TODO: cuando cambie el token de localStorage a cookie, esto fallará
  const decoded = base64Decode(str);

  // intenta convertir a UTF-8 (por si hay caracteres fuera de ASCII)
  try {
    return decodeURIComponent(
      decoded
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join(""),
    );
  } catch {
    return decoded;
  }
}

/**
 * Devuelve el payload JSON del JWT, o null si el token es inválido.
 */
export function decodeJwtPayload(token: string): any | null {
  try {
    // Un JWT debe tener 3 partes
    const parts = token.split(".");
    if (parts.length !== 3) return null;

    const payload = parts[1];
    if (!payload) return null;

    const json = base64UrlDecode(payload);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

/**
 * Lee claim "roles" como array de strings.
 * Devuelve sólo strings válidos.
 */
export function extractRoles(payload: any | null): Role[] {
  if (!payload || typeof payload !== "object") return [];

  const roles = payload.roles;

  if (!Array.isArray(roles)) return [];

  return roles.filter((r): r is Role => typeof r === "string");
}

/**
 * Lee claim estándar "exp" (segundos UNIX) y lo convierte a ms.
 */
export function extractExpiresAtMs(payload: any | null): number | null {
  const exp = payload?.exp;
  if (typeof exp === "number" && Number.isFinite(exp)) {
    return exp * 1000;
  }
  return null;
}

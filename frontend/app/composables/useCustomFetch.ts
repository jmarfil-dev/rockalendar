import type { ApiResult, ProblemDetail } from "~/types/api";

type FetchOptions = NonNullable<Parameters<typeof $fetch>[1]>;

// Forma del error que devuelve ofetch / Nuxt al fallar una petición HTTP
type RawFetchError = {
  response?: {
    status?: number;
    _data?: unknown;
  };
  status?: number;
  statusCode?: number;
  statusText?: string;
  statusMessage?: string;
  data?: unknown;
};

function extractProblemDetail(err: unknown): { status: number; pd: ProblemDetail | null } {
  const e = err as RawFetchError;
  const status = e?.response?.status ?? 500;
  const pd = (e?.response?._data ?? null) as ProblemDetail | null;
  return { status, pd };
}

/**
 * Llamadas a API pública como /login o /register, sin token auth
 */
export async function fetchPublicResult<T>(url: string, options: FetchOptions = {}): Promise<ApiResult<T>> {
  try {
    const data = await $fetch<T>(url, { ...options });
    return { ok: true, data };
  } catch (err: unknown) {
    const { status, pd } = extractProblemDetail(err);
    return { ok: false, status, pd, raw: err };
  }
}

/**
 * Llamadas a API con autenticación necesaria.
 * La cookie httpOnly se envía automáticamente por el navegador (mismo origen).
 */
export async function fetchAuthResult<T>(url: string, options: FetchOptions = {}): Promise<ApiResult<T>> {
  const auth = useAuth();

  try {
    const data = await $fetch<T>(url, {
      ...options,
      onResponse({ response }) {
        // Renovación silenciosa: el backend renueva la cookie y envía el nuevo expiresAt.
        // Solo se actualiza el estado si la sesión sigue activa al recibir la respuesta.
        if (response.ok && import.meta.client && auth.isAuthenticated.value) {
          const newExpiresAt = response.headers.get("X-Refresh-Token-Expires-At");
          if (newExpiresAt) {
            auth.updateExpiry(newExpiresAt);
          }
        }
      },
    });
    return { ok: true, data };
  } catch (err: unknown) {
    const { status, pd } = extractProblemDetail(err);

    if (status === 401) {
      await auth.logout();
    }

    return { ok: false, status, pd, raw: err };
  }
}

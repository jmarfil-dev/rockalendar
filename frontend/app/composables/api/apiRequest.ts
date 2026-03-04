import type { ApiResult, ProblemDetail } from "~/types/api";

function extractProblemDetail(err: any): { status: number; pd: ProblemDetail | null } {
  const status = err?.response?.status ?? 500;
  const pd = (err?.response?._data ?? null) as ProblemDetail | null;
  return { status, pd };
}

/**
 * Llamadas a API pública como /login o /register, sin token auth
 */
export async function apiRequestPublicResult<T>(url: string, options: any = {}): Promise<ApiResult<T>> {
  try {
    const data = await $fetch<T>(url, { ...options });
    return { ok: true, data };
  } catch (err: any) {
    const { status, pd } = extractProblemDetail(err);
    return { ok: false, status, pd, raw: err };
  }
}

/**
 * Llamadas a API con autenticación necesaria
 */
export async function apiRequestAuthResult<T>(url: string, options: any = {}): Promise<ApiResult<T>> {
  const auth = useAuth();

  try {
    const data = await $fetch<T>(url, {
      ...options,
      headers: {
        ...(options.headers ?? {}),
        ...(auth.token.value ? { Authorization: `Bearer ${auth.token.value}` } : {}),
      },
    });
    return { ok: true, data };
  } catch (err: any) {
    const { status, pd } = extractProblemDetail(err);

    if (status === 401) {
      await auth.logout();
    }

    return { ok: false, status, pd, raw: err };
  }
}

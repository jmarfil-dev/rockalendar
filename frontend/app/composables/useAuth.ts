import type { AuthTokenResponse, LoginRequest } from "~/types/auth";
import type { ApiResult } from "~/types/api";
import { apiRequestPublicResult } from "~/composables/api/apiRequest";

const STORAGE_TOKEN = "rockalendar_token";
const STORAGE_EXPIRES = "rockalendar_token_expires_at";

function parseExpiresAt(expiresAt: string | null): number | null {
  if (!expiresAt) return null;
  const ms = Date.parse(expiresAt);
  return Number.isFinite(ms) ? ms : null;
}

export function useAuth() {
  const token = useState<string | null>("auth:token", () => null);
  const expiresAtMs = useState<number | null>("auth:expiresAtMs", () => null);

  const isAuthenticated = computed(() => {
    if (!token.value || !expiresAtMs.value) return false;
    return Date.now() < expiresAtMs.value;
  });

  function loadFromStorage() {
    if (!import.meta.client) return;
    token.value = localStorage.getItem(STORAGE_TOKEN);
    expiresAtMs.value = parseExpiresAt(localStorage.getItem(STORAGE_EXPIRES));
  }

  function saveToStorage(t: string | null, exp: number | null) {
    if (!import.meta.client) return;

    if (t) localStorage.setItem(STORAGE_TOKEN, t);
    else localStorage.removeItem(STORAGE_TOKEN);

    if (exp) localStorage.setItem(STORAGE_EXPIRES, new Date(exp).toISOString());
    else localStorage.removeItem(STORAGE_EXPIRES);
  }

  function setSession(res: AuthTokenResponse) {
    token.value = res.accessToken;
    expiresAtMs.value = parseExpiresAt(res.expiresAt);
    saveToStorage(token.value, expiresAtMs.value);
  }

  async function logout() {
    token.value = null;
    expiresAtMs.value = null;
    saveToStorage(null, null);
    await navigateTo("/login");
  }

  async function login(req: LoginRequest): Promise<ApiResult<AuthTokenResponse>> {
    const res = await apiRequestPublicResult<AuthTokenResponse>("/api/auth/login", {
      method: "POST",
      body: req,
    });
    if (res.ok) setSession(res.data);
    return res;
  }

  async function register(req: LoginRequest): Promise<ApiResult<AuthTokenResponse>> {
    const res = await apiRequestPublicResult<AuthTokenResponse>("/api/auth/register", {
      method: "POST",
      body: req,
    });
    if (res.ok) setSession(res.data);
    return res;
  }

  return {
    token,
    expiresAtMs,
    isAuthenticated,
    loadFromStorage,
    setSession,
    login,
    register,
    logout,
  };
}

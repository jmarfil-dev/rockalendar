import {
  broadcastAuthEvent,
  parseExpiresAt,
  getExpiresAtMsFromToken,
  writeAuthToStorage,
  readAuthFromStorage,
} from "~/utils/authStorage";
import { ROUTES } from "~/constants/routes";
import type { AuthTokenResponse, LoginRequest } from "~/types/auth";
import type { ApiResult } from "~/types/api";
import type { Role } from "~/types/user-roles";
import { decodeJwtPayload, extractRoles } from "~/utils/jwt";

export function useAuth() {
  // Estado global, compartido en toda la app
  const token = useState<string | null>("auth:token", () => null);
  const expiresAtMs = useState<number | null>("auth:expiresAtMs", () => null);

  // Derivados del token: se recalculan (no se guardan en storage)
  const jwtPayload = computed(() => (token.value ? decodeJwtPayload(token.value) : null));
  const roles = computed<Role[]>(() => extractRoles(jwtPayload.value));

  // Datos para guardar algo visible (username/email) para UI:
  const user = computed(() => {
    const p = jwtPayload.value;
    if (!p) return null;

    return {
      sub: typeof p.sub === "string" ? p.sub : null,
      email:
        // Si en el futuro ponemos username o similar, agregarlo aquí
        (typeof p.email === "string" && p.email) || null,
    };
  });

  // Sesión válida si hay token y no ha expirado
  const isAuthenticated = computed(() => {
    if (!token.value || !expiresAtMs.value) return false;
    return Date.now() < expiresAtMs.value;
  });

  const isModerator = computed(() => roles.value.includes("ROLE_MODERATOR") || roles.value.includes("ROLE_ADMIN"));
  const isAdmin = computed(() => roles.value.includes("ROLE_ADMIN"));

  function saveToStorage(t: string | null, exp: number | null) {
    writeAuthToStorage(t, exp);
  }

  /**
   * Limpia estado + storage.
   */
  function clearSession() {
    token.value = null;
    expiresAtMs.value = null;
    saveToStorage(null, null);
  }

  /**
   * Establece sesión desde respuesta del backend.
   * Prioridad: expiresAt del backend. Fallback: claim exp del JWT.
   */
  function setSession(res: AuthTokenResponse) {
    token.value = res.accessToken;

    // Backend decide el tiempo
    let expMs = parseExpiresAt(res.expiresAt);

    // Si no se puede leer de back, usa el claim exp de JWT
    if (!expMs) {
      expMs = getExpiresAtMsFromToken(res.accessToken);
    }

    expiresAtMs.value = expMs;

    // Si no se puede determinar expiración, mejor limpiar para evitar estados raros
    if (!expiresAtMs.value) {
      clearSession();
      return;
    }

    saveToStorage(token.value, expiresAtMs.value);
    broadcastAuthEvent("login");
  }

  /**
   * Carga desde localStorage al iniciar (solo cliente).
   * - Si falta expires, intenta extraerlo del JWT.
   * - Si está expirado, limpia.
   */
  function loadFromStorage() {
    const { token: t, expiresAtMs: expMs } = readAuthFromStorage();

    token.value = t;
    expiresAtMs.value = expMs;

    // Si hay token pero no expires, intenta sacarlo del jwt
    if (token.value && !expiresAtMs.value) {
      expiresAtMs.value = getExpiresAtMsFromToken(token.value);
      saveToStorage(token.value, expiresAtMs.value);
    }

    // Si ya está expirado, se limpia
    if (token.value && expiresAtMs.value && Date.now() >= expiresAtMs.value) {
      clearSession();
    }
  }

  async function logout() {
    clearSession();
    broadcastAuthEvent("logout");
    await navigateTo(ROUTES.home);
  }

  async function login(req: LoginRequest): Promise<ApiResult<AuthTokenResponse>> {
    const res = await fetchPublicResult<AuthTokenResponse>(ROUTES.apiLogin, {
      method: "POST",
      body: req,
    });

    if (res.ok) setSession(res.data); // Crea sesión si login va bien
    return res;
  }

  async function register(req: LoginRequest): Promise<ApiResult<AuthTokenResponse>> {
    const res = await fetchPublicResult<AuthTokenResponse>(ROUTES.apiRegister, {
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
    isModerator,
    isAdmin,
    roles,
    user,

    loadFromStorage,
    setSession,
    clearSession,

    login,
    register,
    logout,
  };
}

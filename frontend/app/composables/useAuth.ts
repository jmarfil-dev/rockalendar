import {
  broadcastAuthEvent,
  parseExpiresAt,
  writeSessionToStorage,
  readSessionFromStorage,
} from "~/utils/authStorage";
import { ROUTES } from "~/constants/routes";
import type { AuthSessionResponse, LoginRequest, RegisterRequest } from "~/types/auth";
import type { ApiResult } from "~/types/api";
import type { Role } from "~/types/user-roles";
import type { MeDto } from "~/types/user";

// Clave compartida con useMe para evitar doble llamada al montar páginas tras login
export const ME_STATE_KEY = "auth:me";

export function useAuth() {
  // Estado global, compartido en toda la app
  const userId = useState<string | null>("auth:userId", () => null);
  const email = useState<string | null>("auth:email", () => null);
  const role = useState<string | null>("auth:role", () => null);
  const expiresAtMs = useState<number | null>("auth:expiresAtMs", () => null);

  // MeDto completo compartido con useMe para evitar doble petición al montar páginas tras login
  const cachedMe = useState<MeDto | null>(ME_STATE_KEY, () => null);

  // role en BD es "USER" / "MODERATOR" / "ADMIN" (sin prefijo ROLE_)
  const roles = computed<Role[]>(() => (role.value ? [`ROLE_${role.value}` as Role] : []));

  const user = computed(() => {
    if (!userId.value) return null;
    return { sub: userId.value, email: email.value };
  });

  const isAuthenticated = computed(() => {
    if (!expiresAtMs.value) return false;
    return Date.now() < expiresAtMs.value;
  });

  const isModerator = computed(() => role.value === "MODERATOR" || role.value === "ADMIN");
  const isAdmin = computed(() => role.value === "ADMIN");

  function setSession(expiresAt: string, me: MeDto) {
    const ms = parseExpiresAt(expiresAt);
    if (!ms) return;

    userId.value = me.id;
    email.value = me.email;
    role.value = me.role;
    expiresAtMs.value = ms;
    cachedMe.value = me;

    writeSessionToStorage({ userId: me.id, email: me.email, role: me.role, expiresAtMs: ms });
  }

  function updateExpiry(expiresAt: string) {
    const ms = parseExpiresAt(expiresAt);
    if (!ms) return;
    expiresAtMs.value = ms;
    const session = readSessionFromStorage();
    if (session) writeSessionToStorage({ ...session, expiresAtMs: ms });
  }

  function clearSession() {
    userId.value = null;
    email.value = null;
    role.value = null;
    expiresAtMs.value = null;
    cachedMe.value = null;
    writeSessionToStorage(null);
  }

  function loadFromStorage() {
    const session = readSessionFromStorage();
    if (!session) return;

    if (Date.now() >= session.expiresAtMs) {
      writeSessionToStorage(null);
      return;
    }

    userId.value = session.userId;
    email.value = session.email;
    role.value = session.role;
    expiresAtMs.value = session.expiresAtMs;
  }

  async function logout() {
    await fetchPublicResult(ROUTES.apiLogout, { method: "POST" });
    clearSession();
    broadcastAuthEvent("logout");
    await navigateTo(ROUTES.home);
  }

  async function login(req: LoginRequest): Promise<ApiResult<void>> {
    const loginRes = await fetchPublicResult<AuthSessionResponse>(ROUTES.apiLogin, {
      method: "POST",
      body: req,
    });

    if (!loginRes.ok) return { ok: false, status: loginRes.status, pd: loginRes.pd, raw: loginRes.raw };

    const meRes = await fetchAuthResult<MeDto>(ROUTES.apiMe);
    if (meRes.ok) {
      setSession(loginRes.data.expiresAt, meRes.data);
      broadcastAuthEvent("login");
    }

    return { ok: true, data: undefined };
  }

  async function register(req: RegisterRequest): Promise<ApiResult<void>> {
    const registerRes = await fetchPublicResult<AuthSessionResponse>(ROUTES.apiRegister, {
      method: "POST",
      body: req,
    });

    if (!registerRes.ok) return { ok: false, status: registerRes.status, pd: registerRes.pd, raw: registerRes.raw };

    const meRes = await fetchAuthResult<MeDto>(ROUTES.apiMe);
    if (meRes.ok) {
      setSession(registerRes.data.expiresAt, meRes.data);
      broadcastAuthEvent("login");
    }

    return { ok: true, data: undefined };
  }

  return {
    expiresAtMs,
    isAuthenticated,
    isModerator,
    isAdmin,
    roles,
    user,

    loadFromStorage,
    updateExpiry,
    clearSession,

    login,
    register,
    logout,
  };
}

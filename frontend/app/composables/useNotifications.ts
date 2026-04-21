import { ROUTES } from "~/constants/routes";
import type { ComputedRef } from "vue";

const POLLING_INTERVAL_MS = 5 * 60 * 1000;

export interface UnreadCount {
  user: number;
  moderation: number;
  admin: number;
}

export function useNotifications() {
  const unreadCount = useState<UnreadCount>("notifications:unreadCount", () => ({ user: 0, moderation: 0, admin: 0 }));

  async function fetchUnreadCount() {
    const res = await fetchAuthResult<UnreadCount>(ROUTES.apiNotificationsUnreadCount);
    if (res.ok) {
      unreadCount.value = res.data;
    }
  }

  async function markAllRead() {
    const res = await fetchAuthResult(ROUTES.apiNotificationsReadAll, { method: "POST" });
    if (res.ok) {
      unreadCount.value = { user: 0, moderation: 0, admin: 0 };
    }
  }

  function initPolling(isAuthenticated: ComputedRef<boolean>) {
    const intervalId = setInterval(() => {
      if (isAuthenticated.value) fetchUnreadCount();
    }, POLLING_INTERVAL_MS);

    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible" && isAuthenticated.value) {
        fetchUnreadCount();
      }
    });

    window.addEventListener("beforeunload", () => clearInterval(intervalId), { once: true });
  }

  return { unreadCount, fetchUnreadCount, markAllRead, initPolling };
}

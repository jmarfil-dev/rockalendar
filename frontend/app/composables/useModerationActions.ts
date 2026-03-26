import type { ModerationApproveRequest, ModerationArchiveRequest } from "~/types/events";
import { ROUTE_PATH } from "~/constants/routes";

export function useModerationActions() {
  const { t } = useI18n();
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function approve(eventId: string, comment?: string): Promise<boolean> {
    loading.value = true;
    error.value = null;
    const body: ModerationApproveRequest = comment ? { comment } : {};
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationApprove(eventId), {
      method: "POST",
      body,
    });
    loading.value = false;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  async function reject(eventId: string, reason: string): Promise<boolean> {
    loading.value = true;
    error.value = null;
    const body: ModerationArchiveRequest = { reason };
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationReject(eventId), {
      method: "POST",
      body,
    });
    loading.value = false;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  async function hide(eventId: string, reason: string): Promise<boolean> {
    loading.value = true;
    error.value = null;
    const body: ModerationArchiveRequest = { reason };
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationHide(eventId), {
      method: "POST",
      body,
    });
    loading.value = false;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  async function requestChanges(eventId: string, reason: string): Promise<boolean> {
    loading.value = true;
    error.value = null;
    const body: ModerationArchiveRequest = { reason };
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationRequestChanges(eventId), {
      method: "POST",
      body,
    });
    loading.value = false;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  return { loading, error, approve, reject, hide, requestChanges };
}

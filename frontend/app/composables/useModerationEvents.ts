import type { PageResponse } from "~/types/pagination";
import type { ModerationTab, ModerationPendingListItem, ModerationApprovedListItem, ModerationArchivedListItem } from "~/types/events";
import { ROUTES } from "~/constants/routes";

export function useModerationEvents() {
  const { t } = useI18n();
  const pendingEvents = ref<ModerationPendingListItem[]>([]);
  const approvedEvents = ref<ModerationApprovedListItem[]>([]);
  const archivedEvents = ref<ModerationArchivedListItem[]>([]);
  const pageMeta = ref<{ size: number; number: number; totalElements: number; totalPages: number } | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function fetchEvents(tab: ModerationTab, page = 0, size = 20, sort?: string) {
    loading.value = true;
    error.value = null;

    const query: Record<string, string> = { page: String(page), size: String(size) };
    if (sort) query.sort = sort;
    const params = new URLSearchParams(query);

    if (tab === "PENDING") {
      const url = `${ROUTES.apiModerationPending}?${params.toString()}`;
      const res = await fetchAuthResult<PageResponse<ModerationPendingListItem>>(url);
      loading.value = false;
      if (res.ok) {
        pendingEvents.value = res.data.content;
        pageMeta.value = res.data.page;
      } else {
        error.value = extractApiError(res, t);
      }
    } else if (tab === "APPROVED") {
      const url = `${ROUTES.apiModerationApproved}?${params.toString()}`;
      const res = await fetchAuthResult<PageResponse<ModerationApprovedListItem>>(url);
      loading.value = false;
      if (res.ok) {
        approvedEvents.value = res.data.content;
        pageMeta.value = res.data.page;
      } else {
        error.value = extractApiError(res, t);
      }
    } else {
      const url = `${ROUTES.apiModerationArchived}?${params.toString()}`;
      const res = await fetchAuthResult<PageResponse<ModerationArchivedListItem>>(url);
      loading.value = false;
      if (res.ok) {
        archivedEvents.value = res.data.content;
        pageMeta.value = res.data.page;
      } else {
        error.value = extractApiError(res, t);
      }
    }
  }

  return { pendingEvents, approvedEvents, archivedEvents, pageMeta, loading, error, fetchEvents };
}

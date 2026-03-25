import type { PageResponse } from "~/types/pagination";
import type { EventPrivateListItem, MeEventTab } from "~/types/events";
import { ROUTES, ROUTE_PATH } from "~/constants/routes";

export function useMyEvents() {
  const { t } = useI18n();
  const events = ref<EventPrivateListItem[]>([]);
  const pageMeta = ref<{ size: number; number: number; totalElements: number; totalPages: number } | null>(null);
  const loading = ref(false);
  const deleting = ref<string | null>(null);
  const error = ref<string | null>(null);

  async function fetchEvents(tab: MeEventTab, page = 0, size = 20, sort?: string) {
    loading.value = true;
    error.value = null;

    const query: Record<string, string> = { tab, page: String(page), size: String(size) };
    if (sort) query.sort = sort;
    const params = new URLSearchParams(query);
    const res = await fetchAuthResult<PageResponse<EventPrivateListItem>>(
      `${ROUTES.apiMeEvents}?${params.toString()}`,
    );

    loading.value = false;

    if (res.ok) {
      events.value = res.data.content;
      pageMeta.value = res.data.page;
    } else {
      error.value = extractApiError(res, t);
    }
  }

  async function deleteEvent(id: string): Promise<boolean> {
    deleting.value = id;
    error.value = null;
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiMeEventDetail(id), { method: "DELETE" });
    deleting.value = null;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  return { events, pageMeta, loading, deleting, error, fetchEvents, deleteEvent };
}

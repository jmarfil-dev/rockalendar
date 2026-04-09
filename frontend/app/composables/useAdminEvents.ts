import type { PageMeta, PageResponse } from "~/types/pagination";
import type { AdminEventListItem, EventStatus } from "~/types/events";
import { ROUTES } from "~/constants/routes";

export function useAdminEvents() {
  const { t } = useI18n();
  const events = ref<AdminEventListItem[]>([]);
  const pageMeta = ref<PageMeta | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function fetchEvents(
    statuses: EventStatus[],
    q: string,
    provinceId: number | null | undefined,
    dateFrom: Date | null,
    dateTo: Date | null,
    page: number,
    size: number,
    sort: string,
  ) {
    loading.value = true;
    error.value = null;

    const params = new URLSearchParams({ page: String(page), size: String(size), sort });
    statuses.forEach((s) => params.append("statuses", s));
    if (q.trim()) params.set("q", q.trim());
    // provinceId puede llegar como undefined cuando el Select se limpia
    if (provinceId != null) params.set("provinceId", String(provinceId));
    if (dateFrom) params.set("dateFrom", dateFrom.toISOString());
    if (dateTo) params.set("dateTo", dateTo.toISOString());

    const res = await fetchAuthResult<PageResponse<AdminEventListItem>>(
      `${ROUTES.apiAdminEvents}?${params.toString()}`,
    );

    loading.value = false;
    if (res.ok) {
      events.value = res.data.content;
      pageMeta.value = res.data.page;
    } else {
      error.value = extractApiError(res, t);
    }
  }

  return { events, pageMeta, loading, error, fetchEvents };
}

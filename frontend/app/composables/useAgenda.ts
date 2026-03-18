import type { AgendaItem, InteractionStatus } from "~/types/events";
import { ROUTES, ROUTE_PATH } from "~/constants/routes";

export function useAgenda() {
  const { t } = useI18n();
  const items = ref<AgendaItem[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const saving = ref<string | null>(null);

  async function fetchAgenda() {
    loading.value = true;
    error.value = null;

    const res = await fetchAuthResult<AgendaItem[]>(ROUTES.apiMeAgenda);

    loading.value = false;

    if (res.ok) {
      items.value = res.data;
    } else {
      error.value = extractApiError(res, t);
    }
  }

  async function setInteraction(eventId: string, status: InteractionStatus): Promise<boolean> {
    saving.value = eventId;
    error.value = null;

    const res = await fetchAuthResult<AgendaItem>(ROUTE_PATH.apiMeAgendaItem(eventId), {
      method: "PUT",
      body: { status },
    });

    saving.value = null;

    if (res.ok) {
      const idx = items.value.findIndex((i) => i.eventId === eventId);
      if (idx >= 0) {
        items.value[idx] = res.data;
      } else {
        items.value.push(res.data);
      }
      return true;
    }

    error.value = extractApiError(res, t);
    return false;
  }

  async function removeInteraction(eventId: string): Promise<boolean> {
    saving.value = eventId;
    error.value = null;

    const res = await fetchAuthResult<void>(ROUTE_PATH.apiMeAgendaItem(eventId), { method: "DELETE" });

    saving.value = null;

    if (res.ok) {
      items.value = items.value.filter((i) => i.eventId !== eventId);
      return true;
    }

    error.value = extractApiError(res, t);
    return false;
  }

  function getInteraction(eventId: string): InteractionStatus | null {
    return items.value.find((i) => i.eventId === eventId)?.status ?? null;
  }

  return { items, loading, saving, error, fetchAgenda, setInteraction, removeInteraction, getInteraction };
}

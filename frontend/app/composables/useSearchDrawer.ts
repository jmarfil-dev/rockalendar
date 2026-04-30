import { ROUTES } from "~/constants/routes";
import type { LocationQuery } from "vue-router";
import type { Artist } from "~/types/artist";

function toMidnightOffsetString(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}T00:00:00Z`;
}

function toEndOfDayOffsetString(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}T23:59:59Z`;
}

function getQueryString(q: LocationQuery, key: string): string {
  const v = q[key];
  if (typeof v === "string") return v;
  if (Array.isArray(v)) return typeof v[0] === "string" ? v[0] : "";
  return "";
}

function parseDateOnly(s: string): Date | null {
  const m = /^(\d{4})-(\d{2})-(\d{2})T/.exec(s);
  if (!m) return null;
  const y = Number(m[1]);
  const mo = Number(m[2]) - 1;
  const d = Number(m[3]);
  return new Date(y, mo, d, 0, 0, 0);
}

export function useSearchDrawer() {
  const isOpen = useState<boolean>("searchDrawer:isOpen", () => false);

  const route = useRoute();

  const searchForm = useState("searchDrawer:form", () => ({
    artistId: null as string | null,
    provinceId: null as number | null,
    dateFrom: null as Date | null,
    dateTo: null as Date | null,
    city: "",
    query: "",
  }));

  const { options: provinceOptions, load: loadProvinces } = useProvinces();

  const selectedArtist = useState<Artist | null>("searchDrawer:selectedArtist", () => null);
  const { suggestions: artistSuggestions, loading: artistLoading, search: searchArtists } = useArtistAutocomplete();

  const isDateRangeInvalid = computed(() => {
    const { dateFrom, dateTo } = searchForm.value;
    return !!(dateFrom && dateTo && dateFrom > dateTo);
  });

  function open() {
    isOpen.value = true;
  }
  function close() {
    isOpen.value = false;
  }

  function runSearch() {
    if (isDateRangeInvalid.value) return;

    const nextQuery: Record<string, string | undefined> = { ...route.query } as Record<string, string | undefined>;

    // Al buscar, volvemos a page 0
    nextQuery.page = "0";

    nextQuery.artistId = searchForm.value.artistId || undefined;
    nextQuery.provinceId = searchForm.value.provinceId != null ? String(searchForm.value.provinceId) : undefined;
    nextQuery.city = searchForm.value.city?.trim() || undefined;
    nextQuery.query = searchForm.value.query?.trim() || undefined;

    nextQuery.dateFrom = searchForm.value.dateFrom ? toMidnightOffsetString(searchForm.value.dateFrom) : undefined;
    nextQuery.dateTo = searchForm.value.dateTo ? toEndOfDayOffsetString(searchForm.value.dateTo) : undefined;

    isOpen.value = false;

    navigateTo({
      path: ROUTES.events,
      query: nextQuery,
    });
  }

  /**
   * Cada vez que cambia la URL, se reflejan esos valores en el formulario del Drawer.
   * Esto hace que al abrir el Drawer siempre se vean los filtros reales.
   */
  watch(
    () => route.query,
    async (q) => {
      const artistId = getQueryString(q, "artistId") || null;

      if (artistId) {
        if (!selectedArtist.value || selectedArtist.value.id !== artistId) {
          const a = await fetchArtistById(artistId);
          selectedArtist.value = a ?? { id: artistId, name: "" };
        }
      } else {
        selectedArtist.value = null;
      }

      searchForm.value.artistId = artistId;
      const pId = getQueryString(q, "provinceId");
      searchForm.value.provinceId = pId ? Number(pId) : null;
      searchForm.value.city = getQueryString(q, "city");

      const from = getQueryString(q, "dateFrom");
      const to = getQueryString(q, "dateTo");
      searchForm.value.dateFrom = from ? parseDateOnly(from) : null;
      searchForm.value.dateTo = to ? parseDateOnly(to) : null;

      searchForm.value.query = getQueryString(q, "query");
    },
    { immediate: true },
  );

  watch(
    selectedArtist,
    (a) => {
      searchForm.value.artistId = a?.id ?? null;
    },
    { immediate: true },
  );

  // Carga provincias solo cuando se abre
  watch(isOpen, async (open) => {
    if (open) await loadProvinces();
  });

  return {
    // UI
    isOpen,
    open,
    close,

    // Formulario
    searchForm,
    selectedArtist,
    provinceOptions,
    artistSuggestions,
    artistLoading,
    isDateRangeInvalid,

    // Acciones
    searchArtists,
    runSearch,
  };
}

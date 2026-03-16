<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { EventPublicListItem } from "~/types/events";
import type { PageResponse } from "~/types/pagination";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

const homeEndpoint = ROUTES.apiHome; // Por ahora usamos proxy para evitar CORS
const searchEndpoint = ROUTES.apiEvents;

const sortOptions = useSortOptions(["date", "title", "province", "city"]);

// Detectar si estamos en modo búsqueda (por ruta /events o por query params de filtros)
// Nota: esto permite que el componente se use tanto en / como en /events con el mismo comportamiento.
const isSearchRoute = computed(() => route.path === ROUTES.events);

const hasSearchFilters = computed(() => {
  const q = route.query;
  const artistId = typeof q.artistId === "string" ? q.artistId.trim() : "";
  const city = typeof q.city === "string" ? q.city.trim() : "";
  const provinceId = typeof q.provinceId === "string" ? q.provinceId : "";
  const dateFrom = typeof q.dateFrom === "string" ? q.dateFrom : "";
  const dateTo = typeof q.dateTo === "string" ? q.dateTo : "";
  const query = typeof q.query === "string" ? q.query.trim() : "";
  return !!(artistId || city || provinceId || dateFrom || dateTo || query);
});

const endpoint = computed(() => (isSearchRoute.value || hasSearchFilters.value ? searchEndpoint : homeEndpoint));

// Estado desde URL (SSR-safe)
const page = computed<number>({
  get: () => {
    const v = route.query.page;
    const n = typeof v === "string" ? parseInt(v, 10) : 0;
    return Number.isFinite(n) && n >= 0 ? n : 0;
  },
  set: (v) => {
    router.replace({ query: { ...route.query, page: String(v) } });
  },
});

const size = computed<number>({
  get: () => {
    const v = route.query.size;
    const n = typeof v === "string" ? parseInt(v, 10) : 20;
    return Number.isFinite(n) && n > 0 ? n : 20;
  },
  set: (v) => {
    // Al cambiar size, volvemos a page 0
    router.replace({ query: { ...route.query, size: String(v), page: "0" } });
  },
});

const sort = computed<string>({
  get: () => (typeof route.query.sort === "string" ? route.query.sort : "date"),
  set: (v) => {
    // Al cambiar orden, volvemos a page 0
    router.replace({ query: { ...route.query, sort: v, page: "0" } });
  },
});

// Paginator trabaja con "first" (offset)
const first = computed<number>({
  get: () => page.value * size.value,
  set: (v) => {
    page.value = Math.floor(v / size.value);
  },
});

// Fetch
const { data, pending, error } = await useApiFetch<PageResponse<EventPublicListItem>>(endpoint, {
  query: computed(() => {
    // Params comunes
    const base: Record<string, any> = {
      page: page.value,
      size: size.value,
      sort: sort.value,
    };

    // Params de búsqueda (solo cuando estemos en búsqueda o haya filtros)
    if (endpoint.value === searchEndpoint) {
      const q = route.query;

      const artistId = typeof q.artistId === "string" ? q.artistId.trim() : "";
      const city = typeof q.city === "string" ? q.city.trim() : "";
      const provinceId = typeof q.provinceId === "string" ? q.provinceId : "";
      const dateFrom = typeof q.dateFrom === "string" ? q.dateFrom : "";
      const dateTo = typeof q.dateTo === "string" ? q.dateTo : "";
      const free = typeof q.query === "string" ? q.query.trim() : "";

      if (artistId) base.artistId = artistId;
      if (city) base.city = city;
      if (provinceId) base.provinceId = provinceId;
      if (dateFrom) base.dateFrom = dateFrom;
      if (dateTo) base.dateTo = dateTo;
      if (free) base.query = free;
    }

    return base;
  }),
  // importante: que refetchee si cambia endpoint (home/search) o cambian query params
  watch: [endpoint, () => route.query],
});

const events = computed(() => data.value?.content ?? []);
const total = computed(() => data.value?.page.totalElements ?? 0);

// Cuando el usuario pagine/cambie rows
const onPage = (e: { page: number; first: number; rows: number }) => {
  // Esto actualiza URL vía setters => refetch auto
  page.value = e.page;
  if (e.rows !== size.value) size.value = e.rows; // esto ya resetea page=0 por setter; pero si rows cambia, prima el setter
};
</script>

<template>
  <article class="flex flex-column gap-3">
    <!-- Barra superior: ordenar -->
    <header>
      <div class="flex align-items-center justify-content-between w-full">
        <span class="text-sm text-color-secondary">{{ t("pagination.sortedBy") }}</span>
        <Select v-model="sort" :options="sortOptions" optionLabel="label" optionValue="value" class="w-10rem" />
      </div>
    </header>

    <!-- Results -->
    <section :aria-label="t('events.listEvents')">
      <!-- Loading -->
      <div v-if="pending" class="flex justify-content-center p-4">
        <ProgressSpinner />
      </div>

      <!-- Listado vacío -->
      <Message v-else-if="events.length === 0" severity="info" :closable="false">{{ t("events.noResults") }}</Message>

      <!-- Listado -->
      <div v-else class="grid">
        <div v-for="ev in events" :key="ev.id" class="col-12 md:col-6 lg:col-4">
          <Card
            class="h-full border-1 surface-50 surface-border cursor-pointer"
            role="link"
            tabindex="0"
            @click="navigateTo(ROUTE_PATH.eventDetail(ev.id))"
            @keydown.enter.prevent="navigateTo(ROUTE_PATH.eventDetail(ev.id))"
            @keydown.space.prevent="navigateTo(ROUTE_PATH.eventDetail(ev.id))">
            <template #title>
              <span class="text-color-primary">{{ ev.title }}</span>
            </template>

            <template #content>
              <div class="text-color-secondary text-sm flex flex-column gap-2">
                <div>
                  <i class="pi pi-calendar mr-2"></i>
                  <time :datetime="ev.startDateTime">
                    {{ formatEventDate(ev.startDateTime) }}
                  </time>
                  <span v-if="ev.endDateTime">
                    &nbsp;>>&nbsp;
                    <time :datetime="ev.endDateTime">
                      {{ formatEventDate(ev.endDateTime) }}
                    </time>
                  </span>
                </div>

                <div>
                  <i class="pi pi-compass mr-2"></i>
                  {{ ev.cityName }}<span v-if="ev.cityName && ev.provinceName">, </span>{{ ev.provinceName }}
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </section>

    <!-- Paginación -->
    <AppPaginator
      v-if="!pending && !error && total > 0"
      :first="first"
      :rows="size"
      :totalRecords="total"
      @page="onPage" />
  </article>
</template>

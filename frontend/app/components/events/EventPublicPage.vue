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

function buildSearchParams(q: typeof route.query): Record<string, string> {
  const params: Record<string, string> = {};
  const artistId = typeof q.artistId === "string" ? q.artistId.trim() : "";
  const city = typeof q.city === "string" ? q.city.trim() : "";
  const provinceId = typeof q.provinceId === "string" ? q.provinceId : "";
  const dateFrom = typeof q.dateFrom === "string" ? q.dateFrom : "";
  const dateTo = typeof q.dateTo === "string" ? q.dateTo : "";
  const free = typeof q.query === "string" ? q.query.trim() : "";

  if (artistId) params.artistId = artistId;
  if (city) params.city = city;
  if (provinceId) params.provinceId = provinceId;
  if (dateFrom) params.dateFrom = dateFrom;
  if (dateTo) params.dateTo = dateTo;
  if (free) params.query = free;

  return params;
}

// Fetch
const { data, pending, error } = await useFetch<PageResponse<EventPublicListItem>>(endpoint, {
  query: computed(() => ({
    page: page.value,
    size: size.value,
    sort: sort.value,
    ...(endpoint.value === searchEndpoint ? buildSearchParams(route.query) : {}),
  })),
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
        <Select
          v-model="sort"
          :options="sortOptions"
          option-label="label"
          option-value="value"
          class="w-10rem"
          :pt="{ label: { 'aria-label': t('pagination.sortedBy') } }" />
      </div>
    </header>

    <!-- Results -->
    <section :aria-label="t('events.listEvents')">
      <!-- Loading -->
      <div v-if="pending" role="status" class="flex justify-content-center p-4">
        <ProgressSpinner />
        <span class="sr-only">{{ t("common.loading") }}</span>
      </div>

      <!-- Listado vacío -->
      <Message v-else-if="events.length === 0" severity="info" :closable="false">{{ t("events.noResults") }}</Message>

      <!-- Listado -->
      <div v-else class="grid">
        <div v-for="(ev, index) in events" :key="ev.id" class="col-12 md:col-6 lg:col-4">
          <NuxtLink :to="ROUTE_PATH.eventDetail(ev.id)" class="no-underline">
            <Card
              class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150"
              :pt="{ body: { style: 'padding: 0.625rem' }, title: { style: 'margin-bottom: 0.5rem' } }">
              <template #title>
                <span class="text-color-primary">{{ ev.title }}</span>
              </template>

              <template #content>
                <div class="flex gap-3" style="overflow: hidden; height: 100px">
                  <NuxtPicture
                    v-if="ev.posterUrl"
                    :src="ev.posterUrl"
                    :alt="ev.title"
                    :width="200"
                    sizes="200px"
                    format="avif,webp"
                    :loading="index === 0 ? 'eager' : 'lazy'"
                    style="width: 40%; flex-shrink: 0; height: 100%; display: block;"
                    :img-attrs="{
                      style: 'object-fit: cover; object-position: top; border-radius: 6px; width: 100%; height: 100%;',
                      fetchpriority: index === 0 ? 'high' : undefined
                    }" />
                  <div
                    v-else
                    class="border-1 surface-border border-round-lg surface-100 flex align-items-center justify-content-center text-center text-color-secondary"
                    style="width: 40%; flex-shrink: 0; height: 100%">
                    <div>
                      <i class="pi pi-image text-xl" />
                      <div class="text-xs mt-1">{{ t("events.noPoster") }}</div>
                    </div>
                  </div>
                  <div class="text-color-secondary text-sm flex flex-column gap-2">
                    <div class="flex flex-column gap-1">
                      <div>
                        <i class="pi pi-calendar mr-2" />
                        <time :datetime="ev.startDateTime">{{ formatEventDate(ev.startDateTime, ev.startTimeUnknown) }}</time>
                      </div>
                      <div v-if="ev.endDate" class="pl-4">
                        <span class="mr-1">→</span>
                        <time :datetime="ev.endDate">{{ formatEventEndDate(ev.endDate) }}</time>
                      </div>
                    </div>

                    <div>
                      <i class="pi pi-compass mr-2" />
                      {{ ev.cityName }}<span v-if="ev.cityName && ev.provinceName">, </span>{{ ev.provinceName }}
                    </div>
                  </div>
                </div>
              </template>
            </Card>
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- Paginación -->
    <AppPaginator
      v-if="!pending && !error && total > 0"
      :first="first"
      :rows="size"
      :total-records="total"
      @page="onPage" />
  </article>
</template>

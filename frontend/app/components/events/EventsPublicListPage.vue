<script setup lang="ts">
import Paginator from "primevue/paginator";
import Card from "primevue/card";
import ProgressSpinner from "primevue/progressspinner";
import Message from "primevue/message";

type EventPublicListItem = {
  // Mismos campos que EventPublicListItemDto
  id: string;
  title: string;
  startDateTime: string;
  endDateTime?: string;
  provinceName: string;
  cityName: string;
};

type PageMeta = {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
};

type PageResponse<T> = {
  content: T[];
  page: PageMeta;
};

type SortOption = { label: string; value: string };

const homeEndpoint = "/api/events/home"; // Por ahora usamos proxy para evitar CORS
const searchEndpoint = "/api/events";

const sortOptions: SortOption[] = [
  { label: "Fecha", value: "date,asc" },
  { label: "Título", value: "title,asc" },
  { label: "Provincia", value: "province,asc" },
  { label: "Ciudad", value: "city,asc" },
];

const route = useRoute();
const router = useRouter();

// Detectar si estamos en modo búsqueda (por ruta /events o por query params de filtros)
// Nota: esto permite que el componente se use tanto en / como en /events con el mismo comportamiento.
const isSearchRoute = computed(() => route.path === "/events");

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
  get: () => (typeof route.query.sort === "string" ? route.query.sort : "date,asc"),
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
const { data, pending, error, refresh } = await useFetch<PageResponse<EventPublicListItem>>(endpoint, {
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
        <span class="text-sm text-color-secondary">Ordenado por</span>
        <Select v-model="sort" :options="sortOptions" optionLabel="label" optionValue="value" class="w-10rem" />
      </div>
    </header>

    <!-- Results -->
    <section aria-label="Listado de eventos">
      <!-- Loading -->
      <div v-if="pending" class="flex justify-content-center p-4">
        <ProgressSpinner />
      </div>

      <Message v-else-if="error" severity="error">
        Error cargando eventos. Revisa que el backend esté levantado y CORS configurado.
        <div class="mt-2">
          <button class="p-button p-component p-button-text" @click="refresh()">Reintentar</button>
        </div>
      </Message>

      <!-- Listado vacío -->
      <Message v-else-if="events.length === 0" severity="info" :closable="false"> No se encontraron eventos. </Message>

      <!-- Listado -->
      <div v-else class="grid">
        <div v-for="ev in events" :key="ev.id" class="col-12 md:col-6 lg:col-4">
          <Card
            class="h-full border-1 surface-50 surface-border cursor-pointer"
            role="link"
            tabindex="0"
            @click="navigateTo(`/events/${ev.id}`)"
            @keydown.enter.prevent="navigateTo(`/events/${ev.id}`)"
            @keydown.space.prevent="navigateTo(`/events/${ev.id}`)">
            <template #title>
              <span class="text-color-primary">{{ ev.title }}</span>
            </template>

            <template #content>
              <div class="text-color-secondary text-sm flex flex-column gap-2">
                <div>
                  <i class="pi pi-calendar mr-2"></i>
                  <time :datetime="ev.startDateTime">
                    {{ new Date(ev.startDateTime).toLocaleString() }}
                  </time>
                  <span v-if="ev.endDateTime">
                    &nbsp;>>&nbsp;
                    <time :datetime="ev.endDateTime">
                      {{ new Date(ev.endDateTime).toLocaleString() }}
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
    <div v-if="!pending && !error && total > 0" class="border-1 border-round-xl p-2">
      <Paginator :first="first" :rows="size" :totalRecords="total" :rowsPerPageOptions="[20, 50, 100]" @page="onPage" />
    </div>
  </article>
</template>

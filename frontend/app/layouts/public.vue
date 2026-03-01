<script setup lang="ts">
const isSearchOpen = ref(false);
const route = useRoute();

const searchForm = ref({
  artist: "",
  city: "",
  provinceId: null as string | null,
  dateFrom: null as Date | null,
  dateTo: null as Date | null,
  query: "",
});

// TODO: cargar desde API
const provinces = ref([
  { id: "5fa896db-2248-5328-b3a5-9e1210ed71cc", name: "Madrid" },
  { id: "8b3deecc-e949-5299-8916-8400146fa19c", name: "Barcelona" },
  { id: "3e7e28d8-f5e8-5678-8370-a6d4fd8eff0a", name: "Valencia" },
]);

const isDateRangeInvalid = computed(() => {
  const { dateFrom: dateFrom, dateTo: dateTo } = searchForm.value;
  return !!(dateFrom && dateTo && dateFrom > dateTo);
});

const bottomItems = [
  {
    label: "Buscar",
    icon: "pi pi-search",
    text: true,
    action: () => {
      isSearchOpen.value = true;
    },
  },
];

function toMidnightOffsetString(date: Date): string {
  // "fecha local" a medianoche
  const localMidnight = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);

  const y = localMidnight.getFullYear();
  const m = String(localMidnight.getMonth() + 1).padStart(2, "0");
  const d = String(localMidnight.getDate()).padStart(2, "0");

  return `${y}-${m}-${d}T00:00:00Z`;
}

function runSearch() {
  if (isDateRangeInvalid.value) return;

  const nextQuery: Record<string, any> = { ...route.query };

  // Al buscar, volvemos a page 0
  nextQuery.page = "0";

  nextQuery.artist = searchForm.value.artist?.trim() || undefined;
  nextQuery.city = searchForm.value.city?.trim() || undefined;
  nextQuery.provinceId = searchForm.value.provinceId || undefined;
  nextQuery.query = searchForm.value.query?.trim() || undefined;

  nextQuery.dateFrom = searchForm.value.dateFrom ? toMidnightOffsetString(searchForm.value.dateFrom) : undefined;
  nextQuery.dateTo = searchForm.value.dateTo ? toMidnightOffsetString(searchForm.value.dateTo) : undefined;

  isSearchOpen.value = false;

  navigateTo({
    path: "/events",
    query: nextQuery,
  });
}

function getQueryString(q: any, key: string): string {
  const v = q[key];
  if (typeof v === "string") return v;
  if (Array.isArray(v)) return typeof v[0] === "string" ? v[0] : "";
  return "";
}

function parseDateOnly(s: string): Date | null {
  const m = /^(\d{4})-(\d{2})-(\d{2})T00:00:00Z$/.exec(s);
  if (!m) return null;
  const y = Number(m[1]);
  const mo = Number(m[2]) - 1;
  const d = Number(m[3]);
  return new Date(y, mo, d, 0, 0, 0);
}

// Cada vez que cambia la URL, reflejamos esos valores en el formulario del Drawer.
// Esto hace que al abrir el Drawer siempre veas los filtros reales.
watch(
  () => route.query,
  (q) => {
    searchForm.value.artist = getQueryString(q, "artist");
    searchForm.value.city = getQueryString(q, "city");
    searchForm.value.provinceId = getQueryString(q, "provinceId") || null;

    const from = getQueryString(q, "dateFrom");
    const to = getQueryString(q, "dateTo");
    searchForm.value.dateFrom = from ? parseDateOnly(from) : null;
    searchForm.value.dateTo = to ? parseDateOnly(to) : null;

    searchForm.value.query = getQueryString(q, "q");
  },
  { immediate: true },
);
</script>

<template>
  <AppShell :bottomItems="bottomItems">
    <!-- Page content -->
    <slot />

    <!-- Search sidebar -->
    <Drawer v-model:visible="isSearchOpen" position="right" header="Let's Rock!!" :style="{ width: '340px' }">
      <form class="flex flex-column gap-4 mt-2" @submit.prevent="runSearch">
        <!-- Artista -->
        <div class="flex flex-column gap-2">
          <label for="artist" class="text-sm text-color-secondary">Grupo o artista</label>
          <InputText id="artist" v-model="searchForm.artist" placeholder="Ej. Accidente" autocomplete="off" />
        </div>

        <!-- Ciudad -->
        <div class="flex flex-column gap-2">
          <label for="city" class="text-sm text-color-secondary">Ciudad</label>
          <InputText id="city" v-model="searchForm.city" placeholder="Ej. Barcelona" autocomplete="off" />
        </div>

        <!-- Provincia -->
        <div class="flex flex-column gap-2">
          <label for="province" class="text-sm text-color-secondary">Provincia</label>
          <Dropdown
            id="province"
            v-model="searchForm.provinceId"
            :options="provinces"
            optionLabel="name"
            optionValue="id"
            placeholder="Selecciona provincia"
            showClear />
        </div>

        <!-- Rango de fechas -->
        <div class="flex flex-column gap-2">
          <label for="dateFrom" class="text-sm text-color-secondary">Desde</label>
          <DatePicker
            id="dateFrom"
            v-model="searchForm.dateFrom"
            dateFormat="dd/mm/yy"
            showIcon
            iconDisplay="input"
            placeholder="Desde…" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="dateTo" class="text-sm text-color-secondary">Hasta</label>
          <DatePicker
            id="dateTo"
            v-model="searchForm.dateTo"
            :minDate="searchForm.dateFrom ?? undefined"
            dateFormat="dd/mm/yy"
            showIcon
            iconDisplay="input"
            placeholder="Hasta…" />
          <small v-if="isDateRangeInvalid" class="text-red-500">
            La fecha fin no puede ser anterior a la fecha inicio
          </small>
        </div>

        <!-- Query libre -->
        <div class="flex flex-column gap-2">
          <label for="query" class="text-sm text-color-secondary">Búsqueda</label>
          <InputText id="query" v-model="searchForm.query" placeholder="Grupo, sala, festival..." autocomplete="off" />
        </div>

        <!-- Botón -->
        <Button type="submit" label="Buscar" icon="pi pi-search" class="w-full" :disabled="isDateRangeInvalid" />
      </form>
    </Drawer>
  </AppShell>
</template>

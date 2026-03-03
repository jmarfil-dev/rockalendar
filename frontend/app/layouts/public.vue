<script setup lang="ts">
type Artist = { id: string; name: string };

const { t } = useI18n();
const isSearchOpen = ref(false);
const route = useRoute();

const searchForm = ref({
  artistId: null as string | null,
  provinceId: null as string | null,
  dateFrom: null as Date | null,
  dateTo: null as Date | null,
  city: "",
  query: "",
});

const { options: provinceOptions, load: loadProvinces } = useProvinces();

const selectedArtist = ref<Artist | null>(null);
const { suggestions: artistSuggestions, loading: artistLoading, search: searchArtists } = useArtistAutocomplete();

const isDateRangeInvalid = computed(() => {
  const { dateFrom: dateFrom, dateTo: dateTo } = searchForm.value;
  return !!(dateFrom && dateTo && dateFrom > dateTo);
});

const bottomItems = [
  {
    label: t("common.searchV"),
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

  nextQuery.artistId = searchForm.value.artistId || undefined;
  nextQuery.provinceId = searchForm.value.provinceId || undefined;
  nextQuery.city = searchForm.value.city?.trim() || undefined;
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
  async (q) => {
    const artistId = getQueryString(q, "artistId") || null;

    if (artistId) {
      if (!selectedArtist.value || selectedArtist.value.id !== artistId) {
        const { data } = await useApiFetch<Artist>(`/api/artists/${artistId}`, {
          key: `artist-${artistId}`,
        });
        selectedArtist.value = data.value ?? { id: artistId, name: "" };
      }
    } else {
      selectedArtist.value = null;
    }

    searchForm.value.artistId = artistId;
    searchForm.value.provinceId = getQueryString(q, "provinceId") || null;
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

// Carga provincias cacheadas en el dropdown
watch(isSearchOpen, async (open) => {
  if (open) await loadProvinces();
});
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
          <label for="artist" class="text-sm text-color-secondary"
            >{{ t("events.group") }} / {{ t("events.artist") }}</label
          >
          <AutoComplete
            v-model="selectedArtist"
            inputId="artist"
            :suggestions="artistSuggestions"
            :loading="artistLoading"
            :minLength="2"
            :maxLength="50"
            optionLabel="name"
            :placeholder="`${t('common.example')}. Elektroduendes`"
            inputClass="w-full"
            @complete="(e) => searchArtists(e.query)" />
        </div>

        <!-- Ciudad -->
        <div class="flex flex-column gap-2">
          <label for="city" class="text-sm text-color-secondary">{{ t("geo.city") }}</label>
          <InputText
            id="city"
            v-model="searchForm.city"
            :placeholder="`${t('common.example')}. Barcelona`"
            autocomplete="off" />
        </div>

        <!-- Provincia -->
        <div class="flex flex-column gap-2">
          <label for="province" class="text-sm text-color-secondary">{{ t("geo.province") }}</label>
          <Dropdown
            v-model="searchForm.provinceId"
            :options="provinceOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="t('geo.province')"
            showClear
            class="w-full" />
        </div>

        <!-- Rango de fechas -->
        <div class="flex flex-column gap-2">
          <label for="dateFrom" class="text-sm text-color-secondary">{{ t("dates.from") }}</label>
          <DatePicker
            id="dateFrom"
            v-model="searchForm.dateFrom"
            dateFormat="dd/mm/yy"
            showIcon
            iconDisplay="input"
            :placeholder="`${t('dates.from')}...`" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="dateTo" class="text-sm text-color-secondary">{{ t("dates.to") }}</label>
          <DatePicker
            id="dateTo"
            v-model="searchForm.dateTo"
            :minDate="searchForm.dateFrom ?? undefined"
            dateFormat="dd/mm/yy"
            showIcon
            iconDisplay="input"
            :placeholder="`${t('dates.to')}...`" />
          <small v-if="isDateRangeInvalid" class="text-red-500">
            {{ t("dates.invalidRange") }}
          </small>
        </div>

        <!-- Query libre -->
        <div class="flex flex-column gap-2">
          <label for="query" class="text-sm text-color-secondary">{{ t("common.searchN") }}</label>
          <InputText
            id="query"
            v-model="searchForm.query"
            :placeholder="t('events.searchPlaceholder')"
            autocomplete="off" />
        </div>

        <!-- Botón -->
        <Button
          type="submit"
          :label="t('common.searchV')"
          icon="pi pi-search"
          class="w-full"
          :disabled="isDateRangeInvalid" />
      </form>
    </Drawer>
  </AppShell>
</template>

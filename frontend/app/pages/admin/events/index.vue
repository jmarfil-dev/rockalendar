<script setup lang="ts">
import type { EventStatus } from "~/types/events";
import { ROUTES, ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "admin", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.adminEvents") });

const { events, pageMeta, loading, error, fetchEvents } = useAdminEvents();
const { options: provinceOptions, load: loadProvinces } = useProvinces();

// --- Filtros ---
const ALL_STATUSES: EventStatus[] = [
  "PENDING_MODERATION",
  "APPROVED",
  "REJECTED",
  "NEEDS_CHANGES",
  "HIDDEN",
  "CANCELED",
  "DRAFT",
  "ERASED",
];

const statusOptions = computed(() =>
  ALL_STATUSES.map((s) => ({ label: t(`me.eventStatus.${s}`), value: s })),
);

const filterStatuses = useState<EventStatus[]>("adminEvents:filterStatuses", () => []);
const filterQ = useState("adminEvents:filterQ", () => "");
const filterProvinceId = useState<number | null>("adminEvents:filterProvinceId", () => null);
const filterDateFrom = useState<Date | null>("adminEvents:filterDateFrom", () => null);
const filterDateTo = useState<Date | null>("adminEvents:filterDateTo", () => null);
const today = new Date();

// --- Paginación ---
const currentPage = useState("adminEvents:currentPage", () => 0);
const pageSize = useState("adminEvents:pageSize", () => 20);
const initialized = ref(false);

const total = computed(() => pageMeta.value?.totalElements ?? 0);
const first = computed(() => currentPage.value * pageSize.value);

// --- Ordenación via DataTable ---
const dtSortField = useState<string | null>("adminEvents:dtSortField", () => null);
const dtSortOrder = useState<number | null>("adminEvents:dtSortOrder", () => null);
const sortParam = useState("adminEvents:sortParam", () => "date,asc");

const FIELD_TO_SORT_KEY: Record<string, string> = {
  startDateTime: "date",
  provinceName: "province",
  title: "title",
  status: "status",
};

function onSort(e: { sortField: string; sortOrder: number | null }) {
  dtSortField.value = e.sortField;
  dtSortOrder.value = e.sortOrder;

  if (!e.sortField || !e.sortOrder) {
    sortParam.value = "date,asc";
  } else {
    const key = FIELD_TO_SORT_KEY[e.sortField] ?? "date";
    const dir = e.sortOrder === 1 ? "asc" : "desc";
    sortParam.value = `${key},${dir}`;
  }
  currentPage.value = 0;
  if (initialized.value) load();
}

// --- Status badge ---
const STATUS_SEVERITY: Record<EventStatus, string> = {
  PENDING_MODERATION: "warn",
  APPROVED: "success",
  REJECTED: "danger",
  NEEDS_CHANGES: "contrast",
  DRAFT: "secondary",
  HIDDEN: "secondary",
  CANCELED: "danger",
  ERASED: "danger",
};

// --- Fetch ---
async function load() {
  await fetchEvents(
    filterStatuses.value,
    filterQ.value,
    filterProvinceId.value,
    filterDateFrom.value,
    filterDateTo.value,
    currentPage.value,
    pageSize.value,
    sortParam.value,
  );
}

function onPageChange(e: { page: number; rows: number }) {
  if (e.rows !== pageSize.value) {
    pageSize.value = e.rows;
    currentPage.value = 0;
  } else {
    currentPage.value = e.page;
  }
}

onMounted(async () => {
  await Promise.all([load(), loadProvinces()]);
  initialized.value = true;
});

watch([filterStatuses, filterProvinceId, filterDateFrom, filterDateTo], () => {
  if (!initialized.value) return;
  currentPage.value = 0;
  load();
});

watch(currentPage, () => {
  if (initialized.value) load();
});

let qTimer: ReturnType<typeof setTimeout> | null = null;
watch(filterQ, () => {
  if (!initialized.value) return;
  if (qTimer) clearTimeout(qTimer);
  qTimer = setTimeout(() => {
    currentPage.value = 0;
    load();
  }, 400);
});
</script>

<template>
  <div class="flex flex-column gap-4">
    <!-- Cabecera -->
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.admin" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("admin.hub.events") }}</h1>
    </div>

    <!-- Filtros -->
    <div class="grid">
      <!-- Búsqueda por título -->
      <div class="col-12">
        <InputText
          v-model="filterQ"
          :placeholder="t('admin.events.filterQ')"
          class="w-full"
          autocomplete="off" />
      </div>

      <!-- Estado -->
      <div class="col-12 md:col-4">
        <MultiSelect
          v-model="filterStatuses"
          :options="statusOptions"
          option-label="label"
          option-value="value"
          :placeholder="t('admin.events.allStatuses')"
          display="chip"
          class="w-full"
          :max-selected-labels="2"
          :show-toggle-all="false" />
      </div>

      <!-- Provincia -->
      <div class="col-12 md:col-4">
        <Select
          v-model="filterProvinceId"
          :options="provinceOptions"
          option-label="label"
          option-value="value"
          :placeholder="t('geo.province')"
          show-clear
          filter
          class="w-full" />
      </div>

      <!-- Fecha desde -->
      <div class="col-6 md:col-2">
        <div class="flex align-items-center gap-1">
          <DatePicker
            v-model="filterDateFrom"
            :placeholder="t('dates.from')"
            :min-date="today"
            date-format="dd/mm/yy"
            :manual-input="false"
            show-icon
            icon-display="input"
            class="flex-1" />
          <Button
            v-if="filterDateFrom"
            type="button"
            icon="pi pi-times"
            severity="secondary"
            text
            rounded
            size="small"
            :aria-label="t('common.clearDate')"
            @click="filterDateFrom = null" />
        </div>
      </div>

      <!-- Fecha hasta -->
      <div class="col-6 md:col-2">
        <div class="flex align-items-center gap-1">
          <DatePicker
            v-model="filterDateTo"
            :placeholder="t('dates.to')"
            :min-date="filterDateFrom ?? today"
            date-format="dd/mm/yy"
            :manual-input="false"
            show-icon
            icon-display="input"
            class="flex-1" />
          <Button
            v-if="filterDateTo"
            type="button"
            icon="pi pi-times"
            severity="secondary"
            text
            rounded
            size="small"
            :aria-label="t('common.clearDate')"
            @click="filterDateTo = null" />
        </div>
      </div>
    </div>

    <!-- Cargando -->
    <div v-if="loading" role="status" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t("common.loading") }}</span>
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">{{ error }}</Message>

    <!-- Sin resultados -->
    <div v-else-if="events.length === 0" class="text-center py-6 text-color-secondary">
      {{ t("admin.events.noEvents") }}
    </div>

    <!-- Tabla -->
    <template v-else>
      <DataTable
        :value="events"
        :sort-field="dtSortField ?? undefined"
        :sort-order="dtSortOrder ?? undefined"
        removable-sort
        lazy
        size="small"
        striped-rows
        @sort="onSort">
        <!-- Título truncado -->
        <Column field="title" :header="t('events.title')" sortable style="min-width: 0">
          <template #body="{ data }">
            <span
              class="block overflow-hidden white-space-nowrap"
              style="text-overflow: ellipsis; max-width: 18ch"
              :title="data.title">
              {{ data.title }}
            </span>
          </template>
        </Column>

        <!-- Fecha (solo día) -->
        <Column
          field="startDateTime"
          :header="t('dates.date')"
          sortable
          style="width: 6rem; white-space: nowrap">
          <template #body="{ data }">
            {{ formatDate(data.startDateTime) }}
          </template>
        </Column>

        <!-- Provincia -->
        <Column
          field="provinceName"
          :header="t('geo.province')"
          sortable
          style="width: 7rem; white-space: nowrap" />

        <!-- Estado -->
        <Column field="status" :header="t('events.status')" sortable style="width: 8rem">
          <template #body="{ data }">
            <Tag
              :value="t(`me.eventStatus.${data.status}`)"
              :severity="STATUS_SEVERITY[data.status as EventStatus]" />
          </template>
        </Column>

        <!-- Acciones -->
        <Column style="width: 3rem">
          <template #body="{ data }">
            <NuxtLink
              :to="ROUTE_PATH.adminEventEdit(data.id)"
              :aria-label="t('common.details')"
              class="text-color-secondary">
              <i class="pi pi-pencil" />
            </NuxtLink>
          </template>
        </Column>
      </DataTable>

      <AppPaginator
        :first="first"
        :rows="pageSize"
        :total-records="total"
        class="mt-2"
        @page="onPageChange" />
    </template>
  </div>
</template>

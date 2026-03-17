<script setup lang="ts">
import { ME_EVENT_TABS } from "~/types/events";
import type { MeEventTab, EventStatus } from "~/types/events";
import { ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();

const { events, pageMeta, loading, error, fetchEvents } = useMyEvents();

const TABS = ME_EVENT_TABS;

const activeTab = ref<MeEventTab>("CHANGES");
const currentPage = ref(0);
const pageSize = ref(20);
const sort = ref("date");
const initialized = ref(false);

const total = computed(() => pageMeta.value?.totalElements ?? 0);
const first = computed(() => currentPage.value * pageSize.value);
const showSort = computed(() => activeTab.value !== "ALL");

const sortOptionsBase = useSortOptions(["date", "title", "province", "city"]);
const sortOptionsOthers = useSortOptions(["date", "title", "province", "city", "status"]);
const sortOptions = computed(() => (activeTab.value === "OTHERS" ? sortOptionsOthers.value : sortOptionsBase.value));

async function load() {
  const sortParam = showSort.value ? sort.value : undefined;
  await fetchEvents(activeTab.value, currentPage.value, pageSize.value, sortParam);
}

function onTabChange(tab: MeEventTab) {
  activeTab.value = tab;
  currentPage.value = 0;
  sort.value = "date";
}

function onPageChange(e: { page: number; rows: number }) {
  if (e.rows !== pageSize.value) {
    pageSize.value = e.rows;
    currentPage.value = 0;
  } else {
    currentPage.value = e.page;
  }
}

// Carga inicial: muestra la primera pestaña con contenido (CHANGES → PENDING → OTHERS)
onMounted(async () => {
  for (const tab of ["CHANGES", "PENDING", "OTHERS"] as MeEventTab[]) {
    activeTab.value = tab;
    await fetchEvents(tab, 0, pageSize.value, sort.value);
    if (events.value.length > 0) break;
  }
  initialized.value = true;
});

// El watcher solo actúa tras la carga inicial para no interferir con ella
watch([activeTab, currentPage, pageSize, sort], () => {
  if (initialized.value) load();
});

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
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("me.myEvents") }}</h1>

    <Tabs :value="activeTab" @update:value="(v) => onTabChange(v as MeEventTab)">
      <TabList>
        <Tab v-for="tab in TABS" :key="tab" :value="tab">
          {{ t(`me.tabs.${tab}`) }}
        </Tab>
      </TabList>

      <TabPanels :pt="{ root: { class: 'px-0 pb-0' } }">
        <TabPanel v-for="tab in TABS" :key="tab" :value="tab">
          <!-- Ordenación (no disponible en ALL) -->
          <div v-if="showSort" class="flex align-items-center justify-content-between mb-3">
            <span class="text-sm text-color-secondary">{{ t("pagination.sortedBy") }}</span>
            <Select v-model="sort" :options="sortOptions" optionLabel="label" optionValue="value" class="w-10rem" />
          </div>
          <p v-else class="text-sm text-color-secondary mb-3">{{ t("me.allTabNote") }}</p>

          <!-- Estado de carga -->
          <div v-if="loading" class="flex justify-content-center py-6">
            <ProgressSpinner style="width: 2rem; height: 2rem" />
          </div>

          <!-- Error -->
          <Message v-else-if="error" severity="error" :closable="false">
            {{ error }}
          </Message>

          <!-- Sin resultados -->
          <div v-else-if="events.length === 0" class="text-center py-6 text-color-secondary">
            {{ t("me.noEvents") }}
          </div>

          <!-- Lista de eventos -->
          <div v-else class="grid">
            <div v-for="event in events" :key="event.id" class="col-12 md:col-6">
              <NuxtLink :to="ROUTE_PATH.meEventDetail(event.id)" class="no-underline">
              <Card class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150">
                <template #title>
                  <div class="flex align-items-start justify-content-between gap-3">
                    <span>{{ event.title }}</span>
                    <Tag
                      :value="t(`me.eventStatus.${event.status}`)"
                      :severity="STATUS_SEVERITY[event.status]"
                      class="flex-shrink-0" />
                  </div>
                </template>
                <template #content>
                  <div class="text-color-secondary text-sm flex flex-column gap-2">
                    <div>
                      <i class="pi pi-calendar mr-2" />
                      {{ new Date(event.startDateTime).toLocaleDateString() }}
                    </div>
                    <div>
                      <i class="pi pi-compass mr-2" />
                      {{ event.cityName }}<span v-if="event.cityName && event.provinceName">, </span
                      >{{ event.provinceName }}
                    </div>
                    <div>
                      <i class="pi pi-clock mr-2" />
                      {{ t("me.submittedAt") }}: {{ new Date(event.submittedAt).toLocaleDateString() }}
                    </div>
                    <Message v-if="event.moderationMessage" severity="warn" :closable="false" class="mt-1">
                      {{ event.moderationMessage }}
                    </Message>
                  </div>
                </template>
              </Card>
              </NuxtLink>
            </div>
          </div>

          <!-- Paginación -->
          <AppPaginator
            v-if="!loading && !error && total > 0"
            :first="first"
            :rows="pageSize"
            :totalRecords="total"
            class="mt-4"
            @page="onPageChange" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

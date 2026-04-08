<script setup lang="ts">
import { MODERATION_TABS } from "~/types/events";
import type { ModerationTab, EventStatus } from "~/types/events";
import { ROUTES, ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
const route = useRoute();
useHead({ title: () => t("page.moderationEvents") });

const { pendingEvents, approvedEvents, archivedEvents, pageMeta, loading, error, fetchEvents } = useModerationEvents();

const TABS = MODERATION_TABS;

const initialTab = route.query.tab as ModerationTab | undefined;
const activeTab = ref<ModerationTab>(
  initialTab && (MODERATION_TABS as readonly string[]).includes(initialTab) ? initialTab : "PENDING"
);
const currentPage = ref(0);
const pageSize = ref(20);
const sort = ref("submitted");
const initialized = ref(false);

const total = computed(() => pageMeta.value?.totalElements ?? 0);
const first = computed(() => currentPage.value * pageSize.value);

const sortOptionsPending = useSortOptions(["title", "submitted"]);
const sortOptionsApproved = useSortOptions(["title", "approved"]);
const sortOptionsArchived = useSortOptions(["title", "status", "moderated"]);
const sortOptions = computed(() => {
  if (activeTab.value === "PENDING") return sortOptionsPending.value;
  if (activeTab.value === "APPROVED") return sortOptionsApproved.value;
  return sortOptionsArchived.value;
});

async function load() {
  await fetchEvents(activeTab.value, currentPage.value, pageSize.value, sort.value);
}

function onTabChange(tab: ModerationTab) {
  activeTab.value = tab;
  currentPage.value = 0;
  if (tab === "PENDING") sort.value = "submitted";
  else if (tab === "APPROVED") sort.value = "approved";
  else sort.value = "moderated";
}

function onPageChange(e: { page: number; rows: number }) {
  if (e.rows !== pageSize.value) {
    pageSize.value = e.rows;
    currentPage.value = 0;
  } else {
    currentPage.value = e.page;
  }
}

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

onMounted(async () => {
  await fetchEvents(activeTab.value, currentPage.value, pageSize.value, sort.value);
  initialized.value = true;
});

watch([activeTab, currentPage, pageSize, sort], () => {
  if (initialized.value) load();
});
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.moderation" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("moderation.hub.events") }}</h1>
    </div>

    <Tabs :value="activeTab" @update:value="(v) => onTabChange(v as ModerationTab)">
      <TabList>
        <Tab v-for="tab in TABS" :key="tab" :value="tab">
          {{ t(`moderation.tabs.${tab}`) }}
        </Tab>
      </TabList>

      <TabPanels :pt="{ root: { class: 'px-0 pb-0' } }">
        <TabPanel v-for="tab in TABS" :key="tab" :value="tab">
          <!-- Ordenación -->
          <div class="flex align-items-center justify-content-between mb-3">
            <span class="text-sm text-color-secondary">{{ t("pagination.sortedBy") }}</span>
            <Select v-model="sort" :options="sortOptions" option-label="label" option-value="value" class="w-10rem" :pt="{ label: { 'aria-label': t('pagination.sortedBy') } }" />
          </div>

          <!-- Estado de carga -->
          <div v-if="loading" role="status" class="flex justify-content-center py-6">
            <ProgressSpinner style="width: 2rem; height: 2rem" />
            <span class="sr-only">{{ t('common.loading') }}</span>
          </div>

          <!-- Error -->
          <Message v-else-if="error" severity="error" :closable="false">
            {{ error }}
          </Message>

          <!-- Tab Pendientes -->
          <template v-else-if="tab === 'PENDING'">
            <div v-if="pendingEvents.length === 0" class="text-center py-6 text-color-secondary">
              {{ t("moderation.noEvents") }}
            </div>
            <div v-else class="grid">
              <div v-for="event in pendingEvents" :key="event.id" class="col-12 md:col-6">
                <NuxtLink
                  :to="{ path: ROUTE_PATH.moderationEventDetail(event.id), query: { from: 'PENDING' } }"
                  class="no-underline">
                  <Card class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150">
                    <template #title>
                      <div class="flex align-items-start justify-content-between gap-3">
                        <span>{{ event.title }}</span>
                        <Tag
                          v-if="event.possibleDuplicateOf"
                          :value="t('moderation.possibleDuplicate')"
                          severity="warn"
                          icon="pi pi-copy"
                          class="flex-shrink-0" />
                      </div>
                    </template>
                    <template #content>
                      <div class="text-color-secondary text-sm">
                        <i class="pi pi-clock mr-2" />
                        {{ t("me.submittedAt") }}: {{ formatDate(event.submittedAt) }}
                      </div>
                    </template>
                  </Card>
                </NuxtLink>
              </div>
            </div>
          </template>

          <!-- Tab Aprobados -->
          <template v-else-if="tab === 'APPROVED'">
            <div v-if="approvedEvents.length === 0" class="text-center py-6 text-color-secondary">
              {{ t("moderation.noEvents") }}
            </div>
            <div v-else class="grid">
              <div v-for="event in approvedEvents" :key="event.id" class="col-12 md:col-6">
                <NuxtLink
                  :to="{ path: ROUTE_PATH.moderationEventDetail(event.id), query: { from: 'APPROVED' } }"
                  class="no-underline">
                  <Card class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150">
                    <template #title>{{ event.title }}</template>
                    <template #content>
                      <div class="text-color-secondary text-sm">
                        <i class="pi pi-check-circle mr-2" />
                        {{ t("moderation.approvedAt") }}: {{ formatDate(event.approvedAt) }}
                      </div>
                    </template>
                  </Card>
                </NuxtLink>
              </div>
            </div>
          </template>

          <!-- Tab Archivados -->
          <template v-else-if="tab === 'ARCHIVED'">
            <div v-if="archivedEvents.length === 0" class="text-center py-6 text-color-secondary">
              {{ t("moderation.noEvents") }}
            </div>
            <div v-else class="grid">
              <div v-for="event in archivedEvents" :key="event.id" class="col-12 md:col-6">
                <NuxtLink
                  :to="{ path: ROUTE_PATH.moderationEventDetail(event.id), query: { from: 'ARCHIVED' } }"
                  class="no-underline">
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
                          <i class="pi pi-check-circle mr-2" />
                          {{ t("moderation.moderatedAt") }}: {{ formatDate(event.moderatedAt) }}
                        </div>
                        <Message v-if="event.moderationMessage" severity="info" :closable="false" class="mt-1">
                          {{ event.moderationMessage }}
                        </Message>
                      </div>
                    </template>
                  </Card>
                </NuxtLink>
              </div>
            </div>
          </template>

          <!-- Paginación -->
          <AppPaginator
            v-if="!loading && !error && total > 0"
            :first="first"
            :rows="pageSize"
            :total-records="total"
            class="mt-4"
            @page="onPageChange" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

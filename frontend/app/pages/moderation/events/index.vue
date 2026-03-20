<script setup lang="ts">
import { MODERATION_TABS } from "~/types/events";
import type { ModerationTab, EventStatus } from "~/types/events";
import { ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.moderationEvents") });

const { pendingEvents, archivedEvents, pageMeta, loading, error, fetchEvents } = useModerationEvents();

const TABS = MODERATION_TABS;

const activeTab = ref<ModerationTab>("PENDING");
const currentPage = ref(0);
const pageSize = ref(20);
const sort = ref("submitted");

const total = computed(() => pageMeta.value?.totalElements ?? 0);
const first = computed(() => currentPage.value * pageSize.value);

const sortOptionsPending = useSortOptions(["title", "submitted"]);
const sortOptionsArchived = useSortOptions(["title", "status", "moderated"]);
const sortOptions = computed(() => (activeTab.value === "PENDING" ? sortOptionsPending.value : sortOptionsArchived.value));

async function load() {
  await fetchEvents(activeTab.value, currentPage.value, pageSize.value, sort.value);
}

function onTabChange(tab: ModerationTab) {
  activeTab.value = tab;
  currentPage.value = 0;
  sort.value = tab === "PENDING" ? "submitted" : "moderated";
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

watch([activeTab, currentPage, pageSize, sort], load, { immediate: true });
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("moderation.hub.events") }}</h1>

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
            <Select v-model="sort" :options="sortOptions" optionLabel="label" optionValue="value" class="w-10rem" :pt="{ label: { 'aria-label': t('pagination.sortedBy') } }" />
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
          <template v-else-if="tab === 'PENDING' && activeTab === 'PENDING'">
            <div v-if="pendingEvents.length === 0" class="text-center py-6 text-color-secondary">
              {{ t("moderation.noEvents") }}
            </div>
            <div v-else class="grid">
              <div v-for="event in pendingEvents" :key="event.id" class="col-12 md:col-6">
                <NuxtLink :to="ROUTE_PATH.moderationEventDetail(event.id)" class="no-underline">
                  <Card
                    class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150">
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
                      <div class="text-color-secondary text-sm flex flex-column gap-2">
                        <div>
                          <i class="pi pi-clock mr-2" />
                          {{ t("me.submittedAt") }}: {{ new Date(event.submittedAt).toLocaleDateString() }}
                        </div>
                      </div>
                    </template>
                  </Card>
                </NuxtLink>
              </div>
            </div>
          </template>

          <!-- Tab Archivados -->
          <template v-else-if="tab === 'ARCHIVED' && activeTab === 'ARCHIVED'">
            <div v-if="archivedEvents.length === 0" class="text-center py-6 text-color-secondary">
              {{ t("moderation.noEvents") }}
            </div>
            <div v-else class="grid">
              <div v-for="event in archivedEvents" :key="event.id" class="col-12 md:col-6">
                <NuxtLink :to="ROUTE_PATH.moderationEventDetail(event.id)" class="no-underline">
                  <Card
                    class="h-full border-1 surface-50 surface-border cursor-pointer hover:surface-100 transition-colors transition-duration-150">
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
                          {{ t("moderation.moderatedAt") }}: {{ new Date(event.moderatedAt).toLocaleDateString() }}
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
            :totalRecords="total"
            class="mt-4"
            @page="onPageChange" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { Artist } from "~/types/artist";
import type { PageResponse, PageMeta } from "~/types/pagination";

definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.moderationArtists") });

const search = ref("");
const currentPage = ref(0);
const pageSize = ref(20);

const artists = ref<Artist[]>([]);
const pageMeta = ref<PageMeta | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const first = computed(() => currentPage.value * pageSize.value);

async function load() {
  loading.value = true;
  error.value = null;

  const params: Record<string, string> = {
    page: String(currentPage.value),
    size: String(pageSize.value),
  };
  if (search.value.trim()) params.query = search.value.trim();

  const url = `${ROUTES.apiModerationArtists}?${new URLSearchParams(params).toString()}`;
  const res = await fetchAuthResult<PageResponse<Artist>>(url);

  loading.value = false;
  if (res.ok) {
    artists.value = res.data.content;
    pageMeta.value = res.data.page;
  } else {
    error.value = extractApiError(res, t);
  }
}

onMounted(load);

let debounceTimer: ReturnType<typeof setTimeout> | null = null;
watch(search, () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    currentPage.value = 0;
    load();
  }, 300);
});

function onPageChange(e: { page: number; rows: number }) {
  if (e.rows !== pageSize.value) {
    pageSize.value = e.rows;
    currentPage.value = 0;
  } else {
    currentPage.value = e.page;
  }
  load();
}

// Eliminar artista
const deletingId = ref<string | null>(null);
const deleteConfirmId = ref<string | null>(null);
const deleteError = ref<string | null>(null);

function askDelete(id: string) {
  deleteConfirmId.value = id;
  deleteError.value = null;
}

async function confirmDelete() {
  if (!deleteConfirmId.value) return;
  const id = deleteConfirmId.value;
  deletingId.value = id;
  deleteError.value = null;

  const res = await fetchAuthResult<void>(ROUTE_PATH.apiModerationArtistDetail(id), { method: "DELETE" });

  deletingId.value = null;
  deleteConfirmId.value = null;

  if (res.ok) {
    clearArtistAutocompleteCache();
    await load();
  } else if (res.status === 409) {
    deleteError.value = t("moderation.artists.errorHasEvents");
  } else {
    deleteError.value = res.pd?.detail ?? t("error.unknown");
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.moderation" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0 flex-1">{{ t("moderation.artists.listTitle") }}</h1>
      <Button
        icon="pi pi-plus"
        :label="t('moderation.artists.createBtn')"
        size="small"
        @click="navigateTo(ROUTES.moderationArtistCreate)" />
    </div>

    <Message v-if="deleteError" severity="error" :closable="true" @close="deleteError = null">{{ deleteError }}</Message>

    <InputText
      v-model="search"
      :placeholder="t('moderation.artists.searchPlaceholder')"
      class="w-full md:w-20rem" />

    <div v-if="loading" role="status" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t("common.loading") }}</span>
    </div>

    <template v-else-if="!error">
      <div v-if="artists.length" class="flex flex-column gap-2">
        <div
          v-for="artist in artists"
          :key="artist.id"
          class="flex align-items-center justify-content-between p-3 border-1 surface-border border-round surface-card">
          <NuxtLink :to="ROUTE_PATH.artistDetail(artist.id)" class="no-underline text-color font-medium">
            {{ artist.name }}
          </NuxtLink>
          <Button
            icon="pi pi-trash"
            severity="danger"
            text
            size="small"
            :loading="deletingId === artist.id"
            :aria-label="t('moderation.artists.delete')"
            @click="askDelete(artist.id)" />
        </div>
      </div>

      <Message v-else severity="info" :closable="false">
        {{ t("moderation.artists.noOrphans") }}
      </Message>

      <Paginator
        v-if="(pageMeta?.totalElements ?? 0) > pageSize"
        :rows="pageSize"
        :total-records="pageMeta?.totalElements ?? 0"
        :first="first"
        :rows-per-page-options="[20, 50, 100]"
        @page="onPageChange" />
    </template>

    <Message v-else severity="error" :closable="false">{{ error }}</Message>
  </div>

  <Dialog
    :visible="!!deleteConfirmId"
    :header="t('moderation.artists.delete')"
    modal
    :closable="false"
    :style="{ width: '24rem' }"
    @update:visible="deleteConfirmId = null">
    <p class="m-0 text-color-secondary">{{ t("moderation.artists.deleteConfirm") }}</p>
    <template #footer>
      <Button
        :label="t('moderation.artists.deleteCancel')"
        severity="secondary"
        outlined
        @click="deleteConfirmId = null" />
      <Button
        :label="t('moderation.artists.deleteOk')"
        severity="danger"
        icon="pi pi-trash"
        :loading="!!deletingId"
        @click="confirmDelete" />
    </template>
  </Dialog>
</template>

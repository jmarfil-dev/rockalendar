<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { ArtistRef, EventPublicListItem } from "~/types/events";

definePageMeta({ layout: "public" });

const { t } = useI18n();
const route = useRoute();
const id = route.params.id as string;

const { data: artist, error: artistError } = await useFetch<ArtistRef>(
  ROUTE_PATH.apiArtistDetail(id),
  { key: `artist-${id}` },
);
if (artistError.value) {
  const err = artistError.value as unknown as { status?: number; statusCode?: number };
  const status = err?.status ?? err?.statusCode ?? 500;
  throw createError({ status: status >= 500 ? status : 404 });
}

useHead({ title: () => artist.value?.name ?? t("page.artistDetail") });
useSeoMeta({
  description: () => `Upcoming concerts and festivals featuring ${artist.value?.name ?? ""} in Spain.`,
  ogTitle: () => artist.value?.name,
  ogDescription: () => `Upcoming concerts and festivals featuring ${artist.value?.name ?? ""} in Spain.`,
  ogType: "website",
  twitterCard: "summary",
  twitterTitle: () => artist.value?.name,
  twitterDescription: () => `Upcoming concerts and festivals featuring ${artist.value?.name ?? ""} in Spain.`,
});

const now = new Date().toISOString();
const { data: eventsPage, pending: eventsPending } = await useFetch<{ content: EventPublicListItem[] }>(
  ROUTES.apiEvents,
  {
    key: `artist-events-${id}`,
    query: { artistId: id, dateFrom: now, size: 20, sort: "date,asc" },
  },
);

const events = computed(() => eventsPage.value?.content ?? []);

const { isModerator } = useAuth();
const router = useRouter();

const deleteConfirmVisible = ref(false);
const deleting = ref(false);
const deleteError = ref<string | null>(null);

const renameDialogVisible = ref(false);
const renameValue = ref("");
const renaming = ref(false);
const renameError = ref<string | null>(null);

function openRenameDialog() {
  renameValue.value = artist.value?.name ?? "";
  renameError.value = null;
  renameDialogVisible.value = true;
}

async function confirmRename() {
  renaming.value = true;
  renameError.value = null;
  const res = await fetchAuthResult<{ id: string; name: string }>(
    ROUTE_PATH.apiModerationArtistDetail(id),
    { method: "PATCH", body: { name: renameValue.value } },
  );
  renaming.value = false;
  if (res.ok) {
    artist.value!.name = res.data.name;
    renameDialogVisible.value = false;
  } else if (res.status === 409) {
    renameError.value = t("moderation.artists.errorExists");
  } else {
    renameError.value = res.pd?.detail ?? t("error.unknown");
  }
}

async function confirmDelete() {
  deleting.value = true;
  deleteError.value = null;

  const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationArtistDetail(id), {
    method: "DELETE",
  });

  deleting.value = false;

  if (res.ok) {
    deleteConfirmVisible.value = false;
    navigateTo(ROUTES.events);
  } else if (res.status === 409) {
    deleteConfirmVisible.value = false;
    deleteError.value = t("moderation.artists.errorHasEvents");
  } else {
    deleteConfirmVisible.value = false;
    deleteError.value = res.pd?.detail ?? t("error.unknown");
  }
}
</script>

<template>
  <article class="p-3 md:p-4 lg:p-5 flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <button class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" :aria-label="t('common.back')" @click="router.back()">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0 flex-1">{{ artist?.name }}</h1>
      <Button
        v-if="isModerator"
        icon="pi pi-pencil"
        severity="secondary"
        text
        :aria-label="t('moderation.artists.rename')"
        @click="openRenameDialog" />
      <Button
        v-if="isModerator"
        icon="pi pi-trash"
        severity="danger"
        text
        :aria-label="t('moderation.artists.delete')"
        @click="deleteConfirmVisible = true" />
    </div>

    <Message v-if="deleteError" severity="error" :closable="false">{{ deleteError }}</Message>

    <Divider class="my-1" />

    <section :aria-label="t('artists.upcomingEvents')">
      <div class="flex align-items-center gap-2 mb-3">
        <i class="pi pi-calendar text-color-secondary" aria-hidden="true" />
        <h2 class="m-0 text-lg font-semibold">{{ t("artists.upcomingEvents") }}</h2>
      </div>

      <div v-if="eventsPending" role="status" class="flex justify-content-center py-6">
        <ProgressSpinner style="width: 2rem; height: 2rem" />
        <span class="sr-only">{{ t("common.loading") }}</span>
      </div>

      <div v-else-if="events.length" class="flex flex-column gap-2">
        <NuxtLink
          v-for="event in events"
          :key="event.id"
          :to="ROUTE_PATH.eventDetail(event.id)"
          class="no-underline">
          <Card class="border-1 surface-border hover:surface-100 transition-colors transition-duration-150 cursor-pointer">
            <template #content>
              <div class="flex flex-column gap-1">
                <span class="font-semibold text-color">{{ event.title }}</span>
                <div class="flex align-items-center gap-2 text-color-secondary text-sm">
                  <i class="pi pi-calendar" aria-hidden="true" />
                  <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime, event.startTimeUnknown) }}</time>
                </div>
                <div class="flex align-items-center gap-2 text-color-secondary text-sm">
                  <i class="pi pi-map-marker" aria-hidden="true" />
                  <span>{{ event.cityName }}<template v-if="event.provinceName">, {{ event.provinceName }}</template></span>
                </div>
              </div>
            </template>
          </Card>
        </NuxtLink>
      </div>

      <Message v-else severity="info" :closable="false">
        {{ t("artists.noUpcomingEvents") }}
      </Message>
    </section>
  </article>

  <Dialog
    v-model:visible="renameDialogVisible"
    :header="t('moderation.artists.rename')"
    modal
    :closable="false"
    :style="{ width: '24rem' }">
    <div class="flex flex-column gap-3">
      <div class="flex flex-column gap-2">
        <label for="rename-input" class="text-sm text-color-secondary">{{ t("moderation.artists.renameTitle") }}</label>
        <InputText
          id="rename-input"
          v-model="renameValue"
          :placeholder="t('moderation.artists.renamePlaceholder')"
          :disabled="renaming"
          @keydown.enter.prevent="confirmRename" />
      </div>
      <Message v-if="renameError" severity="error" :closable="false">{{ renameError }}</Message>
    </div>
    <template #footer>
      <Button
        :label="t('moderation.artists.deleteCancel')"
        severity="secondary"
        outlined
        :disabled="renaming"
        @click="renameDialogVisible = false" />
      <Button
        :label="t('moderation.artists.renameOk')"
        icon="pi pi-check"
        :loading="renaming"
        :disabled="!renameValue.trim()"
        @click="confirmRename" />
    </template>
  </Dialog>

  <Dialog
    v-model:visible="deleteConfirmVisible"
    :header="t('moderation.artists.delete')"
    modal
    :closable="false"
    :style="{ width: '24rem' }">
    <p class="m-0 text-color-secondary">{{ t("moderation.artists.deleteConfirm") }}</p>
    <template #footer>
      <Button
        :label="t('moderation.artists.deleteCancel')"
        severity="secondary"
        outlined
        :disabled="deleting"
        @click="deleteConfirmVisible = false" />
      <Button
        :label="t('moderation.artists.deleteOk')"
        severity="danger"
        icon="pi pi-trash"
        :loading="deleting"
        @click="confirmDelete" />
    </template>
  </Dialog>
</template>

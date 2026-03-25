<script setup lang="ts">
import type { EventStatus, InteractionStatus, EventPrivateDto  } from "~/types/events";

import { ROUTES, ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
const route = useRoute();
const id = route.params.id as string;

const { saving: agendaSaving, getInteraction, setInteraction, removeInteraction, fetchAgenda } = useAgenda();
const currentInteraction = computed(() => getInteraction(id));

const agendaRemoveDialogVisible = ref(false);

function onToggleInteraction(target: InteractionStatus) {
  if (currentInteraction.value === target) {
    agendaRemoveDialogVisible.value = true;
  } else {
    setInteraction(id, target);
  }
}

async function confirmAgendaRemove() {
  await removeInteraction(id);
  agendaRemoveDialogVisible.value = false;
}

const event = ref<EventPrivateDto | null>(null);
useHead({ title: () => event.value?.title ?? t("page.meEvents") });
const loading = ref(true);

const deleteDialogVisible = ref(false);
const deleting = ref(false);
const deleteError = ref<string | null>(null);

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

const EDITABLE_STATUSES: EventStatus[] = ["DRAFT", "NEEDS_CHANGES", "APPROVED"];
const DELETABLE_STATUSES: EventStatus[] = ["PENDING_MODERATION", "NEEDS_CHANGES"];

const canEdit = computed(() => (event.value ? EDITABLE_STATUSES.includes(event.value.status) : false));
const canDelete = computed(() => (event.value ? DELETABLE_STATUSES.includes(event.value.status) : false));

onMounted(async () => {
  const res = await fetchAuthResult<EventPrivateDto>(ROUTE_PATH.apiMeEventDetail(id));
  loading.value = false;
  if (res.ok) {
    event.value = res.data;
    if (res.data.status === "APPROVED") {
      fetchAgenda();
    }
  } else {
    showError({ statusCode: res.status, data: res.pd });
  }
});

async function onDelete() {
  deleting.value = true;
  deleteError.value = null;
  const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiMeEventDetail(id), { method: "DELETE" });
  deleting.value = false;
  if (res.ok) {
    await navigateTo(ROUTES.meEvents);
  } else {
    deleteDialogVisible.value = false;
    deleteError.value = res.pd?.detail ?? t("error.unknown");
  }
}
</script>

<template>
  <article class="flex flex-column gap-4">
    <!-- Cabecera -->
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.meEvents" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("me.myEvents") }}</h1>
    </div>

    <!-- Cargando -->
    <div v-if="loading" role="status" class="flex align-items-center gap-2 py-6 justify-content-center">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t('common.loading') }}</span>
    </div>

    <!-- Contenido -->
    <template v-else-if="event">
      <!-- Título + estado -->
      <div class="flex align-items-start justify-content-between gap-3 flex-wrap">
        <div class="flex flex-column gap-1">
          <h2 class="m-0 text-2xl font-semibold line-height-2">{{ event.title }}</h2>
          <div class="flex align-items-center gap-2 text-color-secondary text-sm">
            <i class="pi pi-map-marker" />
            <span>
              <span v-if="event.venueName">{{ event.venueName }}</span>
              <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
              <span v-if="event.cityName">{{ event.cityName }}</span>
              <span v-if="event.cityName && event.provinceName">, </span>
              <span v-if="event.provinceName">{{ event.provinceName }}</span>
            </span>
          </div>
          <div class="flex align-items-center gap-2 text-color-secondary text-sm">
            <i class="pi pi-calendar" />
            <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime) }}</time>
            <template v-if="event.endDateTime">
              <span>→</span>
              <time :datetime="event.endDateTime">{{ formatEventDate(event.endDateTime) }}</time>
            </template>
          </div>
        </div>
        <Tag
          :value="t(`me.eventStatus.${event.status}`)"
          :severity="STATUS_SEVERITY[event.status]"
          class="flex-shrink-0 text-base" />
      </div>

      <Divider class="my-1" />

      <!-- Poster -->
      <section :aria-label="t('events.poster')">
        <Card class="border-1 surface-border">
          <template #content>
            <div
              v-if="event.posterUrl"
              class="border-round-lg overflow-hidden flex align-items-center justify-content-center surface-50"
              style="min-height: 8rem">
              <img
                :src="event.posterUrl"
                :alt="event.title"
                style="max-width: 100%; max-height: 480px; object-fit: contain; display: block; margin: 0 auto" >
            </div>
            <div
              v-else
              class="border-1 surface-border border-round-lg surface-50 flex align-items-center justify-content-center"
              style="aspect-ratio: 16/9">
              <div class="text-center text-color-secondary">
                <i class="pi pi-image text-2xl" />
                <div class="mt-2 text-sm">{{ t("events.noPoster") }}</div>
              </div>
            </div>
          </template>
        </Card>
      </section>

      <!-- Mensaje de moderación -->
      <Message v-if="event.moderationMessage" severity="warn" :closable="false">
        {{ event.moderationMessage }}
      </Message>

      <!-- Error de borrado -->
      <Message v-if="deleteError" severity="error" :closable="false">{{ deleteError }}</Message>

      <!-- Grid principal -->
      <div class="grid">
        <!-- Columna izquierda: contenido -->
        <div class="col-12 lg:col-8">
          <Card class="border-1 surface-border">
            <template #content>
              <div class="flex flex-column gap-4">
                <!-- Artistas -->
                <section v-if="event.artists?.length" :aria-label="`${t('events.groups')} / ${t('events.artists')}`">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-users text-color-secondary" />
                    <h3 class="m-0 text-lg font-semibold">{{ t("events.groups") }} / {{ t("events.artists") }}</h3>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <NuxtLink
                      v-for="artist in event.artists"
                      :key="artist.id"
                      :to="ROUTE_PATH.artistDetail(artist.id)"
                      class="no-underline">
                      <Tag :value="artist.name" rounded severity="info" class="cursor-pointer" />
                    </NuxtLink>
                  </div>
                </section>

                <!-- Descripción -->
                <section v-if="event.description" :aria-label="t('events.description')">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-align-left text-color-secondary" />
                    <h3 class="m-0 text-lg font-semibold">{{ t("events.description") }}</h3>
                  </div>
                  <div class="text-color-secondary white-space-pre-line line-height-3">
                    {{ event.description }}
                  </div>
                </section>

                <Message v-if="!event.description?.trim() && !event.artists?.length" severity="info" :closable="false">
                  {{ t("events.noDescription") }}
                </Message>
              </div>
            </template>
          </Card>
        </div>

        <!-- Columna derecha: metadatos + acciones -->
        <aside class="col-12 lg:col-4">
          <div class="flex flex-column gap-3">
            <!-- Detalles -->
            <Card class="border-1 surface-border">
              <template #title>
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-info-circle" />
                  <h3 class="m-0 text-base font-semibold">{{ t("common.details") }}</h3>
                </div>
              </template>
              <template #content>
                <div class="flex flex-column gap-3 text-sm">
                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-calendar text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("dates.date") }}</span>
                      <span class="text-color-secondary">
                        <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime) }}</time>
                        <template v-if="event.endDateTime">
                          <span class="mx-1">→</span>
                          <time :datetime="event.endDateTime">{{ formatEventDate(event.endDateTime) }}</time>
                        </template>
                      </span>
                    </div>
                  </div>

                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-map-marker text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("geo.place") }}</span>
                      <span class="text-color-secondary">
                        <span v-if="event.venueName">{{ event.venueName }}</span>
                        <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
                        <span v-if="event.cityName">{{ event.cityName }}</span>
                        <span v-if="event.cityName && event.provinceName">, </span>
                        <span v-if="event.provinceName">{{ event.provinceName }}</span>
                      </span>
                    </div>
                  </div>

                  <div v-if="event.sourceUrl" class="flex align-items-start gap-2">
                    <i class="pi pi-link text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("events.sourceUrl") }}</span>
                      <a
                        :href="event.sourceUrl"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="text-primary underline text-xs break-all">
                        {{ event.sourceUrl }}
                      </a>
                    </div>
                  </div>

                  <Divider class="my-1" />

                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-clock text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("me.submittedAt") }}</span>
                      <span class="text-color-secondary">
                        {{ new Date(event.submittedAt).toLocaleDateString() }}
                      </span>
                    </div>
                  </div>

                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-history text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("me.createdAt") }}</span>
                      <span class="text-color-secondary">
                        {{ new Date(event.createdAt).toLocaleDateString() }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </Card>

            <!-- Acciones -->
            <Card class="border-1 surface-border">
              <template #content>
                <div class="flex flex-column gap-2">
                  <Button
                    :label="t('me.editEvent')"
                    icon="pi pi-pencil"
                    class="w-full"
                    :disabled="!canEdit"
                    type="button"
                    @click="navigateTo(`${ROUTE_PATH.meEventDetail(id)}/edit`)" />
                  <Button
                    :label="t('me.deleteEvent')"
                    icon="pi pi-trash"
                    severity="danger"
                    outlined
                    class="w-full"
                    :disabled="!canDelete"
                    type="button"
                    @click="deleteDialogVisible = true" />
                </div>
              </template>
            </Card>

            <!-- Agenda (solo cuando está aprobado) -->
            <Card v-if="event.status === 'APPROVED'" class="border-1 surface-border">
              <template #title>
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-calendar" />
                  <h3 class="m-0 text-base font-semibold">{{ t("me.agenda.title") }}</h3>
                </div>
              </template>
              <template #content>
                <div class="flex gap-2">
                  <Button
                    :label="t('me.agenda.interested')"
                    :icon="currentInteraction === 'INTERESTED' ? 'pi pi-heart-fill' : 'pi pi-heart'"
                    :severity="currentInteraction === 'INTERESTED' ? 'primary' : 'secondary'"
                    :loading="agendaSaving === id"
                    class="flex-1"
                    type="button"
                    @click="onToggleInteraction('INTERESTED')" />
                  <Button
                    :label="t('me.agenda.going')"
                    :icon="currentInteraction === 'GOING' ? 'pi pi-check-circle' : 'pi pi-circle'"
                    :severity="currentInteraction === 'GOING' ? 'primary' : 'secondary'"
                    :loading="agendaSaving === id"
                    class="flex-1"
                    type="button"
                    @click="onToggleInteraction('GOING')" />
                </div>
              </template>
            </Card>
          </div>
        </aside>
      </div>
    </template>
  </article>

  <!-- Diálogo de confirmación de quitar de agenda -->
  <Dialog v-model:visible="agendaRemoveDialogVisible" :header="t('me.agenda.title')" modal :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("me.agenda.removeConfirm") }}</p>
    <template #footer>
      <Button :label="t('me.agenda.removeCancel')" severity="secondary" outlined @click="agendaRemoveDialogVisible = false" />
      <Button :label="t('me.agenda.removeOk')" severity="danger" icon="pi pi-trash" :loading="!!agendaSaving" @click="confirmAgendaRemove" />
    </template>
  </Dialog>

  <!-- Diálogo de confirmación de borrado -->
  <Dialog v-model:visible="deleteDialogVisible" :header="t('me.deleteEvent')" modal :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("me.deleteConfirm") }}</p>
    <template #footer>
      <Button :label="t('me.deleteCancel')" severity="secondary" outlined @click="deleteDialogVisible = false" />
      <Button :label="t('me.deleteOk')" severity="danger" icon="pi pi-trash" :loading="deleting" @click="onDelete" />
    </template>
  </Dialog>
</template>

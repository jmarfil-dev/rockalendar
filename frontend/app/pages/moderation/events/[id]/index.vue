<script setup lang="ts">
import type { EventStatus, EventPrivateDto } from "~/types/events";
import { ROUTES, ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
const route = useRoute();
const id = route.params.id as string;

// --- Carga del evento ---
const event = ref<EventPrivateDto | null>(null);
const loading = ref(true);

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

const canModerate = computed(() => event.value?.status === "PENDING_MODERATION");

onMounted(async () => {
  const res = await fetchAuthResult<EventPrivateDto>(ROUTE_PATH.apiModerationEventDetail(id));
  loading.value = false;
  if (res.ok) {
    event.value = res.data;
  } else {
    showError({ statusCode: res.status, data: res.pd });
  }
});

// --- Acciones de moderación ---
type ActionType = "approve" | "reject" | "hide" | "requestChanges";

const { loading: actionLoading, error: actionError, approve, reject, hide, requestChanges } = useModerationActions();

const dialogVisible = ref(false);
const currentAction = ref<ActionType | null>(null);
const dialogText = ref("");

const dialogTitle = computed(() => {
  if (!currentAction.value) return "";
  return t(`moderation.actions.${currentAction.value}`);
});

const isCommentOptional = computed(() => currentAction.value === "approve");

function openDialog(action: ActionType) {
  currentAction.value = action;
  dialogText.value = "";
  actionError.value = null;
  dialogVisible.value = true;
}

async function onConfirm() {
  if (!currentAction.value) return;

  let ok = false;
  const text = dialogText.value.trim();

  if (currentAction.value === "approve") {
    ok = await approve(id, text || undefined);
  } else if (currentAction.value === "reject") {
    ok = await reject(id, text);
  } else if (currentAction.value === "hide") {
    ok = await hide(id, text);
  } else if (currentAction.value === "requestChanges") {
    ok = await requestChanges(id, text);
  }

  if (ok) {
    dialogVisible.value = false;
    await navigateTo(ROUTES.moderationEvents);
  }
}
</script>

<template>
  <article class="flex flex-column gap-4">
    <!-- Cabecera -->
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.moderationEvents" class="text-color-secondary">
        <i class="pi pi-arrow-left" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("moderation.hub.events") }}</h1>
    </div>

    <!-- Cargando -->
    <div v-if="loading" class="flex align-items-center gap-2 py-6 justify-content-center">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
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

      <!-- Mensaje de moderación previo -->
      <Message v-if="event.moderationMessage" severity="warn" :closable="false">
        {{ event.moderationMessage }}
      </Message>

      <!-- Error de acción -->
      <Message v-if="actionError" severity="error" :closable="false">{{ actionError }}</Message>

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
                    <Tag v-for="artist in event.artists" :key="artist" :value="artist" rounded severity="info" />
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

            <!-- Acciones de moderación (solo para PENDING_MODERATION) -->
            <Card v-if="canModerate" class="border-1 surface-border">
              <template #content>
                <div class="flex flex-column gap-2">
                  <Button
                    :label="t('moderation.actions.approve')"
                    icon="pi pi-check"
                    severity="success"
                    class="w-full"
                    type="button"
                    @click="openDialog('approve')" />
                  <Button
                    :label="t('moderation.actions.requestChanges')"
                    icon="pi pi-replay"
                    severity="warn"
                    outlined
                    class="w-full"
                    type="button"
                    @click="openDialog('requestChanges')" />
                  <Button
                    :label="t('moderation.actions.reject')"
                    icon="pi pi-times"
                    severity="danger"
                    outlined
                    class="w-full"
                    type="button"
                    @click="openDialog('reject')" />
                  <Button
                    :label="t('moderation.actions.hide')"
                    icon="pi pi-eye-slash"
                    severity="secondary"
                    outlined
                    class="w-full"
                    type="button"
                    @click="openDialog('hide')" />
                </div>
              </template>
            </Card>
          </div>
        </aside>
      </div>
    </template>
  </article>

  <!-- Diálogo de acción de moderación -->
  <Dialog
    v-model:visible="dialogVisible"
    :header="dialogTitle"
    modal
    :style="{ width: '26rem' }">
    <div class="flex flex-column gap-3">
      <label for="action-text" class="text-sm text-color-secondary">
        {{ isCommentOptional ? t("moderation.actions.commentLabel") : t("moderation.actions.reasonLabel") }}
      </label>
      <Textarea
        id="action-text"
        v-model="dialogText"
        rows="4"
        :maxlength="500"
        class="w-full"
        autofocus />
      <Message v-if="actionError" severity="error" :closable="false" class="mt-1">{{ actionError }}</Message>
    </div>
    <template #footer>
      <Button
        :label="t('moderation.actions.cancel')"
        severity="secondary"
        outlined
        @click="dialogVisible = false" />
      <Button
        :label="t('moderation.actions.confirm')"
        :loading="actionLoading"
        :disabled="!isCommentOptional && !dialogText.trim()"
        @click="onConfirm" />
    </template>
  </Dialog>
</template>

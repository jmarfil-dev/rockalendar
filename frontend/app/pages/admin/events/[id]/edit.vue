<script setup lang="ts">
import type { EventStatus } from "~/types/events";
import { ROUTES } from "~/constants/routes";
import ArtistSelector from "~/components/events/ArtistSelector.vue";

definePageMeta({ layout: "admin", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.adminEventEdit") });
const route = useRoute();
const id = route.params.id as string;

const { load: loadProvinces, options: provinceOptions, loading: provincesLoading } = useProvinces();
const {
  form,
  posterField,
  existingPosterUrl,
  removePoster,
  loading,
  submitting,
  errorMsg,
  fieldErrors,
  artistsError,
  moderationMessage,
  load,
  submitData,
  submitStatus,
} = useAdminEditEvent(id);

const {
  mode: posterMode,
  posterFile,
  posterFileError,
  urlInput: posterUrlInput,
  importedPosterUrl,
  isImporting: posterIsImporting,
  importError: posterImportError,
  previewUrl: posterPreviewUrl,
  hasPoster: posterHasPoster,
  showModeConfirm: posterShowModeConfirm,
  requestModeSwitch,
  confirmModeSwitch,
  cancelModeSwitch,
  setPosterFile,
  clearPoster: clearPosterState,
  importFromUrl,
} = posterField;

const posterInputRef = ref<HTMLInputElement | null>(null);
const changingPoster = ref(false);

const posterModeOptions = computed(() => [
  { label: t("events.posterModeFile"), value: "file" },
  { label: t("events.posterModeUrl"), value: "url" },
]);

function onPosterChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0] ?? null;
  if (!file) return;
  const ok = setPosterFile(file);
  if (ok) {
    removePoster.value = false;
  } else if (posterInputRef.value) {
    posterInputRef.value.value = "";
  }
}

function onClearNewPoster() {
  clearPosterState();
  if (posterInputRef.value) posterInputRef.value.value = "";
}

function onCancelChange() {
  clearPosterState();
  if (posterInputRef.value) posterInputRef.value.value = "";
  changingPoster.value = false;
}

function onStartChange() {
  removePoster.value = false;
  changingPoster.value = true;
}

function onPosterUrlBlur() {
  if (posterUrlInput.value && !importedPosterUrl.value) {
    importFromUrl();
  }
}

const today = new Date();

const isDateRangeInvalid = computed(
  () => !!form.startDate && !!form.endDate && form.endDate < form.startDate,
);

// --- Cambio de estado ---
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

// committedStatus = estado real en backend. displayStatus = lo que muestra el Select.
// Se separan porque v-model actualiza displayStatus antes de que @change pueda comparar.
const committedStatus = ref<EventStatus | null>(null);
const displayStatus = ref<EventStatus | null>(null);

// --- Estado de los diálogos ---
const showSaveConfirmDialog = ref(false);
const saveComment = ref("");
const showSaveDialog = ref(false);
const showUnsavedDialog = ref(false);
const pendingNavTarget = ref<string | null>(null);

// Seguimiento de cambios para el modal de salida sin guardar
const initialFormSnapshot = ref<string>("");
const hasChanges = computed(() => {
  const current = JSON.stringify({ ...form, status: undefined });
  return current !== initialFormSnapshot.value;
});

onMounted(async () => {
  const [loadRes] = await Promise.all([load(), loadProvinces()]);
  if (loadRes) {
    showError({ statusCode: loadRes.status, data: loadRes.pd });
    return;
  }
  committedStatus.value = form.status;
  displayStatus.value = form.status;
  initialFormSnapshot.value = JSON.stringify({ ...form, status: undefined });
});

// --- Submit datos ---
function onSubmit() {
  saveComment.value = "";
  showSaveConfirmDialog.value = true;
}

async function confirmSave() {
  const result = await submitData(saveComment.value);
  if (result) {
    showSaveConfirmDialog.value = false;
    initialFormSnapshot.value = JSON.stringify({ ...form, status: undefined });
    showSaveDialog.value = true;
    // Tras guardar, el cartel nuevo (si lo había) pasa a ser el existente:
    // se sale del modo "cambiando cartel" y se limpia el aviso de eliminación pendiente,
    // para que el bloque de cartel refleje el estado ya persistido en vez del transitorio.
    changingPoster.value = false;
    removePoster.value = false;
  }
}

// --- Cambio de estado ---
const pendingStatusChange = ref<EventStatus | null>(null);
const showStatusConfirmDialog = ref(false);
const statusComment = ref("");

function requestStatusChange(newStatus: EventStatus) {
  if (newStatus === committedStatus.value) return;
  pendingStatusChange.value = newStatus;
  statusComment.value = "";
  showStatusConfirmDialog.value = true;
}

async function confirmStatusChange() {
  if (!pendingStatusChange.value) return;
  const result = await submitStatus(pendingStatusChange.value, statusComment.value);
  if (result) {
    showStatusConfirmDialog.value = false;
    committedStatus.value = pendingStatusChange.value;
    showSaveDialog.value = true;
  } else {
    displayStatus.value = committedStatus.value;
  }
  pendingStatusChange.value = null;
}

function cancelStatusChange() {
  showStatusConfirmDialog.value = false;
  displayStatus.value = committedStatus.value;
  pendingStatusChange.value = null;
}

// --- Navegación con aviso de cambios sin guardar ---
function tryNavigate(target: string) {
  if (hasChanges.value) {
    pendingNavTarget.value = target;
    showUnsavedDialog.value = true;
  } else {
    navigateTo(target);
  }
}

function confirmLeave() {
  showUnsavedDialog.value = false;
  if (pendingNavTarget.value) {
    navigateTo(pendingNavTarget.value);
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <button
        class="p-button-link text-color-secondary bg-transparent border-none cursor-pointer p-0"
        :aria-label="t('common.back')"
        @click="tryNavigate(ROUTES.adminEvents)">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("admin.events.editTitle") }}</h1>
    </div>

    <!-- Cargando -->
    <div v-if="loading" role="status" class="flex align-items-center gap-2 py-6 justify-content-center">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t('common.loading') }}</span>
    </div>

    <!-- Formulario -->
    <Card v-else class="border-1 surface-border">
      <template #content>
        <Message v-if="errorMsg" severity="error" :closable="false" class="mb-3">{{ errorMsg }}</Message>

        <form class="flex flex-column gap-4" @submit.prevent="onSubmit">
          <!-- Mensaje de moderación -->
          <Message v-if="moderationMessage" severity="warn" :closable="false" class="mb-1">
            <span class="font-semibold">{{ t('admin.events.moderationMessageLabel') }}:</span>
            {{ moderationMessage }}
          </Message>

          <!-- Estado (solo admin) -->
          <div class="flex flex-column gap-2">
            <span id="admin-status-label" class="text-sm text-color-secondary">{{ t("events.status") }}</span>
            <Select
              v-model="displayStatus"
              aria-labelledby="admin-status-label"
              :options="statusOptions"
              option-label="label"
              option-value="value"
              class="w-full md:w-20rem"
              @change="requestStatusChange(displayStatus!)" />
          </div>

          <Divider />

          <!-- Título -->
          <div class="flex flex-column gap-2">
            <label for="title" class="text-sm text-color-secondary">
              {{ t("events.title") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
            </label>
            <InputText
              id="title"
              v-model="form.title"
              :placeholder="t('me.propose.titlePlaceholder')"
              :invalid="!!fieldErrors['title']"
              maxlength="200"
              required />
            <Message v-if="fieldErrors['title']" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["title"]) }}
            </Message>
          </div>

          <!-- Descripción -->
          <div class="flex flex-column gap-2">
            <label for="description" class="text-sm text-color-secondary">{{ t("events.description") }}</label>
            <Textarea
              id="description"
              v-model="form.description"
              :placeholder="t('me.propose.descriptionPlaceholder')"
              :invalid="!!fieldErrors['description']"
              rows="4"
              maxlength="5000"
              auto-resize />
            <Message v-if="fieldErrors['description']" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["description"]) }}
            </Message>
          </div>

          <!-- Fecha inicio / Fecha fin -->
          <div class="grid">
            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="startDate" class="text-sm text-color-secondary">
                {{ t("me.propose.startDate") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </label>
              <div class="flex align-items-center gap-2">
                <DatePicker
                  v-model="form.startDate"
                  input-id="startDate"
                  :show-time="!form.startTimeUnknown"
                  hour-format="24"
                  date-format="dd/mm/yy"
                  :min-date="form.dateTbd ? undefined : today"
                  :invalid="!!fieldErrors['startDate']"
                  :manual-input="false"
                  show-icon
                  icon-display="input"
                  required
                  class="flex-1" />
                <Button
                  v-if="form.startDate"
                  type="button"
                  icon="pi pi-times"
                  severity="secondary"
                  text
                  rounded
                  size="small"
                  :aria-label="t('common.clearDate')"
                  @click="form.startDate = null" />
              </div>
              <div class="flex align-items-center gap-2 mt-1">
                <Checkbox
                  v-model="form.startTimeUnknown"
                  input-id="startTimeUnknown"
                  :binary="true" />
                <label for="startTimeUnknown" class="text-sm text-color-secondary cursor-pointer">
                  {{ t("me.propose.startTimeUnknown") }}
                </label>
              </div>
              <Message v-if="fieldErrors['startDate']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["startDate"]) }}
              </Message>
              <div class="flex align-items-start gap-2 mt-1">
                <Checkbox
                  v-model="form.dateTbd"
                  input-id="dateTbd"
                  :binary="true" />
                <label for="dateTbd" class="text-sm text-color-secondary cursor-pointer">
                  {{ t("admin.events.dateTbdLabel") }}
                </label>
              </div>
              <small v-if="form.dateTbd" class="text-xs text-color-secondary">{{ t("admin.events.dateTbdHint") }}</small>
            </div>

            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="endDate" class="text-sm text-color-secondary">{{ t("me.propose.endDate") }}</label>
              <div class="flex align-items-center gap-2">
                <DatePicker
                  v-model="form.endDate"
                  input-id="endDate"
                  date-format="dd/mm/yy"
                  :min-date="form.startDate ?? undefined"
                  :invalid="!!fieldErrors['endDate'] || isDateRangeInvalid"
                  :manual-input="false"
                  show-icon
                  icon-display="input"
                  class="flex-1" />
                <Button
                  v-if="form.endDate"
                  type="button"
                  icon="pi pi-times"
                  severity="secondary"
                  text
                  rounded
                  size="small"
                  :aria-label="t('common.clearDate')"
                  @click="form.endDate = null" />
              </div>
              <Message v-if="fieldErrors['endDate']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["endDate"]) }}
              </Message>
              <Message v-else-if="isDateRangeInvalid" severity="error" variant="simple" size="small">
                {{ t("dates.invalidRange") }}
              </Message>
            </div>
          </div>

          <!-- Recinto -->
          <div class="flex flex-column gap-2">
            <label for="venueName" class="text-sm text-color-secondary">
              {{ t("me.propose.venueName") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
            </label>
            <InputText
              id="venueName"
              v-model="form.venueName"
              :placeholder="t('me.propose.venuePlaceholder')"
              :invalid="!!fieldErrors['venueName']"
              maxlength="200"
              required />
            <Message v-if="fieldErrors['venueName']" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["venueName"]) }}
            </Message>
          </div>

          <!-- Provincia / Ciudad -->
          <div class="grid">
            <div class="col-12 md:col-6 flex flex-column gap-2">
              <span id="admin-province-label" class="text-sm text-color-secondary">
                {{ t("geo.province") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </span>
              <Select
                v-model="form.provinceId"
                input-id="provinceId"
                aria-labelledby="admin-province-label"
                :options="provinceOptions"
                option-label="label"
                option-value="value"
                :loading="provincesLoading"
                :invalid="!!fieldErrors['provinceId']"
                filter
                required />
              <Message v-if="fieldErrors['provinceId']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["provinceId"]) }}
              </Message>
            </div>

            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="cityName" class="text-sm text-color-secondary">
                {{ t("geo.city") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </label>
              <InputText
                id="cityName"
                v-model="form.cityName"
                :placeholder="t('me.propose.cityPlaceholder')"
                :invalid="!!fieldErrors['cityName']"
                maxlength="120"
                required />
              <Message v-if="fieldErrors['cityName']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["cityName"]) }}
              </Message>
            </div>
          </div>

          <!-- Artistas -->
          <ArtistSelector v-model="form.artists" :field-error="artistsError ?? undefined" />

          <!-- Cartel -->
          <div class="flex flex-column gap-2">
            <label class="text-sm text-color-secondary">{{ t("events.uploadPoster") }}</label>

            <!-- Preview nuevo cartel (fichero o URL importada) -->
            <div v-if="posterHasPoster" class="flex flex-column gap-2">
              <img
                :src="posterPreviewUrl!"
                :alt="t('events.posterPreview')"
                class="border-round-lg border-1 surface-border"
                style="max-width: 100%; max-height: 320px; object-fit: contain; background: var(--surface-50)" >
              <div class="flex align-items-center gap-2">
                <span v-if="posterFile" class="text-sm text-color-secondary flex-1 overflow-hidden text-overflow-ellipsis white-space-nowrap">{{ posterFile.name }}</span>
                <span v-else class="text-sm text-color-secondary flex-1" />
                <Button
                  type="button"
                  :label="t('events.removePoster')"
                  icon="pi pi-times"
                  severity="secondary"
                  outlined
                  size="small"
                  @click="onClearNewPoster" />
              </div>
            </div>

            <!-- Aviso de eliminación pendiente -->
            <div v-else-if="removePoster" class="flex align-items-center gap-2 p-3 border-1 border-round-lg surface-border">
              <i class="pi pi-info-circle text-color-secondary flex-shrink-0" aria-hidden="true" />
              <span class="text-sm text-color-secondary flex-1">{{ t("events.posterWillBeRemoved") }}</span>
              <Button
                type="button"
                icon="pi pi-undo"
                severity="secondary"
                text
                size="small"
                :aria-label="t('common.retry')"
                @click="removePoster = false" />
            </div>

            <!-- Cartel existente (sin nuevo ni marcado para eliminar) -->
            <div v-else-if="existingPosterUrl && !changingPoster" class="flex flex-column gap-2">
              <img
                :src="existingPosterUrl"
                :alt="t('events.currentPoster')"
                class="border-round-lg border-1 surface-border"
                style="max-width: 100%; max-height: 320px; object-fit: contain; background: var(--surface-50)" >
              <div class="flex gap-2">
                <Button
                  type="button"
                  :label="t('events.changePoster')"
                  icon="pi pi-upload"
                  severity="secondary"
                  outlined
                  size="small"
                  @click="onStartChange" />
                <Button
                  type="button"
                  :label="t('events.removePoster')"
                  icon="pi pi-trash"
                  severity="danger"
                  outlined
                  size="small"
                  @click="removePoster = true" />
              </div>
            </div>

            <!-- Selector: toggle Fichero/URL + picker (sin cartel o cambiando) -->
            <template v-else>
              <div v-if="existingPosterUrl && changingPoster" class="flex justify-content-end">
                <Button
                  type="button"
                  :label="t('me.edit.cancel')"
                  severity="secondary"
                  text
                  size="small"
                  @click="onCancelChange" />
              </div>

              <SelectButton
                :model-value="posterMode"
                :options="posterModeOptions"
                option-value="value"
                option-label="label"
                :allow-empty="false"
                @update:model-value="requestModeSwitch($event)" />

              <!-- Modo fichero -->
              <template v-if="posterMode === 'file'">
                <Button
                  type="button"
                  :label="t('events.uploadPoster')"
                  icon="pi pi-upload"
                  severity="secondary"
                  outlined
                  @click="posterInputRef?.click()" />
                <Message v-if="posterFileError" severity="error" variant="simple" size="small">{{ posterFileError }}</Message>
              </template>

              <!-- Modo URL -->
              <template v-else>
                <div class="flex gap-2">
                  <InputText
                    v-model="posterUrlInput"
                    :placeholder="t('events.posterUrlPlaceholder')"
                    :disabled="posterIsImporting"
                    class="flex-1"
                    @keydown.enter.prevent="importFromUrl()"
                    @blur="onPosterUrlBlur" />
                  <Button
                    type="button"
                    :label="t('events.importPoster')"
                    :loading="posterIsImporting"
                    :disabled="!posterUrlInput || posterIsImporting"
                    @click="importFromUrl()" />
                </div>
                <Message v-if="posterImportError" severity="warn" :closable="false" variant="simple" size="small">
                  {{ posterImportError }}
                </Message>
              </template>
            </template>

            <input ref="posterInputRef" type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="onPosterChange" >
            <small class="text-xs text-color-secondary">{{ t("events.posterHint") }}</small>
          </div>

          <!-- URL externa -->
          <div class="flex flex-column gap-2">
            <label for="sourceUrl" class="text-sm text-color-secondary">{{ t("events.sourceUrl") }}</label>
            <InputText
              id="sourceUrl"
              v-model="form.sourceUrl"
              :placeholder="t('me.propose.sourceUrlPlaceholder')"
              :invalid="!!fieldErrors['sourceUrl']"
              maxlength="2048" />
            <Message v-if="fieldErrors['sourceUrl']" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["sourceUrl"]) }}
            </Message>
          </div>

          <!-- Entradas (URL externa) -->
          <div class="flex flex-column gap-2">
            <label for="ticketUrl" class="text-sm text-color-secondary">{{ t("events.ticketUrl") }}</label>
            <InputText
              id="ticketUrl"
              v-model="form.ticketUrl"
              :placeholder="t('me.propose.ticketUrlPlaceholder')"
              :invalid="!!fieldErrors['ticketUrl']"
              maxlength="2048" />
            <Message v-if="fieldErrors['ticketUrl']" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["ticketUrl"]) }}
            </Message>
          </div>

          <!-- Acciones -->
          <div class="flex justify-content-end gap-3 pt-2">
            <Button
              type="button"
              :label="t('common.back')"
              severity="secondary"
              outlined
              @click="tryNavigate(ROUTES.adminEvents)" />
            <Button
              type="submit"
              :label="t('admin.events.saveChanges')"
              icon="pi pi-check"
              :loading="submitting"
              :disabled="isDateRangeInvalid" />
          </div>
        </form>
      </template>
    </Card>
  </div>

  <!-- Modal: confirmar cambio de modo del cartel -->
  <Dialog
    v-model:visible="posterShowModeConfirm"
    :header="t('events.posterModeChangeTitle')"
    modal
    :closable="false"
    :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("events.posterModeChangeMsg") }}</p>
    <template #footer>
      <Button :label="t('events.posterModeChangeCancel')" severity="secondary" outlined @click="cancelModeSwitch" />
      <Button :label="t('events.posterModeChangeOk')" severity="danger" @click="confirmModeSwitch" />
    </template>
  </Dialog>

  <!-- Modal: guardado con éxito -->
  <Dialog
    v-model:visible="showSaveDialog"
    :header="t('admin.events.saveSuccessTitle')"
    modal
    :closable="false"
    :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("admin.events.saveSuccessMsg") }}</p>
    <template #footer>
      <Button :label="t('common.save')" icon="pi pi-check" @click="showSaveDialog = false" />
    </template>
  </Dialog>

  <!-- Modal: confirmar guardado de datos -->
  <Dialog
    v-model:visible="showSaveConfirmDialog"
    :header="t('admin.events.saveConfirmTitle')"
    modal
    :closable="false"
    :style="{ width: '26rem' }">
    <div class="flex flex-column gap-3">
      <p class="m-0 text-color-secondary">{{ t("admin.events.saveConfirmMsg") }}</p>
      <div class="flex flex-column gap-1">
        <label for="save-comment" class="text-sm text-color-secondary">{{ t("admin.events.auditCommentLabel") }}</label>
        <Textarea
          id="save-comment"
          v-model="saveComment"
          :placeholder="t('admin.events.auditCommentPlaceholder')"
          rows="3"
          maxlength="500"
          auto-resize />
      </div>
    </div>
    <template #footer>
      <div class="flex gap-2 justify-content-end">
        <Button
          :label="t('common.back')"
          severity="secondary"
          outlined
          :disabled="submitting"
          @click="showSaveConfirmDialog = false" />
        <Button
          :label="t('admin.events.saveChanges')"
          icon="pi pi-check"
          :loading="submitting"
          @click="confirmSave" />
      </div>
    </template>
  </Dialog>

  <!-- Modal: confirmar cambio de estado -->
  <Dialog
    v-model:visible="showStatusConfirmDialog"
    :header="t('admin.events.statusChangeTitle')"
    modal
    :closable="false"
    :style="{ width: '26rem' }">
    <div class="flex flex-column gap-3">
      <p class="m-0 text-color-secondary">
        {{ t("admin.events.statusChangeMsg", { status: pendingStatusChange ? t(`me.eventStatus.${pendingStatusChange}`) : "" }) }}
      </p>
      <div class="flex flex-column gap-1">
        <label for="status-comment" class="text-sm text-color-secondary">{{ t("admin.events.auditCommentLabel") }}</label>
        <Textarea
          id="status-comment"
          v-model="statusComment"
          :placeholder="t('admin.events.auditCommentPlaceholder')"
          rows="3"
          maxlength="500"
          auto-resize />
      </div>
    </div>
    <template #footer>
      <div class="flex gap-2 justify-content-end">
        <Button
          :label="t('common.back')"
          severity="secondary"
          outlined
          :disabled="submitting"
          @click="cancelStatusChange" />
        <Button
          :label="t('admin.events.statusChangeConfirm')"
          :loading="submitting"
          @click="confirmStatusChange" />
      </div>
    </template>
  </Dialog>

  <!-- Modal: salir sin guardar -->
  <Dialog
    v-model:visible="showUnsavedDialog"
    :header="t('admin.events.unsavedTitle')"
    modal
    :closable="false"
    :style="{ width: '24rem' }">
    <p class="m-0 text-color-secondary">{{ t("admin.events.unsavedMsg") }}</p>
    <template #footer>
      <div class="flex gap-2 justify-content-end">
        <Button
          :label="t('admin.events.unsavedStay')"
          severity="secondary"
          outlined
          @click="showUnsavedDialog = false; pendingNavTarget = null" />
        <Button
          :label="t('admin.events.unsavedLeave')"
          severity="danger"
          @click="confirmLeave" />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ROUTE_PATH } from "~/constants/routes";
import ArtistSelector from "~/components/events/ArtistSelector.vue";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.meEdit") });
const route = useRoute();
const id = route.params.id as string;

const { load: loadProvinces, options: provinceOptions, loading: provincesLoading } = useProvinces();
const { form, posterField, existingPosterUrl, removePoster, loading, submitting, errorMsg, fieldErrors, artistsError, load, submit } = useEditEvent(id);

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

const showSuccessDialog = ref(false);

onMounted(async () => {
  const [loadRes] = await Promise.all([load(), loadProvinces()]);
  if (loadRes) {
    showError({ statusCode: loadRes.status, data: loadRes.pd });
  }
});

async function onSubmit() {
  const result = await submit();
  if (result) {
    showSuccessDialog.value = true;
  }
}

async function onSuccessClose() {
  showSuccessDialog.value = false;
  await navigateTo(ROUTE_PATH.meEventDetail(id));
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTE_PATH.meEventDetail(id)" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("me.edit.title") }}</h1>
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
                  :min-date="today"
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
              <span id="edit-province-label" class="text-sm text-color-secondary">
                {{ t("geo.province") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </span>
              <Select
                v-model="form.provinceId"
                input-id="provinceId"
                aria-labelledby="edit-province-label"
                :options="provinceOptions"
                option-label="label"
                option-value="value"
                :loading="provincesLoading"
                :invalid="!!fieldErrors['provinceId']"
                filter
                required
                :pt="{ label: { 'aria-describedby': 'province-error' } }" />
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

          <!-- Más info (URL externa) -->
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
              :label="t('me.edit.cancel')"
              severity="secondary"
              outlined
              @click="navigateTo(ROUTE_PATH.meEventDetail(id))" />
            <Button
              type="submit"
              :label="t('me.edit.submit')"
              icon="pi pi-check"
              :loading="submitting"
              :disabled="isDateRangeInvalid" />
          </div>
        </form>
      </template>
    </Card>
  </div>

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

  <Dialog
    v-model:visible="showSuccessDialog"
    :header="t('me.edit.successTitle')"
    modal
    :closable="false"
    :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("me.edit.successMsg") }}</p>
    <template #footer>
      <Button :label="t('me.edit.successOk')" icon="pi pi-check" @click="onSuccessClose" />
    </template>
  </Dialog>
</template>

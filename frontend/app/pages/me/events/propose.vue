<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import ArtistSelector from "~/components/events/ArtistSelector.vue";
import type { PossibleDuplicateDto } from "~/types/events";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.mePropose") });
const { load: loadProvinces, options: provinceOptions, loading: provincesLoading } = useProvinces();
const { form, posterFile, submitting, errorMsg, fieldErrors, artistsError, submit } = useProposeEvent();

const posterInputRef = ref<HTMLInputElement | null>(null);
const posterPreviewUrl = computed(() => (posterFile.value ? URL.createObjectURL(posterFile.value) : null));

function onPosterChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0] ?? null;
  posterFile.value = file;
}

function clearPoster() {
  posterFile.value = null;
  if (posterInputRef.value) posterInputRef.value.value = "";
}

const today = new Date();

const isDateRangeInvalid = computed(
  () => !!form.startDateTime && !!form.endDateTime && form.endDateTime < form.startDateTime,
);

const showSuccessDialog = ref(false);
const possibleDuplicate = ref<PossibleDuplicateDto | null>(null);

onMounted(() => loadProvinces());

async function onSubmit() {
  const result = await submit();
  if (result) {
    possibleDuplicate.value = result.possibleDuplicate;
    showSuccessDialog.value = true;
  }
}

async function onSuccessClose() {
  showSuccessDialog.value = false;
  await navigateTo(ROUTES.meEvents);
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.meEvents" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("me.propose.title") }}</h1>
    </div>

    <Card class="border-1 surface-border">
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
              required
              aria-describedby="title-error" />
            <Message v-if="fieldErrors['title']" id="title-error" severity="error" variant="simple" size="small">
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
              auto-resize
              aria-describedby="description-error" />
            <Message v-if="fieldErrors['description']" id="description-error" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["description"]) }}
            </Message>
          </div>

          <!-- Fecha inicio / Fecha fin -->
          <div class="grid">
            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="startDateTime" class="text-sm text-color-secondary">
                {{ t("me.propose.startDate") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </label>
              <DatePicker
                v-model="form.startDateTime"
                input-id="startDateTime"
                show-time
                hour-format="24"
                date-format="dd/mm/yy"
                :min-date="today"
                :invalid="!!fieldErrors['startDateTime']"
                :manual-input="false"
                show-icon
                icon-display="input"
                required
                aria-describedby="start-error" />
              <Message v-if="fieldErrors['startDateTime']" id="start-error" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["startDateTime"]) }}
              </Message>
            </div>

            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="endDateTime" class="text-sm text-color-secondary">{{ t("me.propose.endDate") }}</label>
              <DatePicker
                v-model="form.endDateTime"
                input-id="endDateTime"
                show-time
                hour-format="24"
                date-format="dd/mm/yy"
                :min-date="form.startDateTime ?? undefined"
                :invalid="!!fieldErrors['endDateTime'] || isDateRangeInvalid"
                :manual-input="false"
                show-icon
                icon-display="input"
                aria-describedby="end-error end-range-error" />
              <Message v-if="fieldErrors['endDateTime']" id="end-error" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["endDateTime"]) }}
              </Message>
              <Message v-else-if="isDateRangeInvalid" id="end-range-error" severity="error" variant="simple" size="small">
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
              required
              aria-describedby="venue-error" />
            <Message v-if="fieldErrors['venueName']" id="venue-error" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["venueName"]) }}
            </Message>
          </div>

          <!-- Provincia / Ciudad -->
          <div class="grid">
            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="provinceId" class="text-sm text-color-secondary">
                {{ t("geo.province") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </label>
              <Select
                v-model="form.provinceId"
                input-id="provinceId"
                :options="provinceOptions"
                option-label="label"
                option-value="value"
                :loading="provincesLoading"
                :invalid="!!fieldErrors['provinceId']"
                filter
                required
                :pt="{ label: { 'aria-label': t('geo.province'), 'aria-describedby': 'province-error' } }" />
              <Message v-if="fieldErrors['provinceId']" id="province-error" severity="error" variant="simple" size="small">
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
                required
                aria-describedby="city-error" />
              <Message v-if="fieldErrors['cityName']" id="city-error" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["cityName"]) }}
              </Message>
            </div>
          </div>

          <!-- Artistas -->
          <ArtistSelector v-model="form.artists" :field-error="artistsError ?? undefined" />

          <!-- Cartel -->
          <div class="flex flex-column gap-2">
            <label class="text-sm text-color-secondary">{{ t("events.uploadPoster") }}</label>

            <!-- Preview del fichero seleccionado -->
            <div v-if="posterFile" class="flex flex-column gap-2">
              <img
                :src="posterPreviewUrl!"
                :alt="t('events.posterPreview')"
                class="border-round-lg border-1 surface-border"
                style="max-width: 100%; max-height: 320px; object-fit: contain; background: var(--surface-50)" >
              <div class="flex align-items-center gap-2">
                <span class="text-sm text-color-secondary flex-1 overflow-hidden text-overflow-ellipsis white-space-nowrap">{{ posterFile.name }}</span>
                <Button
                  type="button"
                  :label="t('events.removePoster')"
                  icon="pi pi-times"
                  severity="secondary"
                  outlined
                  size="small"
                  @click="clearPoster" />
              </div>
            </div>

            <!-- Botón de selección -->
            <div v-else>
              <Button
                type="button"
                :label="t('events.uploadPoster')"
                icon="pi pi-upload"
                severity="secondary"
                outlined
                @click="posterInputRef?.click()" />
            </div>

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
              maxlength="2048"
              aria-describedby="source-error" />
            <Message v-if="fieldErrors['sourceUrl']" id="source-error" severity="error" variant="simple" size="small">
              {{ t(fieldErrors["sourceUrl"]) }}
            </Message>
          </div>

          <!-- Acciones -->
          <div class="flex justify-content-end gap-3 pt-2">
            <Button
              type="button"
              :label="t('me.propose.cancel')"
              severity="secondary"
              outlined
              @click="navigateTo(ROUTES.meEvents)" />
            <Button
              type="submit"
              :label="t('me.propose.submit')"
              icon="pi pi-send"
              :loading="submitting"
              :disabled="isDateRangeInvalid" />
          </div>
        </form>
      </template>
    </Card>
  </div>

  <Dialog
    v-model:visible="showSuccessDialog"
    :header="t('me.propose.successTitle')"
    modal
    :closable="false"
    :style="{ width: '24rem' }">
    <p class="m-0 text-color-secondary">{{ t("me.propose.successMsg") }}</p>
    <Message v-if="possibleDuplicate" severity="warn" :closable="false" class="mt-3">
      {{ t("me.propose.duplicateWarning", { title: possibleDuplicate.title }) }}
      <NuxtLink v-if="possibleDuplicate.approved" :to="ROUTE_PATH.eventDetail(possibleDuplicate.id)" class="block mt-1">
        {{ t("me.propose.duplicateLink") }}
      </NuxtLink>
    </Message>
    <template #footer>
      <Button :label="t('me.propose.successOk')" icon="pi pi-check" @click="onSuccessClose" />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ROUTE_PATH } from "~/constants/routes";
import ArtistSelector from "~/components/events/ArtistSelector.vue";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.meEdit") });
const route = useRoute();
const id = route.params.id as string;

const { load: loadProvinces, options: provinceOptions, loading: provincesLoading } = useProvinces();
const { form, loading, submitting, errorMsg, fieldErrors, artistsError, load, submit } = useEditEvent(id);

const isDateRangeInvalid = computed(
  () => !!form.startDateTime && !!form.endDateTime && form.endDateTime < form.startDateTime,
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
              autoResize />
            <Message v-if="fieldErrors['description']" severity="error" variant="simple" size="small">
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
                inputId="startDateTime"
                v-model="form.startDateTime"
                showTime
                hourFormat="24"
                dateFormat="dd/mm/yy"
                :invalid="!!fieldErrors['startDateTime']"
                :manualInput="false"
                showIcon
                iconDisplay="input"
                required />
              <Message v-if="fieldErrors['startDateTime']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["startDateTime"]) }}
              </Message>
            </div>

            <div class="col-12 md:col-6 flex flex-column gap-2">
              <label for="endDateTime" class="text-sm text-color-secondary">{{ t("me.propose.endDate") }}</label>
              <DatePicker
                inputId="endDateTime"
                v-model="form.endDateTime"
                showTime
                hourFormat="24"
                dateFormat="dd/mm/yy"
                :minDate="form.startDateTime ?? undefined"
                :invalid="!!fieldErrors['endDateTime'] || isDateRangeInvalid"
                :manualInput="false"
                showIcon
                iconDisplay="input" />
              <Message v-if="fieldErrors['endDateTime']" severity="error" variant="simple" size="small">
                {{ t(fieldErrors["endDateTime"]) }}
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
              <label for="provinceId" class="text-sm text-color-secondary">
                {{ t("geo.province") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
              </label>
              <Select
                inputId="provinceId"
                v-model="form.provinceId"
                :options="provinceOptions"
                optionLabel="label"
                optionValue="value"
                :loading="provincesLoading"
                :invalid="!!fieldErrors['provinceId']"
                filter
                required
                :pt="{ label: { 'aria-label': t('geo.province'), 'aria-describedby': 'province-error' } }" />
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
          <ArtistSelector v-model="form.artists" :fieldError="artistsError ?? undefined" />

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

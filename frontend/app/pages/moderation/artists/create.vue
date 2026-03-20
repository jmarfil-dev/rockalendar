<script setup lang="ts">
import { ROUTES } from "~/constants/routes";
import type { Artist } from "~/types/artist";

definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("moderation.artists.createTitle") });

const name = ref("");
const submitting = ref(false);
const errorMsg = ref<string | null>(null);
const successVisible = ref(false);
const createdName = ref("");

async function onSubmit() {
  errorMsg.value = null;
  submitting.value = true;

  const res = await fetchAuthResult<Artist>(ROUTES.apiModerationArtists, {
    method: "POST",
    body: { name: name.value.trim() },
  });

  submitting.value = false;

  if (res.ok) {
    createdName.value = res.data.name;
    successVisible.value = true;
  } else if (res.status === 409) {
    errorMsg.value = t("moderation.artists.errorExists");
  } else {
    errorMsg.value = res.pd?.detail ?? t("error.unknown");
  }
}

function onSuccessOk() {
  successVisible.value = false;
  name.value = "";
  errorMsg.value = null;
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.moderation" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("moderation.artists.createTitle") }}</h1>
    </div>

    <div class="grid">
      <div class="col-12 md:col-6 lg:col-4">
        <Card class="border-1 surface-border">
          <template #content>
            <form class="flex flex-column gap-4" @submit.prevent="onSubmit">
              <div class="flex flex-column gap-2">
                <label for="artist-name" class="font-medium">
                  {{ t("moderation.artists.name") }}
                  <span class="text-color-secondary text-sm ml-1">({{ t("common.required") }})</span>
                </label>
                <InputText
                  id="artist-name"
                  v-model="name"
                  :placeholder="t('moderation.artists.namePlaceholder')"
                  :maxlength="200"
                  required
                  autofocus
                  class="w-full"
                  :invalid="!!errorMsg" />
                <Message v-if="errorMsg" severity="error" :closable="false" class="mt-1">{{ errorMsg }}</Message>
              </div>

              <div class="flex gap-2">
                <Button
                  type="submit"
                  :label="t('moderation.artists.submit')"
                  icon="pi pi-plus"
                  :loading="submitting"
                  :disabled="!name.trim()" />
                <Button
                  type="button"
                  :label="t('moderation.artists.cancel')"
                  severity="secondary"
                  outlined
                  :disabled="submitting"
                  @click="navigateTo(ROUTES.moderation)" />
              </div>
            </form>
          </template>
        </Card>
      </div>
    </div>
  </div>

  <Dialog
    v-model:visible="successVisible"
    :header="t('moderation.artists.successTitle')"
    modal
    :closable="false"
    :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">
      {{ t("moderation.artists.successMsg", { name: createdName }) }}
    </p>
    <template #footer>
      <Button :label="t('moderation.artists.successOk')" icon="pi pi-plus" @click="onSuccessOk" />
    </template>
  </Dialog>
</template>

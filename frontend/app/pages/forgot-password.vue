<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "minimal" });

const { t } = useI18n();
useHead({ title: () => t("auth.forgotPassword.title") });

const email = ref("");
const loading = ref(false);
const submitted = ref(false);
const errorMsg = ref<string | null>(null);

async function onSubmit() {
  errorMsg.value = null;
  loading.value = true;
  try {
    await $fetch(ROUTES.apiForgotPassword, {
      method: "POST",
      body: { email: email.value },
    });
    submitted.value = true;
  } catch {
    // Mostramos éxito de todas formas para no revelar si el email existe
    submitted.value = true;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.login" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("auth.forgotPassword.title") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <div v-if="submitted" class="flex flex-column gap-3">
          <Message severity="success" :closable="false">
            {{ t("auth.forgotPassword.successMsg") }}
          </Message>
          <NuxtLink :to="ROUTES.login" class="text-sm font-medium underline">
            {{ t("auth.forgotPassword.backToLogin") }}
          </NuxtLink>
        </div>

        <form v-else class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <p class="text-sm text-color-secondary m-0">{{ t("auth.forgotPassword.desc") }}</p>

          <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

          <AuthEmailField v-model="email" required />

          <Button type="submit" :label="t('auth.forgotPassword.submit')" icon="pi pi-send" :loading="loading" />

          <NuxtLink :to="ROUTES.login" class="text-sm text-surface-500 underline">
            {{ t("auth.forgotPassword.backToLogin") }}
          </NuxtLink>
        </form>
      </template>
    </Card>
  </div>
</template>

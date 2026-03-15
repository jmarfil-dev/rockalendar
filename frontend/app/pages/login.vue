<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "minimal" });

const { t } = useI18n();
const auth = useAuth();
const route = useRoute();
const { form, loading, errorMsg, fieldErrors, resetErrors, tr } = useAuthForm();

async function onSubmit() {
  resetErrors();
  loading.value = true;
  try {
    const res = await auth.login(form);

    if (!res.ok) {
      applyFormErrors(res.pd, t, errorMsg, fieldErrors);
      return;
    }

    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : ROUTES.home;
    await navigateTo(redirect);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Card class="border-1 surface-border">
    <template #title>
      <h1 class="m-0 text-2xl font-semibold">{{ t("auth.login") }}</h1>
    </template>

    <template #content>
      <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

      <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
        <AuthEmailField v-model="form.email" :fieldError="fieldErrors.email" required />

        <div class="flex flex-column gap-2">
          <label for="password" class="text-sm text-color-secondary">{{ t("user.password") }}</label>
          <Password
            id="password"
            v-model="form.password"
            toggleMask
            :feedback="false"
            autocomplete="current-password"
            required
            :invalid="!!fieldErrors.password" />
          <Message v-show="fieldErrors.password" severity="error" variant="simple" size="small">
            {{ tr("fieldErrors.password") }}
          </Message>
        </div>

        <Button type="submit" :label="t('auth.loginButton')" icon="pi pi-sign-in" :loading="loading" />
      </form>

      <Divider class="my-1" />
      <p class="text-sm text-surface-500">
        {{ t("auth.noAccount") }}
        <NuxtLink :to="ROUTES.register" class="font-medium underline">
          {{ t("auth.goRegister") }}
        </NuxtLink>
      </p>
    </template>
  </Card>
</template>

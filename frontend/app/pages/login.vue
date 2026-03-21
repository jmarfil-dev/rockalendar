<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

// PrimeVue añade aria-expanded al <input> del componente Password, lo cual no es válido en inputs.
// Como usamos :feedback="false" no hay panel que expandir, así que lo eliminamos tras cada render.
const vFixPasswordAria = {
  mounted: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
  updated: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
};

definePageMeta({ layout: "minimal" });

const { t } = useI18n();
useHead({ title: () => t("page.login") });
const auth = useAuth();
const route = useRoute();
const { form, loading, errorMsg, fieldErrors, resetErrors } = useAuthForm();

async function onSubmit() {
  resetErrors();
  loading.value = true;
  try {
    const res = await auth.login(form);

    if (!res.ok) {
      applyFormErrors(res.pd, t, errorMsg, fieldErrors);
      return;
    }

    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : ROUTES.me;
    await navigateTo(redirect);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.home" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("auth.login") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

        <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <AuthEmailField v-model="form.email" :fieldError="fieldErrors.email" required />

          <div class="flex flex-column gap-2">
            <label for="password" class="text-sm text-color-secondary">{{ t("user.password") }}</label>
            <Password
              v-fix-password-aria
              inputId="password"
              v-model="form.password"
              toggleMask
              :feedback="false"
              autocomplete="current-password"
              required
              :invalid="!!fieldErrors.password"
              :pt="{ input: { 'aria-describedby': 'login-password-error' } }" />
            <Message
              id="login-password-error"
              v-show="fieldErrors.password"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.password!) }}
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
  </div>
</template>

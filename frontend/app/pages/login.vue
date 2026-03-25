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

const rateLimitSeconds = ref<number | null>(null);
let rateLimitTimer: ReturnType<typeof setInterval> | null = null;

function startRateLimitCountdown(seconds: number) {
  if (rateLimitTimer) clearInterval(rateLimitTimer);
  rateLimitSeconds.value = seconds;
  rateLimitTimer = setInterval(() => {
    if (rateLimitSeconds.value && rateLimitSeconds.value > 1) {
      rateLimitSeconds.value--;
    } else {
      rateLimitSeconds.value = null;
      clearInterval(rateLimitTimer!);
      rateLimitTimer = null;
    }
  }, 1000);
}

onUnmounted(() => {
  if (rateLimitTimer) clearInterval(rateLimitTimer);
});

async function onSubmit() {
  resetErrors();
  rateLimitSeconds.value = null;
  loading.value = true;
  try {
    const res = await auth.login(form);

    if (!res.ok) {
      if (res.status === 429) {
        startRateLimitCountdown((res.pd?.retryAfter as number) ?? 60);
        return;
      }
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
        <Message v-if="rateLimitSeconds" severity="warn" :closable="false">
          {{ t("error.429.rateLimitExceeded", { seconds: rateLimitSeconds }) }}
        </Message>
        <Message v-else-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

        <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <AuthEmailField v-model="form.email" :field-error="fieldErrors.email" required />

          <div class="flex flex-column gap-2">
            <label for="password" class="text-sm text-color-secondary">{{ t("user.password") }}</label>
            <Password
              v-model="form.password"
              v-fix-password-aria
              input-id="password"
              toggle-mask
              :feedback="false"
              autocomplete="current-password"
              required
              :invalid="!!fieldErrors.password"
              :pt="{ input: { 'aria-describedby': 'login-password-error' } }" />
            <Message
              v-show="fieldErrors.password"
              id="login-password-error"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.password!) }}
            </Message>
          </div>

          <Button type="submit" :label="t('auth.loginButton')" icon="pi pi-sign-in" :loading="loading" />

          <NuxtLink :to="ROUTES.forgotPassword" class="text-sm text-surface-500 underline text-right">
            {{ t("auth.forgotPasswordLink") }}
          </NuxtLink>
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

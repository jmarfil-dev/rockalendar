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
useHead({ title: () => t("page.register") });
const auth = useAuth();
const { form, loading, errorMsg, fieldErrors, resetErrors, tr } = useAuthForm();

const passwordChecks = computed(() => {
  const pw = form.password || "";
  return [
    { ok: pw.length >= 8, key: "auth.pw.length" },
    { ok: /[a-z]/.test(pw), key: "auth.pw.lower" },
    { ok: /[A-Z]/.test(pw), key: "auth.pw.upper" },
    { ok: /\d/.test(pw), key: "auth.pw.number" },
    { ok: /[^A-Za-z\d]/.test(pw), key: "auth.pw.symbol" },
  ] as const;
});

const isPasswordValid = computed(() => passwordChecks.value.every((x) => x.ok));

const privacyAccepted = ref(false);

async function onSubmit() {
  if (!isPasswordValid.value) {
    errorMsg.value = t("auth.pw.invalid");
    return;
  }

  if (!privacyAccepted.value) {
    errorMsg.value = t("auth.privacyRequired");
    return;
  }

  resetErrors();
  loading.value = true;
  try {
    const res = await auth.register({ ...form, privacyAccepted: privacyAccepted.value });

    if (!res.ok) {
      applyFormErrors(res.pd, t, errorMsg, fieldErrors);
      return;
    }

    await navigateTo(ROUTES.me);
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
      <h1 class="text-2xl font-bold m-0">{{ t("auth.register") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

        <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <AuthEmailField v-model="form.email" :fieldError="fieldErrors.email" showRequired required />

          <div class="flex flex-column gap-2">
            <label for="password" class="text-sm text-color-secondary">
              {{ t("user.password") }}
              <span class="text-red-500" aria-hidden="true">*</span
              ><span class="sr-only">{{ t("common.required") }}</span>
            </label>
            <Password
              v-fix-password-aria
              inputId="password"
              v-model="form.password"
              toggleMask
              :feedback="false"
              autocomplete="new-password"
              required
              :invalid="!!fieldErrors.password"
              :pt="{ input: { 'aria-describedby': 'pw-requirements register-password-error pw-hint' } }" />
            <Message
              id="register-password-error"
              v-show="fieldErrors.password"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.password!) }}
            </Message>

            <!-- Checklist de requisitos de contraseña -->
            <div id="pw-requirements" class="border-1 surface-border border-round-lg p-2">
              <div class="text-sm text-color-secondary mb-2">{{ t("auth.pw.title") }}</div>

              <ul class="m-0 p-0 list-none flex flex-column gap-1">
                <li v-for="c in passwordChecks" :key="c.key" class="flex align-items-center gap-2">
                  <i :class="c.ok ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-color-secondary'"></i>
                  <span :class="c.ok ? '' : 'text-color-secondary'">{{ t(c.key) }}</span>
                </li>
              </ul>
            </div>

            <Message id="pw-hint" v-show="!isPasswordValid" severity="error" variant="simple" size="small">
              {{ t("auth.pw.hint") }}
            </Message>
          </div>

          <div class="flex flex-column gap-2">
            <div class="flex align-items-start gap-2">
              <Checkbox inputId="privacy-accept" v-model="privacyAccepted" :binary="true" />
              <label for="privacy-accept" class="text-sm">
                <i18n-t keypath="auth.privacyAccept" tag="span">
                  <template #link>
                    <NuxtLink :to="ROUTES.privacy" target="_blank" class="underline">{{ t("page.privacy") }}</NuxtLink>
                  </template>
                </i18n-t>
              </label>
            </div>
            <Message
              id="register-privacy-accept-error"
              v-show="fieldErrors.privacyAccepted"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.privacyAccepted!) }}
            </Message>
          </div>

          <Button type="submit" :label="t('auth.register')" icon="pi pi-user-plus" :loading="loading" />
        </form>

        <Divider class="my-1" />
        <p class="text-sm text-surface-500">
          <NuxtLink :to="ROUTES.login" class="font-medium underline">
            {{ t("auth.goLogin") }}
          </NuxtLink>
        </p>
      </template>
    </Card>
  </div>
</template>

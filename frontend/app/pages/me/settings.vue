<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

// PrimeVue añade aria-expanded al <input> del componente Password, lo cual no es válido en inputs.
const vFixPasswordAria = {
  mounted: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
  updated: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
};

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.meSettings") });
const toast = useToast();

const form = reactive({ currentPassword: "", newPassword: "", confirmPassword: "" });
const loading = ref(false);
const errorMsg = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

const passwordChecks = computed(() => {
  const pw = form.newPassword || "";
  return [
    { ok: pw.length >= 8, key: "auth.pw.length" },
    { ok: /[a-z]/.test(pw), key: "auth.pw.lower" },
    { ok: /[A-Z]/.test(pw), key: "auth.pw.upper" },
    { ok: /\d/.test(pw), key: "auth.pw.number" },
    { ok: /[^A-Za-z\d]/.test(pw), key: "auth.pw.symbol" },
  ] as const;
});

const isPasswordValid = computed(() => passwordChecks.value.every((x) => x.ok));

async function onSubmit() {
  errorMsg.value = null;
  fieldErrors.value = {};

  if (!isPasswordValid.value) {
    errorMsg.value = t("auth.pw.invalid");
    return;
  }

  if (form.newPassword !== form.confirmPassword) {
    errorMsg.value = t("me.settings.passwordMismatch");
    return;
  }

  loading.value = true;
  try {
    const res = await fetchAuthResult<void>(ROUTES.apiMePassword, {
      method: "PUT",
      body: {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword,
      },
    });

    if (!res.ok) {
      applyFormErrors(res.pd, t, errorMsg, fieldErrors);
      return;
    }

    form.currentPassword = "";
    form.newPassword = "";
    form.confirmPassword = "";

    toast.add({
      severity: "success",
      summary: t("me.settings.successTitle"),
      detail: t("me.settings.successMsg"),
      life: 4000,
    });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.me" class="text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ t("me.settings.title") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #title>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-lock text-xl text-primary" />
          <span class="text-lg">{{ t("me.settings.changePassword") }}</span>
        </div>
      </template>
      <template #content>
        <Message v-if="errorMsg" severity="error" :closable="false" class="mb-3">{{ errorMsg }}</Message>

        <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <!-- Contraseña actual -->
          <div class="flex flex-column gap-2">
            <label for="current-password" class="text-sm text-color-secondary">
              {{ t("me.settings.currentPassword") }}
              <span class="text-red-500" aria-hidden="true">*</span
              ><span class="sr-only">{{ t("common.required") }}</span>
            </label>
            <Password
              v-fix-password-aria
              inputId="current-password"
              v-model="form.currentPassword"
              toggleMask
              :feedback="false"
              autocomplete="current-password"
              required
              :invalid="!!fieldErrors.currentPassword" />
            <Message
              id="current-password-error"
              v-show="fieldErrors.currentPassword"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.currentPassword!) }}
            </Message>
          </div>

          <!-- Nueva contraseña -->
          <div class="flex flex-column gap-2">
            <label for="new-password" class="text-sm text-color-secondary">
              {{ t("me.settings.newPassword") }}
              <span class="text-red-500" aria-hidden="true">*</span
              ><span class="sr-only">{{ t("common.required") }}</span>
            </label>
            <Password
              v-fix-password-aria
              inputId="new-password"
              v-model="form.newPassword"
              toggleMask
              :feedback="false"
              autocomplete="new-password"
              required
              :invalid="!!fieldErrors.newPassword"
              :pt="{ input: { 'aria-describedby': 'pw-requirements pw-hint' } }" />
            <Message
              id="new-password-error"
              v-show="fieldErrors.newPassword"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.newPassword!) }}
            </Message>

            <!-- Checklist de requisitos -->
            <div id="pw-requirements" class="border-1 surface-border border-round-lg p-2">
              <div class="text-sm text-color-secondary mb-2">{{ t("auth.pw.title") }}</div>
              <ul class="m-0 p-0 list-none flex flex-column gap-1">
                <li v-for="c in passwordChecks" :key="c.key" class="flex align-items-center gap-2">
                  <i :class="c.ok ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-color-secondary'" />
                  <span :class="c.ok ? '' : 'text-color-secondary'">{{ t(c.key) }}</span>
                </li>
              </ul>
            </div>
            <Message
              id="pw-hint"
              v-show="!isPasswordValid && form.newPassword"
              severity="error"
              variant="simple"
              size="small">
              {{ t("auth.pw.hint") }}
            </Message>
          </div>

          <!-- Confirmar nueva contraseña -->
          <div class="flex flex-column gap-2">
            <label for="confirm-password" class="text-sm text-color-secondary">
              {{ t("me.settings.confirmPassword") }}
              <span class="text-red-500" aria-hidden="true">*</span
              ><span class="sr-only">{{ t("common.required") }}</span>
            </label>
            <Password
              v-fix-password-aria
              inputId="confirm-password"
              v-model="form.confirmPassword"
              toggleMask
              :feedback="false"
              autocomplete="new-password"
              required />
            <Message
              id="confirm-password-error"
              v-show="fieldErrors.confirmPassword"
              severity="error"
              variant="simple"
              size="small">
              {{ tr(fieldErrors.confirmPassword!) }}
            </Message>
          </div>

          <Button type="submit" :label="t('me.settings.submit')" icon="pi pi-check" :loading="loading" />
        </form>
      </template>
    </Card>
  </div>
</template>

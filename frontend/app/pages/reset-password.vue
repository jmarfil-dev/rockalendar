<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

// PrimeVue añade aria-expanded al <input> del componente Password, lo cual no es válido en inputs.
const vFixPasswordAria = {
  mounted: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
  updated: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
};

definePageMeta({ layout: "minimal" });

const { t } = useI18n();
useHead({ title: () => t("auth.resetPassword.title") });

const route = useRoute();
const token = computed(() => (typeof route.query.token === "string" ? route.query.token : ""));

const newPassword = ref("");
const confirmPassword = ref("");
const loading = ref(false);
const done = ref(false);
const errorMsg = ref<string | null>(null);

const passwordChecks = computed(() => {
  const pw = newPassword.value;
  return [
    { ok: pw.length >= 8, key: "auth.pw.length" },
    { ok: /[a-z]/.test(pw), key: "auth.pw.lower" },
    { ok: /[A-Z]/.test(pw), key: "auth.pw.upper" },
    { ok: /\d/.test(pw), key: "auth.pw.number" },
    { ok: /[^A-Za-z\d]/.test(pw), key: "auth.pw.symbol" },
  ] as const;
});

const isPasswordValid = computed(() => passwordChecks.value.every((x) => x.ok));
const passwordsMatch = computed(() => newPassword.value === confirmPassword.value);

async function onSubmit() {
  errorMsg.value = null;

  if (!isPasswordValid.value) {
    errorMsg.value = t("auth.pw.invalid");
    return;
  }
  if (!passwordsMatch.value) {
    errorMsg.value = t("me.settings.passwordMismatch");
    return;
  }

  loading.value = true;
  try {
    await $fetch(ROUTES.apiResetPassword, {
      method: "POST",
      body: { token: token.value, newPassword: newPassword.value },
    });
    done.value = true;
  } catch {
    errorMsg.value = t("auth.resetPassword.errorMsg");
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
      <h1 class="text-2xl font-bold m-0">{{ t("auth.resetPassword.title") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <div v-if="!token" class="flex flex-column gap-3">
          <Message severity="error" :closable="false">{{ t("auth.resetPassword.noToken") }}</Message>
          <NuxtLink :to="ROUTES.forgotPassword" class="text-sm font-medium underline">
            {{ t("auth.resetPassword.requestNew") }}
          </NuxtLink>
        </div>

        <div v-else-if="done" class="flex flex-column gap-3">
          <Message severity="success" :closable="false">{{ t("auth.resetPassword.successMsg") }}</Message>
          <NuxtLink :to="ROUTES.login" class="text-sm font-medium underline">
            {{ t("auth.forgotPassword.backToLogin") }}
          </NuxtLink>
        </div>

        <form v-else class="flex flex-column gap-3" @submit.prevent="onSubmit">
          <p class="text-sm text-color-secondary m-0">{{ t("auth.resetPassword.desc") }}</p>

          <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

          <!-- Nueva contraseña -->
          <div class="flex flex-column gap-2">
            <label for="new-password" class="text-sm text-color-secondary">{{ t("me.settings.newPassword") }}</label>
            <Password
              v-model="newPassword"
              v-fix-password-aria
              input-id="new-password"
              toggle-mask
              :feedback="false"
              autocomplete="new-password"
              required
              :pt="{ input: { 'aria-describedby': 'reset-pw-requirements' } }" />

            <!-- Checklist de requisitos -->
            <div id="reset-pw-requirements" class="border-1 surface-border border-round-lg p-2">
              <div class="text-sm text-color-secondary mb-2">{{ t("auth.pw.title") }}</div>
              <ul class="m-0 p-0 list-none flex flex-column gap-1">
                <li v-for="c in passwordChecks" :key="c.key" class="flex align-items-center gap-2">
                  <i :class="c.ok ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-color-secondary'" aria-hidden="true"/>
                  <span :class="c.ok ? '' : 'text-color-secondary'">{{ t(c.key) }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- Confirmar contraseña -->
          <div class="flex flex-column gap-2">
            <label for="confirm-password" class="text-sm text-color-secondary">{{ t("me.settings.confirmPassword") }}</label>
            <Password
              v-model="confirmPassword"
              v-fix-password-aria
              input-id="confirm-password"
              toggle-mask
              :feedback="false"
              autocomplete="new-password"
              required
              :invalid="confirmPassword.length > 0 && !passwordsMatch" />
            <Message
              v-show="confirmPassword.length > 0 && !passwordsMatch"
              severity="error"
              variant="simple"
              size="small">
              {{ t("me.settings.passwordMismatch") }}
            </Message>
          </div>

          <Button type="submit" :label="t('auth.resetPassword.submit')" icon="pi pi-lock" :loading="loading" />
        </form>
      </template>
    </Card>
  </div>
</template>

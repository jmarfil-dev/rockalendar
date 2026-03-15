<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "minimal" });

const { t } = useI18n();
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

async function onSubmit() {
  if (!isPasswordValid.value) {
    errorMsg.value = t("auth.pw.invalid");
    return;
  }

  resetErrors();
  loading.value = true;
  try {
    const res = await auth.register(form);

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
  <Card class="border-1 surface-border">
    <template #title>
      <h1 class="m-0 text-2xl font-semibold">{{ t("auth.register") }}</h1>
    </template>

    <template #content>
      <Message v-if="errorMsg" severity="error" :closable="false">{{ errorMsg }}</Message>

      <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
        <AuthEmailField v-model="form.email" :fieldError="fieldErrors.email" />

        <div class="flex flex-column gap-2">
          <label for="password" class="text-sm text-color-secondary">{{ t("user.password") }}</label>
          <Password
            id="password"
            v-model="form.password"
            toggleMask
            :feedback="false"
            autocomplete="new-password"
            required
            :invalid="!!fieldErrors.password" />
          <Message v-show="fieldErrors.password" severity="error" variant="simple" size="small">
            {{ tr("fieldErrors.password") }}
          </Message>

          <!-- Checklist de requisitos de contraseña -->
          <div class="border-1 surface-border border-round-lg p-2">
            <div class="text-sm text-color-secondary mb-2">{{ t("auth.pw.title") }}</div>

            <ul class="m-0 p-0 list-none flex flex-column gap-1">
              <li v-for="c in passwordChecks" :key="c.key" class="flex align-items-center gap-2">
                <i :class="c.ok ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-color-secondary'"></i>
                <span :class="c.ok ? '' : 'text-color-secondary'">{{ t(c.key) }}</span>
              </li>
            </ul>
          </div>

          <Message v-show="!isPasswordValid" severity="error" variant="simple" size="small">
            {{ t("auth.pw.hint") }}
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
</template>

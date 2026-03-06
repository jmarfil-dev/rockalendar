<script setup lang="ts">
import { ROUTES } from "~/constants/routes";
import type { LoginRequest } from "~/types/auth";

definePageMeta({ layout: "minimal" });

const { t, te } = useI18n();
const auth = useAuth();
const route = useRoute();

const form = reactive<LoginRequest>({
  email: "",
  password: "",
});
const loading = ref(false);
const errorMsg = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

function resetErrors() {
  errorMsg.value = null;
  fieldErrors.value = {};
}

function tr(key: string) {
  return te(key) ? t(key) : key;
}

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
        <div class="flex flex-column gap-2">
          <label for="email" class="text-sm text-color-secondary">{{ t("user.email") }}</label>
          <InputText
            id="email"
            v-model="form.email"
            type="email"
            inputmode="email"
            autocomplete="email"
            required
            :invalid="!!fieldErrors.email" />
          <Message v-show="fieldErrors.email" severity="error" variant="simple" size="small">
            {{ tr("fieldErrors.email") }}
          </Message>
        </div>

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

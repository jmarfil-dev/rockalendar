<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

// PrimeVue añade aria-expanded al <input> del componente Password, lo cual no es válido en inputs.
const vFixPasswordAria = {
  mounted: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
  updated: (el: HTMLElement) => el.querySelector("input")?.removeAttribute("aria-expanded"),
};

definePageMeta({ layout: "private", ssr: false });

const { t, locale, setLocale, locales } = useI18n();
useHead({ title: () => t("page.meSettings") });
const toast = useToast();
const { me, fetchMe } = useMe();

const localeOptions = computed(() =>
  (locales.value as { code: string; name: string }[]).map((l) => ({ label: l.name, value: l.code }))
);
const selectedLocale = ref<"es" | "en">(locale.value as "es" | "en");
const localeLoading = ref(false);

// Sincronizar con el valor guardado en BD cuando cargue el perfil
watch(me, (val) => {
  if (val?.preferredLanguage) selectedLocale.value = val.preferredLanguage as "es" | "en";
});

async function saveLocale() {
  localeLoading.value = true;
  try {
    const res = await fetchAuthResult<undefined>(ROUTES.apiMeLocale, {
      method: "PUT",
      body: { locale: selectedLocale.value },
    });
    if (!res.ok) return;
    if (me.value) me.value.preferredLanguage = selectedLocale.value;
    await setLocale(selectedLocale.value);
    toast.add({ severity: "success", summary: t("me.settings.changeLanguageSaved"), life: 3000 });
  } finally {
    localeLoading.value = false;
  }
}

const form = reactive({ currentPassword: "", newPassword: "", confirmPassword: "" });
const loading = ref(false);
const errorMsg = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

// Eliminación de cuenta
const showDeleteModal = ref(false);
const deletionLoading = ref(false);
const cancelLoading = ref(false);

onMounted(fetchMe);

const deletionDate = computed(() => {
  if (!me.value?.deletionRequestedAt) return null;
  const d = new Date(me.value.deletionRequestedAt);
  d.setDate(d.getDate() + 7);
  return d.toLocaleDateString(undefined, { day: "numeric", month: "long", year: "numeric" });
});

async function confirmDeletion() {
  deletionLoading.value = true;
  try {
    const res = await fetchAuthResult<undefined>(ROUTES.apiMe, { method: "DELETE" });
    if (!res.ok) {
      toast.add({ severity: "error", summary: t("me.settings.deleteAccount.errorTitle"), detail: t("me.settings.deleteAccount.errorMsg"), life: 4000 });
      return;
    }
    showDeleteModal.value = false;
    await fetchMe();
    toast.add({ severity: "warn", summary: t("me.settings.deleteAccount.requestedTitle"), detail: t("me.settings.deleteAccount.requestedMsg"), life: 6000 });
  } finally {
    deletionLoading.value = false;
  }
}

async function cancelDeletion() {
  cancelLoading.value = true;
  try {
    const res = await fetchAuthResult<undefined>(ROUTES.apiMeCancelDeletion, { method: "POST" });
    if (!res.ok) {
      toast.add({ severity: "error", summary: t("me.settings.deleteAccount.errorTitle"), detail: t("me.settings.deleteAccount.errorMsg"), life: 4000 });
      return;
    }
    await fetchMe();
    toast.add({ severity: "success", summary: t("me.settings.deleteAccount.cancelledTitle"), detail: t("me.settings.deleteAccount.cancelledMsg"), life: 4000 });
  } finally {
    cancelLoading.value = false;
  }
}

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
    const res = await fetchAuthResult<undefined>(ROUTES.apiMePassword, {
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

    <!-- Idioma preferido -->
    <Card class="border-1 surface-border">
      <template #title>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-globe text-xl text-primary" />
          <span class="text-lg">{{ t("me.settings.changeLanguage") }}</span>
        </div>
      </template>
      <template #content>
        <p class="text-sm text-color-secondary mt-0 mb-3">{{ t("me.settings.changeLanguageDesc") }}</p>
        <div class="flex align-items-center gap-3">
          <SelectButton
            v-model="selectedLocale"
            :options="localeOptions"
            option-label="label"
            option-value="value"
            :disabled="localeLoading" />
          <Button
            :label="t('common.save')"
            icon="pi pi-check"
            :loading="localeLoading"
            :disabled="!selectedLocale || selectedLocale === (me?.preferredLanguage ?? locale)"
            @click="saveLocale" />
        </div>
      </template>
    </Card>

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
              v-model="form.currentPassword"
              v-fix-password-aria
              input-id="current-password"
              toggle-mask
              :feedback="false"
              autocomplete="current-password"
              required
              :invalid="!!fieldErrors.currentPassword" />
            <Message
              v-show="fieldErrors.currentPassword"
              id="current-password-error"
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
              v-model="form.newPassword"
              v-fix-password-aria
              input-id="new-password"
              toggle-mask
              :feedback="false"
              autocomplete="new-password"
              required
              :invalid="!!fieldErrors.newPassword"
              :pt="{ input: { 'aria-describedby': 'pw-requirements pw-hint' } }" />
            <Message
              v-show="fieldErrors.newPassword"
              id="new-password-error"
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
              v-show="!isPasswordValid && form.newPassword"
              id="pw-hint"
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
              v-model="form.confirmPassword"
              v-fix-password-aria
              input-id="confirm-password"
              toggle-mask
              :feedback="false"
              autocomplete="new-password"
              required />
            <Message
              v-show="fieldErrors.confirmPassword"
              id="confirm-password-error"
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
    <!-- Eliminar cuenta -->
    <Card class="border-1 border-red-300">
      <template #title>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-trash text-xl text-red-500" />
          <span class="text-lg">{{ t("me.settings.deleteAccount.title") }}</span>
        </div>
      </template>
      <template #content>
        <!-- Solicitud pendiente -->
        <div v-if="me?.deletionRequestedAt" class="flex flex-column gap-3">
          <Message severity="warn" :closable="false">
            {{ t("me.settings.deleteAccount.pendingWarning", { date: deletionDate }) }}
          </Message>
          <Button
            :label="t('me.settings.deleteAccount.cancelBtn')"
            icon="pi pi-undo"
            severity="secondary"
            :loading="cancelLoading"
            @click="cancelDeletion" />
        </div>

        <!-- Sin solicitud activa -->
        <div v-else class="flex flex-column gap-3">
          <p class="m-0 text-color-secondary">{{ t("me.settings.deleteAccount.desc") }}</p>
          <Button
            :label="t('me.settings.deleteAccount.requestBtn')"
            icon="pi pi-trash"
            severity="danger"
            outlined
            @click="showDeleteModal = true" />
        </div>
      </template>
    </Card>
  </div>

  <!-- Modal confirmación eliminación -->
  <Dialog
    v-model:visible="showDeleteModal"
    modal
    :header="t('me.settings.deleteAccount.modalTitle')"
    :style="{ width: '28rem' }">
    <p class="m-0 mb-3 line-height-3">{{ t("me.settings.deleteAccount.modalBody") }}</p>
    <template #footer>
      <Button :label="t('me.settings.deleteAccount.modalCancel')" severity="secondary" text @click="showDeleteModal = false" />
      <Button
        :label="t('me.settings.deleteAccount.modalConfirm')"
        icon="pi pi-trash"
        severity="danger"
        :loading="deletionLoading"
        @click="confirmDeletion" />
    </template>
  </Dialog>
</template>

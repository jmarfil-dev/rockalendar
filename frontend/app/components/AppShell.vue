<script setup lang="ts">
import type { AppLocale, LocaleOption } from "~/types/languages";

defineProps<{
  bottomItems?: {
    label: string;
    icon: string;
    action: () => void;
    text?: boolean;
  }[];
}>();

const { t, locale, setLocale } = useI18n();
const auth = useAuth();

const localeOptions: LocaleOption[] = [
  { value: "en", label: "English", flagSrc: "/flags/gb.svg" },
  { value: "es", label: "Español", flagSrc: "/flags/es.svg" },
];

const currentLocale = computed<AppLocale>({
  get: () => (locale.value as AppLocale) ?? "en",
  set: (val) => setLocale(val),
});

const optionByValue = (val: AppLocale) => localeOptions.find((o) => o.value === val)!;

const onUserClick = () => {
  if (auth.isAuthenticated.value) return navigateTo("/me/events");
  return navigateTo("/login");
};
</script>

<template>
  <div class="min-h-screen flex flex-column">
    <!-- Header -->
    <header class="surface-0 mt-2">
      <div class="mx-auto w-full max-w-7xl px-3 py-1 flex align-items-center justify-content-between gap-2">
        <NuxtLink to="/" class="no-underline flex align-items-center">
          <img src="/banner.png" alt="Rockalendar" style="margin-top: -1.5rem; margin-bottom: -1.5rem; height: 5rem" />
        </NuxtLink>

        <Select
          v-model="currentLocale"
          :options="localeOptions"
          optionLabel="label"
          optionValue="value"
          class="w-4rem"
          :aria-label="t('common.changeLanguage')">
          <!-- Cerrado: solo bandera -->
          <template #value="slotProps">
            <div class="flex align-items-center justify-content-center w-full">
              <img
                v-if="slotProps.value"
                :src="optionByValue(slotProps.value).flagSrc"
                :alt="optionByValue(slotProps.value).label"
                :title="optionByValue(slotProps.value).label"
                width="18"
                height="12"
                style="display: block" />
            </div>
          </template>

          <!-- Abierto: bandera + texto -->
          <template #option="slotProps">
            <div class="flex align-items-center gap-2">
              <img
                :src="slotProps.option.flagSrc"
                :alt="slotProps.option.label"
                width="18"
                height="12"
                style="display: block" />
              <span>{{ slotProps.option.label }}</span>
            </div>
          </template>
        </Select>

        <Button
          icon="pi pi-user"
          rounded
          outlined
          :aria-label="auth.isAuthenticated ? t('user.myAccount') : t('auth.login')"
          @click="onUserClick" />
      </div>
    </header>

    <!-- Body -->
    <main class="flex-1 surface-0 mb-5">
      <div class="mx-auto w-full max-w-7xl px-3 py-4">
        <slot />
      </div>
    </main>

    <!-- Bottom nav -->
    <nav
      v-if="bottomItems && bottomItems.length"
      class="surface-900 border-top-1 surface-border fixed bottom-0 left-0 right-0"
      :aria-label="t('common.bottomNav')">
      <div class="mx-auto w-full max-w-7xl px-2 py-2 flex justify-content-around">
        <Button
          v-for="item in bottomItems"
          :key="item.label"
          :label="item.label"
          :icon="item.icon"
          :text="item.text ?? false"
          @click="item.action" />
      </div>
    </nav>
  </div>
</template>

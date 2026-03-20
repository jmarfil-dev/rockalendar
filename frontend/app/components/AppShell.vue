<script setup lang="ts">
import { ROUTES } from "~/constants/routes";
import type { AppLocale, LocaleOption } from "~/types/languages";
import type { BottomItem } from "~/types/components";

// ------- Props -------
const props = defineProps<{
  bottomItems?: BottomItem[];
}>();

const { t, locale, setLocale } = useI18n();
const { isAuthenticated, user, logout } = useAuth();

// ------- Header -------
const localeOptions: LocaleOption[] = [
  { value: "en", label: "English", flagSrc: "/flags/gb.svg" },
  { value: "es", label: "Español", flagSrc: "/flags/es.svg" },
];

const optionByValue = (val: AppLocale) => localeOptions.find((o) => o.value === val)!;

const currentLocale = computed<AppLocale>({
  get: () => (locale.value as AppLocale) ?? "en",
  set: (val) => setLocale(val),
});

const onUserClick = () => {
  openUserDrawer();
};

// ------- Navbar -------
const bottomById = computed(() => {
  const m = new Map<string, BottomItem>();
  for (const it of props.bottomItems ?? []) m.set(it.id, it);
  return m;
});

const proposeItem = computed(() => bottomById.value.get("propose") ?? null);
const searchItem = computed(() => bottomById.value.get("search") ?? null);
const meItem = computed(() => bottomById.value.get("me") ?? null);
const moderationItem = computed(() => bottomById.value.get("moderation") ?? null);

// ------- Search Drawer (derecha) -------
const {
  isOpen: isSearchOpen,
  close: closeSearch,
  runSearch,
  searchForm,
  selectedArtist,
  provinceOptions,
  artistSuggestions,
  artistLoading,
  isDateRangeInvalid,
  searchArtists,
} = useSearchDrawer();

// ------- User Drawer (izquierda) -------
const { isOpen: isUserDrawerOpen, open: openUserDrawer, close: closeUserDrawer } = useUserDrawer();

const goToLogin = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.login);
};

const goToRegister = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.register);
};

const goToMyArea = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.me);
};

const onSettingsClick = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.meSettings);
};

const onLogoutClick = async () => {
  closeUserDrawer();
  await logout();
};
</script>

<template>
  <div class="min-h-screen flex flex-column">
    <!-- Skip link: visible solo al recibir foco (usuarios de teclado) -->
    <a href="#main-content" class="skip-link">{{ t('common.skipToContent') }}</a>

    <!-- Header -->
    <header class="surface-0 mt-2">
      <div class="mx-auto w-full max-w-7xl px-3 py-1 flex align-items-center justify-content-between gap-2">
        <NuxtLink :to="ROUTES.home" class="no-underline flex align-items-center">
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
          :aria-label="isAuthenticated ? t('user.myAccount') : t('auth.login')"
          @click="onUserClick" />
      </div>
    </header>

    <!-- Body -->
    <main id="main-content" class="flex-1 surface-0">
      <div class="mx-auto w-full max-w-7xl px-3 py-4">
        <slot />
      </div>
    </main>

    <!-- Footer: pb-6 cuando hay bottom nav fixed para no quedar tapado -->
    <footer class="surface-0 border-top-1 surface-border" :class="{ 'pb-6': props.bottomItems?.length }">
      <div class="mx-auto w-full max-w-7xl px-3 py-3 flex justify-content-center gap-4">
        <NuxtLink :to="ROUTES.about" class="text-sm text-color-secondary no-underline hover:underline">
          {{ t("page.about") }}
        </NuxtLink>
        <NuxtLink :to="ROUTES.privacy" class="text-sm text-color-secondary no-underline hover:underline">
          {{ t("page.privacy") }}
        </NuxtLink>
        <NuxtLink :to="ROUTES.contact" class="text-sm text-color-secondary no-underline hover:underline">
          {{ t("page.contact") }}
        </NuxtLink>
      </div>
    </footer>

    <!-- Bottom nav -->
    <nav
      v-if="props.bottomItems && props.bottomItems.length"
      class="surface-900 border-top-1 surface-border fixed bottom-0 left-0 right-0"
      :aria-label="t('common.bottomNav')">
      <div class="mx-auto w-full max-w-7xl flex justify-content-around">
        <Button
          v-if="proposeItem"
          :key="proposeItem.id"
          :icon="proposeItem.icon"
          size="large"
          text
          rounded
          :aria-label="proposeItem.label"
          @click="proposeItem.action" />

        <Button
          v-if="searchItem"
          :key="searchItem.id"
          :icon="searchItem.icon"
          size="large"
          text
          rounded
          :aria-label="searchItem.label"
          @click="searchItem.action" />

        <Button
          v-if="meItem"
          :key="meItem.id"
          :icon="meItem.icon"
          size="large"
          text
          rounded
          :aria-label="meItem.label"
          @click="meItem.action" />

        <Button
          v-if="moderationItem"
          :key="moderationItem.id"
          :icon="moderationItem.icon"
          size="large"
          text
          rounded
          :aria-label="moderationItem.label"
          @click="moderationItem.action" />
      </div>
    </nav>

    <!-- User drawer (derecha) -->
    <Drawer v-model:visible="isUserDrawerOpen" position="right" :style="{ width: '340px' }" @hide="closeUserDrawer">
      <aside class="flex flex-column h-full">
        <header class="flex align-items-center gap-3 mb-3">
          <i class="pi pi-user text-2xl" />
          <div v-if="isAuthenticated && user" class="font-medium">
            {{ user.email.split('@')[0] }}
          </div>
        </header>

        <Divider class="my-2" />

        <section class="flex flex-column gap-2">
          <template v-if="!isAuthenticated">
            <Button
              :label="t('auth.login')"
              icon="pi pi-sign-in"
              text
              class="justify-content-start"
              @click="goToLogin" />
            <Button
              :label="t('auth.goRegister')"
              icon="pi pi-user-plus"
              text
              class="justify-content-start"
              @click="goToRegister" />
          </template>

          <template v-else>
            <Button
              :label="t('user.myAccount')"
              icon="pi pi-user"
              text
              class="justify-content-start"
              @click="goToMyArea" />
            <Button
              :label="t('user.settings')"
              icon="pi pi-cog"
              text
              class="justify-content-start"
              @click="onSettingsClick" />
            <Button
              :label="t('auth.logout')"
              icon="pi pi-sign-out"
              text
              class="justify-content-start"
              @click="onLogoutClick" />
          </template>
        </section>
      </aside>
    </Drawer>

    <!-- Search sidebar (Izquiera) -->
    <Drawer
      v-model:visible="isSearchOpen"
      position="left"
      header="Let's Rock!!"
      :style="{ width: '340px' }"
      @hide="closeSearch">
      <section class="flex flex-column gap-4 mt-2">
        <form class="flex flex-column gap-4 mt-2" @submit.prevent="runSearch">
          <!-- Artista -->
          <div class="flex flex-column gap-2">
            <label for="artist" class="text-sm text-color-secondary"
              >{{ t("events.group") }} / {{ t("events.artist") }}</label
            >
            <AutoComplete
              v-model="selectedArtist"
              inputId="artist"
              :suggestions="artistSuggestions"
              :loading="artistLoading"
              :minLength="2"
              :maxLength="50"
              optionLabel="name"
              :placeholder="`${t('common.example')}. Elektroduendes`"
              inputClass="w-full"
              @complete="(e) => searchArtists(e.query)" />
          </div>

          <!-- Ciudad -->
          <div class="flex flex-column gap-2">
            <label for="city" class="text-sm text-color-secondary">{{ t("geo.city") }}</label>
            <InputText
              id="city"
              v-model="searchForm.city"
              :placeholder="`${t('common.example')}. Barcelona`"
              autocomplete="off" />
          </div>

          <!-- Provincia -->
          <div class="flex flex-column gap-2">
            <label for="search-province" class="text-sm text-color-secondary">{{ t("geo.province") }}</label>
            <Select
              inputId="search-province"
              v-model="searchForm.provinceId"
              :options="provinceOptions"
              optionLabel="label"
              optionValue="value"
              :placeholder="t('geo.province')"
              showClear
              filter
              class="w-full"
              :pt="{ label: { 'aria-label': t('geo.province') } }" />
          </div>

          <!-- Rango de fechas -->
          <div class="flex flex-column gap-2">
            <label for="dateFrom" class="text-sm text-color-secondary">{{ t("dates.from") }}</label>
            <DatePicker
              inputId="dateFrom"
              v-model="searchForm.dateFrom"
              dateFormat="dd/mm/yy"
              showIcon
              iconDisplay="input"
              :placeholder="`${t('dates.from')}...`" />
          </div>

          <div class="flex flex-column gap-2">
            <label for="dateTo" class="text-sm text-color-secondary">{{ t("dates.to") }}</label>
            <DatePicker
              inputId="dateTo"
              v-model="searchForm.dateTo"
              :minDate="searchForm.dateFrom ?? undefined"
              dateFormat="dd/mm/yy"
              showIcon
              iconDisplay="input"
              :placeholder="`${t('dates.to')}...`" />
            <Message v-show="isDateRangeInvalid" severity="error" variant="simple" size="small">
              {{ t("dates.invalidRange") }}
            </Message>
          </div>

          <!-- Query libre -->
          <div class="flex flex-column gap-2">
            <label for="query" class="text-sm text-color-secondary">{{ t("common.searchN") }}</label>
            <InputText
              id="query"
              v-model="searchForm.query"
              :placeholder="t('events.searchPlaceholder')"
              autocomplete="off" />
          </div>

          <!-- Botón -->
          <Button
            type="submit"
            :label="t('common.searchV')"
            icon="pi pi-search"
            class="w-full"
            :disabled="isDateRangeInvalid" />
        </form>
      </section>
    </Drawer>
  </div>
</template>

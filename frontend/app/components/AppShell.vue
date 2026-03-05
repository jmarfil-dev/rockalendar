<script setup lang="ts">
import type { AppLocale, LocaleOption } from "~/types/languages";
import type { BottomItem } from "~/types/components";

const props = defineProps<{
  bottomItems?: BottomItem[];
}>();

const { t, locale, setLocale } = useI18n();
const auth = useAuth();
const route = useRoute();

const localeOptions: LocaleOption[] = [
  { value: "en", label: "English", flagSrc: "/flags/gb.svg" },
  { value: "es", label: "Español", flagSrc: "/flags/es.svg" },
];

const bottomById = computed(() => {
  const m = new Map<string, BottomItem>();
  for (const it of props.bottomItems ?? []) m.set(it.id, it);
  return m;
});

const proposeItem = computed(() => bottomById.value.get("propose") ?? null);
const searchItem = computed(() => bottomById.value.get("search") ?? null);
const meItem = computed(() => bottomById.value.get("me") ?? null);
// const moderationItem = computed(() => bottomById.value.get("moderation") ?? null);

const currentLocale = computed<AppLocale>({
  get: () => (locale.value as AppLocale) ?? "en",
  set: (val) => setLocale(val),
});

const optionByValue = (val: AppLocale) => localeOptions.find((o) => o.value === val)!;

const onUserClick = () => {
  return navigateTo("/login");
};

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

function isActive(section: "search" | "propose" | "me") {
  const path = route.path;

  if (section === "search") return path.startsWith("/events");
  if (section === "propose") return path.startsWith("/me/events/propose");
  if (section === "me") return path.startsWith("/me");

  return false;
}
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
      v-if="props.bottomItems && props.bottomItems.length"
      class="surface-900 border-top-1 surface-border fixed bottom-0 left-0 right-0"
      :aria-label="t('common.bottomNav')">
      <div class="mx-auto w-full max-w-7xl flex justify-content-around">
        <Button
          v-if="proposeItem"
          :key="proposeItem.id"
          :icon="proposeItem.icon"
          size="large"
          :text="!isActive('propose')"
          rounded
          :aria-label="proposeItem.label"
          @click="proposeItem.action" />

        <Button
          v-if="searchItem"
          :key="searchItem.id"
          :icon="searchItem.icon"
          size="large"
          :text="!isActive('search')"
          rounded
          :aria-label="searchItem.label"
          @click="searchItem.action" />

        <Button
          v-if="meItem"
          :key="meItem.id"
          :icon="meItem.icon"
          size="large"
          :text="!isActive('me')"
          rounded
          :aria-label="meItem.label"
          @click="meItem.action" />
      </div>
    </nav>

    <!-- Search sidebar (GLOBAL) -->
    <Drawer
      v-model:visible="isSearchOpen"
      position="right"
      header="Let's Rock!!"
      :style="{ width: '340px' }"
      @hide="closeSearch">
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
          <label for="province" class="text-sm text-color-secondary">{{ t("geo.province") }}</label>
          <Select
            v-model="searchForm.provinceId"
            :options="provinceOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="t('geo.province')"
            showClear
            class="w-full" />
        </div>

        <!-- Rango de fechas -->
        <div class="flex flex-column gap-2">
          <label for="dateFrom" class="text-sm text-color-secondary">{{ t("dates.from") }}</label>
          <DatePicker
            id="dateFrom"
            v-model="searchForm.dateFrom"
            dateFormat="dd/mm/yy"
            showIcon
            iconDisplay="input"
            :placeholder="`${t('dates.from')}...`" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="dateTo" class="text-sm text-color-secondary">{{ t("dates.to") }}</label>
          <DatePicker
            id="dateTo"
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
    </Drawer>
  </div>
</template>

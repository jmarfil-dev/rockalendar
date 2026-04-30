<script setup lang="ts">
import { ROUTES } from "~/constants/routes";
import type { AppLocale, LocaleOption } from "~/types/languages";

const { t, locale, setLocale } = useI18n({ useScope: "global" });
const { isAuthenticated, isModerator, isAdmin, user } = useAuth();
const { unreadCount, markAllRead } = useNotifications();
const router = useRouter();

const totalUnread = computed(() => unreadCount.value.user + unreadCount.value.moderation + unreadCount.value.admin);

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

// ------- Search Drawer (derecha) -------
const {
  isOpen: isSearchOpen,
  open: openSearch,
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

// ------- User Drawer -------
const { isOpen: isUserDrawerOpen, open: openUserDrawer, close: closeUserDrawer } = useUserDrawer();

const userDrawerHeader = computed(() => {
  if (isAuthenticated.value && user.value) {
    return user.value.email?.split("@")[0] ?? user.value.sub;
  }
  return undefined;
});

const goToLogin = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.login);
};

const goToRegister = async () => {
  closeUserDrawer();
  await navigateTo(ROUTES.register);
};

const goToNotifications = async (route: string) => {
  closeUserDrawer();
  await navigateTo(route);
};

const onMarkAllRead = async () => {
  await markAllRead();
  closeUserDrawer();
};

// ------- Bottom nav -------
const bottomItems = computed(() => {
  const homeItem = {
    id: "home",
    label: t("page.home"),
    icon: "pi pi-home",
    action: () => router.push(ROUTES.home),
  };

  const searchItem = {
    id: "search",
    label: t("common.searchV"),
    icon: "pi pi-search",
    action: () => openSearch(),
  };

  if (!isAuthenticated.value) {
    return [searchItem, homeItem];
  }

  return [
    {
      id: "propose",
      label: t("common.proposeV"),
      icon: "pi pi-plus",
      action: () => router.push(ROUTES.meEventPropose),
    },
    searchItem,
    {
      id: "me",
      label: t("common.myAreaV"),
      icon: "pi pi-user",
      action: () => router.push(ROUTES.me),
    },
    homeItem,
  ];
});
</script>

<template>
  <div class="min-h-screen flex flex-column">
    <!-- Skip link: visible solo al recibir foco (usuarios de teclado) -->
    <a href="#main-content" class="skip-link">{{ t("common.skipToContent") }}</a>

    <!-- Header -->
    <header class="surface-0 mt-2">
      <div class="mx-auto w-full max-w-7xl px-3 py-1 flex align-items-center justify-content-between gap-2">
        <NuxtLink :to="ROUTES.home" class="no-underline flex align-items-center">
          <NuxtImg
            src="/banner.png"
            alt="Rockalendar"
            height="80"
            format="webp"
            style="margin-top: -1.5rem; margin-bottom: -1.5rem; height: 5rem"
            fetchpriority="high" />
        </NuxtLink>

        <Select
          v-model="currentLocale"
          :options="localeOptions"
          option-label="label"
          option-value="value"
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
                style="display: block">
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
                style="display: block">
              <span>{{ slotProps.option.label }}</span>
            </div>
          </template>
        </Select>

        <ClientOnly>
          <div class="relative inline-flex">
            <Button
              icon="pi pi-user"
              rounded
              outlined
              :aria-label="isAuthenticated ? t('user.myAccount') : t('auth.login')"
              @click="openUserDrawer" />
            <span
              v-if="isAuthenticated && totalUnread > 0"
              class="absolute bg-red-500"
              style="width: 0.85rem; height: 0.85rem; top: -0.15rem; right: -0.15rem; border-radius: 50%"
              aria-hidden="true" />
          </div>
        </ClientOnly>
      </div>
    </header>

    <!-- Body -->
    <main id="main-content" class="flex-1 surface-0">
      <div class="mx-auto w-full max-w-7xl px-3 py-4">
        <slot />
      </div>
    </main>

    <!-- Footer: pb-6 cuando hay bottom nav fixed para no quedar tapado -->
    <footer class="surface-0 border-top-1 surface-border pb-6">
      <div class="mx-auto w-full max-w-7xl px-3 py-3 flex flex-column align-items-center gap-2">
        <!-- Navegación: dos columnas -->
        <div class="flex justify-content-center gap-4">
          <NuxtLink :to="ROUTES.about" class="text-sm text-color-secondary no-underline hover:underline">
            {{ t("page.about") }}
          </NuxtLink>
          <NuxtLink :to="ROUTES.privacy" class="text-sm text-color-secondary no-underline hover:underline">
            {{ t("page.privacy") }}
          </NuxtLink>
        </div>

        <Divider class="my-0" />

        <!-- CTAs beta -->
        <p class="text-xs text-color-secondary m-0 text-center">
          {{ t("footer.feedbackLine") }}
          <NuxtLink :to="ROUTES.contact" class="inline-link">{{ t("footer.feedbackLink") }}</NuxtLink>
        </p>
        <p class="text-xs text-color-secondary m-0 text-center">
          {{ t("footer.openSourceLine") }}
          <a
            href="https://github.com/jmarfil-dev/rockalendar"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-link"
            >GitHub</a
          >
        </p>
      </div>
    </footer>

    <!-- Bottom nav: ClientOnly porque los items dependen de isAuthenticated,
         que en SSR siempre es false (token solo disponible en cliente vía localStorage) -->
    <ClientOnly>
      <nav
        class="surface-900 border-top-1 surface-border fixed bottom-0 left-0 right-0"
        :aria-label="t('common.bottomNav')">
        <div class="mx-auto w-full max-w-7xl flex justify-content-around">
          <Button
            v-for="item in bottomItems"
            :key="item.id"
            :icon="item.icon"
            size="large"
            text
            rounded
            :aria-label="item.label"
            @click="item.action" />
        </div>
      </nav>
    </ClientOnly>

    <!-- User drawer -->
    <Drawer
      v-model:visible="isUserDrawerOpen"
      position="right"
      :header="userDrawerHeader"
      :style="{ width: '340px' }"
      @hide="closeUserDrawer">
      <aside class="flex flex-column h-full gap-3">
        <template v-if="!isAuthenticated">
          <div class="flex flex-column gap-2">
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
          </div>
        </template>

        <template v-else>
          <Divider class="my-0" />

          <!-- Sección de notificaciones -->
          <ul class="list-none p-0 m-0 flex flex-column gap-2">
            <li
              class="flex align-items-center justify-content-between py-2 px-2 border-bottom-1 surface-border cursor-pointer border-round hover:surface-hover transition-colors transition-duration-150"
              role="button"
              tabindex="0"
              @click="goToNotifications(ROUTES.meNotifications)"
              @keydown.enter="goToNotifications(ROUTES.meNotifications)">
              <div class="flex align-items-center gap-2">
                <span class="inline-block border-round-full bg-red-500" style="width:0.65rem;height:0.65rem" />
                <span>{{ t('notifications.bandeja.user') }}</span>
              </div>
              <Badge v-if="unreadCount.user > 0" :value="unreadCount.user" severity="danger" />
              <span v-else class="text-color-secondary text-sm">{{ t('notifications.allRead') }}</span>
            </li>
            <li
              v-if="isModerator"
              class="flex align-items-center justify-content-between py-2 px-2 border-bottom-1 surface-border cursor-pointer border-round hover:surface-hover transition-colors transition-duration-150"
              role="button"
              tabindex="0"
              @click="goToNotifications(ROUTES.moderationNotifications)"
              @keydown.enter="goToNotifications(ROUTES.moderationNotifications)">
              <div class="flex align-items-center gap-2">
                <span class="inline-block border-round-full bg-green-500" style="width:0.65rem;height:0.65rem" />
                <span>{{ t('notifications.bandeja.moderation') }}</span>
              </div>
              <Badge v-if="unreadCount.moderation > 0" :value="unreadCount.moderation" severity="success" />
              <span v-else class="text-color-secondary text-sm">{{ t('notifications.allRead') }}</span>
            </li>
            <li
              v-if="isAdmin"
              class="flex align-items-center justify-content-between py-2 px-2 border-bottom-1 surface-border cursor-pointer border-round hover:surface-hover transition-colors transition-duration-150"
              role="button"
              tabindex="0"
              @click="goToNotifications(ROUTES.adminNotifications)"
              @keydown.enter="goToNotifications(ROUTES.adminNotifications)">
              <div class="flex align-items-center gap-2">
                <span class="inline-block border-round-full bg-yellow-500" style="width:0.65rem;height:0.65rem" />
                <span>{{ t('notifications.bandeja.admin') }}</span>
              </div>
              <Badge v-if="unreadCount.admin > 0" :value="unreadCount.admin" severity="warn" />
              <span v-else class="text-color-secondary text-sm">{{ t('notifications.allRead') }}</span>
            </li>
          </ul>

          <Button
            v-if="totalUnread > 0"
            :label="t('notifications.markAllRead')"
            icon="pi pi-check-circle"
            text
            class="align-self-start p-0"
            @click="onMarkAllRead" />

          <Divider class="my-0" />

          <!-- Accesos a paneles de moderación y administración -->
          <div class="flex flex-column gap-2">
            <Button
              v-if="isModerator"
              :label="t('common.moderationV')"
              icon="pi pi-clipboard"
              text
              class="justify-content-start"
              @click="async () => { closeUserDrawer(); await navigateTo(ROUTES.moderation); }" />
            <Button
              v-if="isAdmin"
              :label="t('common.adminV')"
              icon="pi pi-shield"
              text
              class="justify-content-start"
              @click="async () => { closeUserDrawer(); await navigateTo(ROUTES.admin); }" />
          </div>
        </template>
      </aside>
    </Drawer>

    <!-- Search sidebar (Izquierda) -->
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
              input-id="artist"
              :suggestions="artistSuggestions"
              :loading="artistLoading"
              :min-length="2"
              :max-length="50"
              option-label="name"
              :placeholder="`${t('common.example')}. Elektroduendes`"
              input-class="w-full"
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
            <span id="search-province-label" class="text-sm text-color-secondary">{{ t("geo.province") }}</span>
            <Select
              v-model="searchForm.provinceId"
              input-id="search-province"
              aria-labelledby="search-province-label"
              :options="provinceOptions"
              option-label="label"
              option-value="value"
              :placeholder="t('geo.province')"
              show-clear
              filter
              class="w-full" />
          </div>

          <!-- Rango de fechas -->
          <div class="flex flex-column gap-2">
            <label for="dateFrom" class="text-sm text-color-secondary">{{ t("dates.from") }}</label>
            <div class="flex align-items-center gap-2">
              <DatePicker
                v-model="searchForm.dateFrom"
                input-id="dateFrom"
                date-format="dd/mm/yy"
                :manual-input="false"
                show-icon
                icon-display="input"
                class="flex-1"
                :placeholder="`${t('dates.from')}...`" />
              <Button
                v-if="searchForm.dateFrom"
                type="button"
                icon="pi pi-times"
                severity="secondary"
                text
                rounded
                size="small"
                :aria-label="t('common.clearDate')"
                @click="searchForm.dateFrom = null" />
            </div>
          </div>

          <div class="flex flex-column gap-2">
            <label for="dateTo" class="text-sm text-color-secondary">{{ t("dates.to") }}</label>
            <div class="flex align-items-center gap-2">
              <DatePicker
                v-model="searchForm.dateTo"
                input-id="dateTo"
                :min-date="searchForm.dateFrom ?? undefined"
                date-format="dd/mm/yy"
                :manual-input="false"
                show-icon
                icon-display="input"
                class="flex-1"
                :placeholder="`${t('dates.to')}...`" />
              <Button
                v-if="searchForm.dateTo"
                type="button"
                icon="pi pi-times"
                severity="secondary"
                text
                rounded
                size="small"
                :aria-label="t('common.clearDate')"
                @click="searchForm.dateTo = null" />
            </div>
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

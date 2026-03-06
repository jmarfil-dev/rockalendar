<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

const { t } = useI18n();
const auth = useAuth();
const router = useRouter();

const { open: openSearch } = useSearchDrawer();

const bottomItems = computed(() => {
  const items = [
    {
      id: "search",
      label: t("common.searchV"),
      icon: "pi pi-search",
      action: () => openSearch(),
    },
  ];

  if (auth.isAuthenticated.value) {
    items.push({
      id: "propose",
      label: t("common.proposeV"),
      icon: "pi pi-plus",
      action: () => router.push(ROUTES.meEventPropose),
    });

    items.push({
      id: "me",
      label: t("common.myAreaV"),
      icon: "pi pi-user",
      action: () => router.push(ROUTES.me),
    });
  }

  return items;
});
</script>

<template>
  <AppShell :bottomItems="bottomItems">
    <slot />
  </AppShell>
</template>

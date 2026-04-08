<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

const { t } = useI18n({ useScope: "global" });
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

    if (auth.isModerator.value) {
      items.push({
        id: "moderation",
        label: t("common.moderationV"),
        icon: "pi pi-clipboard",
        action: () => router.push(ROUTES.moderation),
      });
    }

    if (auth.isAdmin.value) {
      items.push({
        id: "admin",
        label: t("common.adminV"),
        icon: "pi pi-shield",
        action: () => router.push(ROUTES.admin),
      });
    }
  }

  return items;
});
</script>

<template>
  <AppShell :bottom-items="bottomItems">
    <slot />
  </AppShell>
</template>

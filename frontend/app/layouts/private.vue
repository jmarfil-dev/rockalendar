<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

const { t } = useI18n();
const router = useRouter();

const { open: openSearch } = useSearchDrawer();
const { isModerator, isAdmin } = useAuth();

const bottomItems = computed(() => {
  const items = [
    {
      id: "search",
      label: t("common.searchV"),
      icon: "pi pi-search",
      action: () => openSearch(),
    },
    {
      id: "propose",
      label: t("common.proposeV"),
      icon: "pi pi-plus",
      action: () => router.push(ROUTES.meEventPropose),
    },
    {
      id: "me",
      label: t("common.myAreaV"),
      icon: "pi pi-user",
      action: () => router.push(ROUTES.me),
    },
  ];

  if (isModerator.value) {
    items.push({
      id: "moderation",
      label: t("common.moderationV"),
      icon: "pi pi-clipboard",
      action: () => router.push(ROUTES.moderation),
    });
  }

  if (isAdmin.value) {
    items.push({
      id: "admin",
      label: t("common.adminV"),
      icon: "pi pi-shield",
      action: () => router.push(ROUTES.admin),
    });
  }

  return items;
});
</script>

<template>
  <AppShell :bottom-items="bottomItems">
    <slot />
  </AppShell>
</template>

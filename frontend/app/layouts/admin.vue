<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

const { t } = useI18n();
const router = useRouter();

const { open: openSearch } = useSearchDrawer();

const bottomItems = computed(() => [
  {
    id: "admin",
    label: t("common.adminV"),
    icon: "pi pi-shield",
    action: () => router.push(ROUTES.admin),
  },
  {
    id: "moderation",
    label: t("common.moderationV"),
    icon: "pi pi-clipboard",
    action: () => router.push(ROUTES.moderation),
  },
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
]);
</script>

<template>
  <AppShell :bottom-items="bottomItems">
    <slot />
  </AppShell>
</template>

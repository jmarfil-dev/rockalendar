<script setup lang="ts">
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
      action: () => router.push("/me/events/propose"),
    });

    items.push({
      id: "me",
      label: t("common.myAreaV"),
      icon: "pi pi-user",
      action: () => router.push("/me"),
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

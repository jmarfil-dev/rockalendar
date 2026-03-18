<script setup lang="ts">
const localeHead = useLocaleHead();

useHead({
  htmlAttrs: {
    class: "dark",
    lang: computed(() => localeHead.value.htmlAttrs?.lang ?? "es"),
  },
});
</script>

<template>
  <NuxtRouteAnnouncer />
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

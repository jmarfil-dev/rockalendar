<script setup lang="ts">
import EventPublicPage from "~/components/events/EventPublicPage.vue";

definePageMeta({ layout: "public" });

const { t } = useI18n();
useHead({ title: () => t("page.events") });
useSeoMeta({
  description: "Browse all rock, metal and punk concerts and festivals in Spain.",
  ogTitle: () => `${t("page.events")} — Rockalendar`,
  ogDescription: "Browse all rock, metal and punk concerts and festivals in Spain.",
  ogType: "website",
  twitterTitle: () => `${t("page.events")} — Rockalendar`,
  twitterDescription: "Browse all rock, metal and punk concerts and festivals in Spain.",
});
</script>

<template>
  <EventPublicPage />
</template>

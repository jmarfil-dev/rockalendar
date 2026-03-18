<script setup lang="ts">
import EventPublicPage from "~/components/events/EventPublicPage.vue";

definePageMeta({ layout: "public" });

const { t } = useI18n();
useHead({ title: () => t("page.events") });
</script>

<template>
  <EventPublicPage />
</template>

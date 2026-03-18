<script setup lang="ts">
import EventPublicPage from "~/components/events/EventPublicPage.vue";

definePageMeta({ layout: "public" });

useHead({ titleTemplate: null, title: "Rockalendar" });
</script>

<template>
  <EventPublicPage />
</template>

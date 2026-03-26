<script setup lang="ts">
import EventPublicPage from "~/components/events/EventPublicPage.vue";

definePageMeta({ layout: "public" });

useHead({ titleTemplate: null, title: "Rockalendar" });
useSeoMeta({
  description: "Find rock, metal and punk concerts and festivals in Spain. Community-driven, no algorithms, no corporate bullshit.",
  ogTitle: "Rockalendar",
  ogDescription: "Find rock, metal and punk concerts and festivals in Spain. Community-driven, no algorithms, no corporate bullshit.",
  ogType: "website",
  twitterTitle: "Rockalendar",
  twitterDescription: "Find rock, metal and punk concerts and festivals in Spain. Community-driven, no algorithms, no corporate bullshit.",
});
</script>

<template>
  <EventPublicPage />
</template>

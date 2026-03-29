<script setup lang="ts">
import EventPublicPage from "~/components/events/EventPublicPage.vue";

definePageMeta({ layout: "public" });

useHead({ titleTemplate: null, title: "Rockalendar" });
useSeoMeta({
  description:
    "Encuentra conciertos y festivales de punk, rock y metal en España. Impulsado por la comunidad, sin algoritmos ni tonterías corporativas.",
  ogTitle: "Rockalendar",
  ogDescription:
    "Encuentra conciertos y festivales de punk, rock y metal en España. Impulsado por la comunidad, sin algoritmos ni tonterías corporativas.",
  ogType: "website",
  twitterTitle: "Rockalendar",
  twitterDescription:
    "Encuentra conciertos y festivales de punk, rock y metal en España. Impulsado por la comunidad, sin algoritmos ni tonterías corporativas.",
});
</script>

<template>
  <EventPublicPage />
</template>

<script setup lang="ts">
definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.moderationNotifications") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("notifications.bandeja.moderation") }}</h1>
    <NotificationInbox bandeja="MODERATION" />
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.meNotifications") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("notifications.bandeja.user") }}</h1>
    <NotificationInbox bandeja="USER" />
  </div>
</template>

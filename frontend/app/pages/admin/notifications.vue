<script setup lang="ts">
definePageMeta({ layout: "admin", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.adminNotifications") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("notifications.bandeja.admin") }}</h1>
    <NotificationInbox bandeja="ADMIN" />
  </div>
</template>

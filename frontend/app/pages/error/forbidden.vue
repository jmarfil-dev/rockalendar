<script setup lang="ts">
const { t } = useI18n();
useHead({ title: () => t("page.forbidden") });

showError(
  createError({
    statusCode: 403,
    statusMessage: "Forbidden",
    fatal: true,
  }),
);
</script>

<template></template>

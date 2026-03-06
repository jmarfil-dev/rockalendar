<script setup lang="ts">
const route = useRoute();

showError(
  createError({
    statusCode: 403,
    statusMessage: "Forbidden",
    fatal: true,
  }),
);
</script>

<template></template>

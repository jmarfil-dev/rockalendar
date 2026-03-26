<script setup lang="ts">
defineProps<{
  first: number;
  rows: number;
  totalRecords: number;
}>();

defineEmits<{
  page: [e: { page: number; first: number; rows: number }];
}>();
</script>

<template>
  <div class="border-1 border-round-xl p-2">
    <Paginator
      :first="first"
      :rows="rows"
      :total-records="totalRecords"
      :rows-per-page-options="[20, 50, 100]"
      @page="$emit('page', $event)" />
  </div>
</template>

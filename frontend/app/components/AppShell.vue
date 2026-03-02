<script setup lang="ts">
import Button from "primevue/button";

defineProps<{
  bottomItems?: {
    label: string;
    icon: string;
    action: () => void;
    text?: boolean;
  }[];
}>();
</script>

<template>
  <div class="min-h-screen flex flex-column">
    <!-- Header -->
    <header class="surface-0">
      <div class="mx-auto w-full max-w-7xl px-3 py-1 flex align-items-center justify-content-between gap-2">
        <NuxtLink to="/" class="no-underline flex align-items-center">
          <img src="/banner.png" alt="Rockalendar" style="margin-top: -1.5rem; margin-bottom: -1.5rem; height: 5rem" />
        </NuxtLink>

        <Button icon="pi pi-user" rounded outlined aria-label="Login" />
      </div>
    </header>

    <!-- Body -->
    <main class="flex-1 surface-0 mb-5">
      <div class="mx-auto w-full max-w-7xl px-3 py-4">
        <slot />
      </div>
    </main>

    <!-- Bottom nav -->
    <nav
      v-if="bottomItems && bottomItems.length"
      class="surface-900 border-top-1 surface-border fixed bottom-0 left-0 right-0"
      aria-label="Bottom navigation">
      <div class="mx-auto w-full max-w-7xl px-2 py-2 flex justify-content-around">
        <Button
          v-for="item in bottomItems"
          :key="item.label"
          :label="item.label"
          :icon="item.icon"
          :text="item.text ?? false"
          @click="item.action" />
      </div>
    </nav>
  </div>
</template>

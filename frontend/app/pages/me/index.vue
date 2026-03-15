<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
const router = useRouter();
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("me.title") }}</h1>

    <div class="grid">
      <div class="col-12 md:col-6">
        <div
          class="surface-card border-1 surface-border border-round-xl p-4 flex flex-column gap-3 cursor-pointer hover:surface-hover transition-colors transition-duration-150"
          role="button"
          tabindex="0"
          @click="router.push(ROUTES.meAgenda)"
          @keydown.enter="router.push(ROUTES.meAgenda)"
          @keydown.space.prevent="router.push(ROUTES.meAgenda)">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-calendar text-3xl text-primary" />
            <span class="text-xl font-semibold">{{ t("me.agenda") }}</span>
          </div>
          <p class="m-0 text-color-secondary text-sm">{{ t("me.agendaDesc") }}</p>
        </div>
      </div>

      <div class="col-12 md:col-6">
        <div
          class="surface-card border-1 surface-border border-round-xl p-4 flex flex-column gap-3 cursor-pointer hover:surface-hover transition-colors transition-duration-150"
          role="button"
          tabindex="0"
          @click="router.push(ROUTES.meEvents)"
          @keydown.enter="router.push(ROUTES.meEvents)"
          @keydown.space.prevent="router.push(ROUTES.meEvents)">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-list text-3xl text-primary" />
            <span class="text-xl font-semibold">{{ t("me.myEvents") }}</span>
          </div>
          <p class="m-0 text-color-secondary text-sm">{{ t("me.myEventsDesc") }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

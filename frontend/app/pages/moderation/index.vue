<script setup lang="ts">
import { ROUTES } from "~/constants/routes";


definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.moderation") });
const router = useRouter();
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("moderation.title") }}</h1>

    <div class="grid">
      <div class="col-12 md:col-6">
        <Card
          class="h-full border-1 surface-border cursor-pointer hover:surface-hover transition-colors transition-duration-150"
          role="button"
          tabindex="0"
          @click="router.push(ROUTES.moderationEvents)"
          @keydown.enter="router.push(ROUTES.moderationEvents)"
          @keydown.space.prevent="router.push(ROUTES.moderationEvents)">
          <template #title>
            <div class="flex align-items-center gap-3">
              <i class="pi pi-list-check text-3xl text-primary" />
              <span>{{ t("moderation.hub.events") }}</span>
            </div>
          </template>
          <template #content>
            <p class="m-0 text-color-secondary text-sm">{{ t("moderation.hub.eventsDesc") }}</p>
          </template>
        </Card>
      </div>

      <div class="col-12 md:col-6">
        <Card
          class="h-full border-1 surface-border cursor-pointer hover:surface-hover transition-colors transition-duration-150"
          role="button"
          tabindex="0"
          @click="router.push(ROUTES.moderationArtists)"
          @keydown.enter="router.push(ROUTES.moderationArtists)"
          @keydown.space.prevent="router.push(ROUTES.moderationArtists)">
          <template #title>
            <div class="flex align-items-center gap-3">
              <i class="pi pi-user-plus text-3xl text-primary" />
              <span>{{ t("moderation.hub.artists") }}</span>
            </div>
          </template>
          <template #content>
            <p class="m-0 text-color-secondary text-sm">{{ t("moderation.hub.artistsDesc") }}</p>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

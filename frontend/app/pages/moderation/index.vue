<script setup lang="ts">
import { ROUTES } from "~/constants/routes";


definePageMeta({ layout: "moderation", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.moderation") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("moderation.title") }}</h1>

    <div class="grid">
      <div class="col-12 md:col-6">
        <NuxtLink :to="ROUTES.moderationEvents" class="no-underline block h-full">
          <Card class="h-full border-1 surface-border hover:surface-hover transition-colors transition-duration-150">
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
        </NuxtLink>
      </div>

      <div class="col-12 md:col-6">
        <NuxtLink :to="ROUTES.moderationArtists" class="no-underline block h-full">
          <Card class="h-full border-1 surface-border hover:surface-hover transition-colors transition-duration-150">
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
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

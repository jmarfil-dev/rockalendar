<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.me") });

const { me, promoting, fetchMe, requestPromotion } = useMe();
onMounted(fetchMe);
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("me.title") }}</h1>

    <div class="grid">
      <div class="col-12 md:col-6">
        <NuxtLink :to="ROUTES.meAgenda" class="no-underline block h-full">
          <Card class="h-full border-1 surface-border hover:surface-hover transition-colors transition-duration-150">
            <template #title>
              <div class="flex align-items-center gap-3">
                <i class="pi pi-calendar text-3xl text-primary" />
                <span>{{ t("me.agenda.title") }}</span>
              </div>
            </template>
            <template #content>
              <p class="m-0 text-color-secondary text-sm">{{ t("me.agendaDesc") }}</p>
            </template>
          </Card>
        </NuxtLink>
      </div>

      <div class="col-12 md:col-6">
        <NuxtLink :to="ROUTES.meEvents" class="no-underline block h-full">
          <Card class="h-full border-1 surface-border hover:surface-hover transition-colors transition-duration-150">
            <template #title>
              <div class="flex align-items-center gap-3">
                <i class="pi pi-list text-3xl text-primary" />
                <span>{{ t("me.myEvents") }}</span>
              </div>
            </template>
            <template #content>
              <p class="m-0 text-color-secondary text-sm">{{ t("me.myEventsDesc") }}</p>
            </template>
          </Card>
        </NuxtLink>
      </div>

      <div class="col-12 md:col-6">
        <NuxtLink :to="ROUTES.meSettings" class="no-underline block h-full">
          <Card class="h-full border-1 surface-border hover:surface-hover transition-colors transition-duration-150">
            <template #title>
              <div class="flex align-items-center gap-3">
                <i class="pi pi-cog text-3xl text-primary" />
                <span>{{ t("me.settings.title") }}</span>
              </div>
            </template>
            <template #content>
              <p class="m-0 text-color-secondary text-sm">{{ t("me.settings.title") }}</p>
            </template>
          </Card>
        </NuxtLink>
      </div>
    </div>

    <div v-if="me?.promotionEligible" class="col-12">
      <Message severity="info" :closable="false" class="m-0">
        <div class="flex flex-column sm:flex-row align-items-start sm:align-items-center justify-content-between gap-3">
          <div class="flex flex-column gap-1">
            <span class="font-semibold">{{ t("me.promotion.title") }}</span>
            <span class="text-sm">{{ t("me.promotion.desc") }}</span>
          </div>
          <Button :label="t('me.promotion.cta')" icon="pi pi-arrow-up" :loading="promoting" @click="requestPromotion" />
        </div>
      </Message>
    </div>
  </div>
</template>

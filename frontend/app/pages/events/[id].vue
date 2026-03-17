<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { EventPublic } from "~/types/events";

definePageMeta({ layout: "public" });

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

const id = route.params.id as string;

function goBack() {
  const prev = window.history.state?.back as string | undefined;
  if (prev?.startsWith(ROUTES.events)) {
    router.back();
  } else {
    navigateTo(ROUTES.events);
  }
}

const { data: event, pending } = await useApiFetch<EventPublic>(ROUTE_PATH.apiEventDetail(id), {
  key: `event-${id}`,
});
</script>

<template>
  <article class="p-3 md:p-4 lg:p-5 flex flex-column gap-4">
    <!-- Cabecera de navegación -->
    <div class="flex align-items-center gap-3">
      <button type="button" class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" @click="goBack">
        <i class="pi pi-arrow-left" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("events.listEvents") }}</h1>
    </div>

    <!-- Loading -->
    <div v-if="pending" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
    </div>

    <!-- Contenido -->
    <div v-else-if="event" class="flex flex-column gap-4">
      <!-- Header del evento -->
      <header class="flex flex-column gap-2">
        <h2 class="m-0 text-2xl md:text-3xl font-semibold line-height-2">{{ event.title }}</h2>

        <div class="flex align-items-center gap-2 text-color-secondary">
          <i class="pi pi-map-marker" />
          <span>
            <span v-if="event.venueName">{{ event.venueName }}</span>
            <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
            <span v-if="event.cityName">{{ event.cityName }}</span>
            <span v-if="event.cityName && event.provinceName">, </span>
            <span v-if="event.provinceName">{{ event.provinceName }}</span>
          </span>
        </div>

        <div class="flex align-items-center gap-2 text-color-secondary">
          <i class="pi pi-calendar" />
          <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime) }}</time>
          <template v-if="event.endDateTime">
            <span class="mx-1">→</span>
            <time :datetime="event.endDateTime">{{ formatEventDate(event.endDateTime) }}</time>
          </template>
        </div>

        <Divider class="my-1" />
      </header>

      <!-- Poster -->
      <section :aria-label="t('events.poster')" class="w-full">
        <Card class="border-1 surface-border">
          <template #content>
            <div
              class="border-1 surface-border border-round-lg surface-50 flex align-items-center justify-content-center"
              style="aspect-ratio: 16/9">
              <div class="text-center text-color-secondary">
                <i class="pi pi-image text-2xl" />
                <div class="mt-2 text-sm">{{ t("events.noPoster") }}</div>
              </div>
            </div>
          </template>
        </Card>
      </section>

      <!-- Grid principal -->
      <div class="grid">
        <!-- Columna izquierda: contenido -->
        <div class="col-12 lg:col-8">
          <Card class="border-1 surface-border">
            <template #content>
              <div class="flex flex-column gap-4">
                <!-- Artistas -->
                <section v-if="event.artists?.length" :aria-label="`${t('events.groups')} / ${t('events.artists')}`">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-users text-color-secondary" />
                    <h3 class="m-0 text-lg font-semibold">{{ t("events.groups") }} / {{ t("events.artists") }}</h3>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <Tag v-for="artist in event.artists" :key="artist" :value="artist" rounded severity="info" />
                  </div>
                </section>

                <!-- Descripción -->
                <section v-if="event.description" :aria-label="t('events.description')">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-align-left text-color-secondary" />
                    <h3 class="m-0 text-lg font-semibold">{{ t("events.description") }}</h3>
                  </div>
                  <div class="text-color-secondary white-space-pre-line line-height-3">
                    {{ event.description }}
                  </div>
                </section>

                <!-- Fallback si vacío -->
                <Message v-if="!event.description?.trim() && !event.artists?.length" severity="info" :closable="false">
                  {{ t("events.noDescription") }}
                </Message>
              </div>
            </template>
          </Card>
        </div>

        <!-- Columna derecha: metadatos -->
        <aside class="col-12 lg:col-4" :aria-label="t('common.details')">
          <div class="flex flex-column gap-3">
            <Card class="border-1 surface-border">
              <template #title>
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-info-circle" />
                  <h3 class="m-0 text-base font-semibold">{{ t("common.details") }}</h3>
                </div>
              </template>
              <template #content>
                <div class="flex flex-column gap-3 text-sm">
                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-calendar text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("dates.date") }}</span>
                      <span class="text-color-secondary">
                        <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime) }}</time>
                        <template v-if="event.endDateTime">
                          <span class="mx-1">→</span>
                          <time :datetime="event.endDateTime">{{ formatEventDate(event.endDateTime) }}</time>
                        </template>
                      </span>
                    </div>
                  </div>

                  <div
                    v-if="event.venueName || event.cityName || event.provinceName"
                    class="flex align-items-start gap-2">
                    <i class="pi pi-map-marker text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("geo.place") }}</span>
                      <span class="text-color-secondary">
                        <span v-if="event.venueName">{{ event.venueName }}</span>
                        <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
                        <span v-if="event.cityName">{{ event.cityName }}</span>
                        <span v-if="event.cityName && event.provinceName">, </span>
                        <span v-if="event.provinceName">{{ event.provinceName }}</span>
                      </span>
                    </div>
                  </div>

                  <div v-if="event.sourceUrl" class="flex align-items-start gap-2">
                    <i class="pi pi-link text-color-secondary mt-1" />
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("events.sourceUrl") }}</span>
                      <a
                        :href="event.sourceUrl"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="text-primary underline text-xs break-all">
                        {{ event.sourceUrl }}
                      </a>
                    </div>
                  </div>
                </div>
              </template>
            </Card>

            <Card class="border-1 surface-border">
              <template #content>
                <div class="flex flex-column gap-3">
                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-bell text-color-secondary mt-1" />
                    <div class="flex flex-column gap-1">
                      <span class="font-medium">{{ t("common.anythingWrong") }}</span>
                      <span class="text-color-secondary text-sm">{{ t("moderation.sendCom") }}</span>
                    </div>
                  </div>
                  <!-- Por ahora no hace nada -->
                  <Button :label="t('common.sendCom')" icon="pi pi-send" class="w-full" type="button" />
                </div>
              </template>
            </Card>
          </div>
        </aside>
      </div>
    </div>
  </article>
</template>

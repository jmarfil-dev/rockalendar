<script setup lang="ts">
import { ROUTE_PATH } from "~/constants/routes";
import type { EventPublic } from "~/types/events";

definePageMeta({ layout: "public" });

const { t } = useI18n();
const route = useRoute();

const id = route.params.id as string;

const { data: event, pending } = await useApiFetch<EventPublic>(ROUTE_PATH.apiEventDetail(id), {
  key: `event-${id}`,
});
</script>

<template>
  <article class="p-3 md:p-4 lg:p-5">
    <!-- Loading -->
    <div v-if="pending" class="flex align-items-center gap-2">
      <ProgressSpinner style="width: 22px; height: 22px" strokeWidth="6" />
      <span class="text-color-secondary">{{ t("common.loading") }}</span>
    </div>

    <!-- Content -->
    <div v-else-if="event" class="flex flex-column gap-3 md:gap-4">
      <!-- Header (perfecto tal cual) -->
      <header class="flex flex-column gap-2">
        <div class="flex align-items-start justify-content-between gap-3 flex-wrap">
          <div class="flex flex-column gap-1">
            <h1 class="m-0 text-2xl md:text-3xl font-semibold line-height-2">
              {{ event.title }}
            </h1>

            <div class="flex align-items-center gap-2 text-color-secondary">
              <i class="pi pi-map-marker"></i>
              <span>
                <span v-if="event.venueName">{{ event.venueName }}</span>
                <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
                <span v-if="event.cityName">{{ event.cityName }}</span>
                <span v-if="event.cityName && event.provinceName">, </span>
                <span v-if="event.provinceName">{{ event.provinceName }}</span>
              </span>
            </div>

            <div class="flex align-items-center gap-2 text-color-secondary">
              <i class="pi pi-calendar"></i>
              <span>
                <time :datetime="event.startDateTime">
                  {{ formatEventDate(event.startDateTime) }}
                </time>
                <span v-if="event.endDateTime">
                  <span class="mx-2">→</span>
                  <time :datetime="event.endDateTime">
                    {{ formatEventDate(event.endDateTime) }}
                  </time>
                </span>
              </span>
            </div>
          </div>

          <!-- (sin botón de fuente aquí) -->
          <div class="flex align-items-center gap-2"></div>
        </div>

        <Divider class="my-1" />
      </header>

      <!-- Poster -->
      <section :aria-label="t('events.poster')" class="w-full">
        <Card class="border-1 surface-border">
          <template #content>
            <!-- Placeholder -->
            <div
              class="border-1 surface-border border-round-lg surface-50 flex align-items-center justify-content-center"
              style="aspect-ratio: 16/9">
              <div class="text-center text-color-secondary">
                <i class="pi pi-image text-2xl"></i>
                <div class="mt-2 text-sm">{{ t("events.noPoster") }}</div>
              </div>
            </div>
          </template>
        </Card>
      </section>

      <!-- Main grid -->
      <div class="grid">
        <!-- Left: main -->
        <div class="col-12 lg:col-8">
          <Card class="border-1 surface-border">
            <template #content>
              <div class="flex flex-column gap-4">
                <!-- Artists -->
                <section v-if="event.artists?.length" :aria-label="`${t('events.groups')} / ${t('events.artists')}`">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-users text-color-secondary"></i>
                    <h2 class="m-0 text-xl font-semibold">{{ t("events.groups") }} / {{ t("events.artists") }}</h2>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <Tag v-for="artist in event.artists" :key="artist" :value="artist" rounded severity="info" />
                  </div>
                </section>

                <!-- Description -->
                <section v-if="event.description" :aria-label="t('events.description')">
                  <div class="flex align-items-center gap-2 mb-2">
                    <i class="pi pi-align-left text-color-secondary"></i>
                    <h2 class="m-0 text-xl font-semibold">{{ t("events.description") }}</h2>
                  </div>

                  <div class="text-color-secondary white-space-pre-line line-height-3">
                    {{ event.description }}
                  </div>
                </section>

                <!-- Fallback if empty -->
                <Message
                  v-if="
                    (!event.description || !event.description.trim()) && (!event.artists || event.artists.length === 0)
                  "
                  severity="info"
                  :closable="false">
                  {{ t("events.noDescription") }}
                </Message>
              </div>
            </template>
          </Card>
        </div>

        <!-- Right: meta -->
        <aside class="col-12 lg:col-4" :aria-label="t('common.details')">
          <div class="flex flex-column gap-3">
            <Card class="border-1 surface-border">
              <template #title>
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-info-circle"></i>
                  <h2 class="m-0 text-lg font-semibold">{{ t("common.details") }}</h2>
                </div>
              </template>

              <template #content>
                <div class="flex flex-column gap-3">
                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-calendar text-color-secondary mt-1"></i>
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("dates.date") }}</span>
                      <span class="text-color-secondary">
                        <time :datetime="event.startDateTime">
                          {{ formatEventDate(event.startDateTime) }}
                        </time>
                        <span v-if="event.endDateTime">
                          <span class="mx-2">→</span>
                          <time :datetime="event.endDateTime">
                            {{ formatEventDate(event.endDateTime) }}
                          </time>
                        </span>
                      </span>
                    </div>
                  </div>

                  <div
                    class="flex align-items-start gap-2"
                    v-if="event.venueName || event.cityName || event.provinceName">
                    <i class="pi pi-map-marker text-color-secondary mt-1"></i>
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

                  <div class="flex align-items-start gap-2" v-if="event.provinceName">
                    <i class="pi pi-compass text-color-secondary mt-1"></i>
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("geo.province") }}</span>
                      <span class="text-color-secondary">{{ event.provinceName }}</span>
                    </div>
                  </div>

                  <div class="flex align-items-start gap-2" v-if="event.cityName">
                    <i class="pi pi-building text-color-secondary mt-1"></i>
                    <div class="flex flex-column">
                      <span class="font-medium">{{ t("geo.city") }}</span>
                      <span class="text-color-secondary">{{ event.cityName }}</span>
                    </div>
                  </div>

                  <Divider class="my-1" />

                  <!-- Más info (externo) -->
                  <div class="flex flex-column gap-1">
                    <span class="font-medium">{{ t("events.sourceUrl") }}:</span>
                    <span v-if="event.sourceUrl" class="text-sm">
                      <a
                        :href="event.sourceUrl"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="text-primary underline">
                        {{ event.sourceUrl }}
                      </a>
                    </span>
                    <span v-else class="text-color-secondary text-sm">{{ t("common.noAvail") }}</span>
                  </div>
                </div>
              </template>
            </Card>

            <Card class="border-1 surface-border">
              <template #content>
                <div class="flex flex-column gap-3">
                  <div class="flex align-items-start gap-2">
                    <i class="pi pi-bell text-color-secondary mt-1"></i>
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

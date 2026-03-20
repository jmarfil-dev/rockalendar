<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { ArtistRef, EventPublicListItem } from "~/types/events";

definePageMeta({ layout: "public" });

const { t } = useI18n();
const route = useRoute();
const id = route.params.id as string;

const { data: artist, error: artistError } = await useFetch<ArtistRef>(
  ROUTE_PATH.apiArtistDetail(id),
  { key: `artist-${id}` },
);
if (artistError.value) {
  const status = (artistError.value as any)?.status ?? (artistError.value as any)?.statusCode ?? 500;
  throw createError({ status: status >= 500 ? status : 404 });
}

useHead({ title: () => artist.value?.name ?? t("page.artistDetail") });

const now = new Date().toISOString();
const { data: eventsPage, pending: eventsPending } = await useFetch<{ content: EventPublicListItem[] }>(
  ROUTES.apiEvents,
  {
    key: `artist-events-${id}`,
    query: { artistId: id, dateFrom: now, size: 20, sort: "date,asc" },
  },
);

const events = computed(() => eventsPage.value?.content ?? []);
</script>

<template>
  <article class="p-3 md:p-4 lg:p-5 flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <NuxtLink :to="ROUTES.events" class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" :aria-label="t('common.back')">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </NuxtLink>
      <h1 class="text-2xl font-bold m-0">{{ artist?.name }}</h1>
    </div>

    <Divider class="my-1" />

    <section :aria-label="t('artists.upcomingEvents')">
      <div class="flex align-items-center gap-2 mb-3">
        <i class="pi pi-calendar text-color-secondary" aria-hidden="true" />
        <h2 class="m-0 text-lg font-semibold">{{ t("artists.upcomingEvents") }}</h2>
      </div>

      <div v-if="eventsPending" role="status" class="flex justify-content-center py-6">
        <ProgressSpinner style="width: 2rem; height: 2rem" />
        <span class="sr-only">{{ t("common.loading") }}</span>
      </div>

      <div v-else-if="events.length" class="flex flex-column gap-2">
        <NuxtLink
          v-for="event in events"
          :key="event.id"
          :to="ROUTE_PATH.eventDetail(event.id)"
          class="no-underline">
          <Card class="border-1 surface-border hover:surface-100 transition-colors transition-duration-150 cursor-pointer">
            <template #content>
              <div class="flex flex-column gap-1">
                <span class="font-semibold text-color">{{ event.title }}</span>
                <div class="flex align-items-center gap-2 text-color-secondary text-sm">
                  <i class="pi pi-calendar" aria-hidden="true" />
                  <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime) }}</time>
                </div>
                <div class="flex align-items-center gap-2 text-color-secondary text-sm">
                  <i class="pi pi-map-marker" aria-hidden="true" />
                  <span>{{ event.cityName }}<template v-if="event.provinceName">, {{ event.provinceName }}</template></span>
                </div>
              </div>
            </template>
          </Card>
        </NuxtLink>
      </div>

      <Message v-else severity="info" :closable="false">
        {{ t("artists.noUpcomingEvents") }}
      </Message>
    </section>
  </article>
</template>

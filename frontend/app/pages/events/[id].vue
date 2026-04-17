<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { EventPublic, InteractionStatus } from "~/types/events";

definePageMeta({ layout: "public" });

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const { isAuthenticated } = useAuth();

const id = route.params.id as string;

const { saving, getInteraction, setInteraction, removeInteraction, fetchAgenda } = useAgenda();

const currentInteraction = computed(() => getInteraction(id));

const removeDialogVisible = ref(false);

function onToggleInteraction(target: InteractionStatus) {
  if (currentInteraction.value === target) {
    removeDialogVisible.value = true;
  } else {
    setInteraction(id, target);
  }
}

async function confirmRemove() {
  await removeInteraction(id);
  removeDialogVisible.value = false;
}

onMounted(async () => {
  if (isAuthenticated.value) {
    await fetchAgenda();
  }
});

function goBack() {
  const prev = window.history.state?.back as string | undefined;
  if (prev?.startsWith(ROUTES.events) || prev?.startsWith(ROUTES.meAgenda)) {
    router.back();
  } else {
    navigateTo(ROUTES.events);
  }
}

// Cualquier error 4xx (UUID inválido, no encontrado, etc.) se normaliza a 404:
// el usuario no debe poder distinguir entre un ID malformado y uno inexistente.
// Los errores 5xx se preservan para que sean reportables.
const { data: event, pending, error: fetchError } = await useFetch<EventPublic>(ROUTE_PATH.apiEventDetail(id), {
  key: `event-${id}`,
});
if (fetchError.value) {
  const err = fetchError.value as unknown as { status?: number; statusCode?: number };
  const status = err?.status ?? err?.statusCode ?? 500;
  throw createError({ status: status >= 500 ? status : 404 });
}

const eventDescription = computed(() => {
  if (!event.value) return "";
  if (event.value.description) return event.value.description.slice(0, 200);
  const parts = [event.value.venueName, event.value.cityName, event.value.provinceName].filter(Boolean);
  return parts.length ? `${event.value.title} — ${parts.join(", ")}` : event.value.title;
});

function escapeJsonForScriptTag(json: string): string {
  // JSON.stringify no escapa < > & por defecto.
  // Sin este escape, <\/script> en datos de usuario podría romper el contexto del tag.
  return json.replace(/</g, "\\u003c").replace(/>/g, "\\u003e").replace(/&/g, "\\u0026");
}

const eventJsonLd = computed(() => {
  if (!event.value) return null;
  const ev = event.value;
  const schema: Record<string, unknown> = {
    "@context": "https://schema.org",
    "@type": "Event",
    name: ev.title,
    startDate: ev.startDateTime,
    location: {
      "@type": "Place",
      name: ev.venueName ?? ev.cityName,
      address: {
        "@type": "PostalAddress",
        addressLocality: ev.cityName,
        addressRegion: ev.provinceName,
        addressCountry: "ES",
      },
    },
  };
  if (ev.endDate) schema.endDate = ev.endDate;
  if (ev.description) schema.description = ev.description;
  if (ev.posterUrl) schema.image = ev.posterUrl;
  if (ev.artists?.length) schema.performer = ev.artists.map((a) => ({ "@type": "MusicGroup", name: a.name }));
  return escapeJsonForScriptTag(JSON.stringify(schema));
});

useHead({
  title: () => event.value?.title ?? t("page.events"),
  script: () => eventJsonLd.value ? [{ type: "application/ld+json", innerHTML: eventJsonLd.value }] : [],
});

useSeoMeta({
  description: () => eventDescription.value,
  ogTitle: () => event.value?.title,
  ogDescription: () => eventDescription.value,
  ogImage: () => event.value?.posterUrl ?? undefined,
  ogType: "website",
  twitterCard: () => event.value?.posterUrl ? "summary_large_image" : "summary",
  twitterTitle: () => event.value?.title,
  twitterDescription: () => eventDescription.value,
  twitterImage: () => event.value?.posterUrl ?? undefined,
});

function mapsUrl(e: EventPublic): string {
  const parts = [e.venueName, e.cityName, e.provinceName].filter(Boolean);
  return `https://www.google.com/maps/search/${encodeURIComponent(parts.join(" "))}`;
}
</script>

<template>
  <article class="p-3 md:p-4 lg:p-5 flex flex-column gap-4">
    <!-- Cabecera de navegación -->
    <div class="flex align-items-center gap-3">
      <button type="button" class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" :aria-label="t('common.back')" @click="goBack">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("events.listEvents") }}</h1>
    </div>

    <!-- Loading -->
    <div v-if="pending" role="status" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t('common.loading') }}</span>
    </div>

    <!-- Contenido -->
    <div v-else-if="event" class="flex flex-column gap-4">
      <!-- Header del evento -->
      <header class="flex flex-column gap-2">
        <h2 class="m-0 text-2xl md:text-3xl font-semibold line-height-2">{{ event.title }}</h2>

        <div class="flex align-items-center gap-2 text-color-secondary">
          <i class="pi pi-map-marker" />
          <span>
            <a v-if="event.venueName" :href="mapsUrl(event)" target="_blank" rel="noopener noreferrer" class="inline-link">{{ event.venueName }}</a>
            <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
            <span v-if="event.cityName">{{ event.cityName }}</span>
            <span v-if="event.cityName && event.provinceName">, </span>
            <span v-if="event.provinceName">{{ event.provinceName }}</span>
          </span>
        </div>

        <div class="flex align-items-center gap-2 text-color-secondary">
          <i class="pi pi-calendar" />
          <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime, event.startTimeUnknown) }}</time>
          <template v-if="event.endDate">
            <span class="mx-1">→</span>
            <time :datetime="event.endDate">{{ formatEventEndDate(event.endDate) }}</time>
          </template>
        </div>

        <Divider class="my-1" />
      </header>

      <!-- Poster -->
      <section :aria-label="t('events.poster')" class="w-full">
        <Card class="border-1 surface-border">
          <template #content>
            <div
              v-if="event.posterUrl"
              class="border-round-lg overflow-hidden flex align-items-center justify-content-center surface-50"
              style="min-height: 8rem">
              <NuxtPicture
                :src="event.posterUrl"
                :alt="event.title"
                sizes="(max-width: 640px) 360px, 800px"
                format="avif,webp"
                style="display: block; margin: 0 auto;"
                :img-attrs="{
                  style: 'max-width: 100%; max-height: 480px; object-fit: contain; display: block;'
                }" />
            </div>
            <div
              v-else
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
                    <NuxtLink
                      v-for="artist in event.artists"
                      :key="artist.id"
                      :to="ROUTE_PATH.artistDetail(artist.id)"
                      class="no-underline">
                      <Tag :value="artist.name" rounded severity="info" class="cursor-pointer" />
                    </NuxtLink>
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
                        <time :datetime="event.startDateTime">{{ formatEventDate(event.startDateTime, event.startTimeUnknown) }}</time>
                        <template v-if="event.endDate">
                          <span class="mx-1">→</span>
                          <time :datetime="event.endDate">{{ formatEventEndDate(event.endDate) }}</time>
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
                      <span>
                        <a v-if="event.venueName" :href="mapsUrl(event)" target="_blank" rel="noopener noreferrer" class="inline-link">{{ event.venueName }}</a>
                        <span v-if="event.venueName && (event.cityName || event.provinceName)"> · </span>
                        <span v-if="event.cityName">{{ event.cityName }}</span>
                        <span v-if="event.cityName && event.provinceName">, </span>
                        <span v-if="event.provinceName">{{ event.provinceName }}</span>
                      </span>
                    </div>
                  </div>

                  <div v-if="event.sourceUrl" class="flex align-items-start gap-2">
                    <i class="pi pi-link text-color-secondary mt-1" />
                    <div class="flex flex-column min-w-0 flex-1">
                      <span class="font-medium">{{ t("events.sourceUrl") }}</span>
                      <a
                        v-if="isSafeUrl(event.sourceUrl)"
                        :href="event.sourceUrl"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="text-primary underline text-xs block white-space-nowrap overflow-hidden text-overflow-ellipsis">
                        {{ event.sourceUrl }}
                      </a>
                      <span v-else class="text-color-secondary text-xs block white-space-nowrap overflow-hidden text-overflow-ellipsis">{{ event.sourceUrl }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </Card>

            <!-- Botones de agenda (solo para usuarios autenticados) -->
            <!-- ClientOnly: isAuthenticated depende de localStorage, no disponible en SSR -->
            <ClientOnly>
            <Card v-if="isAuthenticated" class="border-1 surface-border">
              <template #title>
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-calendar" />
                  <h3 class="m-0 text-base font-semibold">{{ t("me.agenda.title") }}</h3>
                </div>
              </template>
              <template #content>
                <div class="flex gap-2">
                  <Button
                    :label="t('me.agenda.interested')"
                    :icon="currentInteraction === 'INTERESTED' ? 'pi pi-heart-fill' : 'pi pi-heart'"
                    :severity="currentInteraction === 'INTERESTED' ? 'primary' : 'secondary'"
                    :loading="saving === id"
                    class="flex-1"
                    type="button"
                    @click="onToggleInteraction('INTERESTED')" />
                  <Button
                    :label="t('me.agenda.going')"
                    :icon="currentInteraction === 'GOING' ? 'pi pi-check-circle' : 'pi pi-circle'"
                    :severity="currentInteraction === 'GOING' ? 'primary' : 'secondary'"
                    :loading="saving === id"
                    class="flex-1"
                    type="button"
                    @click="onToggleInteraction('GOING')" />
                </div>
              </template>
            </Card>
            </ClientOnly>

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
                  <Button :label="t('common.sendCom')" icon="pi pi-send" class="w-full" type="button" @click="navigateTo(ROUTES.contact)" />
                </div>
              </template>
            </Card>
          </div>
        </aside>
      </div>
    </div>
  </article>

  <Dialog v-model:visible="removeDialogVisible" :header="t('me.agenda.title')" modal :style="{ width: '22rem' }">
    <p class="m-0 text-color-secondary">{{ t("me.agenda.removeConfirm") }}</p>
    <template #footer>
      <Button :label="t('me.agenda.removeCancel')" severity="secondary" outlined @click="removeDialogVisible = false" />
      <Button :label="t('me.agenda.removeOk')" severity="danger" icon="pi pi-trash" :loading="saving === id" @click="confirmRemove" />
    </template>
  </Dialog>
</template>

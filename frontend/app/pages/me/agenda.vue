<script setup lang="ts">
import type { InteractionStatus } from "~/types/events";
import { ROUTE_PATH } from "~/constants/routes";

definePageMeta({ layout: "private", ssr: false });

const { t } = useI18n();
useHead({ title: () => t("page.meAgenda") });

const { items, loading, saving, error, fetchAgenda, setInteraction, removeInteraction } = useAgenda();

const STATUS_SEVERITY: Record<InteractionStatus, string> = {
  INTERESTED: "info",
  GOING: "success",
};

async function onToggle(eventId: string, currentStatus: InteractionStatus, target: InteractionStatus) {
  if (currentStatus === target) {
    await removeInteraction(eventId);
  } else {
    await setInteraction(eventId, target);
  }
}

onMounted(fetchAgenda);
</script>

<template>
  <div class="flex flex-column gap-4">
    <h1 class="text-2xl font-bold m-0">{{ t("me.agenda") }}</h1>

    <!-- Estado de carga -->
    <div v-if="loading" role="status" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
      <span class="sr-only">{{ t("common.loading") }}</span>
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">
      {{ error }}
    </Message>

    <!-- Sin resultados -->
    <div v-else-if="items.length === 0" class="text-center py-6 text-color-secondary">
      {{ t("me.agenda.noItems") }}
    </div>

    <!-- Lista de ítems -->
    <div v-else class="grid">
      <div v-for="item in items" :key="item.eventId" class="col-12 md:col-6">
        <Card class="h-full border-1 surface-50 surface-border">
          <template #title>
            <div class="flex align-items-start justify-content-between gap-3">
              <NuxtLink :to="ROUTE_PATH.eventDetail(item.eventId)" class="no-underline text-color">
                <span>{{ item.title }}</span>
              </NuxtLink>
              <Tag
                :value="t(`me.agenda.status.${item.status}`)"
                :severity="STATUS_SEVERITY[item.status]"
                class="flex-shrink-0" />
            </div>
          </template>
          <template #content>
            <div class="flex flex-column gap-3">
              <div class="text-color-secondary text-sm flex flex-column gap-2">
                <div>
                  <i class="pi pi-calendar mr-2" aria-hidden="true" />
                  {{ new Date(item.startDateTime).toLocaleDateString() }}
                  <template v-if="item.endDateTime">
                    <span class="mx-1">→</span>
                    {{ new Date(item.endDateTime).toLocaleDateString() }}
                  </template>
                </div>
                <div>
                  <i class="pi pi-map-marker mr-2" aria-hidden="true" />
                  {{ item.venueName }} · {{ item.cityName }}<span v-if="item.provinceName">, {{ item.provinceName }}</span>
                </div>
              </div>

              <!-- Botones de interacción -->
              <div class="flex gap-2">
                <Button
                  :label="t('me.agenda.interested')"
                  :icon="item.status === 'INTERESTED' ? 'pi pi-heart-fill' : 'pi pi-heart'"
                  :severity="item.status === 'INTERESTED' ? 'primary' : 'secondary'"
                  :loading="saving === item.eventId"
                  size="small"
                  @click="onToggle(item.eventId, item.status, 'INTERESTED')" />
                <Button
                  :label="t('me.agenda.going')"
                  :icon="item.status === 'GOING' ? 'pi pi-check-circle' : 'pi pi-circle'"
                  :severity="item.status === 'GOING' ? 'primary' : 'secondary'"
                  :loading="saving === item.eventId"
                  size="small"
                  @click="onToggle(item.eventId, item.status, 'GOING')" />
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { Notification, NotificationBandeja, NotificationType } from "~/types/notification";
import { NOTIFICATION_TYPES_BY_BANDEJA } from "~/types/notification";
import type { PageResponse } from "~/types/pagination";

const props = defineProps<{ bandeja: NotificationBandeja }>();

const { t, locale } = useI18n();
const { markAsRead, unreadCount } = useNotifications();

const items = ref<Notification[]>([]);
const loading = ref(false);
const currentPage = ref(0);
const totalPages = ref(1);

const bandejaKey = computed(() => props.bandeja.toLowerCase() as "user" | "moderation" | "admin");
const hasMore = computed(() => currentPage.value < totalPages.value - 1);

async function load(page = 0) {
  loading.value = true;
  const types = NOTIFICATION_TYPES_BY_BANDEJA[props.bandeja];
  const params = new URLSearchParams({ page: String(page), size: "20" });
  types.forEach((type) => params.append("types", type));
  const res = await fetchAuthResult<PageResponse<Notification>>(`${ROUTES.apiNotifications}?${params}`);
  if (res.ok) {
    if (page === 0) {
      items.value = res.data.content;
    } else {
      items.value.push(...res.data.content);
    }
    totalPages.value = res.data.page.totalPages;
    currentPage.value = page;
  }
  loading.value = false;
}

async function onItemClick(item: Notification) {
  if (!item.isRead) {
    const res = await markAsRead(item.id);
    if (res.ok) {
      item.isRead = true;
      if (unreadCount.value[bandejaKey.value] > 0) {
        unreadCount.value[bandejaKey.value]--;
      }
    }
  }
  if (item.eventId) {
    await navigateTo(ROUTE_PATH.eventDetail(item.eventId));
  }
}

function notificationText(item: Notification): string {
  return t(`notifications.types.${item.type}`, item.payload);
}

const ICON_BY_TYPE: Record<NotificationType, string> = {
  EVENT_APPROVED: "pi pi-check-circle",
  EVENT_REJECTED: "pi pi-times-circle",
  EVENT_NEEDS_CHANGES: "pi pi-pencil",
  EVENT_COMMENT: "pi pi-comment",
  EVENT_PENDING_MODERATION: "pi pi-clock",
  EVENT_FLAGGED: "pi pi-flag",
  POSSIBLE_DUPLICATE_DETECTED: "pi pi-copy",
  PROMOTION_REQUEST: "pi pi-arrow-up",
  USER_AUTOBANNED: "pi pi-ban",
};

const ICON_COLOR_BY_TYPE: Record<NotificationType, string> = {
  EVENT_APPROVED: "text-green-500",
  EVENT_REJECTED: "text-red-500",
  EVENT_NEEDS_CHANGES: "text-orange-500",
  EVENT_COMMENT: "text-blue-400",
  EVENT_PENDING_MODERATION: "text-blue-500",
  EVENT_FLAGGED: "text-red-400",
  POSSIBLE_DUPLICATE_DETECTED: "text-yellow-500",
  PROMOTION_REQUEST: "text-purple-500",
  USER_AUTOBANNED: "text-red-600",
};

onMounted(() => load(0));
</script>

<template>
  <div class="flex flex-column gap-3">
    <div v-if="loading && items.length === 0" class="flex justify-content-center py-6">
      <ProgressSpinner style="width: 2rem; height: 2rem" />
    </div>

    <div v-else-if="items.length === 0" class="flex flex-column align-items-center gap-3 py-8 text-color-secondary">
      <i class="pi pi-bell text-5xl" />
      <p class="m-0 text-sm">{{ t("notifications.empty") }}</p>
    </div>

    <ul v-else class="list-none p-0 m-0 flex flex-column gap-1">
      <li
        v-for="item in items"
        :key="item.id"
        :class="[
          'flex align-items-start gap-3 p-3 border-round transition-colors transition-duration-150',
          item.eventId || !item.isRead ? 'cursor-pointer hover:surface-hover' : '',
        ]"
        :role="item.eventId ? 'button' : undefined"
        :tabindex="item.eventId ? 0 : undefined"
        @click="onItemClick(item)"
        @keydown.enter="onItemClick(item)">
        <i :class="[ICON_BY_TYPE[item.type] ?? 'pi pi-bell', ICON_COLOR_BY_TYPE[item.type], 'text-xl mt-1 flex-shrink-0']" />
        <div class="flex flex-column gap-1 flex-1 min-w-0">
          <span :class="['text-sm', !item.isRead && 'font-semibold']">
            {{ notificationText(item) }}
          </span>
          <span class="text-xs text-color-secondary">
            {{ formatRelativeDate(item.createdAt, locale) }}
          </span>
        </div>
        <span
          v-if="!item.isRead"
          class="inline-block border-round-full bg-primary flex-shrink-0 mt-2"
          style="width: 0.5rem; height: 0.5rem" />
      </li>
    </ul>

    <div class="flex justify-content-center">
      <Button
        v-if="hasMore"
        :label="t('notifications.loadMore')"
        :loading="loading"
        text
        size="small"
        @click="load(currentPage + 1)" />
    </div>
  </div>
</template>

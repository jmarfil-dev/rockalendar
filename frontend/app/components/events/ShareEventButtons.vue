<script setup lang="ts">
interface Props {
  eventTitle: string;
}

const props = defineProps<Props>();
const { t } = useI18n();

const eventUrl = ref("");

onMounted(() => {
  eventUrl.value = window.location.href;
});

function shareWhatsapp() {
  const text = encodeURIComponent(eventUrl.value);
  window.open(`https://wa.me/?text=${text}`, "_blank", "noopener,noreferrer");
}

function shareFacebook() {
  window.open(
    `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(eventUrl.value)}`,
    "_blank",
    "noopener,noreferrer",
  );
}

const copied = ref(false);

async function shareToInstagram() {
  if (navigator.share) {
    try {
      await navigator.share({ title: props.eventTitle, url: eventUrl.value });
    } catch {
      // usuario canceló o el browser lo bloqueó
    }
    return;
  }
  try {
    await navigator.clipboard.writeText(eventUrl.value);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 3000);
  } catch {
    // clipboard denegado
  }
}
</script>

<template>
  <div class="flex flex-column gap-2">
    <span class="text-sm font-medium">{{ t("share.title") }}</span>
    <div class="flex gap-2">
      <Button
        icon="pi pi-whatsapp"
        severity="secondary"
        outlined
        class="flex-1"
        type="button"
        :aria-label="t('share.whatsapp')"
        :title="t('share.whatsapp')"
        @click="shareWhatsapp" />

      <Button
        icon="pi pi-facebook"
        severity="secondary"
        outlined
        class="flex-1"
        type="button"
        :aria-label="t('share.facebook')"
        :title="t('share.facebook')"
        @click="shareFacebook" />

      <Button
        icon="pi pi-instagram"
        :severity="copied ? 'success' : 'secondary'"
        outlined
        class="flex-1"
        type="button"
        :aria-label="t('share.instagram')"
        :title="copied ? t('share.copied') : t('share.instagram')"
        @click="shareToInstagram" />
    </div>
    <p v-if="copied" class="m-0 text-xs text-color-secondary text-center">
      {{ t("share.instagramHint") }}
    </p>
  </div>
</template>

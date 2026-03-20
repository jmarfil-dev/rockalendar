<script setup lang="ts">
import type { ArtistChip, Artist } from "~/types/artist";
import { useArtistAutocomplete } from "~/composables/useArtist";

const MAX_ARTISTS = 200;

const props = defineProps<{
  modelValue: ArtistChip[];
  fieldError?: string;
}>();

const emit = defineEmits<{ "update:modelValue": [value: ArtistChip[]] }>();

const { suggestions, search } = useArtistAutocomplete();
const { t, te } = useI18n();

function tr(key: string) {
  return te(key) ? t(key) : key;
}

const inputValue = ref<string | Artist>("");
const atLimit = computed(() => props.modelValue.length >= MAX_ARTISTS);
const canAdd = computed(
  () => !atLimit.value && typeof inputValue.value === "string" && inputValue.value.trim().length > 0,
);

// PrimeVue Chip guarda estado interno (visible=false al eliminar). Si usamos el índice como :key,
// Vue recicla la instancia del chip adyacente y hereda ese estado, eliminando chips de más.
// Solución: claves estables generadas al añadir cada chip.
let keyCounter = 0;
const chipKeys = ref<number[]>([]);

function addChip(chip: ArtistChip) {
  chipKeys.value = [...chipKeys.value, keyCounter++];
  emit("update:modelValue", [...props.modelValue, chip]);
  nextTick(() => {
    inputValue.value = "";
  });
}

function removeChip(index: number) {
  chipKeys.value = chipKeys.value.filter((_, i) => i !== index);
  const newVal = [...props.modelValue];
  newVal.splice(index, 1);
  emit("update:modelValue", newVal);
}

// Cuando el usuario selecciona una sugerencia del dropdown
function onItemSelect(event: { value: Artist }) {
  addChip({ id: event.value.id, name: event.value.name });
}

// Añadir el texto libre escrito actualmente
function onAddCurrentText() {
  const name = typeof inputValue.value === "string" ? inputValue.value.trim() : "";
  if (name) addChip({ name });
}

// El wrapper captura Enter antes de que llegue al <form>
function onWrapperKeydown(e: KeyboardEvent) {
  if (e.key !== "Enter") return;
  e.preventDefault();
  e.stopPropagation();
  // Si PrimeVue ya procesó la selección del dropdown, inputValue es un objeto → no hacer nada (item-select lo gestionó)
  if (typeof inputValue.value === "string") onAddCurrentText();
}

// Pegado masivo: parsea lista separada por ; o salto de línea
function onPaste(e: ClipboardEvent) {
  const text = e.clipboardData?.getData("text") ?? "";
  const tokens = text.split(/[;\n]/).map((s) => s.trim()).filter(Boolean);
  if (tokens.length <= 1) return; // paste normal → el autocomplete lo gestiona
  e.preventDefault();
  const newChips = [...props.modelValue];
  const newKeys = [...chipKeys.value];
  for (const name of tokens) {
    if (newChips.length >= MAX_ARTISTS) break;
    newChips.push({ name });
    newKeys.push(keyCounter++);
  }
  chipKeys.value = newKeys;
  emit("update:modelValue", newChips);
  nextTick(() => { inputValue.value = ""; });
}
</script>

<template>
  <div class="flex flex-column gap-2">
    <label for="artists-input" class="text-sm text-color-secondary">
      {{ t("events.artists") }} <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t('common.required') }}</span>
    </label>

    <div class="flex gap-2 align-items-center" @keydown="onWrapperKeydown" @paste="onPaste">
      <AutoComplete
        v-model="inputValue"
        inputId="artists-input"
        :suggestions="suggestions"
        option-label="name"
        :disabled="atLimit"
        :invalid="!!fieldError"
        :placeholder="atLimit ? '' : t('me.propose.artistSelectorPlaceholder')"
        class="flex-1"
        aria-describedby="artists-error"
        @complete="search($event.query)"
        @item-select="onItemSelect" />
      <Button
        type="button"
        icon="pi pi-plus"
        severity="secondary"
        outlined
        :disabled="!canAdd"
        :aria-label="t('me.propose.addArtist')"
        @click="onAddCurrentText" />
    </div>

    <div v-if="modelValue.length > 0" class="flex flex-wrap gap-2">
      <Chip v-for="(chip, i) in modelValue" :key="chipKeys[i]" :label="chip.name" removable @remove="removeChip(i)" />
    </div>

    <Message id="artists-error" v-show="fieldError" severity="error" variant="simple" size="small">
      {{ tr(fieldError!) }}
    </Message>
  </div>
</template>

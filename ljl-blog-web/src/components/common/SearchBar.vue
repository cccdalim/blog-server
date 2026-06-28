<script setup lang="ts">
import { ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

interface Props {
  placeholder?: string
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '搜索...',
  modelValue: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
}>()

const inputValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (val) => {
    inputValue.value = val
  },
)

const debouncedSearch = useDebounceFn((val: string) => {
  emit('update:modelValue', val)
  emit('search', val)
}, 300)

function onInput() {
  debouncedSearch(inputValue.value)
}

function onClear() {
  inputValue.value = ''
  emit('update:modelValue', '')
  emit('search', '')
}
</script>

<template>
  <div class="search-bar">
    <span class="search-bar__icon" aria-hidden="true">⌕</span>
    <input
      v-model="inputValue"
      type="search"
      class="search-bar__input"
      :placeholder="placeholder"
      @input="onInput"
    />
    <button
      v-if="inputValue"
      type="button"
      class="search-bar__clear"
      aria-label="清除搜索"
      @click="onClear"
    >
      ×
    </button>
  </div>
</template>

<style scoped lang="scss">
.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: var(--color-bg-card);
  border: 2px solid var(--pixel-border);
  border-radius: $radius-sm;
  transition: border-color var(--transition-fast);

  &:focus-within {
    border-color: var(--pixel-accent-strong);
  }

  &__icon {
    color: var(--color-text-muted);
    font-size: $font-size-lg;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    border: none;
    background: transparent;
    color: var(--color-text-primary);
    font-size: $font-size-sm;
    outline: none;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }

  &__clear {
    color: var(--color-text-muted);
    font-size: $font-size-lg;
    line-height: 1;
    padding: 0 $spacing-xs;
    transition: color var(--transition-fast);

    &:hover {
      color: var(--color-text-primary);
    }
  }
}
</style>

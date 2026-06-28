<script setup lang="ts">
interface Props {
  label: string
  variant?: 'grass' | 'wood' | 'stone' | 'outline'
  size?: 'sm' | 'md'
}

withDefaults(defineProps<Props>(), {
  variant: 'outline',
  size: 'sm',
})
</script>

<template>
  <span class="app-tag" :class="[`app-tag--${variant}`, `app-tag--${size}`]">
    {{ label }}
  </span>
</template>

<style scoped lang="scss">
.app-tag {
  display: inline-block;
  font-weight: 500;
  border-radius: $radius-sm;
  white-space: nowrap;

  &--sm {
    padding: 2px $spacing-sm;
    font-size: $font-size-xs;
  }

  &--md {
    padding: $spacing-xs $spacing-md;
    font-size: $font-size-sm;
  }

  &--grass {
    background: var(--pixel-grass-bg);
    color: var(--pixel-accent);
    border: 1px solid var(--pixel-grass-border-alpha);
  }

  &--wood {
    background: var(--pixel-wood-bg);
    color: var(--pixel-wood-text);
    border: 1px solid var(--pixel-wood-border-alpha);
  }

  &--stone {
    background: var(--pixel-stone-bg);
    color: var(--color-text-secondary);
    border: 1px solid var(--pixel-stone-border-alpha);
  }

  &--outline {
    background: transparent;
    color: var(--color-text-secondary);
    border: 1px solid var(--color-border);
  }
}
</style>

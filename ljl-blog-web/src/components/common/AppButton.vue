<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  block?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  block: false,
})
</script>

<template>
  <button
    class="app-button press-scale"
    :class="[`app-button--${variant}`, `app-button--${size}`, { 'app-button--block': block }]"
  >
    <slot />
  </button>
</template>

<style scoped lang="scss">
.app-button {
  @include pixel-button;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  font-weight: 600;
  border-radius: $radius-sm;
  white-space: nowrap;

  &--primary {
    background: var(--pixel-accent-strong);
    border-color: var(--pixel-accent-strong);
    color: #fff;

    &:hover {
      background: var(--pixel-accent);
      border-color: var(--pixel-accent);
    }
  }

  &--secondary {
    border-color: var(--pixel-wood);
  }

  &--ghost {
    background: transparent;
    box-shadow: none;
    border-color: transparent;

    &:hover {
      background: var(--color-bg-elevated);
      box-shadow: none;
      transform: none;
    }
  }

  &--sm {
    padding: $spacing-xs $spacing-sm;
    font-size: $font-size-xs;
  }

  &--md {
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
  }

  &--lg {
    padding: $spacing-md $spacing-lg;
    font-size: $font-size-base;
  }

  &--block {
    width: 100%;
  }
}
</style>

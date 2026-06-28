<script setup lang="ts">
interface Props {
  hoverable?: boolean
  padding?: 'sm' | 'md' | 'lg'
}

withDefaults(defineProps<Props>(), {
  hoverable: true,
  padding: 'md',
})
</script>

<template>
  <div
    class="app-card"
    :class="[
      { 'app-card--hoverable': hoverable },
      `app-card--padding-${padding}`,
    ]"
  >
    <slot />
  </div>
</template>

<style scoped lang="scss">
.app-card {
  @include card-base;
  background: var(--color-bg-card);

  &--hoverable {
    cursor: pointer;
  }

  &--padding-sm {
    padding: $spacing-sm;
  }

  &--padding-md {
    padding: $spacing-md;
  }

  &--padding-lg {
    padding: $spacing-lg;
  }
}
</style>

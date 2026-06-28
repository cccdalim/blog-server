<script setup lang="ts">
interface Props {
  progress: number
}

defineProps<Props>()
</script>

<template>
  <div class="reading-progress" role="progressbar" :aria-valuenow="progress" aria-valuemin="0" aria-valuemax="100">
    <div class="reading-progress__bar" :style="{ width: `${progress}%` }" />
  </div>
</template>

<style scoped lang="scss">
.reading-progress {
  position: fixed;
  top: var(--navbar-height);
  left: 0;
  right: 0;
  height: 3px;
  background: var(--color-border);
  z-index: calc(#{$z-navbar} - 1);

  &__bar {
    height: 100%;
    background: var(--pixel-accent-strong);
    transition: width 0.15s ease;
    box-shadow: 0 0 8px rgba(60, 133, 39, 0.4);
  }
}
</style>

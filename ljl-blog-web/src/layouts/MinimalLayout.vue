<script setup lang="ts">
/** MinimalLayout — 404 等极简页面 */
</script>

<template>
  <div class="minimal-layout">
    <slot />
  </div>
</template>

<style scoped lang="scss">
.minimal-layout {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  @include texture-bg;
}
</style>

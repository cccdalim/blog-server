<script setup lang="ts">
import { useScroll } from '@vueuse/core'
import { computed } from 'vue'

const { y } = useScroll(window)
const visible = computed(() => y.value > 400)

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <Transition name="fade">
    <button
      v-show="visible"
      class="back-top"
      aria-label="返回顶部"
      @click="scrollToTop"
    >
      ↑
    </button>
  </Transition>
</template>

<style scoped lang="scss">
.back-top {
  position: fixed;
  right: $spacing-lg;
  bottom: $spacing-lg;
  z-index: $z-backtop;
  width: 44px;
  height: 44px;
  @include pixel-border(var(--pixel-accent-strong));
  background: var(--color-bg-card);
  color: var(--pixel-accent);
  font-size: $font-size-lg;
  font-weight: 700;
  border-radius: $radius-sm;
  transition:
    transform var(--transition-fast),
    background var(--transition-fast);

  &:hover {
    background: var(--pixel-accent-strong);
    color: #fff;
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(1px);
  }

  @include mobile {
    right: $spacing-md;
    bottom: $spacing-md;
    width: 40px;
    height: 40px;
  }
}
</style>

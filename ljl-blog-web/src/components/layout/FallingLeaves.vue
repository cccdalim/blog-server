<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { storeToRefs } from 'pinia'
import { computed, ref, watch } from 'vue'
import {
  createFallingLeaves,
  createRandomLeaf,
  type FallingLeafConfig,
} from '@/composables/useFallingLeaves'
import { useEffectsStore } from '@/stores/effects'

const effectsStore = useEffectsStore()
const { fallingLeavesEnabled } = storeToRefs(effectsStore)

const { width } = useWindowSize()

const leafCount = computed(() => (width.value < 768 ? 10 : 18))
const leaves = ref<FallingLeafConfig[]>([])

function refreshLeaves() {
  leaves.value = createFallingLeaves(leafCount.value)
}

function topUpLeaves() {
  const target = leafCount.value
  while (leaves.value.length < target) {
    leaves.value.push(createRandomLeaf())
  }
}

watch(
  leafCount,
  () => {
    if (fallingLeavesEnabled.value) refreshLeaves()
  },
  { immediate: true },
)

watch(fallingLeavesEnabled, (enabled) => {
  if (enabled) topUpLeaves()
})

/** 每轮动画结束：开启则换新叶，关闭则移除（不再生成） */
function onLeafCycleEnd(id: string) {
  const index = leaves.value.findIndex((leaf) => leaf.id === id)
  if (index === -1) return

  if (fallingLeavesEnabled.value) {
    leaves.value[index] = createRandomLeaf()
  } else {
    leaves.value.splice(index, 1)
  }
}

function leafStyle(leaf: FallingLeafConfig): Record<string, string> {
  return {
    left: `${leaf.left}%`,
    '--leaf-delay': `${leaf.delay}s`,
    '--leaf-duration': `${leaf.duration}s`,
    '--leaf-sway': `${leaf.sway}px`,
    '--leaf-spin': `${leaf.spin}deg`,
    '--leaf-opacity': String(leaf.opacity),
    '--leaf-scale': String(leaf.scale),
  }
}
</script>

<template>
  <div v-show="leaves.length" class="falling-leaves" aria-hidden="true">
    <span
      v-for="leaf in leaves"
      :key="leaf.id"
      class="falling-leaves__item"
      :class="`falling-leaves__item--${leaf.variant}`"
      :style="leafStyle(leaf)"
      @animationiteration="onLeafCycleEnd(leaf.id)"
    >
      <i class="falling-leaves__pixel" />
    </span>
  </div>
</template>

<style scoped lang="scss">
.falling-leaves {
  position: fixed;
  inset: 0;
  z-index: 50;
  pointer-events: none;
  overflow: hidden;

  &__item {
    position: absolute;
    top: -5vh;
    display: block;
    transform: scale(var(--leaf-scale));
    animation: leaf-fall var(--leaf-duration) linear infinite;
    animation-delay: var(--leaf-delay);
    will-change: transform, opacity;
  }

  &__pixel {
    display: block;
    width: 4px;
    height: 4px;
    image-rendering: pixelated;
    background: var(--leaf-main);
    box-shadow:
      4px 0 0 var(--leaf-light),
      4px 4px 0 var(--leaf-dark),
      0 4px 0 var(--leaf-main),
      -4px 4px 0 var(--leaf-dark),
      0 -4px 0 var(--leaf-light);
  }

  &__item--green {
    --leaf-main: var(--pixel-vine-leaf);
    --leaf-light: var(--pixel-vine-leaf-light);
    --leaf-dark: var(--pixel-vine-dark);
  }

  &__item--yellow {
    --leaf-main: var(--pixel-leaf-yellow);
    --leaf-light: var(--pixel-leaf-yellow-light);
    --leaf-dark: var(--pixel-leaf-yellow-dark);
  }

  &__item--orange {
    --leaf-main: var(--pixel-leaf-orange);
    --leaf-light: var(--pixel-leaf-orange-light);
    --leaf-dark: var(--pixel-leaf-orange-dark);
  }

  &__item--brown {
    --leaf-main: var(--pixel-leaf-brown);
    --leaf-light: var(--pixel-leaf-brown-light);
    --leaf-dark: var(--pixel-leaf-brown-dark);
  }
}

@keyframes leaf-fall {
  0% {
    transform: translate3d(0, -10vh, 0) rotate(0deg) scale(var(--leaf-scale));
    opacity: 0;
  }

  8% {
    opacity: var(--leaf-opacity);
  }

  25% {
    transform: translate3d(calc(var(--leaf-sway) * 1), 25vh, 0)
      rotate(calc(var(--leaf-spin) * 0.25)) scale(var(--leaf-scale));
  }

  50% {
    transform: translate3d(calc(var(--leaf-sway) * -0.65), 50vh, 0)
      rotate(calc(var(--leaf-spin) * 0.5)) scale(var(--leaf-scale));
  }

  75% {
    transform: translate3d(calc(var(--leaf-sway) * 0.75), 75vh, 0)
      rotate(calc(var(--leaf-spin) * 0.75)) scale(var(--leaf-scale));
  }

  92% {
    opacity: calc(var(--leaf-opacity) * 0.6);
  }

  100% {
    transform: translate3d(calc(var(--leaf-sway) * -0.35), 110vh, 0) rotate(var(--leaf-spin))
      scale(var(--leaf-scale));
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .falling-leaves {
    display: none;
  }
}
</style>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useEffectsStore } from '@/stores/effects'

const effectsStore = useEffectsStore()
const { fallingLeavesEnabled } = storeToRefs(effectsStore)

const PIXEL = 4
const WALL_ROWS = 14
const COBBLE_ROWS = Math.ceil(WALL_ROWS / 3)

type Material = 'cobble' | 'stone'
type Shade = 'main' | 'light' | 'dark'

interface WallPixel {
  x: number
  y: number
  material: Material
  shade: Shade
}

const COBBLE_SHADES: Shade[] = ['main', 'light', 'dark']
const STONE_SHADES: Shade[] = ['main', 'dark', 'light']

/** 顶部 1/3 圆石，其余石头 */
const WALL_PIXELS: WallPixel[] = Array.from({ length: WALL_ROWS * 2 }, (_, i) => {
  const x = i % 2
  const y = Math.floor(i / 2)
  const isCobble = y < COBBLE_ROWS
  const shades = isCobble ? COBBLE_SHADES : STONE_SHADES
  return {
    x,
    y,
    material: isCobble ? 'cobble' : 'stone',
    shade: shades[(x + y) % shades.length] ?? 'main',
  }
})

function toggle() {
  effectsStore.toggleFallingLeaves()
}
</script>

<template>
  <div class="mc-lever-wrap">
    <button
      type="button"
      class="mc-lever"
      :class="{ 'mc-lever--on': fallingLeavesEnabled }"
      :aria-label="fallingLeavesEnabled ? '关闭落叶效果' : '开启落叶效果'"
      :aria-pressed="fallingLeavesEnabled"
      :title="fallingLeavesEnabled ? '落叶：开' : '落叶：关'"
      @click="toggle"
    >
      <span
        class="mc-lever__wall"
        :style="{ width: `${2 * PIXEL}px`, height: `${WALL_ROWS * PIXEL}px` }"
        aria-hidden="true"
      >
        <span
          v-for="(cell, i) in WALL_PIXELS"
          :key="i"
          class="mc-lever__wall-pixel"
          :class="`mc-lever__wall-pixel--${cell.material}-${cell.shade}`"
          :style="{
            left: `${cell.x * PIXEL}px`,
            top: `${cell.y * PIXEL}px`,
          }"
        />
        <span class="mc-lever__pivot" />
      </span>

      <span class="mc-lever__arm" aria-hidden="true">
        <span class="mc-lever__arm-bar" />
        <span class="mc-lever__arm-tip" />
      </span>

      <span v-if="fallingLeavesEnabled" class="mc-lever__glow" aria-hidden="true" />
    </button>
  </div>
</template>

<style scoped lang="scss">
.mc-lever-wrap {
  position: fixed;
  left: 0;
  top: calc(var(--navbar-height) + #{$spacing-xl});
  z-index: 55;

  @include mobile {
    top: calc(var(--navbar-height) + #{$spacing-lg});
    transform: scale(0.9);
    transform-origin: top left;
  }
}

.mc-lever {
  position: relative;
  width: 44px;
  height: 56px;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  cursor: pointer;
  image-rendering: pixelated;

  &:focus-visible {
    outline: 2px solid var(--pixel-accent);
    outline-offset: 2px;
  }

  &__wall {
    position: absolute;
    left: 0;
    top: 0;
    background: var(--pixel-stone-dark);
    box-shadow:
      2px 0 0 var(--pixel-stone),
      inset -1px 0 0 rgba(0, 0, 0, 0.25);
  }

  &__wall-pixel {
    position: absolute;
    width: 4px;
    height: 4px;

    &--cobble-main {
      background: var(--pixel-cobble);
    }

    &--cobble-light {
      background: var(--pixel-cobble-light);
    }

    &--cobble-dark {
      background: var(--pixel-cobble-dark);
    }

    &--stone-main {
      background: var(--pixel-stone);
    }

    &--stone-light {
      background: #8f8f8f;
    }

    &--stone-dark {
      background: var(--pixel-stone-dark);
    }
  }

  &__pivot {
    position: absolute;
    right: -2px;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 4px;
    background: var(--pixel-cobble-light);
    border: 1px solid var(--pixel-cobble-dark);
    z-index: 2;
  }

  &__arm {
    position: absolute;
    left: 8px;
    top: 50%;
    width: 0;
    height: 0;
    transform-origin: 0 0;
    transform: translateY(-1px) rotate(-42deg);
    transition: transform 0.2s steps(5, end);
    z-index: 1;
  }

  &__arm-bar {
    position: absolute;
    left: 0;
    top: -2px;
    width: 28px;
    height: 4px;
    background: var(--pixel-lever-stick);
    box-shadow:
      0 1px 0 var(--pixel-lever-stick-dark),
      0 -1px 0 var(--pixel-lever-stick-light);
  }

  &__arm-tip {
    position: absolute;
    left: 26px;
    top: -4px;
    width: 6px;
    height: 6px;
    background: var(--pixel-lever-stick-light);
    border: 1px solid var(--pixel-lever-stick-dark);
  }

  &__glow {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 8px;
    height: 8px;
    background: var(--pixel-redstone-glow);
    opacity: 0.35;
    filter: blur(4px);
    pointer-events: none;
  }

  &:hover &__arm-bar {
    filter: brightness(1.1);
  }

  &:active &__arm {
    transition-duration: 0.08s;
  }

  &--on &__arm {
    transform: translateY(-1px) rotate(42deg);
  }

  &--on &__pivot {
    background: var(--pixel-redstone-glow);
    border-color: var(--pixel-redstone);
    box-shadow: 0 0 6px var(--pixel-redstone-glow);
  }
}
</style>

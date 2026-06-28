<script setup lang="ts">
/** MC 藤蔓像素图 — # 藤干 D 暗部 L 亮叶 l 叶 */
type VineShade = 'vine' | 'vine-dark' | 'leaf' | 'leaf-light'

interface VineCell {
  x: number
  y: number
  shade: VineShade
}

const SHADE_MAP: Record<string, VineShade> = {
  '#': 'vine',
  D: 'vine-dark',
  L: 'leaf',
  l: 'leaf-light',
}

const PIXEL_SIZE = 4

function parsePattern(rows: string[]): VineCell[] {
  const cells: VineCell[] = []
  rows.forEach((row, y) => {
    ;[...row].forEach((char, x) => {
      const shade = SHADE_MAP[char]
      if (shade) cells.push({ x, y, shade })
    })
  })
  return cells
}

function mirrorCells(cells: VineCell[], width: number): VineCell[] {
  return cells.map(({ x, y, shade }) => ({ x: width - 1 - x, y, shade }))
}

/** 左上角垂下，沿左侧缠绕并越过底边 */
const LEFT_HANG = parsePattern([
  'l##  ',
  '###l ',
  ' ##  ',
  '  ###',
  ' D## ',
  '###  ',
  ' ##  ',
  '  #  ',
  ' ##  ',
  ' #   ',
  '  #  ',
  ' #   ',
  'l##  ',
  ' ##  ',
  '##   ',
  '#    ',
  ' ##  ',
])

/** 顶部横向蔓延 — 两段拼接 */
const TOP_CRAWL = parsePattern([
  'l##.###.##l.###.##..l##.###.##l',
])

/** 右上角垂下（镜像） */
const RIGHT_HANG = mirrorCells(LEFT_HANG, 6)

const leftCells = LEFT_HANG
const rightCells = RIGHT_HANG
const topCells = TOP_CRAWL
</script>

<template>
  <div class="mc-vines" aria-hidden="true">
    <!-- 顶部横向藤曼 -->
    <div
      class="mc-vines__cluster mc-vines__cluster--top"
      :style="{
        width: `${32 * PIXEL_SIZE}px`,
        height: `${PIXEL_SIZE}px`,
      }"
    >
      <span
        v-for="cell in topCells"
        :key="`top-${cell.x}-${cell.y}`"
        class="mc-vines__pixel"
        :class="`mc-vines__pixel--${cell.shade}`"
        :style="{
          left: `${cell.x * PIXEL_SIZE}px`,
          top: `${cell.y * PIXEL_SIZE}px`,
        }"
      />
    </div>

    <!-- 左侧垂挂 -->
    <div
      class="mc-vines__cluster mc-vines__cluster--left"
      :style="{
        width: `${6 * PIXEL_SIZE}px`,
        height: `${17 * PIXEL_SIZE}px`,
      }"
    >
      <span
        v-for="cell in leftCells"
        :key="`left-${cell.x}-${cell.y}`"
        class="mc-vines__pixel"
        :class="`mc-vines__pixel--${cell.shade}`"
        :style="{
          left: `${cell.x * PIXEL_SIZE}px`,
          top: `${cell.y * PIXEL_SIZE}px`,
        }"
      />
    </div>

    <!-- 右侧垂挂 -->
    <div
      class="mc-vines__cluster mc-vines__cluster--right"
      :style="{
        width: `${6 * PIXEL_SIZE}px`,
        height: `${17 * PIXEL_SIZE}px`,
      }"
    >
      <span
        v-for="cell in rightCells"
        :key="`right-${cell.x}-${cell.y}`"
        class="mc-vines__pixel"
        :class="`mc-vines__pixel--${cell.shade}`"
        :style="{
          left: `${cell.x * PIXEL_SIZE}px`,
          top: `${cell.y * PIXEL_SIZE}px`,
        }"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.mc-vines {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  overflow: visible;

  &__cluster {
    position: absolute;
    image-rendering: pixelated;

    &--top {
      top: 0;
      left: 50%;
      transform: translateX(-50%);
    }

    &--left {
      top: 0;
      left: 0;
      transform-origin: top center;
      animation: vine-sway-left 5s ease-in-out infinite;
    }

    &--right {
      top: 0;
      right: 0;
      transform-origin: top center;
      animation: vine-sway-right 5s ease-in-out infinite;
      animation-delay: -2.5s;
    }
  }

  &__pixel {
    position: absolute;
    width: 4px;
    height: 4px;
    image-rendering: pixelated;

    &--vine {
      background: var(--pixel-vine);
    }

    &--vine-dark {
      background: var(--pixel-vine-dark);
    }

    &--leaf {
      background: var(--pixel-vine-leaf);
    }

    &--leaf-light {
      background: var(--pixel-vine-leaf-light);
    }
  }
}

@keyframes vine-sway-left {
  0%,
  100% {
    transform: rotate(0deg);
  }

  50% {
    transform: rotate(1.5deg);
  }
}

@keyframes vine-sway-right {
  0%,
  100% {
    transform: rotate(0deg);
  }

  50% {
    transform: rotate(-1.5deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .mc-vines__cluster--left,
  .mc-vines__cluster--right {
    animation: none;
  }
}

@include mobile {
  .mc-vines__cluster--top {
    opacity: 0.6;
    transform: translateX(-50%) scale(0.85);
  }

  .mc-vines__cluster--left {
    left: -2px;
    transform: scale(0.9);
    transform-origin: top left;
    animation: none;
  }

  .mc-vines__cluster--right {
    right: -2px;
    transform: scale(0.9);
    transform-origin: top right;
    animation: none;
  }
}
</style>

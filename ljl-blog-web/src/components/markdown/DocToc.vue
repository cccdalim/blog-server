<script setup lang="ts">
import type { TocItem } from '@/types'

interface Props {
  items: TocItem[]
  activeId?: string
}

defineProps<Props>()

const emit = defineEmits<{
  click: [id: string]
}>()
</script>

<template>
  <nav v-if="items.length" class="doc-toc" aria-label="文档目录">
    <h3 class="doc-toc__title">目录</h3>
    <ul class="doc-toc__list">
      <li
        v-for="item in items"
        :key="item.id"
        class="doc-toc__item"
        :class="[
          `doc-toc__item--level-${item.level}`,
          { 'doc-toc__item--active': activeId === item.id },
        ]"
      >
        <a
          href="#"
          class="doc-toc__link"
          @click.prevent="emit('click', item.id)"
        >
          {{ item.text }}
        </a>
      </li>
    </ul>
  </nav>
</template>

<style scoped lang="scss">
.doc-toc {
  &__title {
    font-size: $font-size-sm;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-sm;
    border-bottom: 2px solid var(--color-border);

    &::before {
      content: '▌';
      color: var(--pixel-accent);
      margin-right: $spacing-xs;
    }
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__item {
    &--level-2 .doc-toc__link {
      padding-left: 0;
    }

    &--level-3 .doc-toc__link {
      padding-left: $spacing-md;
      font-size: $font-size-xs;
    }

    &--level-4 .doc-toc__link {
      padding-left: $spacing-lg;
      font-size: $font-size-xs;
    }

    &--active .doc-toc__link {
      color: var(--pixel-accent);
      border-left-color: var(--pixel-accent-strong);
      background: var(--pixel-grass-bg-subtle);
    }
  }

  &__link {
    display: block;
    padding: $spacing-xs $spacing-sm;
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    border-left: 2px solid transparent;
    border-radius: 0 $radius-sm $radius-sm 0;
    transition:
      color var(--transition-fast),
      background var(--transition-fast),
      border-color var(--transition-fast);

    &:hover {
      color: var(--pixel-accent);
      background: var(--pixel-grass-bg-subtle);
    }
  }
}
</style>

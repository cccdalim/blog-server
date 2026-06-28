<script setup lang="ts">
import AppTag from '@/components/common/AppTag.vue'
import type { LearningPathItem } from '@/types'

interface Props {
  items: LearningPathItem[]
}

defineProps<Props>()

const STATUS_LABELS: Record<LearningPathItem['status'], string> = {
  completed: '已完成',
  'in-progress': '进行中',
  planned: '计划中',
}
</script>

<template>
  <div class="learning-path">
    <div
      v-for="(item, index) in items"
      :key="item.id"
      class="learning-path__item"
      :class="`learning-path__item--${item.status}`"
    >
      <div class="learning-path__marker">
        <span class="learning-path__dot" />
        <span v-if="index < items.length - 1" class="learning-path__line" />
      </div>

      <div class="learning-path__content">
        <div class="learning-path__header">
          <h3 class="learning-path__title">{{ item.title }}</h3>
          <span class="learning-path__status">{{ STATUS_LABELS[item.status] }}</span>
        </div>
        <p class="learning-path__desc">{{ item.description }}</p>
        <div v-if="item.skills.length" class="learning-path__skills">
          <AppTag
            v-for="skill in item.skills"
            :key="skill"
            :label="skill"
            variant="outline"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.learning-path {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.learning-path__item {
  display: flex;
  gap: $spacing-md;

  &--completed .learning-path__dot {
    background: var(--pixel-accent-strong);
    box-shadow: 0 0 0 2px var(--pixel-accent);
  }

  &--in-progress .learning-path__dot {
    background: var(--pixel-wood);
    box-shadow: 0 0 0 2px #c4a882;
  }

  &--planned .learning-path__dot {
    background: var(--pixel-stone);
    box-shadow: 0 0 0 2px var(--pixel-border);
  }

  &--completed .learning-path__status {
    color: var(--pixel-accent);
    background: var(--pixel-grass-bg-subtle);
  }

  &--in-progress .learning-path__status {
    color: #c4a882;
    background: var(--pixel-wood-bg);
  }

  &--planned .learning-path__status {
    color: var(--color-text-muted);
    background: var(--pixel-stone-bg);
  }
}

.learning-path__marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 20px;
  padding-top: 6px;
}

.learning-path__dot {
  width: 12px;
  height: 12px;
  border-radius: $radius-sm;
  flex-shrink: 0;
}

.learning-path__line {
  flex: 1;
  width: 2px;
  min-height: 40px;
  margin-top: $spacing-xs;
  background: repeating-linear-gradient(
    180deg,
    var(--pixel-border) 0,
    var(--pixel-border) 4px,
    transparent 4px,
    transparent 8px
  );
}

.learning-path__content {
  flex: 1;
  padding-bottom: $spacing-lg;
}

.learning-path__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
  margin-bottom: $spacing-xs;
}

.learning-path__title {
  font-size: $font-size-base;
  font-weight: 700;
  color: var(--color-text-primary);
}

.learning-path__status {
  font-size: $font-size-xs;
  padding: 2px $spacing-sm;
  border-radius: $radius-sm;
  white-space: nowrap;
  flex-shrink: 0;
}

.learning-path__desc {
  font-size: $font-size-sm;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: $spacing-sm;
}

.learning-path__skills {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
}
</style>

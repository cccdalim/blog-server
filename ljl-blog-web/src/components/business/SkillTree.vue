<script setup lang="ts">
import { computed } from 'vue'
import AppTag from '@/components/common/AppTag.vue'
import type { Skill } from '@/types'

interface Props {
  skills: Skill[]
}

const props = defineProps<Props>()

const CATEGORY_LABELS: Record<string, string> = {
  frontend: '前端',
  backend: '后端',
  database: '数据库',
  devops: 'DevOps',
  tools: '工具',
}

const LEVEL_PERCENT: Record<Skill['level'], number> = {
  beginner: 25,
  intermediate: 50,
  advanced: 75,
  expert: 100,
}

const LEVEL_LABELS: Record<Skill['level'], string> = {
  beginner: '入门',
  intermediate: '熟练',
  advanced: '精通',
  expert: '专家',
}

const groupedSkills = computed(() => {
  const groups = new Map<string, Skill[]>()
  for (const skill of props.skills) {
    const cat = skill.category ?? 'other'
    if (!groups.has(cat)) groups.set(cat, [])
    groups.get(cat)!.push(skill)
  }
  return Array.from(groups.entries()).map(([category, items]) => ({
    category,
    label: CATEGORY_LABELS[category] ?? category,
    items,
  }))
})
</script>

<template>
  <div class="skill-tree">
    <div
      v-for="group in groupedSkills"
      :key="group.category"
      class="skill-tree__group"
    >
      <h3 class="skill-tree__category">{{ group.label }}</h3>
      <div class="skill-tree__list">
        <div
          v-for="skill in group.items"
          :key="skill.name"
          class="skill-tree__item"
        >
          <div class="skill-tree__header">
            <span class="skill-tree__name">{{ skill.name }}</span>
            <AppTag :label="LEVEL_LABELS[skill.level]" variant="grass" />
          </div>
          <div class="skill-tree__bar">
            <div
              class="skill-tree__fill"
              :style="{ width: `${LEVEL_PERCENT[skill.level]}%` }"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skill-tree {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;

  @include mobile {
    grid-template-columns: 1fr;
  }

  &__group {
    padding: $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
  }

  &__category {
    font-size: $font-size-base;
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
    gap: $spacing-md;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-size-sm;
    font-weight: 600;
    color: var(--color-text-primary);
  }

  &__bar {
    height: 8px;
    background: var(--color-bg-elevated);
    border: 1px solid var(--color-border);
    border-radius: $radius-sm;
    overflow: hidden;
  }

  &__fill {
    height: 100%;
    background: linear-gradient(90deg, var(--pixel-accent-strong), var(--pixel-accent));
    border-radius: $radius-sm;
    transition: width 0.6s ease;
  }
}
</style>

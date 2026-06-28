<script setup lang="ts">
import AppCard from '@/components/common/AppCard.vue'
import AppTag from '@/components/common/AppTag.vue'
import type { Project } from '@/types'

interface Props {
  project: Project
}

defineProps<Props>()
</script>

<template>
  <RouterLink :to="`/projects/${project.id}`" class="project-card">
    <AppCard padding="sm">
      <div class="project-card__cover img-zoom">
        <img :src="project.cover" :alt="project.name" loading="lazy" />
      </div>
      <div class="project-card__body">
        <h3 class="project-card__name">{{ project.name }}</h3>
        <p class="project-card__summary">{{ project.summary }}</p>
        <div class="project-card__stack">
          <AppTag
            v-for="tech in project.techStack.slice(0, 4)"
            :key="tech"
            :label="tech"
            variant="wood"
          />
        </div>
      </div>
    </AppCard>
  </RouterLink>
</template>

<style scoped lang="scss">
.project-card {
  display: block;
  color: inherit;
  height: 100%;

  &:hover {
    color: inherit;
  }

  &__cover {
    border-radius: $radius-sm;
    overflow: hidden;
    aspect-ratio: 16 / 10;
    margin-bottom: $spacing-md;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__name {
    font-size: $font-size-base;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-sm;
  }

  &__summary {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    margin-bottom: $spacing-md;
    @include text-ellipsis(2);
    line-height: 1.6;
  }

  &__stack {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }
}
</style>

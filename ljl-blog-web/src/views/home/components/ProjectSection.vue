<script setup lang="ts">
import { onMounted } from 'vue'
import ProjectCard from '@/components/business/ProjectCard.vue'
import { HOME_LIMITS } from '@/constants'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useProjectStore } from '@/stores/project'
import { storeToRefs } from 'pinia'

const projectStore = useProjectStore()
const { featuredProjects, loading } = storeToRefs(projectStore)

useRevealAfterLoad(loading)

onMounted(() => {
  projectStore.loadFeatured(HOME_LIMITS.projects)
})
</script>

<template>
  <section class="section reveal">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">项目推荐</h2>
        <RouterLink to="/projects" class="section-link">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="section-loading">加载中...</div>

      <div v-else class="section-grid">
        <ProjectCard
          v-for="(project, index) in featuredProjects"
          :key="project.id"
          :project="project"
          :class="['reveal', `reveal-delay-${index + 1}`]"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.section-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.section-loading {
  text-align: center;
  color: var(--color-text-muted);
  padding: $spacing-2xl;
}
</style>

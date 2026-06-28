<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import ProjectCard from '@/components/business/ProjectCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useProjectStore } from '@/stores/project'

const projectStore = useProjectStore()
const { projectList, loading, totalPages, currentPage, keyword } = storeToRefs(projectStore)

useRevealAfterLoad(loading)

onMounted(() => {
  projectStore.loadList(1)
})

function onSearch(kw: string) {
  projectStore.setKeyword(kw)
}
</script>

<template>
  <div class="projects-page">
    <header class="projects-page__header container reveal">
      <h1 class="projects-page__title">项目中心</h1>
      <p class="projects-page__desc">个人项目作品集，记录从想法到落地的全过程</p>
    </header>

    <div class="projects-page__filters container reveal reveal-delay-1">
      <SearchBar
        :model-value="keyword"
        placeholder="搜索项目名称、技术栈..."
        @search="onSearch"
      />
    </div>

    <section class="projects-page__list container">
      <div v-if="loading" class="projects-page__loading">加载中...</div>

      <div v-else-if="!projectList.length" class="projects-page__empty">
        <p>没有找到匹配的项目</p>
        <button type="button" class="projects-page__reset" @click="projectStore.setKeyword('')">
          清除搜索
        </button>
      </div>

      <div v-else class="projects-page__grid">
        <ProjectCard
          v-for="(project, index) in projectList"
          :key="project.id"
          :project="project"
          :class="['reveal', `reveal-delay-${Math.min(index + 1, 12)}`]"
        />
      </div>

      <Pagination
        class="reveal"
        :current="currentPage"
        :total="totalPages"
        @change="projectStore.loadList"
      />
    </section>
  </div>
</template>

<style scoped lang="scss">
.projects-page {
  padding-bottom: $spacing-2xl;

  &__header {
    padding-block: $spacing-xl $spacing-lg;
  }

  &__title {
    @include section-title;
    font-size: $font-size-2xl;
    margin-bottom: $spacing-sm;
  }

  &__desc {
    color: var(--color-text-secondary);
    font-size: $font-size-base;
  }

  &__filters {
    margin-bottom: $spacing-xl;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-lg;
    margin-bottom: $spacing-lg;

    @include tablet {
      grid-template-columns: repeat(2, 1fr);
    }

    @include mobile {
      grid-template-columns: 1fr;
    }
  }

  &__loading,
  &__empty {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__reset {
    margin-top: $spacing-md;
    color: var(--pixel-accent);
    font-size: $font-size-sm;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>

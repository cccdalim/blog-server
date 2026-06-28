<script setup lang="ts">
import { computed, watch } from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import ImageViewer from '@/components/business/ImageViewer.vue'
import Timeline from '@/components/business/Timeline.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppTag from '@/components/common/AppTag.vue'
import { useProjectStore } from '@/stores/project'
import { formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const { currentProject, projectNav, detailLoading } = storeToRefs(projectStore)

const projectId = computed(() => route.params.id as string)

function formatPeriod(start: string, end?: string | null): string {
  const startStr = formatDate(start)
  if (!end) return `${startStr} — 至今`
  return `${startStr} — ${formatDate(end)}`
}

async function loadProject(id: string) {
  if (!id) return
  await projectStore.loadDetail(id)
  if (!currentProject.value) {
    router.replace({ name: 'NotFound' })
  }
}

onBeforeRouteUpdate((to) => {
  loadProject(to.params.id as string)
  window.scrollTo({ top: 0 })
})

onBeforeRouteLeave(() => {
  projectStore.clearDetail()
})

watch(projectId, (id) => loadProject(id), { immediate: true })
</script>

<template>
  <div class="project-detail">
    <div v-if="detailLoading" class="project-detail__loading container">加载中...</div>

    <template v-else-if="currentProject">
      <!-- Hero -->
      <section class="project-detail__hero">
        <div class="project-detail__hero-inner container">
          <RouterLink to="/projects" class="project-detail__back">← 返回项目列表</RouterLink>
          <div class="project-detail__hero-content">
            <div class="project-detail__info">
              <h1 class="project-detail__title">{{ currentProject.name }}</h1>
              <p class="project-detail__summary">{{ currentProject.summary }}</p>
              <p class="project-detail__period">
                {{ formatPeriod(currentProject.startDate, currentProject.endDate) }}
              </p>
              <div class="project-detail__actions">
                <a
                  v-if="currentProject.gitUrl"
                  :href="currentProject.gitUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <AppButton variant="primary">GitHub</AppButton>
                </a>
                <a
                  v-if="currentProject.demoUrl"
                  :href="currentProject.demoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <AppButton variant="secondary">在线预览</AppButton>
                </a>
              </div>
            </div>
            <div class="project-detail__cover img-zoom">
              <img :src="currentProject.cover" :alt="currentProject.name" />
            </div>
          </div>
        </div>
      </section>

      <div class="container">
        <!-- 技术栈 -->
        <section class="project-detail__section">
          <h2 class="section-title">技术栈</h2>
          <div class="project-detail__stack">
            <AppTag
              v-for="tech in currentProject.techStack"
              :key="tech"
              :label="tech"
              variant="wood"
              size="md"
            />
          </div>
        </section>

        <!-- 项目介绍 -->
        <section class="project-detail__section">
          <h2 class="section-title">项目介绍</h2>
          <p class="project-detail__desc">{{ currentProject.description }}</p>
        </section>

        <!-- 开发流程 -->
        <section v-if="currentProject.devProcess?.length" class="project-detail__section">
          <h2 class="section-title">开发流程</h2>
          <div class="project-detail__process">
            <div
              v-for="(step, index) in currentProject.devProcess"
              :key="step"
              class="project-detail__step"
            >
              <span class="project-detail__step-num">{{ index + 1 }}</span>
              <span class="project-detail__step-text">{{ step }}</span>
            </div>
          </div>
        </section>

        <!-- 截图展示 -->
        <section v-if="currentProject.screenshots?.length" class="project-detail__section">
          <h2 class="section-title">截图展示</h2>
          <ImageViewer :images="currentProject.screenshots" :alt="currentProject.name" />
        </section>

        <!-- 时间轴 -->
        <section v-if="currentProject.timeline?.length" class="project-detail__section">
          <h2 class="section-title">项目时间轴</h2>
          <Timeline :items="currentProject.timeline" />
        </section>

        <!-- 上下项目 -->
        <nav v-if="projectNav.prev || projectNav.next" class="project-detail__nav">
          <RouterLink
            v-if="projectNav.prev"
            :to="`/projects/${projectNav.prev.id}`"
            class="project-detail__nav-link"
          >
            <span class="project-detail__nav-label">上一个项目</span>
            <span class="project-detail__nav-title">{{ projectNav.prev.name }}</span>
          </RouterLink>
          <div v-else />

          <RouterLink
            v-if="projectNav.next"
            :to="`/projects/${projectNav.next.id}`"
            class="project-detail__nav-link project-detail__nav-link--next"
          >
            <span class="project-detail__nav-label">下一个项目</span>
            <span class="project-detail__nav-title">{{ projectNav.next.name }}</span>
          </RouterLink>
        </nav>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.project-detail {
  padding-bottom: $spacing-2xl;

  &__loading {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__hero {
    background: var(--color-bg-card);
    border-bottom: 2px solid var(--color-border);
    padding-block: $spacing-xl;
    margin-bottom: $spacing-xl;
  }

  &__back {
    display: inline-block;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-lg;

    &:hover {
      color: var(--pixel-accent);
    }
  }

  &__hero-content {
    display: flex;
    gap: $spacing-xl;
    align-items: center;

    @include mobile {
      flex-direction: column-reverse;
    }
  }

  &__info {
    flex: 1;
  }

  &__title {
    font-size: $font-size-2xl;
    font-weight: 800;
    color: var(--color-text-primary);
    margin-bottom: $spacing-sm;

    @include mobile {
      font-size: $font-size-xl;
    }
  }

  &__summary {
    font-size: $font-size-lg;
    color: var(--pixel-accent);
    margin-bottom: $spacing-sm;
  }

  &__period {
    font-size: $font-size-sm;
    font-family: $font-family-pixel;
    color: var(--color-text-muted);
    margin-bottom: $spacing-lg;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
  }

  &__cover {
    flex-shrink: 0;
    width: 360px;
    border-radius: $radius-md;
    overflow: hidden;
    border: 2px solid var(--pixel-border);

    @include mobile {
      width: 100%;
    }

    img {
      width: 100%;
      aspect-ratio: 16 / 10;
      object-fit: cover;
      display: block;
    }
  }

  &__section {
    margin-bottom: $spacing-2xl;
  }

  &__stack {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__desc {
    font-size: $font-size-base;
    color: var(--color-text-secondary);
    line-height: 1.8;
  }

  &__process {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
  }

  &__step {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-sm;
    transition: border-color var(--transition-fast);

    &:hover {
      border-color: var(--pixel-accent);
    }
  }

  &__step-num {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    font-size: $font-size-xs;
    font-weight: 700;
    font-family: $font-family-pixel;
    color: #fff;
    background: var(--pixel-accent-strong);
    border-radius: $radius-sm;
    flex-shrink: 0;
  }

  &__step-text {
    font-size: $font-size-sm;
    color: var(--color-text-primary);
  }

  &__nav {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
    padding-top: $spacing-lg;
    border-top: 2px solid var(--color-border);

    @include mobile {
      grid-template-columns: 1fr;
    }
  }

  &__nav-link {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    padding: $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    transition:
      border-color var(--transition-fast),
      transform var(--transition-fast);

    &:hover {
      border-color: var(--pixel-accent);
      transform: translateY(-2px);
      color: inherit;
    }

    &--next {
      text-align: right;
    }
  }

  &__nav-label {
    font-size: $font-size-xs;
    color: var(--color-text-muted);
  }

  &__nav-title {
    font-size: $font-size-sm;
    font-weight: 600;
    color: var(--color-text-primary);
    @include text-ellipsis(2);
  }
}
</style>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppTag from '@/components/common/AppTag.vue'
import ReadingProgress from '@/components/common/ReadingProgress.vue'
import DocToc from '@/components/markdown/DocToc.vue'
import MarkdownViewer from '@/components/markdown/MarkdownViewer.vue'
import ContentLayout from '@/layouts/ContentLayout.vue'
import { useMarkdownRenderer } from '@/composables/useMarkdown'
import { useReadingProgress } from '@/composables/useReadingProgress'
import { useTocHighlight } from '@/composables/useTocHighlight'
import { useRecipeStore } from '@/stores/recipe'
import { formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()
const recipeStore = useRecipeStore()
const { currentRecipe, recipeNav, detailLoading } = storeToRefs(recipeStore)

const articleRef = ref<HTMLElement | null>(null)
const mobileTocOpen = ref(false)
const loadError = ref(false)
const { extractToc } = useMarkdownRenderer()

function decodeRouteParam(value: string | string[] | undefined): string {
  const raw = Array.isArray(value) ? value[0] : value
  if (!raw) return ''
  try {
    return decodeURIComponent(raw)
  } catch {
    return raw
  }
}

const slug = computed(() => decodeRouteParam(route.params.slug))

const tocItems = computed(() => {
  if (!currentRecipe.value?.content) return []
  return extractToc(currentRecipe.value.content)
})

const { progress } = useReadingProgress(articleRef)
const { activeId, scrollToHeading } = useTocHighlight(tocItems)

async function loadRecipe(recipeSlug: string) {
  if (!recipeSlug) return
  loadError.value = false
  await recipeStore.loadDetail(recipeSlug)
  if (slug.value !== recipeSlug) return
  if (!currentRecipe.value) {
    loadError.value = true
    router.replace({ name: 'NotFound' })
  }
}

function onTocClick(id: string) {
  scrollToHeading(id)
  mobileTocOpen.value = false
}

onBeforeRouteUpdate((to) => {
  const nextSlug = decodeRouteParam(to.params.slug)
  loadRecipe(nextSlug)
  window.scrollTo({ top: 0 })
})

onBeforeRouteLeave((to) => {
  if (to.name === 'RecipeDetail') return
  recipeStore.clearDetail()
})

watch(
  slug,
  (newSlug, oldSlug) => {
    if (!newSlug || newSlug === oldSlug) return
    loadRecipe(newSlug)
    window.scrollTo({ top: 0 })
  },
  { immediate: true },
)
</script>

<template>
  <ReadingProgress :progress="progress" />

  <ContentLayout :show-mobile-sidebar="mobileTocOpen">
    <template #sidebar>
      <DocToc :items="tocItems" :active-id="activeId" @click="onTocClick" />
    </template>

    <template #toolbar>
      <button type="button" class="recipe-detail__toc-toggle" @click="mobileTocOpen = !mobileTocOpen">
        {{ mobileTocOpen ? '✕ 关闭目录' : '☰ 目录' }}
      </button>
    </template>

    <div v-if="detailLoading" class="recipe-detail__loading">加载中...</div>

    <article v-else-if="currentRecipe && !loadError" ref="articleRef" class="recipe-detail">
      <header class="recipe-detail__header">
        <RouterLink to="/recipes" class="recipe-detail__back">← 返回菜谱列表</RouterLink>
        <h1 class="recipe-detail__title">{{ currentRecipe.title }}</h1>
        <div class="recipe-detail__meta">
          <span class="recipe-detail__category">{{ currentRecipe.category }}</span>
          <span class="recipe-detail__date">{{ formatDate(currentRecipe.publishDate) }}</span>
          <span v-if="currentRecipe.readTime" class="recipe-detail__cook-time">
            约 {{ currentRecipe.readTime }} 分钟
          </span>
        </div>
        <div v-if="currentRecipe.tags.length" class="recipe-detail__tags">
          <AppTag v-for="tag in currentRecipe.tags" :key="tag" :label="tag" variant="grass" />
        </div>
      </header>

      <MarkdownViewer v-if="currentRecipe.content" :content="currentRecipe.content" />

      <nav v-if="recipeNav.prev || recipeNav.next" class="recipe-detail__nav">
        <RouterLink
          v-if="recipeNav.prev"
          :to="`/recipes/${recipeNav.prev.slug}`"
          class="recipe-detail__nav-link recipe-detail__nav-link--prev"
        >
          <span class="recipe-detail__nav-label">上一道</span>
          <span class="recipe-detail__nav-title">{{ recipeNav.prev.title }}</span>
        </RouterLink>
        <div v-else class="recipe-detail__nav-placeholder" />

        <RouterLink
          v-if="recipeNav.next"
          :to="`/recipes/${recipeNav.next.slug}`"
          class="recipe-detail__nav-link recipe-detail__nav-link--next"
        >
          <span class="recipe-detail__nav-label">下一道</span>
          <span class="recipe-detail__nav-title">{{ recipeNav.next.title }}</span>
        </RouterLink>
      </nav>
    </article>
  </ContentLayout>

  <div v-if="mobileTocOpen" class="recipe-detail__overlay" @click="mobileTocOpen = false" />
</template>

<style scoped lang="scss">
.recipe-detail {
  &__toc-toggle {
    display: none;
    @include pixel-button;
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
    border-radius: $radius-sm;
    margin-bottom: $spacing-md;

    @include mobile {
      display: inline-flex;
    }
  }

  &__loading {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__header {
    margin-bottom: $spacing-xl;
    padding-bottom: $spacing-lg;
    border-bottom: 2px solid var(--color-border);
  }

  &__back {
    display: inline-block;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-md;

    &:hover {
      color: var(--pixel-accent);
    }
  }

  &__title {
    font-size: $font-size-2xl;
    font-weight: 800;
    color: var(--color-text-primary);
    line-height: 1.3;
    margin-bottom: $spacing-md;

    @include mobile {
      font-size: $font-size-xl;
    }
  }

  &__meta {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-md;
  }

  &__category {
    color: var(--pixel-accent);
    font-weight: 600;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__nav {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
    margin-top: $spacing-2xl;
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

  &__nav-placeholder {
    @include mobile {
      display: none;
    }
  }

  &__overlay {
    position: fixed;
    inset: 0;
    top: var(--navbar-height);
    background: rgba(0, 0, 0, 0.5);
    z-index: calc(#{$z-sidebar} - 1);

    @include tablet-up {
      display: none;
    }
  }
}
</style>

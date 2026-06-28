<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import ArticleCard from '@/components/business/ArticleCard.vue'
import AppTag from '@/components/common/AppTag.vue'
import Pagination from '@/components/common/Pagination.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useDocsStore } from '@/stores/docs'

const docsStore = useDocsStore()
const {
  docList,
  categories,
  tags,
  loading,
  totalPages,
  currentPage,
  activeCategory,
  activeTag,
  keyword,
} = storeToRefs(docsStore)

useRevealAfterLoad(loading)

onMounted(async () => {
  await docsStore.loadMeta()
  await docsStore.loadList(1)
})

function onSearch(kw: string) {
  docsStore.setKeyword(kw)
}
</script>

<template>
  <div class="docs-page">
    <header class="docs-page__header container reveal">
      <h1 class="docs-page__title">学习文档</h1>
      <p class="docs-page__desc">系统化学习笔记，记录技术探索的每一步</p>
    </header>

    <div class="docs-page__filters container reveal reveal-delay-1">
      <SearchBar :model-value="keyword" placeholder="搜索文档标题、标签..." @search="onSearch" />

      <div class="docs-page__categories">
        <button
          type="button"
          class="docs-page__filter-btn"
          :class="{ 'docs-page__filter-btn--active': !activeCategory }"
          @click="docsStore.setCategory('')"
        >
          全部
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="docs-page__filter-btn"
          :class="{ 'docs-page__filter-btn--active': activeCategory === cat.slug }"
          @click="docsStore.setCategory(cat.slug)"
        >
          {{ cat.name }}
        </button>
      </div>

      <div v-if="tags.length" class="docs-page__tags">
        <button
          v-for="tag in tags.slice(0, 8)"
          :key="tag.id"
          type="button"
          class="docs-page__tag-btn"
          @click="docsStore.setTag(tag.name)"
        >
          <AppTag
            :label="tag.name"
            :variant="activeTag === tag.name ? 'grass' : 'outline'"
            size="md"
          />
        </button>
      </div>
    </div>

    <section class="docs-page__list container">
      <div v-if="loading" class="docs-page__loading">加载中...</div>

      <div v-else-if="!docList.length" class="docs-page__empty">
        <p>没有找到匹配的文档</p>
        <button type="button" class="docs-page__reset" @click="docsStore.resetFilters()">
          清除筛选
        </button>
      </div>

      <div v-else class="docs-page__grid">
        <ArticleCard
          v-for="(doc, index) in docList"
          :key="doc.id"
          :article="doc"
          link-prefix="/docs"
          :class="['reveal', `reveal-delay-${Math.min(index + 1, 12)}`]"
        />
      </div>

      <Pagination
        class="reveal"
        :current="currentPage"
        :total="totalPages"
        @change="docsStore.loadList"
      />
    </section>
  </div>
</template>

<style scoped lang="scss">
.docs-page {
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
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
    margin-bottom: $spacing-xl;
  }

  &__categories {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__filter-btn {
    padding: $spacing-xs $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-sm;
    transition:
      color var(--transition-fast),
      border-color var(--transition-fast),
      background var(--transition-fast);

    &:hover {
      color: var(--pixel-accent);
      border-color: var(--pixel-accent);
    }

    &--active {
      color: var(--pixel-accent);
      border-color: var(--pixel-accent-strong);
      background: var(--pixel-grass-bg-subtle);
    }
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__tag-btn {
    cursor: pointer;
    background: none;
    border: none;
    padding: 0;
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

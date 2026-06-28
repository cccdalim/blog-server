<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import Timeline from '@/components/business/Timeline.vue'
import WaterfallItem from '@/components/business/WaterfallItem.vue'
import Pagination from '@/components/common/Pagination.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useAlbumStore } from '@/stores/album'

const albumStore = useAlbumStore()
const {
  photoList,
  categories,
  countries,
  filteredCities,
  timeline,
  loading,
  totalPages,
  currentPage,
  previewUrls,
  activeCategory,
  activeCountry,
  activeCity,
} = storeToRefs(albumStore)

useRevealAfterLoad(loading)

onMounted(async () => {
  await albumStore.loadFilters()
  await albumStore.loadList(1)
})
</script>

<template>
  <div class="album-page">
    <header class="album-page__header container reveal">
      <h1 class="album-page__title">旅行相册</h1>
      <p class="album-page__desc">记录旅途中的风景与人文瞬间</p>
    </header>

    <div class="album-page__filters container reveal reveal-delay-1">
      <div class="album-page__filter-group">
        <span class="album-page__filter-label">分类</span>
        <div class="album-page__chips">
          <button
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': !activeCategory }"
            @click="albumStore.setCategory('')"
          >
            全部
          </button>
          <button
            v-for="cat in categories"
            :key="cat.value"
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': activeCategory === cat.value }"
            @click="albumStore.setCategory(cat.value)"
          >
            {{ cat.label }} ({{ cat.count }})
          </button>
        </div>
      </div>

      <div class="album-page__filter-group">
        <span class="album-page__filter-label">国家</span>
        <div class="album-page__chips">
          <button
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': !activeCountry }"
            @click="albumStore.setCountry('')"
          >
            全部
          </button>
          <button
            v-for="country in countries"
            :key="country.value"
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': activeCountry === country.value }"
            @click="albumStore.setCountry(country.value)"
          >
            {{ country.label }} ({{ country.count }})
          </button>
        </div>
      </div>

      <div v-if="activeCountry && filteredCities.length" class="album-page__filter-group">
        <span class="album-page__filter-label">城市</span>
        <div class="album-page__chips">
          <button
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': !activeCity }"
            @click="albumStore.setCity('')"
          >
            全部
          </button>
          <button
            v-for="city in filteredCities"
            :key="city.value"
            type="button"
            class="album-page__chip"
            :class="{ 'album-page__chip--active': activeCity === city.value }"
            @click="albumStore.setCity(city.value)"
          >
            {{ city.label }} ({{ city.count }})
          </button>
        </div>
      </div>
    </div>

    <div class="album-page__body container">
      <section class="album-page__main">
        <div v-if="loading" class="album-page__loading">加载中...</div>

        <div v-else-if="!photoList.length" class="album-page__empty">
          <p>没有找到匹配的照片</p>
          <button type="button" class="album-page__reset" @click="albumStore.resetFilters()">
            清除筛选
          </button>
        </div>

        <div v-else class="album-page__waterfall">
          <WaterfallItem
            v-for="(photo, index) in photoList"
            :key="photo.id"
            :photo="photo"
            :preview-list="previewUrls"
            :index="index"
            :class="['reveal', `reveal-delay-${Math.min((index % 6) + 1, 6)}`]"
          />
        </div>

        <Pagination
          class="reveal"
          :current="currentPage"
          :total="totalPages"
          @change="albumStore.loadList"
        />
      </section>

      <aside v-if="timeline.length" class="album-page__sidebar reveal reveal-delay-2">
        <h2 class="album-page__sidebar-title">旅行足迹</h2>
        <Timeline :items="timeline" />
      </aside>
    </div>

    <!-- 移动端时间轴 -->
    <section v-if="timeline.length" class="album-page__mobile-timeline container reveal">
      <h2 class="album-page__sidebar-title">旅行足迹</h2>
      <Timeline :items="timeline" />
    </section>
  </div>
</template>

<style scoped lang="scss">
.album-page {
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

  &__filter-group {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__filter-label {
    font-size: $font-size-xs;
    font-weight: 600;
    color: var(--color-text-muted);
    letter-spacing: 0.05em;
  }

  &__chips {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__chip {
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

  &__body {
    display: flex;
    gap: $spacing-xl;
    align-items: flex-start;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__waterfall {
    column-count: 3;
    column-gap: $spacing-md;
    margin-bottom: $spacing-lg;

    @include tablet {
      column-count: 2;
    }

    @include mobile {
      column-count: 1;
    }
  }

  &__sidebar {
    width: 280px;
    flex-shrink: 0;
    position: sticky;
    top: calc(var(--navbar-height) + #{$spacing-lg});
    max-height: calc(100vh - var(--navbar-height) - #{$spacing-2xl});
    overflow-y: auto;
    padding: $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;

    @include mobile {
      display: none;
    }
  }

  &__mobile-timeline {
    display: none;
    margin-top: $spacing-xl;
    padding: $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;

    @include mobile {
      display: block;
    }
  }

  &__sidebar-title {
    font-size: $font-size-base;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-lg;
    padding-bottom: $spacing-sm;
    border-bottom: 2px solid var(--color-border);

    &::before {
      content: '▌';
      color: var(--pixel-accent);
      margin-right: $spacing-xs;
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

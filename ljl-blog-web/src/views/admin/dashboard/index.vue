<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchDashboardStats, type DashboardStats } from '@/api/modules/dashboard'

const loading = ref(false)
const stats = ref<DashboardStats | null>(null)

function formatBytes(bytes: number) {
  if (bytes <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** index
  return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`
}

async function loadStats() {
  loading.value = true
  try {
    const res = await fetchDashboardStats()
    if (res.code === 0) {
      stats.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>控制台</h1>
        <p class="admin-page__desc">站点内容概览与存储状态</p>
      </div>
      <button type="button" class="admin-page__secondary" :disabled="loading" @click="loadStats">
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </header>

    <p v-if="loading && !stats" class="admin-page__loading">加载中...</p>

    <template v-else-if="stats">
      <section class="admin-stats-grid">
        <RouterLink to="/admin/blog" class="admin-stat-card">
          <span class="admin-stat-card__label">文章</span>
          <strong class="admin-stat-card__value">{{ stats.blogCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/docs" class="admin-stat-card">
          <span class="admin-stat-card__label">文档</span>
          <strong class="admin-stat-card__value">{{ stats.docCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/recipes" class="admin-stat-card">
          <span class="admin-stat-card__label">菜谱</span>
          <strong class="admin-stat-card__value">{{ stats.recipeCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/projects" class="admin-stat-card">
          <span class="admin-stat-card__label">项目</span>
          <strong class="admin-stat-card__value">{{ stats.projectCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/album" class="admin-stat-card">
          <span class="admin-stat-card__label">相册</span>
          <strong class="admin-stat-card__value">{{ stats.photoCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/meta" class="admin-stat-card">
          <span class="admin-stat-card__label">分类</span>
          <strong class="admin-stat-card__value">{{ stats.categoryCount }}</strong>
        </RouterLink>
        <RouterLink to="/admin/meta" class="admin-stat-card">
          <span class="admin-stat-card__label">标签</span>
          <strong class="admin-stat-card__value">{{ stats.tagCount }}</strong>
        </RouterLink>
      </section>

      <section class="admin-card">
        <h2>存储</h2>
        <div class="admin-stats-row">
          <div>
            <span class="admin-stats-row__label">上传图片</span>
            <strong>{{ stats.uploadFileCount }} 个 · {{ formatBytes(stats.uploadSizeBytes) }}</strong>
          </div>
          <div>
            <span class="admin-stats-row__label">孤儿图片</span>
            <strong>
              {{ stats.orphanImageCount }} 个 · {{ formatBytes(stats.orphanSizeBytes) }}
            </strong>
          </div>
        </div>
        <p class="admin-card__desc">
          孤儿图片未被文章、文档、项目、相册或关于页引用，可在运维工具中扫描并清理。
        </p>
        <RouterLink to="/admin/maintenance" class="admin-page__primary admin-page__link-btn">
          前往运维工具
        </RouterLink>
      </section>

      <section class="admin-card">
        <h2>快捷入口</h2>
        <div class="admin-quick-links">
          <RouterLink to="/admin/blog/new">新建文章</RouterLink>
          <RouterLink to="/admin/docs/new">新建文档</RouterLink>
          <RouterLink to="/admin/recipes/new">新建菜谱</RouterLink>
          <RouterLink to="/admin/projects/new">新建项目</RouterLink>
          <RouterLink to="/admin/album">管理相册</RouterLink>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__desc {
    margin: $spacing-xs 0 0;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
  }

  &__loading {
    color: var(--color-text-muted);
  }

  &__primary,
  &__secondary {
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    cursor: pointer;
    text-decoration: none;
    display: inline-block;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__primary {
    border: none;
    background: var(--pixel-accent-strong);
    color: #fff;
    font-weight: 600;
  }

  &__secondary {
    border: 1px solid var(--color-border);
    background: var(--color-bg-card);
    color: var(--color-text-primary);
  }

  &__link-btn {
    margin-top: $spacing-sm;
  }
}

.admin-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.admin-stat-card {
  padding: $spacing-md;
  border: 2px solid var(--color-border);
  border-radius: $radius-md;
  background: var(--color-bg-card);
  text-decoration: none;
  color: inherit;
  transition: border-color 0.15s ease;

  &:hover {
    border-color: var(--pixel-accent-strong);
  }

  &__label {
    display: block;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-xs;
  }

  &__value {
    font-size: $font-size-xl;
    color: var(--pixel-accent-strong);
  }
}

.admin-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  border: 2px solid var(--color-border);
  border-radius: $radius-md;
  background: var(--color-bg-card);

  h2 {
    margin: 0 0 $spacing-md;
    font-size: $font-size-lg;
  }

  &__desc {
    margin: 0 0 $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    line-height: 1.6;
  }
}

.admin-stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: $spacing-md;
  margin-bottom: $spacing-sm;

  &__label {
    display: block;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-xs;
  }

  strong {
    font-size: $font-size-lg;
  }
}

.admin-quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;

  a {
    padding: $spacing-sm $spacing-md;
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg);
    color: var(--color-text-secondary);
    text-decoration: none;

    &:hover {
      border-color: var(--pixel-accent-strong);
      color: var(--pixel-accent-strong);
    }
  }
}
</style>

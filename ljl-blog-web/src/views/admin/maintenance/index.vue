<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  cleanupOrphanImages,
  cleanupOrphanTags,
  scanOrphanImages,
  type OrphanImageScanResult,
} from '@/api/modules/maintenance'

const scanning = ref(false)
const cleaningImages = ref(false)
const cleaningTags = ref(false)
const scanResult = ref<OrphanImageScanResult | null>(null)

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

async function onScan() {
  scanning.value = true
  try {
    const res = await scanOrphanImages()
    if (res.code === 0) {
      scanResult.value = res.data
      if (res.data.orphanCount === 0) {
        ElMessage.success('未发现孤儿图片')
      }
    } else {
      ElMessage.error(res.message || '扫描失败')
    }
  } catch {
    ElMessage.error('扫描失败，请稍后重试')
  } finally {
    scanning.value = false
  }
}

async function onCleanupImages() {
  if (!scanResult.value) {
    await onScan()
  }
  const result = scanResult.value
  if (!result || result.orphanCount === 0) {
    if (result?.orphanCount === 0) {
      ElMessage.info('没有可清理的孤儿图片')
    }
    return
  }
  if (!window.confirm(`确定删除 ${result.orphanCount} 个孤儿图片吗？此操作不可恢复。`)) {
    return
  }

  cleaningImages.value = true
  try {
    const res = await cleanupOrphanImages()
    if (res.code === 0) {
      ElMessage.success(res.message || `已删除 ${res.data.deletedCount} 个文件`)
      await onScan()
    } else {
      ElMessage.error(res.message || '清理失败')
    }
  } catch {
    ElMessage.error('清理失败，请稍后重试')
  } finally {
    cleaningImages.value = false
  }
}

async function onCleanupTags() {
  if (!window.confirm('将删除所有未被文章引用的标签，是否继续？')) return

  cleaningTags.value = true
  try {
    const res = await cleanupOrphanTags()
    if (res.code === 0) {
      ElMessage.success(res.message || `已清理 ${res.data.count} 个标签`)
    } else {
      ElMessage.error(res.message || '清理失败')
    }
  } catch {
    ElMessage.error('清理失败，请稍后重试')
  } finally {
    cleaningTags.value = false
  }
}
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>运维工具</h1>
    </header>

    <section class="admin-card">
      <h2>孤儿图片清理</h2>
      <p class="admin-card__desc">
        扫描 <code>uploads/images</code> 目录，找出未被文章、文档、项目、相册、关于页引用的图片并清理。
        仅处理本站上传的图片，不影响外部链接（如 picsum）。
      </p>

      <div class="admin-card__actions">
        <button type="button" class="admin-page__secondary" :disabled="scanning" @click="onScan">
          {{ scanning ? '扫描中...' : '扫描孤儿图片' }}
        </button>
        <button
          type="button"
          class="admin-page__primary"
          :disabled="cleaningImages || !scanResult?.orphanCount"
          @click="onCleanupImages"
        >
          {{ cleaningImages ? '清理中...' : '删除全部孤儿图片' }}
        </button>
      </div>

      <div v-if="scanResult" class="admin-stats">
        <span>目录文件：{{ scanResult.totalFiles }}</span>
        <span>已引用：{{ scanResult.referencedCount }}</span>
        <span>孤儿：{{ scanResult.orphanCount }}</span>
        <span>可释放：{{ formatBytes(scanResult.orphanSizeBytes) }}</span>
      </div>

      <table v-if="scanResult && scanResult.orphans.length" class="admin-table">
        <thead>
          <tr>
            <th>路径</th>
            <th>大小</th>
            <th>预览</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in scanResult.orphans" :key="item.path">
            <td class="admin-table__path">{{ item.path }}</td>
            <td>{{ formatBytes(item.sizeBytes) }}</td>
            <td>
              <a :href="item.url" target="_blank" rel="noopener">打开</a>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="admin-card">
      <h2>孤儿标签清理</h2>
      <p class="admin-card__desc">
        删除数据库中没有任何文章关联的标签（编辑文章保存时也会自动清理，此处用于手动维护）。
      </p>
      <button
        type="button"
        class="admin-page__secondary"
        :disabled="cleaningTags"
        @click="onCleanupTags"
      >
        {{ cleaningTags ? '清理中...' : '清理未使用标签' }}
      </button>
    </section>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  &__header {
    margin-bottom: $spacing-lg;
  }

  &__primary,
  &__secondary {
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    cursor: pointer;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__primary {
    border: none;
    background: var(--pixel-accent-strong);
    color: #fff;
  }

  &__secondary {
    border: 1px solid var(--color-border);
    background: var(--color-bg-card);
    color: var(--color-text-primary);
  }
}

.admin-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  border: 2px solid var(--color-border);
  border-radius: $radius-md;
  background: var(--color-bg-card);

  h2 {
    margin: 0 0 $spacing-sm;
    font-size: $font-size-lg;
  }

  &__desc {
    margin: 0 0 $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    line-height: 1.6;

    code {
      padding: 1px 4px;
      border-radius: $radius-sm;
      background: var(--color-bg-elevated);
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }
}

.admin-stats {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  font-size: $font-size-sm;
  color: var(--color-text-secondary);
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  th,
  td {
    padding: $spacing-sm;
    border-bottom: 1px solid var(--color-border);
    text-align: left;
  }

  &__path {
    word-break: break-all;
    font-family: $font-family-pixel;
  }

  a {
    color: var(--pixel-accent-strong);
  }
}
</style>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deleteProject, fetchProjectList, importProjectSeed } from '@/api/modules/project'
import type { Project } from '@/types'

const router = useRouter()
const loading = ref(false)
const seeding = ref(false)
const projects = ref<Project[]>([])
const keyword = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

async function loadList() {
  loading.value = true
  try {
    const res = await fetchProjectList({
      page: page.value,
      pageSize,
      keyword: keyword.value.trim() || undefined,
    })
    if (res.code === 0) {
      projects.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  loadList()
}

function goPage(next: number) {
  if (next < 1 || next > totalPages.value) return
  page.value = next
  loadList()
}

async function onDelete(id: string) {
  if (!window.confirm('确定删除这个项目吗？')) return
  const res = await deleteProject(id)
  if (res.code === 0) {
    ElMessage.success('删除成功')
    if (projects.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadList()
  } else {
    ElMessage.error(res.message || '删除失败')
  }
}

async function onImportSeed() {
  const replace = total.value > 0
  const message = replace
    ? '将清空现有项目并导入 4 条示例数据，是否继续？'
    : '将导入 4 条示例项目数据，是否继续？'
  if (!window.confirm(message)) return

  seeding.value = true
  try {
    const res = await importProjectSeed(replace)
    if (res.code === 0) {
      ElMessage.success(res.message || `已导入 ${res.data.count} 条数据`)
      page.value = 1
      await loadList()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch {
    ElMessage.error('导入失败，请检查网络或稍后重试')
  } finally {
    seeding.value = false
  }
}

onMounted(loadList)
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>项目管理</h1>
      <div class="admin-page__actions">
        <button type="button" class="admin-page__secondary" :disabled="seeding" @click="onImportSeed">
          {{ seeding ? '导入中...' : '导入示例数据' }}
        </button>
        <button type="button" class="admin-page__primary" @click="router.push('/admin/projects/new')">
          新建项目
        </button>
      </div>
    </header>

    <form class="admin-search" @submit.prevent="onSearch">
      <input v-model="keyword" type="search" placeholder="搜索名称、摘要、技术栈..." />
      <button type="submit">搜索</button>
      <button v-if="keyword" type="button" @click="keyword = ''; onSearch()">清除</button>
    </form>

    <div v-if="loading" class="admin-page__loading">加载中...</div>

    <template v-else>
      <table class="admin-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>摘要</th>
            <th>开始日期</th>
            <th>推荐</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="projects.length === 0">
            <td colspan="5" class="admin-table__empty">暂无项目，可点击「导入示例数据」快速填充</td>
          </tr>
          <tr v-for="item in projects" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.summary }}</td>
            <td>{{ item.startDate }}</td>
            <td>{{ item.featured ? '是' : '否' }}</td>
            <td class="admin-table__actions">
              <button type="button" @click="router.push(`/admin/projects/${item.id}/edit`)">编辑</button>
              <button type="button" class="danger" @click="onDelete(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="admin-pagination">
        <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ totalPages }}（共 {{ total }} 个）</span>
        <button type="button" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
  }

  &__primary,
  &__secondary {
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    cursor: pointer;
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

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__loading {
    color: var(--color-text-muted);
  }
}

.admin-search {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
  max-width: 480px;

  input {
    flex: 1;
    padding: $spacing-sm $spacing-md;
    border: 2px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    color: var(--color-text-primary);
  }

  button {
    padding: $spacing-sm $spacing-md;
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    cursor: pointer;
  }
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-bg-card);
  border: 2px solid var(--color-border);
  border-radius: $radius-md;
  overflow: hidden;

  th,
  td {
    padding: $spacing-sm $spacing-md;
    border-bottom: 1px solid var(--color-border);
    text-align: left;
  }

  &__empty {
    text-align: center;
    color: var(--color-text-muted);
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;

    button {
      border: 1px solid var(--color-border);
      background: var(--color-bg);
      border-radius: $radius-sm;
      padding: 4px 10px;
      cursor: pointer;

      &.danger {
        color: var(--pixel-redstone);
      }
    }
  }
}

.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  margin-top: $spacing-lg;

  button {
    padding: $spacing-xs $spacing-md;
    border: 1px solid var(--color-border);
    border-radius: $radius-sm;
    background: var(--color-bg-card);
    cursor: pointer;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  span {
    font-size: $font-size-sm;
    color: var(--color-text-muted);
  }
}
</style>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  cleanupOrphanTags,
  createCategory,
  createTag,
  deleteCategory,
  deleteTag,
  fetchCategories,
  fetchTags,
  updateCategory,
  updateTag,
} from '@/api/modules/meta'
import type { CategoryItem, MetaSavePayload, TagItem, TagScope } from '@/types'

type TabKey = 'content-categories' | 'recipe-categories' | 'content-tags' | 'recipe-tags'

const activeTab = ref<TabKey>('content-categories')
const loading = ref(false)
const saving = ref(false)
const cleaning = ref(false)

const contentCategories = ref<CategoryItem[]>([])
const recipeCategories = ref<CategoryItem[]>([])
const contentTags = ref<TagItem[]>([])
const recipeTags = ref<TagItem[]>([])

const editingCategoryId = ref<string | null>(null)
const editingTagId = ref<string | null>(null)
const editingCategoryScope = ref<TagScope>('content')
const editingTagScope = ref<TagScope>('content')

const emptyForm = (): MetaSavePayload => ({ name: '', slug: '' })
const categoryForm = ref<MetaSavePayload>(emptyForm())
const tagForm = ref<MetaSavePayload>(emptyForm())

const isEditingCategory = computed(() => Boolean(editingCategoryId.value))
const isEditingTag = computed(() => Boolean(editingTagId.value))
const activeCategoryScope = computed<TagScope>(() =>
  activeTab.value === 'recipe-categories' ? 'recipe' : 'content',
)
const activeTagScope = computed<TagScope>(() =>
  activeTab.value === 'recipe-tags' ? 'recipe' : 'content',
)
const activeCategories = computed(() =>
  activeCategoryScope.value === 'recipe' ? recipeCategories.value : contentCategories.value,
)
const activeTags = computed(() =>
  activeTagScope.value === 'recipe' ? recipeTags.value : contentTags.value,
)

function slugify(text: string) {
  return text
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

function autoCategorySlug() {
  if (!isEditingCategory.value && categoryForm.value.name) {
    categoryForm.value.slug = slugify(categoryForm.value.name)
  }
}

function autoTagSlug() {
  if (!isEditingTag.value && tagForm.value.name) {
    tagForm.value.slug = slugify(tagForm.value.name)
  }
}

async function loadContentCategories() {
  const res = await fetchCategories('content')
  if (res.code === 0) {
    contentCategories.value = res.data
  }
}

async function loadRecipeCategories() {
  const res = await fetchCategories('recipe')
  if (res.code === 0) {
    recipeCategories.value = res.data
  }
}

async function loadCategoriesForScope(scope: TagScope) {
  if (scope === 'recipe') {
    await loadRecipeCategories()
  } else {
    await loadContentCategories()
  }
}

async function loadContentTags() {
  const res = await fetchTags('content')
  if (res.code === 0) {
    contentTags.value = res.data
  }
}

async function loadRecipeTags() {
  const res = await fetchTags('recipe')
  if (res.code === 0) {
    recipeTags.value = res.data
  }
}

async function loadTagsForScope(scope: TagScope) {
  if (scope === 'recipe') {
    await loadRecipeTags()
  } else {
    await loadContentTags()
  }
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadContentCategories(), loadRecipeCategories(), loadContentTags(), loadRecipeTags()])
  } finally {
    loading.value = false
  }
}

function resetCategoryForm() {
  editingCategoryId.value = null
  editingCategoryScope.value = activeCategoryScope.value
  categoryForm.value = { ...emptyForm(), scope: activeCategoryScope.value }
}

function switchCategoryTab(tab: 'content-categories' | 'recipe-categories') {
  if (activeTab.value === tab) return
  activeTab.value = tab
  if (isEditingCategory.value && editingCategoryScope.value !== activeCategoryScope.value) {
    resetCategoryForm()
  } else {
    categoryForm.value.scope = activeCategoryScope.value
  }
}

function resetTagForm() {
  editingTagId.value = null
  editingTagScope.value = activeTagScope.value
  tagForm.value = { ...emptyForm(), scope: activeTagScope.value }
}

function switchTagTab(tab: 'content-tags' | 'recipe-tags') {
  if (activeTab.value === tab) return
  activeTab.value = tab
  if (isEditingTag.value && editingTagScope.value !== activeTagScope.value) {
    resetTagForm()
  } else {
    tagForm.value.scope = activeTagScope.value
  }
}

function startEditCategory(item: CategoryItem) {
  editingCategoryId.value = item.id
  editingCategoryScope.value = item.scope ?? 'content'
  categoryForm.value = { name: item.name, slug: item.slug, scope: editingCategoryScope.value }
  activeTab.value = editingCategoryScope.value === 'recipe' ? 'recipe-categories' : 'content-categories'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function startEditTag(item: TagItem) {
  editingTagId.value = item.id
  editingTagScope.value = item.scope ?? 'content'
  tagForm.value = { name: item.name, slug: item.slug, scope: editingTagScope.value }
  activeTab.value = editingTagScope.value === 'recipe' ? 'recipe-tags' : 'content-tags'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function onSubmitCategory() {
  if (!categoryForm.value.name.trim() || !categoryForm.value.slug.trim()) {
    ElMessage.error('名称和 slug 不能为空')
    return
  }
  categoryForm.value.scope = activeCategoryScope.value
  saving.value = true
  try {
    const res = isEditingCategory.value
      ? await updateCategory(editingCategoryId.value!, categoryForm.value)
      : await createCategory(categoryForm.value)
    if (res.code === 0) {
      ElMessage.success(isEditingCategory.value ? '更新成功' : '创建成功')
      resetCategoryForm()
      await loadCategoriesForScope(activeCategoryScope.value)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function onSubmitTag() {
  if (!tagForm.value.name.trim() || !tagForm.value.slug.trim()) {
    ElMessage.error('名称和 slug 不能为空')
    return
  }
  tagForm.value.scope = activeTagScope.value
  saving.value = true
  try {
    const res = isEditingTag.value
      ? await updateTag(editingTagId.value!, tagForm.value)
      : await createTag(tagForm.value)
    if (res.code === 0) {
      ElMessage.success(isEditingTag.value ? '更新成功' : '创建成功')
      resetTagForm()
      await loadTagsForScope(activeTagScope.value)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function onDeleteCategory(item: CategoryItem) {
  if (item.count > 0) {
    ElMessage.warning(`仍有 ${item.count} 篇文章使用该分类，无法删除`)
    return
  }
  if (!window.confirm(`确定删除分类「${item.name}」吗？`)) return
  const res = await deleteCategory(item.id)
  if (res.code === 0) {
    ElMessage.success('删除成功')
    if (editingCategoryId.value === item.id) resetCategoryForm()
    await loadCategoriesForScope(item.scope ?? 'content')
  } else {
    ElMessage.error(res.message || '删除失败')
  }
}

async function onDeleteTag(item: TagItem) {
  if (item.count > 0) {
    ElMessage.warning(`仍有 ${item.count} 篇文章使用该标签，无法删除`)
    return
  }
  if (!window.confirm(`确定删除标签「${item.name}」吗？`)) return
  const res = await deleteTag(item.id)
  if (res.code === 0) {
    ElMessage.success('删除成功')
    if (editingTagId.value === item.id) resetTagForm()
    await loadTagsForScope(item.scope ?? 'content')
  } else {
    ElMessage.error(res.message || '删除失败')
  }
}

async function onCleanupOrphans() {
  cleaning.value = true
  try {
    const res = await cleanupOrphanTags()
    if (res.code === 0) {
      ElMessage.success(res.message || `已清理 ${res.data.count} 个未使用标签`)
      await Promise.all([loadContentTags(), loadRecipeTags()])
    } else {
      ElMessage.error(res.message || '清理失败')
    }
  } catch {
    ElMessage.error('清理失败，请稍后重试')
  } finally {
    cleaning.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>分类 / 标签管理</h1>
    </header>

    <div class="admin-tabs">
      <button
        type="button"
        :class="{ 'admin-tabs__active': activeTab === 'content-categories' }"
        @click="switchCategoryTab('content-categories')"
      >
        内容分类（{{ contentCategories.length }}）
      </button>
      <button
        type="button"
        :class="{ 'admin-tabs__active': activeTab === 'recipe-categories' }"
        @click="switchCategoryTab('recipe-categories')"
      >
        菜系分类（{{ recipeCategories.length }}）
      </button>
      <button
        type="button"
        :class="{ 'admin-tabs__active': activeTab === 'content-tags' }"
        @click="switchTagTab('content-tags')"
      >
        内容标签（{{ contentTags.length }}）
      </button>
      <button
        type="button"
        :class="{ 'admin-tabs__active': activeTab === 'recipe-tags' }"
        @click="switchTagTab('recipe-tags')"
      >
        菜谱标签（{{ recipeTags.length }}）
      </button>
    </div>

    <div v-if="loading" class="admin-page__loading">加载中...</div>

    <template v-else>
      <!-- 分类 -->
      <section v-show="activeTab === 'content-categories' || activeTab === 'recipe-categories'" class="admin-section">
        <form class="admin-form" @submit.prevent="onSubmitCategory">
          <h2>
            {{ isEditingCategory ? '编辑分类' : activeCategoryScope === 'recipe' ? '新建菜系分类' : '新建内容分类' }}
          </h2>
          <div class="admin-form__row">
            <label>
              名称
              <input v-model="categoryForm.name" required @blur="autoCategorySlug" />
            </label>
            <label>
              Slug
              <input v-model="categoryForm.slug" required />
            </label>
          </div>
          <div class="admin-form__actions">
            <button type="submit" class="admin-page__primary" :disabled="saving">
              {{ saving ? '保存中...' : isEditingCategory ? '更新' : '创建' }}
            </button>
            <button v-if="isEditingCategory" type="button" @click="resetCategoryForm">取消编辑</button>
          </div>
        </form>

        <table class="admin-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>Slug</th>
              <th>文章数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="activeCategories.length === 0">
              <td colspan="4" class="admin-table__empty">暂无分类</td>
            </tr>
            <tr v-for="item in activeCategories" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.slug }}</td>
              <td>{{ item.count }}</td>
              <td class="admin-table__actions">
                <button type="button" @click="startEditCategory(item)">编辑</button>
                <button type="button" class="danger" @click="onDeleteCategory(item)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 标签（Blog/Docs 或菜谱） -->
      <section v-show="activeTab === 'content-tags' || activeTab === 'recipe-tags'" class="admin-section">
        <form class="admin-form" @submit.prevent="onSubmitTag">
          <h2>
            {{ isEditingTag ? '编辑标签' : activeTagScope === 'recipe' ? '新建菜谱标签' : '新建内容标签' }}
          </h2>
          <div class="admin-form__row">
            <label>
              名称
              <input v-model="tagForm.name" required @blur="autoTagSlug" />
            </label>
            <label>
              Slug
              <input v-model="tagForm.slug" required />
            </label>
          </div>
          <div class="admin-form__actions">
            <button type="submit" class="admin-page__primary" :disabled="saving">
              {{ saving ? '保存中...' : isEditingTag ? '更新' : '创建' }}
            </button>
            <button v-if="isEditingTag" type="button" @click="resetTagForm">取消编辑</button>
            <button type="button" class="admin-page__secondary" :disabled="cleaning" @click="onCleanupOrphans">
              {{ cleaning ? '清理中...' : '清理未使用标签' }}
            </button>
          </div>
        </form>

        <table class="admin-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>Slug</th>
              <th>文章数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="activeTags.length === 0">
              <td colspan="4" class="admin-table__empty">暂无标签</td>
            </tr>
            <tr v-for="item in activeTags" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.slug }}</td>
              <td>{{ item.count }}</td>
              <td class="admin-table__actions">
                <button type="button" @click="startEditTag(item)">编辑</button>
                <button type="button" class="danger" @click="onDeleteTag(item)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  &__header {
    margin-bottom: $spacing-lg;
  }

  &__primary {
    padding: $spacing-sm $spacing-md;
    border: none;
    border-radius: $radius-md;
    background: var(--pixel-accent-strong);
    color: #fff;
    cursor: pointer;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__loading {
    color: var(--color-text-muted);
  }
}

.admin-tabs {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;

  button {
    padding: $spacing-sm $spacing-md;
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    cursor: pointer;

    &.admin-tabs__active {
      background: rgba(60, 133, 39, 0.15);
      border-color: var(--pixel-accent-strong);
      color: var(--pixel-accent-strong);
      font-weight: 600;
    }
  }
}

.admin-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-md;
  border: 2px solid var(--color-border);
  border-radius: $radius-md;
  background: var(--color-bg-card);

  h2 {
    margin: 0;
    font-size: $font-size-lg;
  }

  label {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    font-size: $font-size-sm;
  }

  input {
    padding: $spacing-sm $spacing-md;
    border: 2px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg);
    color: var(--color-text-primary);
    font: inherit;
  }

  &__row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    button {
      padding: $spacing-sm $spacing-md;
      border: 1px solid var(--color-border);
      border-radius: $radius-md;
      background: var(--color-bg);
      cursor: pointer;
    }
  }
}

.admin-page__secondary {
  padding: $spacing-sm $spacing-md;
  border: 1px dashed var(--color-border);
  border-radius: $radius-md;
  background: var(--color-bg-card);
  cursor: pointer;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
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
</style>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createRecipe,
  fetchRecipeBySlug,
  updateRecipe,
} from '@/api/modules/recipe'
import { fetchCategories } from '@/api/modules/meta'
import { uploadImage } from '@/api/modules/upload'
import MarkdownViewer from '@/components/markdown/MarkdownViewer.vue'
import TagSelect from '@/components/admin/TagSelect.vue'
import type { ArticleSavePayload, CategoryItem } from '@/types'
import { MARKDOWN_RECIPE_TEMPLATE, normalizeMarkdown } from '@/utils/normalizeMarkdown'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => Boolean(route.params.slug))
const saving = ref(false)
const uploading = ref(false)
const uploadingContent = ref(false)
const showPreview = ref(true)
const contentTextarea = ref<HTMLTextAreaElement | null>(null)
const categories = ref<CategoryItem[]>([])

const form = ref<ArticleSavePayload>({
  slug: '',
  title: '',
  summary: '',
  content: '',
  cover: '',
  categorySlug: '',
  tags: [],
  publishDate: new Date().toISOString().slice(0, 10),
  readTime: 30,
  featured: false,
})

const selectedTags = ref<string[]>([])

async function loadCategories() {
  const res = await fetchCategories('recipe')
  if (res.code === 0) {
    categories.value = res.data
    const firstCategory = categories.value[0]
    if (!form.value.categorySlug && firstCategory) {
      form.value.categorySlug = firstCategory.slug
    }
  }
}

async function loadRecipe() {
  if (!isEdit.value) return
  const slug = String(route.params.slug)
  const res = await fetchRecipeBySlug(slug)
  if (res.code === 0 && res.data) {
    const recipe = res.data
    form.value = {
      slug: recipe.slug,
      title: recipe.title,
      summary: recipe.summary,
      content: recipe.content ?? '',
      cover: recipe.cover ?? '',
      categorySlug: recipe.categorySlug,
      tags: recipe.tags,
      publishDate: recipe.publishDate,
      readTime: recipe.readTime,
      featured: recipe.featured,
    }
    selectedTags.value = [...recipe.tags]
  }
}

function slugify(text: string) {
  return text
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

function autoSlug() {
  if (!isEdit.value && form.value.title) {
    form.value.slug = slugify(form.value.title)
  }
}

function insertTemplate() {
  if (form.value.content.trim() && !window.confirm('将覆盖当前正文，是否继续？')) return
  form.value.content = MARKDOWN_RECIPE_TEMPLATE
}

function formatContent() {
  form.value.content = normalizeMarkdown(form.value.content)
}

function insertAtCursor(text: string) {
  const el = contentTextarea.value
  if (!el) {
    form.value.content += text
    return
  }
  const start = el.selectionStart
  const end = el.selectionEnd
  const before = form.value.content.slice(0, start)
  const after = form.value.content.slice(end)
  form.value.content = before + text + after
  nextTick(() => {
    el.focus()
    const pos = start + text.length
    el.setSelectionRange(pos, pos)
  })
}

async function onUploadCover(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0) {
      form.value.cover = res.data.url
    } else {
      ElMessage.error(res.message || '封面上传失败')
    }
  } catch {
    ElMessage.error('封面上传失败，请稍后重试')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function onUploadContentImage(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadingContent.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0) {
      const alt = file.name.replace(/\.[^.]+$/, '')
      insertAtCursor(`\n![${alt}](${res.data.url})\n`)
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch {
    ElMessage.error('图片上传失败，请稍后重试')
  } finally {
    uploadingContent.value = false
    input.value = ''
  }
}

async function onSubmit() {
  form.value.tags = [...selectedTags.value]
  form.value.content = normalizeMarkdown(form.value.content)

  if (!form.value.title.trim() || !form.value.slug.trim()) {
    ElMessage.error('菜名和 Slug 不能为空')
    return
  }
  if (!form.value.summary.trim()) {
    ElMessage.error('摘要不能为空')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.error('正文不能为空')
    return
  }
  if (!form.value.categorySlug) {
    ElMessage.error('请选择分类')
    return
  }

  saving.value = true
  try {
    const res = isEdit.value
      ? await updateRecipe(String(route.params.slug), form.value)
      : await createRecipe(form.value)

    if (res.code === 0) {
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      await router.push('/admin/recipes')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请检查网络或稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadRecipe()
  if (!isEdit.value && !form.value.content.trim()) {
    form.value.content = MARKDOWN_RECIPE_TEMPLATE
  }
})
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>{{ isEdit ? '编辑菜谱' : '新建菜谱' }}</h1>
      <button type="button" @click="router.push('/admin/recipes')">返回列表</button>
    </header>

    <form class="admin-form" novalidate @submit.prevent="onSubmit">
      <label>
        菜名
        <input v-model="form.title" required @blur="autoSlug" />
      </label>

      <label>
        Slug
        <input v-model="form.slug" required />
      </label>

      <label>
        摘要
        <textarea v-model="form.summary" rows="3" required />
      </label>

      <label>
        分类
        <select v-model="form.categorySlug" required>
          <option v-for="cat in categories" :key="cat.id" :value="cat.slug">
            {{ cat.name }}
          </option>
        </select>
      </label>

      <label>
        标签
        <TagSelect v-model="selectedTags" scope="recipe" />
      </label>

      <div class="admin-form__row">
        <label>
          发布日期
          <input v-model="form.publishDate" type="date" required />
        </label>
        <label>
          烹饪时长（分钟）
          <input v-model.number="form.readTime" type="number" min="1" />
        </label>
        <label class="admin-form__checkbox">
          <input v-model="form.featured" type="checkbox" />
          首页推荐
        </label>
      </div>

      <label>
        成品图（可选）
        <div class="admin-form__cover">
          <input v-model="form.cover" placeholder="/uploads/images/..." />
          <label class="admin-form__upload">
            {{ uploading ? '上传中...' : '上传图片' }}
            <input type="file" accept="image/*" hidden @change="onUploadCover" />
          </label>
        </div>
        <img v-if="form.cover" :src="form.cover" alt="cover" class="admin-form__preview" />
      </label>

      <div class="admin-form__editor">
        <div class="admin-form__editor-toolbar">
          <span>Markdown 正文</span>
          <div class="admin-form__editor-actions">
            <button type="button" @click="insertTemplate">插入模板</button>
            <button type="button" @click="formatContent">自动排版</button>
            <label class="admin-form__editor-upload">
              {{ uploadingContent ? '上传中...' : '插入图片' }}
              <input type="file" accept="image/*" hidden @change="onUploadContentImage" />
            </label>
            <button type="button" @click="showPreview = !showPreview">
              {{ showPreview ? '隐藏预览' : '显示预览' }}
            </button>
          </div>
        </div>
        <p class="admin-form__editor-hint">
          建议使用「菜品简介 → 食材清单 → 制作步骤 → 小贴士」结构，步骤用 <code>###</code> 编号。
        </p>
        <div class="admin-form__editor-panels" :class="{ 'admin-form__editor-panels--preview': showPreview }">
          <textarea
            ref="contentTextarea"
            v-model="form.content"
            class="admin-form__editor-input"
            rows="20"
            required
            placeholder="在此输入食材与制作流程..."
          />
          <div v-if="showPreview" class="admin-form__editor-preview">
            <MarkdownViewer :content="form.content" />
          </div>
        </div>
      </div>

      <button type="submit" class="admin-page__primary" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </form>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    button {
      border: 1px solid var(--color-border);
      background: var(--color-bg-card);
      border-radius: $radius-md;
      padding: $spacing-sm $spacing-md;
      cursor: pointer;
    }
  }

  &__primary {
    padding: $spacing-sm $spacing-lg;
    border: none;
    border-radius: $radius-md;
    background: var(--pixel-accent-strong);
    color: #fff;
    cursor: pointer;
  }
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  max-width: 1200px;

  label {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    font-size: $font-size-sm;
  }

  input,
  textarea,
  select {
    padding: $spacing-sm $spacing-md;
    border: 2px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    color: var(--color-text-primary);
    font: inherit;
  }

  &__row {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: $spacing-md;
  }

  &__checkbox {
    flex-direction: row !important;
    align-items: center;
    gap: $spacing-sm !important;
    margin-top: $spacing-lg;
  }

  &__cover {
    display: flex;
    gap: $spacing-sm;
  }

  &__upload {
    flex-shrink: 0;
    padding: $spacing-sm $spacing-md;
    border: 1px dashed var(--color-border);
    border-radius: $radius-md;
    cursor: pointer;
    white-space: nowrap;
  }

  &__preview {
    margin-top: $spacing-sm;
    max-width: 240px;
    border-radius: $radius-md;
    border: 2px solid var(--color-border);
  }

  &__editor-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-md;
    font-size: $font-size-sm;
    font-weight: 600;
  }

  &__editor-actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    button {
      padding: $spacing-xs $spacing-sm;
      border: 1px solid var(--color-border);
      border-radius: $radius-sm;
      background: var(--color-bg-card);
      cursor: pointer;
      font-size: $font-size-xs;
    }
  }

  &__editor-upload {
    padding: $spacing-xs $spacing-sm;
    border: 1px dashed var(--color-border);
    border-radius: $radius-sm;
    background: var(--color-bg-card);
    cursor: pointer;
    font-size: $font-size-xs;
    font-weight: 400;
  }

  &__editor-hint {
    margin: 0;
    font-size: $font-size-xs;
    color: var(--color-text-muted);

    code {
      padding: 1px 4px;
      border-radius: $radius-sm;
      background: var(--color-bg-elevated);
    }
  }

  &__editor-panels {
    display: grid;
    grid-template-columns: 1fr;
    gap: $spacing-md;

    &--preview {
      grid-template-columns: 1fr 1fr;
    }
  }

  &__editor-input {
    min-height: 420px;
    resize: vertical;
    font-family: $font-family-pixel;
    font-size: $font-size-sm;
    line-height: 1.7;
  }

  &__editor-preview {
    min-height: 420px;
    padding: $spacing-md;
    border: 2px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    overflow: auto;
  }
}
</style>

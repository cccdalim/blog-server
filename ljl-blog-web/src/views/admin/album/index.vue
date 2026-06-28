<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createPhoto, deletePhoto, fetchPhotoList, importAlbumSeed, updatePhoto } from '@/api/modules/album'
import { uploadImage } from '@/api/modules/upload'
import type { Photo, PhotoSavePayload } from '@/types'

const loading = ref(false)
const seeding = ref(false)
const saving = ref(false)
const uploading = ref(false)
const photos = ref<Photo[]>([])
const editingId = ref<string | null>(null)
const page = ref(1)
const pageSize = 12
const total = ref(0)

const emptyForm = (): PhotoSavePayload => ({
  url: '',
  title: '',
  city: '',
  country: '',
  category: '城市',
  date: new Date().toISOString().slice(0, 10),
})

const form = ref<PhotoSavePayload>(emptyForm())

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
const isEditing = computed(() => Boolean(editingId.value))

async function loadList() {
  loading.value = true
  try {
    const res = await fetchPhotoList({ page: page.value, pageSize })
    if (res.code === 0) {
      photos.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.value = emptyForm()
}

function startEdit(photo: Photo) {
  editingId.value = photo.id
  form.value = {
    url: photo.url,
    title: photo.title,
    city: photo.city,
    country: photo.country,
    category: photo.category,
    date: photo.date,
    width: photo.width,
    height: photo.height,
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function onUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0) {
      form.value.url = res.data.url
      if (!form.value.title) {
        form.value.title = file.name.replace(/\.[^.]+$/, '')
      }
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch {
    ElMessage.error('图片上传失败，请稍后重试')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function onSubmit() {
  if (!form.value.url) {
    ElMessage.error('请先上传图片')
    return
  }
  saving.value = true
  try {
    const res = isEditing.value
      ? await updatePhoto(editingId.value!, form.value)
      : await createPhoto(form.value)
    if (res.code === 0) {
      ElMessage.success(isEditing.value ? '保存成功' : '创建成功')
      resetForm()
      await loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function onDelete(id: string) {
  if (!window.confirm('确定删除这张照片吗？')) return
  try {
    const res = await deletePhoto(id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      if (editingId.value === id) resetForm()
      if (photos.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      await loadList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败，请稍后重试')
  }
}

async function onImportSeed() {
  const replace = total.value > 0
  const message = replace
    ? '将清空现有相册并导入示例旅行照片，是否继续？'
    : '将导入示例旅行照片，是否继续？'
  if (!window.confirm(message)) return

  seeding.value = true
  try {
    const res = await importAlbumSeed(replace)
    if (res.code === 0) {
      ElMessage.success(res.message || `已导入 ${res.data.count} 条数据`)
      page.value = 1
      resetForm()
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

function goPage(next: number) {
  if (next < 1 || next > totalPages.value) return
  page.value = next
  loadList()
}

onMounted(loadList)
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>相册管理</h1>
      <div class="admin-page__actions">
        <span v-if="isEditing" class="admin-page__badge">正在编辑</span>
        <button type="button" class="admin-page__secondary" :disabled="seeding" @click="onImportSeed">
          {{ seeding ? '导入中...' : '导入示例数据' }}
        </button>
      </div>
    </header>

    <form class="admin-form" @submit.prevent="onSubmit">
      <label class="admin-form__upload-box">
        {{ uploading ? '上传中...' : '点击上传图片' }}
        <input type="file" accept="image/*" hidden @change="onUpload" />
      </label>

      <img v-if="form.url" :src="form.url" alt="preview" class="admin-form__preview" />

      <label>
        标题
        <input v-model="form.title" required />
      </label>

      <div class="admin-form__row">
        <label>
          城市
          <input v-model="form.city" />
        </label>
        <label>
          国家
          <input v-model="form.country" />
        </label>
        <label>
          分类
          <input v-model="form.category" />
        </label>
        <label>
          日期
          <input v-model="form.date" type="date" required />
        </label>
      </div>

      <div class="admin-form__actions">
        <button type="submit" class="admin-page__primary" :disabled="saving || !form.url">
          {{ saving ? '保存中...' : isEditing ? '保存修改' : '添加照片' }}
        </button>
        <button v-if="isEditing" type="button" class="admin-page__secondary" @click="resetForm">
          取消编辑
        </button>
      </div>
    </form>

    <div v-if="loading" class="admin-page__loading">加载中...</div>

    <template v-else>
      <div class="photo-grid">
        <div v-for="photo in photos" :key="photo.id" class="photo-grid__item">
          <img :src="photo.url" :alt="photo.title" />
          <div class="photo-grid__meta">
            <strong>{{ photo.title }}</strong>
            <span>{{ photo.city }} · {{ photo.country }}</span>
          </div>
          <div class="photo-grid__actions">
            <button type="button" @click="startEdit(photo)">编辑</button>
            <button type="button" class="danger" @click="onDelete(photo.id)">删除</button>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="admin-pagination">
        <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ totalPages }}（共 {{ total }} 张）</span>
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
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__secondary {
    padding: $spacing-sm $spacing-md;
    border: 1px dashed var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    color: var(--color-text-primary);
    cursor: pointer;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__badge {
    padding: 2px 8px;
    border-radius: $radius-sm;
    background: var(--pixel-accent);
    color: #fff;
    font-size: $font-size-xs;
  }

  &__primary {
    padding: $spacing-sm $spacing-lg;
    border: none;
    border-radius: $radius-md;
    background: var(--pixel-accent-strong);
    color: #fff;
    cursor: pointer;
  }

  &__secondary {
    padding: $spacing-sm $spacing-lg;
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    background: var(--color-bg-card);
    cursor: pointer;
  }

  &__loading {
    margin-top: $spacing-xl;
    color: var(--color-text-muted);
  }
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  max-width: 720px;
  margin-bottom: $spacing-2xl;

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
    background: var(--color-bg-card);
    color: var(--color-text-primary);
  }

  &__upload-box {
    align-items: center;
    justify-content: center;
    min-height: 120px;
    border: 2px dashed var(--color-border);
    border-radius: $radius-md;
    cursor: pointer;
    color: var(--color-text-muted);
  }

  &__preview {
    max-width: 320px;
    border-radius: $radius-md;
    border: 2px solid var(--color-border);
  }

  &__row {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: $spacing-md;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
  }
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $spacing-md;

  &__item {
    background: var(--color-bg-card);
    border: 2px solid var(--color-border);
    border-radius: $radius-md;
    overflow: hidden;

    img {
      width: 100%;
      aspect-ratio: 4 / 3;
      object-fit: cover;
    }
  }

  &__meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
  }

  &__actions {
    display: flex;
    border-top: 1px solid var(--color-border);

    button {
      flex: 1;
      border: none;
      background: var(--color-bg);
      padding: $spacing-sm;
      cursor: pointer;

      &:not(:last-child) {
        border-right: 1px solid var(--color-border);
      }

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
  margin-top: $spacing-xl;

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

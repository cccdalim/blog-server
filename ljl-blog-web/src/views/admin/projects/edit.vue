<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createProject, fetchProjectById, updateProject } from '@/api/modules/project'
import { uploadImage } from '@/api/modules/upload'
import type { ProjectSavePayload, ProjectTimelineItem } from '@/types'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => route.params.id !== undefined && route.path.endsWith('/edit'))
const saving = ref(false)
const uploading = ref(false)
const uploadingScreenshot = ref(false)

const techStackInput = ref('')
const devProcessInput = ref('')
const screenshotsInput = ref('')

const form = ref<ProjectSavePayload>({
  name: '',
  description: '',
  summary: '',
  techStack: [],
  cover: '',
  gitUrl: '',
  demoUrl: '',
  featured: false,
  startDate: new Date().toISOString().slice(0, 10),
  endDate: null,
  devProcess: [],
  screenshots: [],
  timeline: [],
})

function splitLines(text: string): string[] {
  return text
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function syncArrayInputs() {
  form.value.techStack = splitLines(techStackInput.value)
  form.value.devProcess = splitLines(devProcessInput.value)
  form.value.screenshots = splitLines(screenshotsInput.value)
}

function setArrayInputsFromForm() {
  techStackInput.value = form.value.techStack.join(', ')
  devProcessInput.value = form.value.devProcess.join(', ')
  screenshotsInput.value = form.value.screenshots.join('\n')
}

function emptyTimelineItem(): ProjectTimelineItem {
  return { date: form.value.startDate, title: '', description: '' }
}

function addTimelineItem() {
  form.value.timeline.push(emptyTimelineItem())
}

function removeTimelineItem(index: number) {
  form.value.timeline.splice(index, 1)
}

async function loadProject() {
  if (!isEdit.value) return
  const id = String(route.params.id)
  const res = await fetchProjectById(id)
  if (res.code === 0 && res.data) {
    const project = res.data
    form.value = {
      name: project.name,
      description: project.description,
      summary: project.summary,
      techStack: project.techStack ?? [],
      cover: project.cover ?? '',
      gitUrl: project.gitUrl ?? '',
      demoUrl: project.demoUrl ?? '',
      featured: project.featured ?? false,
      startDate: project.startDate,
      endDate: project.endDate ?? null,
      devProcess: project.devProcess ?? [],
      screenshots: project.screenshots ?? [],
      timeline: project.timeline ?? [],
    }
    setArrayInputsFromForm()
  }
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

async function onUploadScreenshot(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadingScreenshot.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0) {
      const lines = screenshotsInput.value.trim()
      screenshotsInput.value = lines ? `${lines}\n${res.data.url}` : res.data.url
    } else {
      ElMessage.error(res.message || '截图上传失败')
    }
  } catch {
    ElMessage.error('截图上传失败，请稍后重试')
  } finally {
    uploadingScreenshot.value = false
    input.value = ''
  }
}

async function onSubmit() {
  syncArrayInputs()

  if (!form.value.name.trim() || !form.value.summary.trim()) {
    ElMessage.error('名称和摘要不能为空')
    return
  }
  if (!form.value.startDate) {
    ElMessage.error('请选择开始日期')
    return
  }

  saving.value = true
  try {
    const res = isEdit.value
      ? await updateProject(String(route.params.id), form.value)
      : await createProject(form.value)

    if (res.code === 0) {
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      await router.push('/admin/projects')
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
  await loadProject()
  if (!isEdit.value && form.value.timeline.length === 0) {
    form.value.timeline.push(emptyTimelineItem())
  }
})
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>{{ isEdit ? '编辑项目' : '新建项目' }}</h1>
      <button type="button" @click="router.push('/admin/projects')">返回列表</button>
    </header>

    <form class="admin-form" novalidate @submit.prevent="onSubmit">
      <label>
        项目名称
        <input v-model="form.name" required />
      </label>

      <label>
        摘要
        <input v-model="form.summary" required />
      </label>

      <label>
        详细描述
        <textarea v-model="form.description" rows="4" />
      </label>

      <div class="admin-form__row">
        <label>
          开始日期
          <input v-model="form.startDate" type="date" required />
        </label>
        <label>
          结束日期（可选）
          <input v-model="form.endDate" type="date" />
        </label>
        <label class="admin-form__checkbox">
          <input v-model="form.featured" type="checkbox" />
          首页推荐
        </label>
      </div>

      <label>
        技术栈（逗号分隔）
        <input v-model="techStackInput" placeholder="Vue 3, TypeScript, Spring Boot" />
      </label>

      <label>
        封面
        <div class="admin-form__cover">
          <input v-model="form.cover" placeholder="图片 URL 或上传" />
          <label class="admin-form__upload">
            {{ uploading ? '上传中...' : '上传封面' }}
            <input type="file" accept="image/*" hidden @change="onUploadCover" />
          </label>
        </div>
        <img v-if="form.cover" :src="form.cover" alt="cover" class="admin-form__preview" />
      </label>

      <div class="admin-form__row">
        <label>
          Git 地址
          <input v-model="form.gitUrl" placeholder="https://github.com/..." />
        </label>
        <label>
          演示地址
          <input v-model="form.demoUrl" placeholder="https://..." />
        </label>
      </div>

      <label>
        开发流程（逗号分隔）
        <input v-model="devProcessInput" placeholder="需求分析, 架构设计, 开发, 上线" />
      </label>

      <label>
        截图 URL（每行一个）
        <textarea v-model="screenshotsInput" rows="4" placeholder="https://..." />
        <label class="admin-form__upload admin-form__upload--inline">
          {{ uploadingScreenshot ? '上传中...' : '上传截图' }}
          <input type="file" accept="image/*" hidden @change="onUploadScreenshot" />
        </label>
      </label>

      <div class="admin-form__section">
        <div class="admin-form__section-header">
          <span>项目时间轴</span>
          <button type="button" @click="addTimelineItem">添加节点</button>
        </div>
        <div v-for="(item, index) in form.timeline" :key="index" class="admin-form__timeline">
          <input v-model="item.date" type="date" />
          <input v-model="item.title" placeholder="标题" />
          <input v-model="item.description" placeholder="描述" />
          <button type="button" class="danger" @click="removeTimelineItem(index)">删除</button>
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

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  max-width: 900px;

  label {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    font-size: $font-size-sm;
  }

  input,
  textarea {
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

    &--inline {
      align-self: flex-start;
      margin-top: $spacing-xs;
    }
  }

  &__preview {
    margin-top: $spacing-sm;
    max-width: 240px;
    border-radius: $radius-md;
    border: 2px solid var(--color-border);
  }

  &__section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
    font-weight: 600;

    button {
      padding: $spacing-xs $spacing-sm;
      border: 1px solid var(--color-border);
      border-radius: $radius-sm;
      background: var(--color-bg-card);
      cursor: pointer;
      font-size: $font-size-xs;
    }
  }

  &__timeline {
    display: grid;
    grid-template-columns: 140px 1fr 2fr auto;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;

    .danger {
      border: 1px solid var(--color-border);
      background: var(--color-bg-card);
      border-radius: $radius-sm;
      color: var(--pixel-redstone);
      cursor: pointer;
      padding: $spacing-xs $spacing-sm;
    }
  }
}
</style>

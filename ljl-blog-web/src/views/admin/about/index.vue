<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAboutPage,
  fetchSkills,
  importAboutSeed,
  updateAboutPage,
  updateAboutSkills,
} from '@/api/modules/about'
import { uploadImage } from '@/api/modules/upload'
import type { AboutPageData, LearningPathItem, Skill } from '@/types'

const loading = ref(false)
const saving = ref(false)
const seeding = ref(false)
const uploading = ref(false)

const skills = ref<Skill[]>([])

const form = ref<AboutPageData>({
  profile: {
    name: '',
    avatar: '',
    title: '',
    bio: '',
    tagline: '',
  },
  social: {
    github: '',
    email: '',
    twitter: '',
  },
  timeline: [],
  learningPath: [],
})

const skillLevels = [
  { value: 'beginner', label: '入门' },
  { value: 'intermediate', label: '中级' },
  { value: 'advanced', label: '高级' },
  { value: 'expert', label: '专家' },
]

const skillCategories = [
  { value: 'frontend', label: '前端' },
  { value: 'backend', label: '后端' },
  { value: 'database', label: '数据库' },
  { value: 'devops', label: 'DevOps' },
  { value: 'tools', label: '工具' },
]

const pathStatuses = [
  { value: 'completed', label: '已完成' },
  { value: 'in-progress', label: '进行中' },
  { value: 'planned', label: '计划中' },
]

const learningPathSkillsInput = ref<string[]>([])

async function loadData() {
  loading.value = true
  try {
    const [pageRes, skillsRes] = await Promise.all([fetchAboutPage(), fetchSkills()])
    if (pageRes.code === 0 && pageRes.data) {
      form.value = {
        profile: { ...pageRes.data.profile },
        social: { ...pageRes.data.social },
        timeline: pageRes.data.timeline ?? [],
        learningPath: pageRes.data.learningPath ?? [],
      }
      learningPathSkillsInput.value = form.value.learningPath.map((item) => item.skills.join(', '))
    }
    if (skillsRes.code === 0) {
      skills.value = skillsRes.data
    }
  } finally {
    loading.value = false
  }
}

function addTimelineItem() {
  form.value.timeline.push({ date: new Date().toISOString().slice(0, 10), title: '', description: '' })
}

function removeTimelineItem(index: number) {
  form.value.timeline.splice(index, 1)
}

function addLearningPathItem() {
  const id = `lp${form.value.learningPath.length + 1}`
  form.value.learningPath.push({
    id,
    title: '',
    description: '',
    status: 'planned',
    skills: [],
  })
  learningPathSkillsInput.value.push('')
}

function removeLearningPathItem(index: number) {
  form.value.learningPath.splice(index, 1)
  learningPathSkillsInput.value.splice(index, 1)
}

function addSkill() {
  skills.value.push({ name: '', level: 'intermediate', category: 'frontend' })
}

function removeSkill(index: number) {
  skills.value.splice(index, 1)
}

function syncLearningPathSkills() {
  form.value.learningPath.forEach((item, index) => {
    item.skills = learningPathSkillsInput.value[index]
      ?.split(',')
      .map((s) => s.trim())
      .filter(Boolean) ?? []
  })
}

async function onUploadAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0) {
      form.value.profile.avatar = res.data.url
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch {
    ElMessage.error('头像上传失败，请稍后重试')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function onSubmit() {
  if (!form.value.profile.name.trim()) {
    ElMessage.error('姓名不能为空')
    return
  }
  syncLearningPathSkills()

  saving.value = true
  try {
    const pageRes = await updateAboutPage(form.value)
    const skillsRes = await updateAboutSkills(skills.value)
    if (pageRes.code === 0 && skillsRes.code === 0) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(pageRes.message || skillsRes.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请检查网络或稍后重试')
  } finally {
    saving.value = false
  }
}

async function onImportSeed() {
  if (!window.confirm('将用示例数据覆盖当前关于页与技能配置，是否继续？')) return

  seeding.value = true
  try {
    const res = await importAboutSeed(true)
    if (res.code === 0) {
      ElMessage.success(res.message || '导入成功')
      await loadData()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch {
    ElMessage.error('导入失败，请稍后重试')
  } finally {
    seeding.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <h1>关于页管理</h1>
      <div class="admin-page__actions">
        <button type="button" class="admin-page__secondary" :disabled="seeding" @click="onImportSeed">
          {{ seeding ? '导入中...' : '导入示例数据' }}
        </button>
      </div>
    </header>

    <div v-if="loading" class="admin-page__loading">加载中...</div>

    <form v-else class="admin-form" novalidate @submit.prevent="onSubmit">
      <section class="admin-form__section">
        <h2>个人资料</h2>
        <label>
          姓名
          <input v-model="form.profile.name" required />
        </label>
        <label>
          职位标题
          <input v-model="form.profile.title" />
        </label>
        <label>
          标语
          <input v-model="form.profile.tagline" />
        </label>
        <label>
          简介
          <textarea v-model="form.profile.bio" rows="4" />
        </label>
        <label>
          头像
          <div class="admin-form__cover">
            <input v-model="form.profile.avatar" placeholder="头像 URL" />
            <label class="admin-form__upload">
              {{ uploading ? '上传中...' : '上传头像' }}
              <input type="file" accept="image/*" hidden @change="onUploadAvatar" />
            </label>
          </div>
          <img v-if="form.profile.avatar" :src="form.profile.avatar" alt="avatar" class="admin-form__preview" />
        </label>
      </section>

      <section class="admin-form__section">
        <h2>社交链接</h2>
        <div class="admin-form__row">
          <label>
            GitHub
            <input v-model="form.social.github" placeholder="https://github.com/..." />
          </label>
          <label>
            Email
            <input v-model="form.social.email" type="email" />
          </label>
          <label>
            Twitter
            <input v-model="form.social.twitter" placeholder="https://twitter.com/..." />
          </label>
        </div>
      </section>

      <section class="admin-form__section">
        <div class="admin-form__section-header">
          <h2>技能树</h2>
          <button type="button" @click="addSkill">添加技能</button>
        </div>
        <div v-for="(skill, index) in skills" :key="index" class="admin-form__skill">
          <input v-model="skill.name" placeholder="技能名称" />
          <select v-model="skill.level">
            <option v-for="opt in skillLevels" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <select v-model="skill.category">
            <option v-for="opt in skillCategories" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <button type="button" class="danger" @click="removeSkill(index)">删除</button>
        </div>
      </section>

      <section class="admin-form__section">
        <div class="admin-form__section-header">
          <h2>学习路线</h2>
          <button type="button" @click="addLearningPathItem">添加节点</button>
        </div>
        <div v-for="(item, index) in form.learningPath" :key="item.id" class="admin-form__learning">
          <input v-model="item.id" placeholder="ID" />
          <input v-model="item.title" placeholder="标题" />
          <select v-model="item.status">
            <option v-for="opt in pathStatuses" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <textarea v-model="item.description" rows="2" placeholder="描述" />
          <input
            v-model="learningPathSkillsInput[index]"
            placeholder="关联技能（逗号分隔）"
          />
          <button type="button" class="danger" @click="removeLearningPathItem(index)">删除</button>
        </div>
      </section>

      <section class="admin-form__section">
        <div class="admin-form__section-header">
          <h2>成长时间轴</h2>
          <button type="button" @click="addTimelineItem">添加节点</button>
        </div>
        <div v-for="(item, index) in form.timeline" :key="index" class="admin-form__timeline">
          <input v-model="item.date" type="date" />
          <input v-model="item.title" placeholder="标题" />
          <input v-model="item.description" placeholder="描述" />
          <button type="button" class="danger" @click="removeTimelineItem(index)">删除</button>
        </div>
      </section>

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

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
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

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
  max-width: 960px;

  h2 {
    margin: 0 0 $spacing-md;
    font-size: $font-size-lg;
  }

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

  &__section {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
    padding-bottom: $spacing-lg;
    border-bottom: 1px solid var(--color-border);
  }

  &__section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    button {
      padding: $spacing-xs $spacing-sm;
      border: 1px solid var(--color-border);
      border-radius: $radius-sm;
      background: var(--color-bg-card);
      cursor: pointer;
      font-size: $font-size-xs;
    }
  }

  &__row {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: $spacing-md;
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
    max-width: 120px;
    max-height: 120px;
    border-radius: $radius-md;
    border: 2px solid var(--color-border);
  }

  &__skill {
    display: grid;
    grid-template-columns: 1fr 120px 120px auto;
    gap: $spacing-sm;
    align-items: center;
  }

  &__learning {
    display: grid;
    grid-template-columns: 80px 1fr 120px;
    gap: $spacing-sm;
    padding: $spacing-sm;
    border: 1px solid var(--color-border);
    border-radius: $radius-md;

    textarea {
      grid-column: 1 / -2;
    }

    input:last-of-type {
      grid-column: 1 / -2;
    }
  }

  &__timeline {
    display: grid;
    grid-template-columns: 140px 1fr 2fr auto;
    gap: $spacing-sm;
    align-items: center;
  }

  .danger {
    border: 1px solid var(--color-border);
    background: var(--color-bg-card);
    border-radius: $radius-sm;
    color: var(--pixel-redstone);
    cursor: pointer;
    padding: $spacing-xs $spacing-sm;
    white-space: nowrap;
  }
}
</style>

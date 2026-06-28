<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const saving = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const avatarInitial = computed(() => {
  const name = authStore.username.trim()
  return name ? name.charAt(0).toUpperCase() : '?'
})

async function onSubmit() {
  if (!form.value.oldPassword || !form.value.newPassword) {
    ElMessage.error('请填写完整')
    return
  }
  if (form.value.newPassword.length < 6) {
    ElMessage.error('新密码至少 6 位')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  saving.value = true
  try {
    const res = await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
    })
    if (res.code === 0) {
      ElMessage.success('密码已更新')
      form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>账号设置</h1>
        <p class="admin-page__desc">管理登录凭据与安全选项</p>
      </div>
    </header>

    <section class="admin-card admin-card--profile">
      <div class="admin-card__avatar">{{ avatarInitial }}</div>
      <div class="admin-card__profile">
        <h2>{{ authStore.username || '管理员' }}</h2>
        <p>当前登录账号，修改密码后下次登录需使用新密码</p>
      </div>
    </section>

    <section class="admin-card">
      <h2>修改密码</h2>
      <p class="admin-card__desc">新密码至少 6 位，建议使用字母与数字组合</p>

      <form class="admin-form" novalidate @submit.prevent="onSubmit">
        <label>
          当前密码
          <input
            v-model="form.oldPassword"
            type="password"
            autocomplete="current-password"
            placeholder="输入当前密码"
            required
          />
        </label>

        <div class="admin-form__row">
          <label>
            新密码
            <input
              v-model="form.newPassword"
              type="password"
              autocomplete="new-password"
              placeholder="至少 6 位"
              required
            />
          </label>
          <label>
            确认新密码
            <input
              v-model="form.confirmPassword"
              type="password"
              autocomplete="new-password"
              placeholder="再次输入新密码"
              required
            />
          </label>
        </div>

        <div class="admin-form__actions">
          <button type="submit" class="admin-page__primary" :disabled="saving">
            {{ saving ? '保存中...' : '更新密码' }}
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.admin-page {
  max-width: 720px;

  &__header {
    margin-bottom: $spacing-lg;
  }

  &__desc {
    margin: $spacing-xs 0 0;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
  }

  &__primary {
    padding: $spacing-sm $spacing-md;
    border: none;
    border-radius: $radius-md;
    background: var(--pixel-accent-strong);
    color: #fff;
    cursor: pointer;
    font-weight: 600;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
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
  }

  &--profile {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  &__avatar {
    flex-shrink: 0;
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    background: rgba(60, 133, 39, 0.15);
    color: var(--pixel-accent-strong);
    font-size: $font-size-xl;
    font-weight: 700;
  }

  &__profile {
    h2 {
      margin: 0 0 $spacing-xs;
    }

    p {
      margin: 0;
      font-size: $font-size-sm;
      color: var(--color-text-muted);
      line-height: 1.5;
    }
  }
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;

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

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    padding-top: $spacing-xs;
  }
}
</style>

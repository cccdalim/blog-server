<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MinimalLayout from '@/layouts/MinimalLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('admin')
const password = ref('')
const rememberMe = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  try {
    const ok = await authStore.login(username.value, password.value, rememberMe.value)
    if (ok) {
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard'
      await router.replace(redirect)
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  }
}
</script>

<template>
  <MinimalLayout>
    <div class="admin-login">
      <h1 class="admin-login__title">管理员登录</h1>
      <form class="admin-login__form" @submit.prevent="onSubmit">
        <label>
          用户名
          <input v-model="username" type="text" autocomplete="username" required />
        </label>
        <label>
          密码
          <input v-model="password" type="password" autocomplete="current-password" required />
        </label>
        <label class="admin-login__remember">
          <input v-model="rememberMe" type="checkbox" />
          <span>保持登录（关闭浏览器后仍有效，8 小时内）</span>
        </label>
        <p v-if="error" class="admin-login__error">{{ error }}</p>
        <button type="submit" class="admin-login__submit" :disabled="authStore.loading">
          {{ authStore.loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </MinimalLayout>
</template>

<style scoped lang="scss">
.admin-login {
  max-width: 420px;
  margin: 0 auto;
  padding: $spacing-2xl $spacing-lg;

  &__title {
    text-align: center;
    margin-bottom: $spacing-xl;
  }

  &__form {
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
      background: var(--color-bg-card);
      color: var(--color-text-primary);
    }
  }

  &__error {
    color: var(--pixel-redstone);
    font-size: $font-size-sm;
  }

  &__remember {
    flex-direction: row !important;
    align-items: center;
    gap: $spacing-sm !important;
    cursor: pointer;
    color: var(--color-text-secondary);

    input {
      width: auto;
      margin: 0;
    }

    span {
      font-size: $font-size-sm;
      line-height: 1.4;
    }
  }

  &__submit {
    margin-top: $spacing-sm;
    padding: $spacing-sm $spacing-lg;
    border: none;
    border-radius: $radius-md;
    background: var(--pixel-accent-strong);
    color: #fff;
    cursor: pointer;
    font-weight: 600;

    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }
}
</style>

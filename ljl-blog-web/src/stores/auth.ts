import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchCurrentUser, login as loginApi } from '@/api/modules/auth'
import { clearToken, getToken, setToken } from '@/utils/tokenStorage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const username = ref('')
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))

  function applyToken(value: string, remember = false) {
    setToken(value, remember)
    token.value = value
  }

  function clearAuth() {
    token.value = ''
    username.value = ''
    clearToken()
  }

  async function login(user: string, password: string, remember = false) {
    loading.value = true
    try {
      const res = await loginApi({ username: user, password })
      if (res.code !== 0) {
        throw new Error(res.message || '登录失败')
      }
      applyToken(res.data.token, remember)
      username.value = res.data.username
      return true
    } finally {
      loading.value = false
    }
  }

  async function restoreSession() {
    const stored = getToken()
    if (!stored) {
      clearAuth()
      return false
    }
    if (token.value !== stored) {
      token.value = stored
    }
    try {
      const res = await fetchCurrentUser()
      if (res.code === 0) {
        username.value = res.data.username
        return true
      }
      clearAuth()
      return false
    } catch {
      clearAuth()
      return false
    }
  }

  function logout() {
    clearAuth()
  }

  function resetSession() {
    clearAuth()
  }

  return {
    token,
    username,
    loading,
    isLoggedIn,
    login,
    logout,
    resetSession,
    restoreSession,
  }
})

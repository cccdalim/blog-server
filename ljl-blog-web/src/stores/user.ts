import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 用户信息 Store — 预留，后续对接 Spring Boot 认证 */
export const useUserStore = defineStore('user', () => {
  const isLoggedIn = ref(false)
  const username = ref('')

  function logout() {
    isLoggedIn.value = false
    username.value = ''
  }

  return { isLoggedIn, username, logout }
})

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/image/style/css'
import 'element-plus/es/components/image-viewer/style/css'
import 'element-plus/es/components/select/style/css'

import App from './App.vue'
import { onUnauthorized } from './api/unauthorized'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useThemeStore } from './stores/theme'
import '@/styles/index.scss'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

onUnauthorized(() => {
  const authStore = useAuthStore(pinia)
  authStore.resetSession()
  const { fullPath, path } = router.currentRoute.value
  if (path.startsWith('/admin') && path !== '/admin/login') {
    router.replace({ name: 'AdminLogin', query: { redirect: fullPath } })
  }
})

const themeStore = useThemeStore(pinia)
themeStore.initTheme()

app.mount('#app')

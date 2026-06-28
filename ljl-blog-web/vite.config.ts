import { loadEnv } from 'vite'
import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'
import { defineConfig } from 'vite'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8080'
  const basePath = env.VITE_BASE_PATH || '/'

  return {
    base: basePath,
    appType: 'spa',
    plugins: [
      vue(),
      vueDevTools(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
        dts: 'src/auto-imports.d.ts',
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: 'src/components.d.ts',
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/styles/variables.scss" as *;\n@use "@/styles/mixins.scss" as *;\n`,
        },
      },
    },
    server: {
      host: true,
      port: 5173,
      // 开发环境 API 代理 — 对接 Spring Boot 后，/api 转发到后端
      proxy: env.VITE_API_PROXY_TARGET
        ? {
            '/api': {
              target: apiProxyTarget,
              changeOrigin: true,
              secure: false,
            },
            '/uploads': {
              target: apiProxyTarget,
              changeOrigin: true,
              secure: false,
            },
          }
        : undefined,
    },
    preview: {
      host: true,
      port: 4173,
      // 本地 preview 也支持 API 代理，模拟公网部署
      proxy: env.VITE_API_PROXY_TARGET
        ? {
            '/api': {
              target: apiProxyTarget,
              changeOrigin: true,
              secure: false,
            },
            '/uploads': {
              target: apiProxyTarget,
              changeOrigin: true,
              secure: false,
            },
          }
        : undefined,
    },
  }
})

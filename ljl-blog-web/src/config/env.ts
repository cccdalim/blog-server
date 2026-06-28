/**
 * 应用环境配置 — 公网域名、API 地址等统一从此读取
 * 修改 .env.production 或服务器环境变量即可，无需改代码
 */

export interface AppEnv {
  /** 站点公网 URL，如 https://blog.example.com */
  siteUrl: string
  siteName: string
  siteDescription: string
  /** API 基础路径 */
  apiBaseUrl: string
  /** 是否使用 Mock JSON */
  useMock: boolean
  /** 部署子路径 */
  basePath: string
  isDev: boolean
  isProd: boolean
}

function trimTrailingSlash(url: string): string {
  return url.replace(/\/+$/, '')
}

function readEnv(key: keyof ImportMetaEnv, fallback: string): string {
  return import.meta.env[key]?.trim() || fallback
}

function readBool(key: keyof ImportMetaEnv, fallback: boolean): boolean {
  const raw = import.meta.env[key]
  if (raw === undefined || raw === '') return fallback
  return raw === 'true' || raw === '1'
}

export const appEnv: AppEnv = {
  siteUrl: trimTrailingSlash(readEnv('VITE_SITE_URL', '')),
  siteName: readEnv('VITE_SITE_NAME', 'LJL Knowledge Hub'),
  siteDescription: readEnv(
    'VITE_SITE_DESCRIPTION',
    '个人知识库 · 学习笔记 · 项目沉淀 · 旅行记录',
  ),
  apiBaseUrl: readEnv('VITE_API_BASE_URL', '/api'),
  useMock: readBool('VITE_USE_MOCK', true),
  basePath: readEnv('VITE_BASE_PATH', '/'),
  isDev: import.meta.env.DEV,
  isProd: import.meta.env.PROD,
}

/** 获取当前环境下的站点 Origin（局域网访问时使用实际地址） */
export function getSiteOrigin(): string {
  if (typeof window !== 'undefined') {
    const currentOrigin = window.location.origin
    // 开发环境或未配置域名：使用当前浏览器地址（支持手机局域网访问）
    if (!appEnv.siteUrl || appEnv.isDev) return currentOrigin
    return appEnv.siteUrl
  }
  return appEnv.siteUrl
}

/** 拼接绝对 URL — SEO、分享链接、canonical 等场景使用 */
export function absoluteUrl(path: string): string {
  const origin = getSiteOrigin()
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${origin}${normalizedPath}`
}

/** 是否为公网 HTTPS 生产环境 */
export function isPublicProduction(): boolean {
  return appEnv.isProd && appEnv.siteUrl.startsWith('https://')
}

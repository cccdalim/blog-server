/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 站点公网访问地址，绑定域名后填写，如 https://blog.example.com */
  readonly VITE_SITE_URL: string
  /** 站点名称 */
  readonly VITE_SITE_NAME: string
  /** 站点描述 */
  readonly VITE_SITE_DESCRIPTION: string
  /** 后端 API 地址，生产环境可填 https://api.example.com 或 /api */
  readonly VITE_API_BASE_URL: string
  /** 是否使用 Mock 数据，对接 Spring Boot 后设为 false */
  readonly VITE_USE_MOCK: string
  /** 应用部署子路径，默认 /（若部署在 https://domain.com/blog/ 则填 /blog/） */
  readonly VITE_BASE_PATH: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

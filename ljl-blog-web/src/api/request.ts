import axios from 'axios'
import { triggerUnauthorized } from '@/api/unauthorized'
import { appEnv } from '@/config/env'
import { clearToken, getToken } from '@/utils/tokenStorage'

/** Axios 实例 — baseURL 由环境变量 VITE_API_BASE_URL 控制 */
const request = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: appEnv.isProd,
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      clearToken()
      triggerUnauthorized()
    }
    console.error('[API Error]', error.message)
    return Promise.reject(error)
  },
)

export default request

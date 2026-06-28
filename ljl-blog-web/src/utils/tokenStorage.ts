export const TOKEN_KEY = 'ljl_admin_token'
const REMEMBER_FLAG_KEY = 'ljl_admin_remember'

/** 清理旧版仅写入 localStorage 的 token（无「保持登录」标记） */
function migrateLegacyToken() {
  if (localStorage.getItem(REMEMBER_FLAG_KEY) !== '1' && localStorage.getItem(TOKEN_KEY)) {
    localStorage.removeItem(TOKEN_KEY)
  }
}

migrateLegacyToken()

export function isRememberMe(): boolean {
  return localStorage.getItem(REMEMBER_FLAG_KEY) === '1'
}

export function getToken(): string {
  if (isRememberMe()) {
    return localStorage.getItem(TOKEN_KEY) ?? ''
  }
  return sessionStorage.getItem(TOKEN_KEY) ?? ''
}

export function setToken(token: string, remember = false) {
  clearToken()
  if (remember) {
    localStorage.setItem(REMEMBER_FLAG_KEY, '1')
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
}

export function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REMEMBER_FLAG_KEY)
}

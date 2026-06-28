import request from '@/api/request'
import type { ApiResponse, LoginResponse, UserInfo } from '@/types'

export interface LoginPayload {
  username: string
  password: string
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
}

export async function login(payload: LoginPayload): Promise<ApiResponse<LoginResponse>> {
  return request.post('/auth/login', payload)
}

export async function fetchCurrentUser(): Promise<ApiResponse<UserInfo>> {
  return request.get('/auth/me')
}

export async function changePassword(payload: ChangePasswordPayload): Promise<ApiResponse<null>> {
  return request.put('/auth/password', payload)
}

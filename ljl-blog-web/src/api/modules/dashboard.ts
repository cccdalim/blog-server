import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface DashboardStats {
  blogCount: number
  docCount: number
  projectCount: number
  photoCount: number
  categoryCount: number
  tagCount: number
  uploadFileCount: number
  uploadSizeBytes: number
  orphanImageCount: number
  orphanSizeBytes: number
}

export async function fetchDashboardStats(): Promise<ApiResponse<DashboardStats>> {
  return request.get('/admin/dashboard')
}

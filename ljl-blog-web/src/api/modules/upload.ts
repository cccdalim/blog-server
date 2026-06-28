import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface UploadResult {
  url: string
  path: string
}

export async function uploadImage(file: File): Promise<ApiResponse<UploadResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

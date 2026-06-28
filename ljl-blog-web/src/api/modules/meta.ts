import categoryData from '@/mock/category.json'
import tagData from '@/mock/tag.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type { ApiResponse, CategoryItem, MetaSavePayload, TagItem } from '@/types'
import { mockDelay, successResponse } from '@/utils'

export async function fetchCategories(): Promise<ApiResponse<CategoryItem[]>> {
  if (!USE_MOCK) {
    return request.get('/meta/categories')
  }

  await mockDelay(100)
  return successResponse(categoryData.list as CategoryItem[])
}

export async function fetchTags(): Promise<ApiResponse<TagItem[]>> {
  if (!USE_MOCK) {
    return request.get('/meta/tags')
  }

  await mockDelay(100)
  return successResponse(tagData.list as TagItem[])
}

export async function createCategory(payload: MetaSavePayload): Promise<ApiResponse<CategoryItem>> {
  return request.post('/meta/categories', payload)
}

export async function updateCategory(
  id: string,
  payload: MetaSavePayload,
): Promise<ApiResponse<CategoryItem>> {
  return request.put(`/meta/categories/${id}`, payload)
}

export async function deleteCategory(id: string): Promise<ApiResponse<null>> {
  return request.delete(`/meta/categories/${id}`)
}

export async function createTag(payload: MetaSavePayload): Promise<ApiResponse<TagItem>> {
  return request.post('/meta/tags', payload)
}

export async function updateTag(id: string, payload: MetaSavePayload): Promise<ApiResponse<TagItem>> {
  return request.put(`/meta/tags/${id}`, payload)
}

export async function deleteTag(id: string): Promise<ApiResponse<null>> {
  return request.delete(`/meta/tags/${id}`)
}

export async function cleanupOrphanTags(): Promise<ApiResponse<{ count: number }>> {
  return request.post('/meta/tags/cleanup-orphans')
}

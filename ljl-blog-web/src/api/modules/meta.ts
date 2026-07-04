import categoryData from '@/mock/category.json'
import tagData from '@/mock/tag.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type { ApiResponse, CategoryItem, MetaSavePayload, TagItem, TagScope } from '@/types'
import { mockDelay, successResponse } from '@/utils'

export async function fetchCategories(scope: TagScope = 'content'): Promise<ApiResponse<CategoryItem[]>> {
  if (!USE_MOCK) {
    return request.get('/meta/categories', { params: { scope } })
  }

  await mockDelay(100)
  const list = (categoryData.list as CategoryItem[]).filter(
    (cat) => (cat.scope ?? 'content') === scope,
  )
  return successResponse(list)
}

export async function fetchTags(scope: TagScope = 'content'): Promise<ApiResponse<TagItem[]>> {
  if (!USE_MOCK) {
    return request.get('/meta/tags', { params: { scope } })
  }

  await mockDelay(100)
  const list = (tagData.list as TagItem[]).filter((tag) => (tag.scope ?? 'content') === scope)
  return successResponse(list)
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

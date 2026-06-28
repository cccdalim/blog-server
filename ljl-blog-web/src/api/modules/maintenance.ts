import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface OrphanImageItem {
  path: string
  url: string
  sizeBytes: number
}

export interface OrphanImageScanResult {
  totalFiles: number
  referencedCount: number
  orphanCount: number
  orphanSizeBytes: number
  orphans: OrphanImageItem[]
}

export interface OrphanImageCleanupResult {
  deletedCount: number
  freedBytes: number
  deleted: string[]
}

export async function scanOrphanImages(): Promise<ApiResponse<OrphanImageScanResult>> {
  return request.get('/maintenance/orphan-images')
}

export async function cleanupOrphanImages(): Promise<ApiResponse<OrphanImageCleanupResult>> {
  return request.post('/maintenance/orphan-images/cleanup')
}

export async function cleanupOrphanTags(): Promise<ApiResponse<{ count: number }>> {
  return request.post('/maintenance/orphan-tags/cleanup')
}

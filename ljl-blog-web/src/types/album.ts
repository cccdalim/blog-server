/** 旅行相册照片 */
export interface Photo {
  id: string
  url: string
  title: string
  city: string
  country: string
  category: string
  date: string
  width?: number
  height?: number
}

/** 相册查询参数 */
export interface AlbumQueryParams {
  page?: number
  pageSize?: number
  category?: string
  country?: string
  city?: string
}

/** 筛选选项 */
export interface AlbumFilterOption {
  label: string
  value: string
  count: number
}

/** 旅行时间轴项 */
export interface AlbumTimelineItem {
  date: string
  title: string
  description: string
}

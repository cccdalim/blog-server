/** 统一 API 响应格式，与 Spring Boot 后端对齐 */
export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

/** 分页数据结构 */
export interface PaginatedData<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

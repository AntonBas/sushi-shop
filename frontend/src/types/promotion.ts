import type { ProductListResponse } from "./product"

export interface CreatePromotionRequest {
  title: string
  description?: string
  discountPercent: number
  startDate: string
  endDate: string
  productIds: number[]
}

export interface UpdatePromotionRequest {
  title?: string
  description?: string
  discountPercent?: number
  startDate?: string
  endDate?: string
  productIds?: number[]
}

export interface PromotionResponse {
  id: number
  slug: string
  title: string
  description?: string
  discountPercent: number
  startDate: string
  endDate: string
  active: boolean
  products: ProductListResponse[]
}
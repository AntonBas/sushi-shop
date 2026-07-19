import type { Category } from "./enums"

export interface CreateProductRequest {
  name: string
  description?: string
  price: number
  category: Category
  weight?: string
  pieces?: number
}

export interface UpdateProductRequest {
  name?: string
  description?: string
  price?: number
  category?: Category
  isAvailable?: boolean
  weight?: string
  pieces?: number
}

export interface ProductListResponse {
  id: number
  name: string
  price: number
  discountedPrice?: number | null
  averageRating?: number | null
  category: Category
  mainImage?: string | null
  available: boolean
  weight?: string | null
  pieces?: number | null
}

export interface ProductResponse {
  id: number
  name: string
  description?: string
  price: number
  discountedPrice?: number | null
  discountPercent?: number | null
  promotionTitle?: string | null
  category: Category
  images: string[]
  reviewCount: number
  averageRating?: number | null
  available: boolean
  weight?: string | null
  pieces?: number | null
}

export interface ProductFilters {
  search?: string
  category?: Category
  available?: boolean
}
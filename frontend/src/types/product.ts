import type { Category } from "./enums"

export interface CreateProductRequest {
  name: string
  description?: string
  price: number
  category: Category
  weight?: number
  pieces?: number
}

export interface UpdateProductRequest {
  name?: string
  description?: string
  price?: number
  category?: Category
  weight?: number
  pieces?: number
}

export interface ProductListResponse {
  id: number
  slug: string
  name: string
  price: number
  discountedPrice?: number | null
  averageRating?: number | null
  category: Category
  mainImage?: string | null
  available: boolean
  weight?: number | null
  pieces?: number | null
}

export interface ProductResponse {
  id: number
  slug: string
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
  weight?: number | null
  pieces?: number | null
}

export interface ProductFilters {
  search?: string
  category?: Category
  available?: boolean
}
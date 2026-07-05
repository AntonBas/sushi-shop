export interface CreateReviewRequest {
  productId: number
  rating: number
  comment?: string
}

export interface ReviewResponse {
  id: number
  userName: string
  rating: number
  comment?: string
  createdAt: string
}
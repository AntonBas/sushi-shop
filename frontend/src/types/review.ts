export interface CreateReviewRequest {
  productId: number
  rating: number
  comment?: string
}

export interface CreateReviewReplyRequest {
  message: string
}

export interface ReviewReplyResponse {
  id: number
  message: string
  authorName: string
  createdAt: string
}

export interface ReviewResponse {
  id: number
  userId: number
  userName: string
  rating: number
  comment?: string
  replies?: ReviewReplyResponse[]
  createdAt: string
  updatedAt?: string
}
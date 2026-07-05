import api from './client'
import type { CreateReviewRequest, ReviewResponse } from '../types'
import type { Page } from '../types/common'

export const createReview = async (data: CreateReviewRequest): Promise<ReviewResponse> => {
  const { data: res } = await api.post('/reviews', data)
  return res
}

export const getReviews = async (productId: number, page = 0, size = 5): Promise<Page<ReviewResponse>> => {
  const { data } = await api.get(`/reviews/product/${productId}`, { params: { page, size, sort: 'createdAt,desc' } })
  return data
}

export const deleteReview = async (id: number): Promise<void> => {
  await api.delete(`/reviews/${id}`)
}
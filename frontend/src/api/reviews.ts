import api from './client'
import type { CreateReviewRequest, CreateReviewReplyRequest, ReviewResponse, ReviewReplyResponse } from '../types'
import type { Page } from '../types/common'

export const createReview = async (data: CreateReviewRequest): Promise<ReviewResponse> => {
  const { data: res } = await api.post<ReviewResponse>('/reviews', data)
  return res
}

export const updateReview = async (id: number, data: CreateReviewRequest): Promise<ReviewResponse> => {
  const { data: res } = await api.put<ReviewResponse>(`/reviews/${id}`, data)
  return res
}

export const getReviews = async (productId: number, page = 0, size = 5, sort = 'createdAt,desc'): Promise<Page<ReviewResponse>> => {
  const { data } = await api.get<Page<ReviewResponse>>(`/reviews/product/${productId}`, { params: { page, size, sort } })
  return data
}

export const addReply = async (reviewId: number, data: CreateReviewReplyRequest): Promise<ReviewReplyResponse> => {
  const { data: res } = await api.post<ReviewReplyResponse>(`/reviews/${reviewId}/replies`, data)
  return res
}

export const updateReply = async (replyId: number, data: CreateReviewReplyRequest): Promise<ReviewReplyResponse> => {
  const { data: res } = await api.put<ReviewReplyResponse>(`/reviews/replies/${replyId}`, data)
  return res
}

export const deleteReply = async (replyId: number): Promise<void> => {
  await api.delete(`/reviews/replies/${replyId}`)
}

export const deleteReview = async (id: number): Promise<void> => {
  await api.delete(`/reviews/${id}`)
}
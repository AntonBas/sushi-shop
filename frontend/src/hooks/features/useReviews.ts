import { useCallback } from 'react'
import { useApi } from '.././common/useApi'
import * as reviewsApi from '../../api/reviews'
import type { ReviewResponse, ReviewReplyResponse, CreateReviewRequest, CreateReviewReplyRequest } from '../../types'
import type { Page } from '../../types/common'

export function useReviews() {
  const listApi = useApi<Page<ReviewResponse>>()
  const createApi = useApi<ReviewResponse>()
  const updateApi = useApi<ReviewResponse>()
  const replyApi = useApi<ReviewReplyResponse>()
  const updateReplyApi = useApi<ReviewReplyResponse>()

  const loadReviews = useCallback((productId: number, page = 0) => {
    return listApi.execute(() => reviewsApi.getReviews(productId, page))
  }, [])

  const createReview = useCallback((data: CreateReviewRequest) => {
    return createApi.execute(() => reviewsApi.createReview(data))
  }, [])

  const updateReview = useCallback((id: number, data: CreateReviewRequest) => {
    return updateApi.execute(() => reviewsApi.updateReview(id, data))
  }, [])

  const addReply = useCallback((reviewId: number, data: CreateReviewReplyRequest) => {
    return replyApi.execute(() => reviewsApi.addReply(reviewId, data))
  }, [])

  const updateReply = useCallback((replyId: number, data: CreateReviewReplyRequest) => {
    return updateReplyApi.execute(() => reviewsApi.updateReply(replyId, data))
  }, [])

  const deleteReply = useCallback((replyId: number) => {
    return reviewsApi.deleteReply(replyId)
  }, [])

  return {
    reviews: listApi.data?.content || [],
    totalPages: listApi.data?.page.totalPages || 0,
    loading: listApi.loading,
    error: listApi.error,
    loadReviews,
    createReview,
    updateReview,
    addReply,
    updateReply,
    deleteReply,
    createdReview: createApi.data
  }
}
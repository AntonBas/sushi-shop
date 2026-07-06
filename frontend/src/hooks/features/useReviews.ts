import { useCallback } from 'react'
import { useApi } from './common/useApi'
import * as reviewsApi from '../api/reviews'
import type { ReviewResponse, CreateReviewRequest } from '../types'
import type { Page } from '../types/common'

export function useReviews() {
  const listApi = useApi<Page<ReviewResponse>>()
  const createApi = useApi<ReviewResponse>()

  const loadReviews = useCallback((productId: number, page = 0) => {
    return listApi.execute(() => reviewsApi.getReviews(productId, page))
  }, [])

  const createReview = useCallback((data: CreateReviewRequest) => {
    return createApi.execute(() => reviewsApi.createReview(data))
  }, [])

  return {
    reviews: listApi.data?.content || [],
    totalPages: listApi.data?.totalPages || 0,
    loading: listApi.loading,
    error: listApi.error,
    loadReviews,
    createReview,
    createdReview: createApi.data
  }
}
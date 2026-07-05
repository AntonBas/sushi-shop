import api from './client'
import type { CreatePromotionRequest, PromotionResponse } from '../types'
import type { Page } from '../types/common'

export const getActivePromotions = async (): Promise<PromotionResponse[]> => {
  const { data } = await api.get('/promotions/active')
  return data
}

export const getPromotions = async (page = 0, size = 12): Promise<Page<PromotionResponse>> => {
  const { data } = await api.get('/promotions', { params: { page, size, sort: 'startDate,desc' } })
  return data
}

export const createPromotion = async (data: CreatePromotionRequest): Promise<PromotionResponse> => {
  const { data: res } = await api.post('/promotions', data)
  return res
}

export const deletePromotion = async (id: number): Promise<void> => {
  await api.delete(`/promotions/${id}`)
}
import api from './client'
import type { CreatePromotionRequest, UpdatePromotionRequest, PromotionResponse } from '../types'
import type { Page } from '../types/common'

export const getActivePromotions = async (): Promise<PromotionResponse[]> => {
  const { data } = await api.get('/promotions/active')
  return data
}

export const getPromotions = async (page = 0, size = 12, search?: string): Promise<Page<PromotionResponse>> => {
  const { data } = await api.get('/promotions', { params: { page, size, sort: 'startDate,desc', ...(search && { search }) } })
  return data
}

export const getPromotionById = async (id: number): Promise<PromotionResponse> => {
  const { data } = await api.get(`/promotions/${id}`)
  return data
}

export const getPromotionBySlug = async (slug: string): Promise<PromotionResponse> => {
  const { data } = await api.get(`/promotions/slug/${slug}`)
  return data
}

export const createPromotion = async (data: CreatePromotionRequest): Promise<PromotionResponse> => {
  const { data: res } = await api.post('/promotions', data)
  return res
}

export const updatePromotion = async (id: number, data: UpdatePromotionRequest): Promise<PromotionResponse> => {
  const { data: res } = await api.put(`/promotions/${id}`, data)
  return res
}

export const deletePromotion = async (id: number): Promise<void> => {
  await api.delete(`/promotions/${id}`)
}
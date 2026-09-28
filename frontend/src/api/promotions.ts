import api from './client'
import type { CreatePromotionRequest, UpdatePromotionRequest, PromotionResponse } from '../types'
import type { Page } from '../types/common'

export const getActivePromotions = async (): Promise<PromotionResponse[]> => {
  const { data } = await api.get<PromotionResponse[]>('/promotions/active')
  return data
}

export const getPromotions = async (page = 0, size = 12, search?: string): Promise<Page<PromotionResponse>> => {
  const { data } = await api.get<Page<PromotionResponse>>('/promotions', { params: { page, size, sort: 'startDate,desc', ...(search && { search }) } })
  return data
}

export const getPromotionById = async (id: number): Promise<PromotionResponse> => {
  const { data } = await api.get<PromotionResponse>(`/promotions/${id}`)
  return data
}

export const getPromotionBySlug = async (slug: string): Promise<PromotionResponse> => {
  const { data } = await api.get<PromotionResponse>(`/promotions/slug/${slug}`)
  return data
}

export const createPromotion = async (data: CreatePromotionRequest): Promise<PromotionResponse> => {
  const { data: res } = await api.post<PromotionResponse>('/promotions', data)
  return res
}

export const updatePromotion = async (id: number, data: UpdatePromotionRequest): Promise<PromotionResponse> => {
  const { data: res } = await api.put<PromotionResponse>(`/promotions/${id}`, data)
  return res
}

export const deletePromotion = async (id: number): Promise<void> => {
  await api.delete(`/promotions/${id}`)
}
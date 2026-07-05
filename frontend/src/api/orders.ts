import api from './client'
import type { CreateOrderRequest, OrderResponse } from '../types'
import type { Page } from '../types/common'

export const createOrder = async (data: CreateOrderRequest): Promise<OrderResponse> => {
  const { data: res } = await api.post('/orders', data)
  return res
}

export const getOrders = async (page = 0, size = 12): Promise<Page<OrderResponse>> => {
  const { data } = await api.get('/orders', { params: { page, size, sort: 'createdAt,desc' } })
  return data
}

export const getOrder = async (id: number): Promise<OrderResponse> => {
  const { data } = await api.get(`/orders/${id}`)
  return data
}

export const updateOrderStatus = async (id: number, status: string): Promise<OrderResponse> => {
  const { data } = await api.patch(`/orders/${id}/status`, null, { params: { status } })
  return data
}
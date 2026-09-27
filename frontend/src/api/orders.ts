import api from './client'
import type { CreateOrderRequest, OrderResponse, UserOrderResponse, OrderStatus } from '../types'
import type { OrderFilters } from '../types/order'
import type { Page } from '../types/common'

export const createOrder = async (data: CreateOrderRequest): Promise<OrderResponse> => {
  const { data: res } = await api.post<OrderResponse>('/orders', data)
  return res
}

export const getMyOrders = async (page = 0, size = 12): Promise<Page<UserOrderResponse>> => {
  const { data } = await api.get<Page<UserOrderResponse>>('/orders/my', { params: { page, size, sort: 'createdAt,desc' } })
  return data
}

export const getAllOrders = async (page = 0, size = 12, filters?: OrderFilters): Promise<Page<OrderResponse>> => {
  const { data } = await api.get<Page<OrderResponse>>('/orders', { params: { page, size, sort: 'createdAt,desc', ...filters } })
  return data
}

export const updateOrderStatus = async (id: number, status: OrderStatus): Promise<OrderResponse> => {
  const { data } = await api.patch<OrderResponse>(`/orders/${id}/status`, null, { params: { status } })
  return data
}